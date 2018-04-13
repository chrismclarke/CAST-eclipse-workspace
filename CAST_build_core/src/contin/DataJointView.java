package contin;

import java.awt.*;

import dataView.*;


public class DataJointView extends CoreTableView {
//	static public final String DATAJOINTVIEW = "dataJoint";
	
	private LabelValue kPropnString, kCountString;
	
	public DataJointView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
		kPropnString = new LabelValue(applet.translate("Sample proportions"));
		kCountString = new LabelValue(applet.translate("Sample counts"));
	}
	
	private LabelValue getHeadingLabel() {
		return (probDecimals > 0) ? kPropnString : kCountString;
	}
	
	protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar) {
		int width = getHeadingLabel().stringWidth(g);
		int height = ascent + descent + kYLabelBorder;
		return new Dimension(width, height);
	}
	
	protected int noOfTableCols() {
		return nYCats;
	}
	
	protected int noOfTableRows() {
		return nXCats;
	}
	
	protected void drawHeading(Graphics g, int horizCenter, CoreVariable yVar, CoreVariable xVar) {
		getHeadingLabel().drawCentred(g, horizCenter, ascent);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (probDecimals > 0)
			return super.getMaxValueWidth(g);
		else {
			int maxVal = 1;
			for (int i=0 ; i<-probDecimals ; i++)
				maxVal *= 10;
			return g.getFontMetrics().stringWidth(String.valueOf(maxVal));
		}
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		CatVariable yCatVar = (CatVariable)yVar;
		CatVariable xCatVar = (CatVariable)xVar;
		int[][] counts = xCatVar.getCounts(yCatVar);
		int totalCount = xCatVar.noOfValues();
		
		int nXCats = xCatVar.noOfCategories();
		int nYCats = yCatVar.noOfCategories();
		
		double[][] probs = new double[nXCats][];
		for (int i=0 ; i<nXCats ; i++) {
			probs[i] = new double[nYCats];
			for (int j=0 ; j<nYCats ; j++) {
				probs[i][j] = counts[i][j];
				if (probDecimals > 0)
					probs[i][j] /= (double)totalCount;
			}
		}
		
		return probs;
	}
}
