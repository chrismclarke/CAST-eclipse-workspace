package normal;

import dataView.*;
import formula.*;



public class ZCalcPanel extends MainFormulaPanel {
	static final private NumValue kMaxZValue = new NumValue("-99.999");
	
	static private FormulaPanel createFormula(FormulaContext context) {
		FormulaPanel numer = new Binary(Binary.MINUS,
								new Edit("0.0", 3, context), 
								new Edit("0.0", 3, context),
								context);
		return new Ratio(numer, new Edit("1.0", 3, context), context);
	}
	
	public ZCalcPanel(FormulaContext context) {
		super("z =", createFormula(context), kMaxZValue, context);
	}
}