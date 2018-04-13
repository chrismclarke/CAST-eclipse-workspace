package contin;

import java.awt.*;

import dataView.*;


public class MarginArrowView extends JointArrowView {
//	static public final String MARGINARROW = "marginArrow";
	
	public MarginArrowView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
	}
	
	protected int noOfValues() {
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		return xVar.noOfCategories();
	}
	
	protected void drawSign(Graphics g, int horizPos, int baseline) {
																//		Plus
		g.drawLine(horizPos, baseline - kSignSize / 2 - 1, horizPos + kSignSize, baseline - kSignSize / 2 - 1);
		g.drawLine(horizPos + kSignSize / 2, baseline - 1 - kSignSize, horizPos + kSignSize / 2, baseline - 1);
	}
	
	protected double[] getValues(ContinResponseVariable yVar, CatDistnVariable xVar) {
		double[][] yConditXProb = yVar.getConditionalProbs();
		double[] xMarginalProb = xVar.getProbs();
		
		double[] vals = new double[xMarginalProb.length];
		for (int i=0 ; i<xMarginalProb.length ; i++)
			vals[i] = xMarginalProb[i] * yConditXProb[i][selectedY];
		return vals;
	}
}
