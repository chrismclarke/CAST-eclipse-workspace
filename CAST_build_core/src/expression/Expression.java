package expression;

import java.util.*;
import java.io.*;

import dataView.*;


public class Expression {
  private Hashtable variables = new Hashtable();
	private TermInterface expr;
	private String parseError = null;
	
	public Expression(String s) {
		StreamTokenizer st = new StreamTokenizer(new StringReader(s));
		st.lowerCaseMode(true);
		st.ordinaryChar('/');
		st.ordinaryChar('-');
		
		try {
			expr = expression(st);
			
			try {
				int nextToken = st.nextToken();
				if (nextToken != StreamTokenizer.TT_EOF)
					throw new ExpressionError("Extra text at end of expression");
			} catch (IOException e) {
				throw new ExpressionError("IO error");
			}
		} catch (ExpressionError e) {
			parseError = e.toString();
		}
	}
	
	public void defineVariable(String var, Double value) {
		variables.put(var, value);
	}
	
	public Value evaluate(int decimals) {
		if (parseError != null)
			return new LabelValue(parseError.toString());
		
		try {
			double value = expr.evaluate(variables);
			return new NumValue(value, decimals);
		} catch (ExpressionError e) {
			return new LabelValue(e.toString());
		}
	}

	private TermInterface element(StreamTokenizer st) throws ExpressionError {
		TermInterface result = null;

		try {
			switch (st.nextToken()) {
				case StreamTokenizer.TT_NUMBER :
					result = new ConstantTerm(st.nval);
					break;
				case StreamTokenizer.TT_WORD :
					String word = st.sval;
					int function = FunctionTerm.findOperator(word);
					if (function < 0)
						result = new VariableTerm(word);
					else {
						st.nextToken();
						if (st.ttype != '(') {
							st.pushBack();
							throw new ExpressionError("Function name not followed by open bracket.");
						}
						TermInterface param = expression(st);
						st.nextToken();
						if (st.ttype != ')') {
							st.pushBack();
							throw new ExpressionError("Mismatched parenthesis or missing operator.");
						}
						return new FunctionTerm(function, param);
					}
					break;
				case '(' :
					result = expression(st);
					st.nextToken();
					if (st.ttype != ')') {
						st.pushBack();
						throw new ExpressionError("Mismatched parenthesis or missing operator.");
					}
					break;
				default:
					st.pushBack();
					throw new ExpressionError("Unexpected symbol on input.");
			}
		} catch (IOException ioe) {
			throw new ExpressionError("Caught an I/O exception.");
		}
		return result;
	}

	private TermInterface primary(StreamTokenizer st) throws ExpressionError {
		try {
			switch (st.nextToken()) {
				case '-' :
					return new FunctionTerm(FunctionTerm.OP_NEG, primary(st));
				default:
					st.pushBack();
					return element(st);
			}
		} catch (IOException ioe) {
			throw new ExpressionError("Caught an I/O Exception.");
		}
	}

	private TermInterface factor(StreamTokenizer st) throws ExpressionError {
		TermInterface result;

		result = primary(st);
		try {
			switch (st.nextToken()) {
				case '^':
					result = new BinaryTerm(BinaryTerm.OP_EXP, result, factor(st));
					break;
				default:
					st.pushBack();
					break;
			}
		} catch (IOException ioe) {
			throw new ExpressionError("Caught an I/O Exception.");
		}
		return result;
	}

	private TermInterface term(StreamTokenizer st) throws ExpressionError {
		TermInterface result;
		boolean done = false;

		result = factor(st);
		while (! done) {
			try {
				switch (st.nextToken()) {
					case '*' :
						result = new BinaryTerm(BinaryTerm.OP_MUL, result, factor(st));
						break;
					case '/' :
						result = new BinaryTerm(BinaryTerm.OP_DIV, result, factor(st));
						break;
					default :
						st.pushBack();
						done = true;
						break;
				}
			} catch (IOException ioe) {
				throw new ExpressionError("Caught an I/O exception.");
			}
		}
		return result;
	}

	private TermInterface expression(StreamTokenizer st) throws ExpressionError {
		TermInterface result;
		boolean done = false;

		result = term(st);

		while (! done) {
			try {
				switch (st.nextToken()) {
					case '+':
						result = new BinaryTerm(BinaryTerm.OP_ADD, result, term(st));
						break;
					case '-':
						result = new BinaryTerm(BinaryTerm.OP_SUB, result, term(st));
						break;
					default :
						st.pushBack();
						done = true;
						break;
				}
			} catch (IOException ioe) {
				throw new ExpressionError("Caught an I/O Exception.");
			}
		}
		return result;
	}
}