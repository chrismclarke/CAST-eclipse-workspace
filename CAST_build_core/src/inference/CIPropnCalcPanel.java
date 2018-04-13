package inference;

import dataView.*;
import formula.*;



public class CIPropnCalcPanel extends MainFormulaPanel {
	static final private NumValue kMaxWidthValue = new NumValue("0.0000");
	
	static private FormulaPanel createFormula(FormulaContext context) {
		FormulaPanel ratio = new Root(new Ratio(new Binary(Binary.TIMES, new Edit("0.5", 3, context),
														new Edit("0.5", 3, context), context),
											new Edit("1", 3, context), context),
											context);
		
		return new Binary(Binary.TIMES,
								new Edit("0.000", 3, context), ratio, context);
	}
	
	public CIPropnCalcPanel(FormulaContext context) {
		super(createFormula(context), kMaxWidthValue, context);
	}
}