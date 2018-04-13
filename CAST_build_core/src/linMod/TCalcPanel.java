package linMod;

import java.awt.*;

import dataView.*;
import formula.*;



public class TCalcPanel extends MainFormulaPanel {
	static private FormulaPanel createFormula(FormulaContext context) {
		Edit e1 = new Edit("1.0", 3, context);
		Edit e2 = new Edit("1.0", 3, context);
		
		return new Ratio(e1, e2, context);
	}
	
	static private Image leftImage(FormulaContext context) {
		RatioImages.loadRatio(context.getApplet());
		return RatioImages.t;
	}
	
	public TCalcPanel(NumValue maxResultVal, FormulaContext context) {
		super(leftImage(context), RatioImages.kWidth, RatioImages.kAscent,
									RatioImages.kDescent, createFormula(context), maxResultVal, context);
	}
}