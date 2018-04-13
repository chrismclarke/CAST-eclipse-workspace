package normal;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class ZProbFormulaPanel extends TextFormulaSequence {
	static final private Color kXColor = Color.blue;
	static final private Color kZColor = new Color(0xCC0000);
	
	public ZProbFormulaPanel(DataSet data, String xKey, String zKey,
												String probKey, NumValue maxX, FormulaContext context) {
		super(context);
		
		XApplet applet = context.getApplet();
		FormulaContext xContext = context.getRecoloredContext(kXColor);
		FormulaContext zContext = context.getRecoloredContext(kZColor);
		
		String xName = data.getVariable(xKey).name;
		
		addItem("P(" + xName + " \u2264");
		
			OneValueView xView = new OneValueView(data, xKey, applet, maxX);
			xView.setCenterValue(true);
			xView.setNameDraw(false);
			xView.unboxValue();
			SummaryValue xValue = new SummaryValue(xView, xContext);
		addItem(xValue);
		
		addItem(")  =  P(Z \u2264 ");
		
			int zDecimals = ((NumVariable)data.getVariable(zKey)).getMaxDecimals();
			NumValue maxZ = new NumValue(-1.0, zDecimals);
			OneValueView zView = new OneValueView(data, zKey, applet, maxZ);
			zView.setCenterValue(true);
			zView.setNameDraw(false);
			zView.unboxValue();
			SummaryValue zValue = new SummaryValue(zView, zContext);
		addItem(zValue);
		
		addItem(") = ");
		
			int probDecimals = ((NumVariable)data.getVariable(probKey)).getMaxDecimals();
			NumValue maxProb = new NumValue(1.0, probDecimals);
			OneValueView probView = new OneValueView(data, probKey, applet, maxProb);
			probView.setNameDraw(false);
			SummaryValue probValue = new SummaryValue(probView, context);
		addItem(probValue);
	}
}