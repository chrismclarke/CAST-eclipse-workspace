package exerciseGroups;

import dataView.*;
import formula.*;


public class ProbCalcFormula extends MBinary {
	
	private MConst xConst, nConst, pConst;
	private MFormula ratio;
	
	public ProbCalcFormula(int subscript, FormulaContext context) {
		super(MBinary.EQUALS, context);
		
		MFormula label = new MSubscript(new MText("p", context), String.valueOf(subscript), context);
		
			xConst = new MConst(new NumValue(0), context);
			nConst = new MConst(new NumValue(0), context);
		ratio = new MRatio(xConst, nConst, context);
		
			pConst = new MConst(new NumValue(0), context);
		
		MBinary equation = new MBinary(MBinary.EQUALS, ratio, pConst, context);
		setFormula(label, equation);
	}
	
	public void setRatio(int x, int n, int pDecimals) {
		xConst.setValue(new NumValue(x, 0));
		nConst.setValue(new NumValue(n, 0));
		double p = ((double)x) / n;
		pConst.setValue(new NumValue(p, pDecimals));
		ratio.reinitialise();
		reinitialise();
		repaint();
	}
}