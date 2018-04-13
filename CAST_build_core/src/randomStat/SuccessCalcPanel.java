package randomStat;

import java.awt.*;

import dataView.*;
import formula.*;



public class SuccessCalcPanel extends MainFormulaPanel {
	static final public int MEAN = 0;
	static final public int SD = 1;
	
//	static final private Color kGreenColor = new Color(0x006600);
	
	static final private NumValue kMaxWidthValue = new NumValue("999.99");
	
	static private FormulaPanel createFormula(int paramType, FormulaContext context) {
		FormulaPanel result = new Binary(Binary.TIMES,
								new Edit("1", 3, context), new Edit("0.5", 3, context), context);
		if (paramType == SD) {
			FormulaPanel oneMinus = new Binary(Binary.MINUS, new Const(new NumValue(1, 0), context),
																			new Edit("0.5", 3, context),
																			context);
			oneMinus = new Bracket(oneMinus, context);
			
			result = new Root(new Binary(Binary.TIMES, result, oneMinus, context), context);
		}
		return result;
	}
	
	static private Image getImage(int paramType, FormulaContext context) {
		SuccessImages.loadSuccess(context.getApplet());
		return (paramType == MEAN) ? SuccessImages.muXEquals : SuccessImages.sdXEquals;
	}
	
	public SuccessCalcPanel(int paramType, FormulaContext context) {
		super(getImage(paramType, context), SuccessImages.kParamWidth, SuccessImages.kParamAscent,
								SuccessImages.kParamDescent, createFormula(paramType, context),
								kMaxWidthValue, context);
	}
}