package matrix;

import java.awt.*;

import dataView.*;



public class DataMatrixView extends CoreMatrixView {
//	static public final String MATRIX_VIEW = "matrixView";
	
	static final private int kColumnGap = 14;
	
	static final public Color kPaleGray = new Color(0xEEEEEE);
	
	private int columnWidth[];
	private int headingWidth[];
	
	public DataMatrixView(DataSet theData, XApplet applet) {
		super(theData, applet);
	}
	
//----------------------------------
	
	protected int getContentWidth(Graphics g) {
		int noOfColGroups = columns.length;
		columnWidth = new int[noOfColGroups];
		headingWidth = new int[noOfColGroups];
		
		int totalWidth = 0;
		for (int i=0 ; i<noOfColGroups ; i++) {
			columnWidth[i] = columns[i].matrixColumnWidth(g);
			
			Dimension headingDim = columns[i].matrixHeadingSize(g);
			if (headingDim != null)
				headingWidth[i] = headingDim.width;
			
			totalWidth += (kColumnGap + Math.max(columnWidth[i], headingWidth[i]));
		}
		return totalWidth;
	}
	
	protected int getNoOfRows(Graphics g) {
		return getSelection().getNoOfFlags();
	}
	
	protected boolean isSelectedRow(int row) {
		return getSelection().valueAt(row);
	}
	
//----------------------------------
	
	protected int getHeadingHeight(Graphics g) {
		int headingHt = 0;
		int noOfColGroups = columns.length;
		
		for (int i=0 ; i<noOfColGroups ; i++) {
			Dimension headingDim = columns[i].matrixHeadingSize(g);
			if (headingDim != null)
				headingHt = Math.max(headingHt, headingDim.height);
		}
		return headingHt;
	}
	
//----------------------------------
	
	protected void drawHeading(int horiz, int bottom, Graphics g) {
		int columnLeft = horiz + kColumnGap / 2;
		for (int i=0 ; i<columns.length ; i++) {
			int colWidth = Math.max(columnWidth[i], headingWidth[i]);
			if (headingWidth[i] > 0) {
				Color c = columns[i].getTermColor();
				g.setColor(c == null ? getForeground() : c);
				columns[i].drawMatrixHeading(columnLeft + colWidth / 2, bottom, g);
			}
			columnLeft += colWidth + kColumnGap;
		}
	}
	
	protected void drawMatrixRow(int row, boolean selected, int horiz, int baseline,
																																					Graphics g) {
		int colStart = horiz + kColumnGap / 2;
		for (int i=0 ; i<columns.length ; i++) {
			int colWidth = Math.max(columnWidth[i], headingWidth[i]);
			int valueRight = colStart + (colWidth + columnWidth[i]) / 2;
			
			if (selected) {
				Color c = columns[i].getTermColor();
				if (c != null)
					g.setColor(c);
			}
			columns[i].drawMatrixValue(row, valueRight, baseline, g);
			if (selected)
				g.setColor(getForeground());
			
			colStart += colWidth + kColumnGap;
		}
	}
}