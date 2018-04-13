package contin;

import java.awt.*;

import dataView.*;


public class JointView extends YConditionalView {
//	static public final String JOINTVIEW = "jointView";
	
	protected LabelValue kJointString;
	
	public JointView(DataSet theData, XApplet applet, String yKey, String xKey, int probDecimals) {
		super(theData, applet, yKey, xKey, probDecimals);
		kJointString = new LabelValue(applet.translate("Joint probabilities"));
	}
	
	protected Dimension getHeadingSize(Graphics g, CoreVariable yVar, CoreVariable xVar) {
		int width = kJointString.stringWidth(g);
		int height = ascent + descent + kYLabelBorder;
		return new Dimension(width, height);
	}
	
	protected int noOfTableCols() {
		return nYCats;
	}
	
	protected int noOfTableRows() {
		return nXCats;
	}
	
	protected boolean hasRightMargin() {
		return false;
	}
	
	protected void drawHeading(Graphics g, int horizCenter, CoreVariable yVar, CoreVariable xVar) {
		int baseline = ascent;
		kJointString.drawCentred(g, horizCenter, baseline);
	}
	
	protected double[][] getProbs(CoreVariable yVar, CoreVariable xVar) {
		double[][] yConditXProb = ((ContinResponseVariable)yVar).getConditionalProbs();
		double[] xMarginalProb = ((CatDistnVariable)xVar).getProbs();
		
		double[][] jointProb = new double[nXCats][];
		for (int i=0 ; i<nXCats ; i++) {
			jointProb[i] = new double[nYCats];
			for (int j=0 ; j<nYCats ; j++)
				jointProb[i][j] = xMarginalProb[i] * yConditXProb[i][j];
		}
		
		return jointProb;
	}
	
	protected boolean canDrag() {
		return true;
	}
}
