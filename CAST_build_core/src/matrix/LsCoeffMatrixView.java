package matrix;

import java.awt.*;

import dataView.*;
import models.*;



public class LsCoeffMatrixView extends CoreMatrixView {
//	static public final String LS_COEFF_MATRIX_VIEW = "LsCoeffMatrixView";
	
	private String lsKey;
	
	private int maxCoeffWidth;
	
	public LsCoeffMatrixView(DataSet theData, XApplet applet, String lsKey) {
		super(theData, applet);
		this.lsKey = lsKey;
	}
	
//----------------------------------

	protected Color getBackgroundColor() {
		int selectedRow = getSelection().findSingleSetFlag();
		return (selectedRow < 0) ? Color.white : kSelectedBackground;
	}
	
//----------------------------------
	
	protected int getContentWidth(Graphics g) {		//	called from doInitialisation()
		MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(lsKey);
		int noOfParams = lsModel.noOfParameters();
		
//		FontMetrics fm = g.getFontMetrics();
		
		maxCoeffWidth = 0;
		for (int i=0 ; i<noOfParams ; i++)
			maxCoeffWidth = Math.max(maxCoeffWidth, getParameter(lsModel, i).stringWidth(g));
		
		return maxCoeffWidth;
	}
	
	protected int getNoOfRows(Graphics g) {
		MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(lsKey);
		return lsModel.noOfParameters();
	}
	
	protected boolean isSelectedRow(int row) {
		return false;
	}
	
//----------------------------------
	
	protected int getHeadingHeight(Graphics g) {
		return 0;
	}
	
	protected void drawHeading(int horiz, int bottom, Graphics g) {
	}
	
	protected NumValue getParameter(MultipleRegnModel lsModel, int row) {
		return lsModel.getParameter(row);
	}
	
	protected void drawMatrixRow(int row, boolean selected, int horiz, int baseline,
																																					Graphics g) {
		initialise(g);
		
		int columnIndex = Math.min(row, columns.length - 1);
		Color c = columns[columnIndex].getTermColor();
		if (c != null)
			g.setColor(c);
		
		MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(lsKey);
		NumValue paramValue = getParameter(lsModel, row);
		paramValue.drawLeft(g, horiz + maxCoeffWidth, baseline);
	}
}