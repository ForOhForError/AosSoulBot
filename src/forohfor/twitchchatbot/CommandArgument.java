package forohfor.twitchchatbot;

public class CommandArgument {

	private static final int ARG_STRING = 0;
	private static final int ARG_INT = 1;
	private static final int ARG_DOUBLE = 2;

	private int intValue;
	private String stringValue;
	private double doubleValue;

	private int type;

	/**
	 * Constructs a new string argument
	 */
	public CommandArgument(String stringValue) {
		super();
		this.stringValue = stringValue;
		this.type = ARG_STRING;
	}

	/**
	 * Constructs a new integer argument
	 */
	public CommandArgument(int intValue) {
		super();
		this.intValue = intValue;
		this.type = ARG_INT;
	}

	/**
	 * Constructs a new double argument
	 */
	public CommandArgument(Double doubleValue) {
		super();
		this.doubleValue = doubleValue;
		this.type = ARG_DOUBLE;
	}

	/**
	 * Returns this argument's value as an integer if it's an integer argument,
	 * otherwise returns null
	 */
	public Integer intValue() {
		if (type == ARG_INT) {
			return intValue;
		} else {
			return null;
		}
	}

	/**
	 * Returns this argument's value as a string if it's a string argument,
	 * otherwise returns null
	 */
	public String stringValue() {
		if (type == ARG_STRING) {
			return stringValue;
		} else {
			return null;
		}
	}

	/**
	 * Returns this argument's value as a double if it's a double argument,
	 * otherwise returns null
	 */
	public Double doubleValue() {
		if (type == ARG_DOUBLE) {
			return doubleValue;
		} else {
			return null;
		}
	}
}
