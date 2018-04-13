package expression;


public class ExpressionError extends Exception {
	String msg = "<NONE>";

	public ExpressionError() {
		super();
	}

	public ExpressionError(String msg) {
		super(msg);
		this.msg = msg;
	}

	public String toString() {
		return msg;
	}
}
