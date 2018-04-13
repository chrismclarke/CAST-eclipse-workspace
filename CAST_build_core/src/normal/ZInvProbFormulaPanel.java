package normal;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class ZInvProbFormulaPanel extends TextFormulaSequence {
//	static final private Color kXColor = Color.blue;
	static final private Color kZColor = new Color(0xCC0000);
	
	public ZInvProbFormulaPanel(DataSet data, String zKey,
																	String probKey, FormulaContext context) {
		super(context);
		
		XApplet applet = context.getApplet();
		FormulaContext zContext = context.getRecoloredContext(kZColor);
		
		addItem("P(Z \u2264 ");
		
			int zDecimals = ((NumVariable)data.getVariable(zKey)).getMaxDecimals();
			NumValue maxZ = new NumValue(-1.0, zDecimals);
			OneValueView zView = new OneValueView(data, zKey, applet, maxZ);
			zView.setCenterValue(true);
			zView.setNameDraw(false);
			SummaryValue zValue = new SummaryValue(zView, zContext);
		addItem(zValue);
		
		addItem(") = ");
		
			int probDecimals = ((NumVariable)data.getVariable(probKey)).getMaxDecimals();
			NumValue maxProb = new NumValue(1.0, probDecimals);
			OneValueView probView = new OneValueView(data, probKey, applet, maxProb);
			probView.setNameDraw(false);
			probView.unboxValue();
			SummaryValue probValue = new SummaryValue(probView, context);
		addItem(probValue);
	}
}