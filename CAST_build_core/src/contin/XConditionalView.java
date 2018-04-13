package contin;

import dataView.*;


public class XConditionalView extends YConditionalView {
//	static public final String XCONDITVIEW = "xCondit";
	
	public XConditionalView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
	}
	
	protected String getConditString(CoreVariable yVar, CoreVariable xVar) {
		return "(" + xVar.name + " | " + yVar.name + ")";
	}
	
	protected boolean hasRightMargin() {
		return false;
	}
	
	protected boolean hasBottomMargin() {
		return true;
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		double[][] yConditXProb = ((ContinResponseVariable)yVar).getConditionalProbs();
		double[] xMarginalProb = ((CatDistnVariable)xVar).getProbs();
		
		double[] yMarginalProb = new double[nYCats];
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++)
				yMarginalProb[j] += xMarginalProb[i] * yConditXProb[i][j];
		
		double[][] xConditionalProb = new double[nXCats][];
		
		for (int i=0 ; i<nXCats ; i++) {
			xConditionalProb[i] = new double[nYCats];
			for (int j=0 ; j<nYCats ; j++)
				if (yMarginalProb[j] > 0.0)
					xConditionalProb[i][j] = yConditXProb[i][j] * xMarginalProb[i] / yMarginalProb[j];
		}
		
		return xConditionalProb;
	}
	
	protected boolean canDrag() {
		return true;
	}
}
