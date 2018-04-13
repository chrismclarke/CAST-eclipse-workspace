package twoGroup;

import java.awt.*;

import dataView.*;
import formula.*;
import imageGroups.*;



public class DiffMeanCalcPanel extends MainFormulaPanel {
	static final private Color kGreenColor = new Color(0x006600);
	
	static private FormulaPanel createFormula(FormulaContext context) {
		FormulaContext greenContext = context.getRecoloredContext(kGreenColor);
		
		return new Binary(Binary.MINUS,
								new Edit("1.0", 3, greenContext),
								new Edit("1.0", 3, greenContext),
								greenContext);
	}
	
	public DiffMeanCalcPanel(NumValue maxResultVal, FormulaContext context) {
		super(GroupsEqualsImages.muDiffHat, GroupsEqualsImages.kDiffHatParamWidth,
					GroupsEqualsImages.kDiffHatParamAscent, GroupsEqualsImages.kDiffHatParamDescent,
					createFormula(context), maxResultVal, context.getRecoloredContext(kGreenColor));
	}
}