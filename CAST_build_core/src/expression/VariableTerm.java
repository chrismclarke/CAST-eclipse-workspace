package expression;

import java.util.Hashtable;

class VariableTerm implements TermInterface {
	private String v;

	VariableTerm(String v) {
		this.v = v;
	}
	
	public double evaluate(Hashtable vars) throws ExpressionError {
		Double dd = (Double) vars.get(v);
		if (dd == null)
			throw new ExpressionError(v + " is undefined");
		return (dd.doubleValue());
	}
	
	public String toString() {
		return v;
	}
}
