package formula;

import dataView.*;


public class MStandardFormulae {

	static public MFormula zFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		
		MBinary diff = new MBinary(MBinary.MINUS, new MText("x", stdContext), new MText("#mu#", stdContext),
																																				stdContext);
		MRatio zRatio = new MRatio(diff, new MText("#sigma#", stdContext), stdContext);
		return new MBinary(MBinary.EQUALS, new MText("z", stdContext), zRatio, stdContext);
	}
	
	
	static public MFormula sdSumFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		MFormula rootN = new MRoot(new MText("n", stdContext), stdContext);
		return new MBinary(MBinary.TIMES, rootN, new MText("#sigma#", stdContext), stdContext);
	}
	
	static public MFormula sdMeanFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		MFormula rootN = new MRoot(new MText("n", stdContext), stdContext);
		return new MRatio(new MText("#sigma#", stdContext), rootN, stdContext);
	}
	
	static public MFormula xSdFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		
		MFormula piOneMinusPi = new MText("#pi#(1 - #pi#)", stdContext);
		MFormula varFormula = new MBinary(MBinary.TIMES, new MText("n", stdContext), piOneMinusPi, stdContext);
		return new MBinary(MBinary.EQUALS, new MText("#sigma#", stdContext),
																					new MRoot(varFormula, stdContext), stdContext);
	}
	
	static public MFormula pSdFormula(XApplet applet) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		
		MFormula piOneMinusPi = new MText("#pi#(1 - #pi#)", stdContext);
		MFormula varFormula = new MRatio(piOneMinusPi, new MText("n", stdContext), stdContext);
		return new MBinary(MBinary.EQUALS, new MText("#sigma#", stdContext),
																		new MRoot(varFormula, stdContext), stdContext);
	}
	
}