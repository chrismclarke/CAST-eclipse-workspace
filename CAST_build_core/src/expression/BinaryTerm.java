package expression;

import java.util.Hashtable;

class BinaryTerm implements TermInterface {
	TermInterface arg1, arg2;
	int oper;

	/**
	* These are the valid operator types.
	*/

	static final public int OP_ADD  = 0;   // Addition '+'
	static final public int OP_SUB  = 1;   // Subtraction '-'
	static final public int OP_MUL  = 2;   // Multiplication '*'
	static final public int OP_DIV  = 3;   // Division '/'
	static final public int OP_EXP  = 4;   // Exponentiation '^'

	static final public String opVals[] = {"+", "-", "*", "/", "^"};

	BinaryTerm(int op, TermInterface a, TermInterface b) {
		arg1 = a;
		arg2 = b;
		oper = op;
	}
	
	public double evaluate(Hashtable vars) throws ExpressionError {
		switch (oper) {
			case OP_ADD :
				return arg1.evaluate(vars) + arg2.evaluate(vars);

			case OP_SUB :
				return arg1.evaluate(vars) - arg2.evaluate(vars);

			case OP_MUL :
				return arg1.evaluate(vars) * arg2.evaluate(vars);

			case OP_DIV :
				if (arg2.evaluate(vars) == 0) {
					throw new ExpressionError("Divide by zero!");
				}
				return arg1.evaluate(vars) / arg2.evaluate(vars);
			
			case OP_EXP :
				return (Math.pow(arg1.evaluate(vars), arg2.evaluate(vars)));
			
			default:
				throw new ExpressionError("Illegal operator in expression!");		}
	}
	
	public String toString() {
		return "(" + arg1.toString() + " " + opVals[oper] + " " + arg2.toString() + ")";
	}
}
