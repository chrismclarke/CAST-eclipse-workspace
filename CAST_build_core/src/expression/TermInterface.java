package expression;

import java.util.*;

public interface TermInterface {
	public double evaluate(Hashtable vars) throws ExpressionError;
	public String toString();
}
