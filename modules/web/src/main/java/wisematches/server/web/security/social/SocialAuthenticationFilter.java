package wisematches.server.web.security.social;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.*;
import org.springframework.social.UserIdSource;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInAttempt;
import org.springframework.social.security.*;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO:  https://jira.springsource.org/browse/SOCIAL-406
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class SocialAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private SocialAuthenticationServiceLocator authServiceLocator;

	private String signupUrl = "/signup";

	private String connectionAddedRedirectUrl = "/";

	private boolean updateConnections = true;

	private UserIdSource userIdSource;

	private UsersConnectionRepository usersConnectionRepository;

	private SimpleUrlAuthenticationFailureHandler delegateAuthenticationFailureHandler;

	public SocialAuthenticationFilter(AuthenticationManager authManager, UserIdSource userIdSource, UsersConnectionRepository usersConnectionRepository, SocialAuthenticationServiceLocator authServiceLocator) {
		super("/auth");
		setAuthenticationManager(authManager);
		this.userIdSource = userIdSource;
		this.usersConnectionRepository = usersConnectionRepository;
		this.authServiceLocator = authServiceLocator;
		this.delegateAuthenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler(DEFAULT_FAILURE_URL);
		super.setAuthenticationFailureHandler(new SocialAuthenticationFailureHandler(delegateAuthenticationFailureHandler));
	}

	public void setSignupUrl(String signupUrl) {
		this.signupUrl = signupUrl;
	}

	/**
	 * The URL to redirect to if authentication fails or if authorization is denied by the user.
	 *
	 * @param defaultFailureUrl The failure URL. Defaults to "/signin" (relative to the servlet context).
	 */
	public void setDefaultFailureUrl(String defaultFailureUrl) {
		delegateAuthenticationFailureHandler.setDefaultFailureUrl(defaultFailureUrl);
	}

	public void setConnectionAddedRedirectUrl(String connectionAddedRedirectUrl) {
		this.connectionAddedRedirectUrl = connectionAddedRedirectUrl;
	}

	public void setUpdateConnections(boolean updateConnections) {
		this.updateConnections = updateConnections;
	}

	public void setPostLoginUrl(String postLoginUrl) {
		AuthenticationSuccessHandler successHandler = getSuccessHandler();
		if (successHandler instanceof AbstractAuthenticationTargetUrlRequestHandler) {
			AbstractAuthenticationTargetUrlRequestHandler h = (AbstractAuthenticationTargetUrlRequestHandler) successHandler;
			h.setDefaultTargetUrl(postLoginUrl);
		} else {
			throw new IllegalStateException("can't set postLoginUrl on unknown successHandler, type is " + successHandler.getClass().getName());
		}
	}

	public void setPostFailureUrl(String postFailureUrl) {
		AuthenticationFailureHandler failureHandler = getFailureHandler();
		if (failureHandler instanceof SimpleUrlAuthenticationFailureHandler) {
			SimpleUrlAuthenticationFailureHandler h = (SimpleUrlAuthenticationFailureHandler) failureHandler;
			h.setDefaultFailureUrl(postFailureUrl);
		} else {
			throw new IllegalStateException("can't set postFailureUrl on unknown failureHandler, type is " + failureHandler.getClass().getName());
		}
	}

	public UsersConnectionRepository getUsersConnectionRepository() {
		return usersConnectionRepository;
	}

	public SocialAuthenticationServiceLocator getAuthServiceLocator() {
		return authServiceLocator;
	}

	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (detectRejection(request)) {
			throw new SocialAuthenticationException("Authentication failed because user rejected authorization.");
		}

		Authentication auth = null;
		Set<String> authProviders = authServiceLocator.registeredAuthenticationProviderIds();
		String authProviderId = getRequestedProviderId(request);
		if (!authProviders.isEmpty() && authProviderId != null && authProviders.contains(authProviderId)) {
			SocialAuthenticationService<?> authService = authServiceLocator.getAuthenticationService(authProviderId);
			auth = attemptAuthService(authService, request, response);
			if (auth == null) {
				throw new AuthenticationServiceException("authentication failed");
			}
		}
		return auth;
	}

	/**
	 * Detects a callback request after a user rejects authorization to prevent a never-ending redirect loop.
	 * Default implementation detects a rejection as a request that has one or more parameters, but none of the expected parameters (oauth_token, code, scope).
	 * May be overridden to customize rejection detection.
	 *
	 * @param request the request to check for rejection.
	 * @return true if the request appears to be the result of a rejected authorization; false otherwise.
	 */
	protected boolean detectRejection(HttpServletRequest request) {
		Set<?> parameterKeys = request.getParameterMap().keySet();
		return parameterKeys.size() > 0
				&& !parameterKeys.contains("oauth_token")
				&& !parameterKeys.contains("code")
				&& !parameterKeys.contains("scope");
	}

	/**
	 * Indicates whether this filter should attempt to process a social network login request for the current invocation.
	 * <p>Check if request URL matches filterProcessesUrl with valid providerId.
	 * The URL must be like {filterProcessesUrl}/{providerId}.
	 *
	 * @return <code>true</code> if the filter should attempt authentication, <code>false</code> otherwise.
	 */
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		String providerId = getRequestedProviderId(request);
		if (providerId != null) {
			Set<String> authProviders = authServiceLocator.registeredAuthenticationProviderIds();
			return authProviders.contains(providerId);
		}
		return false;
	}

	protected Connection<?> addConnection(SocialAuthenticationService<?> authService, String userId, ConnectionData data) {
		HashSet<String> userIdSet = new HashSet<String>();
		userIdSet.add(data.getProviderUserId());
		Set<String> connectedUserIds = usersConnectionRepository.findUserIdsConnectedTo(data.getProviderId(), userIdSet);
		if (connectedUserIds.contains(userId)) {
			// my own code
			final ConnectionRepository connectionRepository = usersConnectionRepository.createConnectionRepository(userId);

			final Connection<?> connection = authService.getConnectionFactory().createConnection(data);
			connection.sync();

			connectionRepository.updateConnection(connection);
			return connection;
		} else if (!authService.getConnectionCardinality().isMultiUserId() && !connectedUserIds.isEmpty()) {
			return null;
		}

		ConnectionRepository repo = usersConnectionRepository.createConnectionRepository(userId);

		if (!authService.getConnectionCardinality().isMultiProviderUserId()) {
			List<Connection<?>> connections = repo.findConnections(data.getProviderId());
			if (!connections.isEmpty()) {
				return null;
			}
		}

		// add new connection
		Connection<?> connection = authService.getConnectionFactory().createConnection(data);
		connection.sync();
		repo.addConnection(connection);
		return connection;
	}

	// private helpers
	private Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/*
	 * Call SocialAuthenticationService.getAuthToken() to get SocialAuthenticationToken:
	 *     If first phase, throw AuthenticationRedirectException to redirect to provider website.
	 *     If second phase, get token/code from request parameter and call provider API to get accessToken/accessGrant.
	 * Check Authentication object in spring security context, if null or not authenticated,  call doAuthentication()
	 * Otherwise, it is already authenticated, add this connection.
	 */
	private Authentication attemptAuthService(final SocialAuthenticationService<?> authService, final HttpServletRequest request, HttpServletResponse response)
			throws SocialAuthenticationRedirectException, AuthenticationException {

		final SocialAuthenticationToken token = authService.getAuthToken(request, response);
		if (token == null) return null;

		Assert.notNull(token.getConnection());

		Authentication auth = getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return doAuthentication(authService, request, token);
		} else {
			final String s = addConnection(authService, request, token, auth);
			if (s != null) {
				try {
					response.sendRedirect(s);
				} catch (IOException ignore) {
				}
			}
			return auth;
		}
	}

	private String getRequestedProviderId(HttpServletRequest request) {
		String uri = request.getRequestURI();
		int pathParamIndex = uri.indexOf(';');

		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}

		// uri must start with context path
		uri = uri.substring(request.getContextPath().length());

		// remaining uri must start with filterProcessesUrl
		if (!uri.startsWith(getFilterProcessesUrl())) {
			return null;
		}
		uri = uri.substring(getFilterProcessesUrl().length());

		// expect /filterprocessesurl/provider, not /filterprocessesurlproviderr
		if (uri.startsWith("/")) {
			return uri.substring(1);
		} else {
			return null;
		}
	}

	private String addConnection(final SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token, Authentication auth) {
		// already authenticated - add connection instead
		String userId = userIdSource.getUserId();
		if (userId == null) return null;

		ConnectionData connectionData = token.getConnection().createData();
		Connection<?> connection = addConnection(authService, userId, connectionData);
		if (connection != null) {
			String redirectUrl = authService.getConnectionAddedRedirectUrl(request, connection);
			if (redirectUrl == null) {
				// use default instead
				redirectUrl = connectionAddedRedirectUrl;
			}
			return redirectUrl;
		}
		return null;
	}

	private Authentication doAuthentication(SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token) {
		try {
			if (!authService.getConnectionCardinality().isAuthenticatePossible()) return null;
			token.setDetails(authenticationDetailsSource.buildDetails(request));
			Authentication success = getAuthenticationManager().authenticate(token);
			Assert.isInstanceOf(SocialUserDetails.class, success.getPrincipal(), "unexpected principle type");
			updateConnections(authService, token, success);
			return success;
		} catch (BadCredentialsException e) {
			// connection unknown, register new user?
			if (signupUrl != null) {
				// store ConnectionData in session and redirect to register page
				addSignInAttempt(request.getSession(), token.getConnection());
				throw new SocialAuthenticationRedirectException(signupUrl);
			}
			throw e;
		}
	}

	private void updateConnections(SocialAuthenticationService<?> authService, SocialAuthenticationToken token, Authentication success) {
		if (updateConnections) {
			String userId = ((SocialUserDetails) success.getPrincipal()).getUserId();
			Connection<?> connection = token.getConnection();
			ConnectionRepository repo = getUsersConnectionRepository().createConnectionRepository(userId);
			repo.updateConnection(connection);
		}
	}

	private void addSignInAttempt(HttpSession session, Connection<?> connection) {
		session.setAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE, new ProviderSignInAttempt(connection, authServiceLocator, usersConnectionRepository));
	}

	private static final String DEFAULT_FAILURE_URL = "/signin";
}
