package expression;

import java.util.Hashtable;

class ConstantTerm implements TermInterface {
	private double v;

	ConstantTerm(double v) {
		this.v = v;
	}
	
	public double evaluate(Hashtable vars) throws ExpressionError {
		return v;
	}
	
	public String toString() {
		return String.valueOf(v);
	}
}
