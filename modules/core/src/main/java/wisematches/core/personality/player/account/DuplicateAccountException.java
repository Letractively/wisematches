package wisematches.core.personality.player.account;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This exception is thrown is you try register or modify an account and any of the account's attributes
 * already registred.
 *
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class DuplicateAccountException extends AccountException {
	private final Set<String> fieldNames;

	public DuplicateAccountException(Account account, String... fieldNames) {
		super("DuplicateAccount: " + Arrays.toString(fieldNames), account);
		if (fieldNames != null) {
			this.fieldNames = new HashSet<String>(Arrays.asList(fieldNames));
		} else {
			this.fieldNames = null;
		}
	}

	/**
	 * Returns the names of fields which already registered and can't be duplicate.
	 *
	 * @return the duplicate field names.
	 */
	public Set<String> getFieldNames() {
		if (fieldNames != null) {
			return Collections.unmodifiableSet(fieldNames);
		}
		return null;
	}
}
