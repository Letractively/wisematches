package wisematches.server.web.security.guest;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import wisematches.server.security.impl.WMPlayerDetails;

import java.io.Serializable;

/**
 * Represents an guest <code>Authentication</code>.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class GuestAuthenticationToken extends AbstractAuthenticationToken implements Serializable {
	private final int keyHash;
	private final UserDetails principal;

	public GuestAuthenticationToken(String key, WMPlayerDetails principal) {
		super(principal.getAuthorities());

		if ((key == null) || ("".equals(key))) {
			throw new IllegalArgumentException("Cannot pass null or empty values to constructor");
		}

		this.keyHash = key.hashCode();
		this.principal = principal;
		setAuthenticated(true);
	}

	@Override
	@Deprecated
	public Object getDetails() {
		throw new UnsupportedOperationException("commented");
//		return GuestPlayer.GUEST;
	}

	@Override
	public Object getCredentials() {
		return principal.getPassword();
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		final GuestAuthenticationToken token = (GuestAuthenticationToken) o;
		return keyHash == token.keyHash;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + keyHash;
		return result;
	}
}