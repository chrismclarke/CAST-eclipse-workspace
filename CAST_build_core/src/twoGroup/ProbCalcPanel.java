package twoGroup;

import dataView.*;
import formula.*;
import imageGroups.*;



public class ProbCalcPanel extends MainFormulaPanel {
	
	static private FormulaPanel createFormula(FormulaContext context) {
		return new Ratio(new Edit("1", 3, context),  new Edit("1", 3, context),  context);
	}
	
	public ProbCalcPanel(int decimals, FormulaContext context) {
		super(GroupsEqualsImages.p, GroupsEqualsImages.kParamWidth,
					GroupsEqualsImages.kParamAscent, GroupsEqualsImages.kParamDescent,
					createFormula(context), new NumValue(1.0, decimals), context);
	}
}