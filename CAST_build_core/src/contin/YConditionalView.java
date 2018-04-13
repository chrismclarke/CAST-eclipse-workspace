package contin;

import java.awt.*;

import dataView.*;


public class YConditionalView extends CoreTableView {
//	static public final String YCONDITVIEW = "yCondit";
	
	public YConditionalView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
	}
	
	protected String getConditString(CoreVariable yVar, CoreVariable xVar) {
		return "(" + yVar.name + " | " + xVar.name + ")";
	}
	
	protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar) {
		FontMetrics fm = g.getFontMetrics();
		LabelValue conditString = new LabelValue(getApplet().translate("Conditional"));
		int width = Math.max(conditString.stringWidth(g), fm.stringWidth(getConditString(yVar, xVar)));
		int height = 2 * (ascent + descent) + kYLabelBorder;
		return new Dimension(width, height);
	}
	
	protected int noOfTableCols() {
		return nYCats;
	}
	
	protected int noOfTableRows() {
		return nXCats;
	}
	
	protected boolean hasRightMargin() {
		return true;
	}
	
	protected void drawHeading(Graphics g, int horizCenter, CoreVariable yVar, CoreVariable xVar) {
		int baseline = ascent;
		LabelValue conditString = new LabelValue(getApplet().translate("Conditional"));
		conditString.drawCentred(g, horizCenter, baseline);
		baseline += ascent + descent;
		(new LabelValue(getConditString(yVar, xVar))).drawCentred(g, horizCenter, baseline);
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		return ((ContinResponseVariable)yVar).getConditionalProbs();
	}
}
