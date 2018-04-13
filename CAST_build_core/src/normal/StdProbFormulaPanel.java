package normal;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class StdProbFormulaPanel extends TextFormulaSequence {
	static final private Color kXColor = Color.blue;
	static final private Color kZColor = new Color(0xCC0000);
	
	public StdProbFormulaPanel(DataSet data, String xKey, String zKey,
																						NumValue maxX, FormulaContext context) {
		super(context);
		
		XApplet applet = context.getApplet();
		String xName = data.getVariable(xKey).name;
		FormulaContext xContext = context.getRecoloredContext(kXColor);
		FormulaContext zContext = context.getRecoloredContext(kZColor);
		
		addItem("P(" + xName + " \u2264");
		
			OneValueView xView = new OneValueView(data, xKey, applet, maxX);
			xView.setCenterValue(true);
			xView.setNameDraw(false);
			xView.unboxValue();
			SummaryValue xValue = new SummaryValue(xView, xContext);
		addItem(xValue);
		
		addItem(")  =  P(" + xName + "  \u2264  \u03BC +");
		
			int zDecimals = ((NumVariable)data.getVariable(zKey)).getMaxDecimals();
			NumValue maxZ = new NumValue(-1.0, zDecimals);
			OneValueView zView = new OneValueView(data, zKey, applet, maxZ);
			zView.setCenterValue(true);
			zView.setNameDraw(false);
			SummaryValue zValue = new SummaryValue(zView, zContext);
		addItem(zValue);
		
		addItem("\u03C3)");
	}
}