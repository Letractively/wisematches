package wisematches.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import wisematches.server.player.Membership;

import java.util.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public enum WMAuthorities implements GrantedAuthority {
	ADMIN,
	USER,
	ANONYMOUS,
	MEMBER,
	GUEST,
	SILVER,
	GOLD,
	PLATINUM;

	private static final Map<Membership, EnumSet<WMAuthorities>> authoritiesCache = new HashMap<Membership, EnumSet<WMAuthorities>>();

	static {
		authoritiesCache.put(Membership.BASIC, EnumSet.of(USER, MEMBER));
		authoritiesCache.put(Membership.SILVER, EnumSet.of(USER, MEMBER, SILVER));
		authoritiesCache.put(Membership.GOLD, EnumSet.of(USER, MEMBER, GOLD));
		authoritiesCache.put(Membership.PLATINUM, EnumSet.of(USER, MEMBER, PLATINUM));
	}

	@Override
	public String getAuthority() {
		return name().toLowerCase();
	}

	/**
	 * Checks is this authority granted.
	 * <p/>
	 * This method uses {@code SecurityContextHolder#getContext()} to get a {@code Authentication}
	 * object and check the authorities.
	 *
	 * @return {@code true} if the authority granted; {@code false} - otherwise.
	 * @see SecurityContext
	 * @see SecurityContextHolder#getContext()
	 */
	public boolean isAuthorityGranted() {
		final SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			final Authentication authentication = context.getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				return authentication.getAuthorities().contains(this);
			}
		}
		return false;
	}

	public static Collection<? extends GrantedAuthority> forMembership(Membership membership) {
		final EnumSet<WMAuthorities> authorities = authoritiesCache.get(membership);
		if (authorities == null) {
			return Collections.emptySet();
		}
		return authorities;
	}
}