package normal;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;


public class ZFormulaPanel extends Binary {
	static final private Color kXColor = Color.blue;
	static final private Color kZColor = new Color(0xCC0000);
	
//	static final private LabelValue kZEquals = new LabelValue("z");
	
	static Binary rightFormula(DataSet data, String xKey, String zKey,
										NumValue maxX, NumValue mean, NumValue sd, FormulaContext context) {
			XApplet applet = context.getApplet();
			FormulaContext xContext = context.getRecoloredContext(kXColor);
			FormulaContext zContext = context.getRecoloredContext(kZColor);
			
			OneValueView xView = new OneValueView(data, xKey, applet, maxX);
			xView.setCenterValue(true);
			xView.setNameDraw(false);
			xView.unboxValue();
			SummaryValue xValue = new SummaryValue(xView, xContext);
			
		Binary numer = new Binary(Binary.MINUS, xValue, new TextLabel(mean, context), context);
		Ratio middlePart = new Ratio(numer, new TextLabel(sd, context), context);
		
			int zDecimals = ((NumVariable)data.getVariable(zKey)).getMaxDecimals();
			NumValue maxZ = new NumValue(-1.0, zDecimals);
			OneValueView zView = new OneValueView(data, zKey, applet, maxZ);
			zView.setCenterValue(true);
			zView.setNameDraw(false);
		SummaryValue rightPart = new SummaryValue(zView, zContext);
		
		return new Binary(Binary.EQUALS, middlePart, rightPart, context);
	}
	
	public ZFormulaPanel(DataSet data, String xKey, String zKey,
													NumValue maxX, NumValue mean, NumValue sd, FormulaContext context) {
		super(Binary.EQUALS, new TextLabel(data.getVariable(zKey).name, context.getRecoloredContext(kZColor)),
														rightFormula(data, xKey, zKey, maxX, mean, sd, context), context);
	}
}