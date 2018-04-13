package expression;

import java.util.Hashtable;

class FunctionTerm implements TermInterface {
	static final public int OP_NEG  = 0;   // Negation '-'
	static final public int OP_SQRT  = 1;   // Square root 'sqrt'
	
	static public int findOperator(String op) {
		for (int i=0 ; i<opVals.length ; i++)
			if (op.equals(opVals[i]))
				return i;
		return -1;
	}

	static final public String opVals[] = {"-", "sqrt"};
	
	
	TermInterface arg;
	int oper;
	
	FunctionTerm(int op, TermInterface a) {
		arg = a;
		oper = op;
	}
	
	public double evaluate(Hashtable vars) throws ExpressionError {
		switch (oper) {
			case OP_NEG :
				return - arg.evaluate(vars);
			
			case OP_SQRT :
				return Math.sqrt(arg.evaluate(vars));
			
			default:
				throw new ExpressionError("Illegal operator in expression!");		}
	}
	
	public String toString() {
		return opVals[oper] + "(" + arg.toString() + ")";
	}
}
