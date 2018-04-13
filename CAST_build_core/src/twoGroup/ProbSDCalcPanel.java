package twoGroup;

import dataView.*;
import formula.*;
import imageGroups.*;



public class ProbSDCalcPanel extends MainFormulaPanel {
	
	static private FormulaPanel createFormula(FormulaContext context) {
		FormulaPanel oneMinus = new Binary(Binary.MINUS, new Const(new NumValue(1.0, 0), context),
																			new Edit("0.5", 3, context), context);
		oneMinus = new Bracket(oneMinus, context);
		
		FormulaPanel numer = new Binary(Binary.TIMES, new Edit("0.5", 3, context), oneMinus, context);
		
		FormulaPanel ratio = new Ratio(numer, new Edit("1", 3, context), context);
		
		return new Root(ratio, context);
	}
	
	public ProbSDCalcPanel(int decimals, FormulaContext context) {
		super(GroupsEqualsImages.pSDHat, GroupsEqualsImages.kPSDHatWidth,
					GroupsEqualsImages.kPSDHatAscent, GroupsEqualsImages.kPSDHatDescent,
					createFormula(context), new NumValue(1.0, decimals), context);
	}
}