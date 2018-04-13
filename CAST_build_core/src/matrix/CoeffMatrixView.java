package matrix;

import java.awt.*;

import dataView.*;



public class CoeffMatrixView extends CoreMatrixView {
//	static public final String COEFF_MATRIX_VIEW = "coeffMatrixView";
	
	public CoeffMatrixView(DataSet theData, XApplet applet) {
		super(theData, applet);
	}
	
//----------------------------------

	protected Color getBackgroundColor() {
		int selectedRow = getSelection().findSingleSetFlag();
		return (selectedRow < 0) ? Color.white : kSelectedBackground;
	}
	
//----------------------------------
	
	protected int getContentWidth(Graphics g) {
		int maxSymbolWidth = 0;
		for (int i=0 ; i<columns.length ; i++)
			maxSymbolWidth = Math.max(maxSymbolWidth, columns[i].maxParamWidth(g));
		return maxSymbolWidth;
	}
	
	protected int getNoOfRows(Graphics g) {
		int noOfCoeffs = 0;
		for (int i=0 ; i<columns.length ; i++)
			noOfCoeffs += columns[i].noOfParameters();
		return noOfCoeffs;
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
	
	protected void drawMatrixRow(int row, boolean selected, int horiz, int baseline,
																																					Graphics g) {
		for (int i=0 ; i<columns.length ; i++) {
			int termParams = columns[i].noOfParameters();
			if (row < termParams) {
				Color c = columns[i].getTermColor();
				if (c != null)
					g.setColor(c);
				columns[i].drawParameter(row, horiz, baseline, g);
				return;
			}
			else
				row -= termParams;
		}
	}
}