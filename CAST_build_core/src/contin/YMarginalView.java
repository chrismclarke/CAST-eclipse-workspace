package contin;

import java.awt.*;

import dataView.*;


public class YMarginalView extends CoreTableView {
//	static public final String YMARGINVIEW = "yMargin";
	
//	static final private int kPlusSize = 21;
	
	public YMarginalView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
	}
	
	protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar) {
		return new Dimension(0, 0);
	}
	
	protected int noOfTableCols() {
		return nYCats;
	}
	
	protected int noOfTableRows() {
		return 1;
	}
	
	protected void drawHeading(Graphics g, int horizCenter, CoreVariable yVar, CoreVariable xVar) {
	}
	
	protected double[] getSingleProbs(CoreVariable yVar, CoreVariable xVar) {
		double[][] yConditXProb = ((ContinResponseVariable)yVar).getConditionalProbs();
		double[] xMarginalProb = ((CatDistnVariable)xVar).getProbs();
		
		double[] yMarginalProb = new double[nYCats];
		for (int i=0 ; i<nXCats ; i++)
			for (int j=0 ; j<nYCats ; j++)
				yMarginalProb[j] += xMarginalProb[i] * yConditXProb[i][j];
		
		return yMarginalProb;
	}
	
	protected boolean canDrag() {
		return true;
	}
}
