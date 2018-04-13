package normal;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class ZInvFormulaPanel extends Binary {
	static final private Color kXColor = Color.blue;
	static final private Color kZColor = new Color(0xCC0000);
	
//	static final private LabelValue kZEquals = new LabelValue("z");
	
	static Binary rightFormula(DataSet data, String xKey, String zKey,
												NumValue maxX, NumValue mean, NumValue sd, FormulaContext context) {
			XApplet applet = context.getApplet();
			FormulaContext xContext = context.getRecoloredContext(kXColor);
			FormulaContext zContext = context.getRecoloredContext(kZColor);
			
			int zDecimals = ((NumVariable)data.getVariable(zKey)).getMaxDecimals();
			NumValue maxZ = new NumValue(-1.0, zDecimals);
			OneValueView zView = new OneValueView(data, zKey, applet, maxZ);
			zView.setCenterValue(true);
			zView.setNameDraw(false);
			zView.unboxValue();
			SummaryValue zValue = new SummaryValue(zView, zContext);
			
		Binary product = new Binary(Binary.TIMES, zValue, new TextLabel(sd, context), context);
		Binary xFormula = new Binary(Binary.PLUS, new TextLabel(mean, context), product, context);
		
			OneValueView xView = new OneValueView(data, xKey, applet, maxX);
			xView.setCenterValue(true);
			xView.setNameDraw(false);
		SummaryValue rightPart = new SummaryValue(xView, xContext);
		
		return new Binary(Binary.EQUALS, xFormula, rightPart, context);
	}
	
	public ZInvFormulaPanel(DataSet data, String xKey, String zKey,
													NumValue maxX, NumValue mean, NumValue sd, FormulaContext context) {
		super(Binary.EQUALS, new TextLabel(data.getVariable(xKey).name, context.getRecoloredContext(kZColor)),
														rightFormula(data, xKey, zKey, maxX, mean, sd, context), context);
	}
}