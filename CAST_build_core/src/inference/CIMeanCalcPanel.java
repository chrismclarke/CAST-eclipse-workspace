package inference;

import dataView.*;
import formula.*;



public class CIMeanCalcPanel extends MainFormulaPanel {
//	static final private Color kGreenColor = new Color(0x006600);
	
	static final private NumValue kMaxWidthValue = new NumValue("-999.999");
	
	static private FormulaPanel createFormula(FormulaContext context) {
		FormulaPanel ratio = new Ratio(new Edit("1.0", 3, context),
											new Root(new Edit("1", 3, context), context),
											context);
		
		return new Binary(Binary.TIMES,
								new Edit("0.000", 3, context), ratio, context);
	}
	
	public CIMeanCalcPanel(FormulaContext context) {
		super(createFormula(context), kMaxWidthValue, context);
	}
}