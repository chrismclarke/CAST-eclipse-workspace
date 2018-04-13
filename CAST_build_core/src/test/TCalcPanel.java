package test;

import java.awt.*;

import dataView.*;
import formula.*;



public class TCalcPanel extends MainFormulaPanel {
	static final private Color kGreenColor = new Color(0x006600);
	
	static final private NumValue kMaxWidthValue = new NumValue("-999.999");
	
	static private FormulaPanel createFormula(FormulaContext blackContext) {
		FormulaContext greenContext = blackContext.getRecoloredContext(kGreenColor);
		FormulaContext blueContext = blackContext.getRecoloredContext(Color.blue);
		FormulaContext redContext = blackContext.getRecoloredContext(Color.red);
		
		FormulaPanel numer = new Binary(Binary.MINUS,
								new Edit("0.0", 3, blueContext), new Edit("0.0", 3, blueContext), blueContext);
		
		FormulaPanel denomCore = new Ratio(new Edit("1.0", 3, redContext),
											new Root(new Edit("1", 3, greenContext), greenContext),
											blackContext);
		
		
		return new Ratio(numer, new Bracket(denomCore, blackContext), blackContext);
	}
	
	public TCalcPanel(FormulaContext context) {
		super(createFormula(context), kMaxWidthValue, context);
	}
}