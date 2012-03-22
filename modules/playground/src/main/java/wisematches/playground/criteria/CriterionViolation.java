package wisematches.playground.criteria;

import java.io.Serializable;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public final class CriterionViolation implements Serializable {
	private final String code;
	private final Object expected;
	private final Object received;
	private final PlayerCriterion criterion;

	private static final long serialVersionUID = 4808346159699224069L;

	public CriterionViolation(String code, Object expected, Object received, PlayerCriterion criterion) {
		if (code == null) {
			throw new NullPointerException("Code can't be null");
		}
		this.code = code;
		this.expected = expected;
		this.received = received;
		this.criterion = criterion;
	}

	public String getCode() {
		return code;
	}

	public Object getExpected() {
		return expected;
	}

	public Object getReceived() {
		return received;
	}

	public PlayerCriterion getCriterion() {
		return criterion;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("CriterionViolation");
		sb.append("{code='").append(code).append('\'');
		sb.append(", expected=").append(expected);
		sb.append(", received=").append(received);
		sb.append(", criterion=").append(criterion);
		sb.append('}');
		return sb.toString();
	}
}
