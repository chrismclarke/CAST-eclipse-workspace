package matrix;

import java.awt.*;

import dataView.*;



public class LinearPartMatrixView extends CoreMatrixView {
//	static public final String LINEAR_MATRIX_VIEW = "linearMatrixView";
	
	static final private int kHeadingBorder = 2;
	
	private String heading;
	
	public LinearPartMatrixView(DataSet theData, XApplet applet, String heading) {
		super(theData, applet);
		this.heading = heading;
	}
	
//----------------------------------
	
	protected int getContentWidth(Graphics g) {
		int width = 0;
		for (int i=0 ; i<columns.length ; i++) {
			boolean withPlus = i > 0;
			width += columns[i].maxLinearPartWidth(g, withPlus);
//			System.out.println("getContentWidth(), col " + i + ": width = " + columns[i].maxLinearPartWidth(g, withPlus));
		}
			
		if (heading != null)
			width = Math.max(width, g.getFontMetrics().stringWidth(heading));
		
		return width;
	}
	
	protected int getNoOfRows(Graphics g) {
		return getSelection().getNoOfFlags();
	}
	
	protected boolean isSelectedRow(int row) {
		return getSelection().valueAt(row);
	}
	
//----------------------------------
	
	protected int getHeadingHeight(Graphics g) {
		return heading == null ? 0 : (ascent + descent + kHeadingBorder);
	}
	
	protected void drawHeading(int horiz, int bottom, Graphics g) {
		if (heading != null) {
			horiz += (getContentWidth(g) - g.getFontMetrics().stringWidth(heading)) / 2;
			g.drawString(heading, horiz, bottom - kHeadingBorder - descent);
		}
	}
	
//----------------------------------
	
	protected void drawMatrixRow(int row, boolean selected, int horiz, int baseline,
																																					Graphics g) {
		for (int i=0 ; i<columns.length ; i++) {
			Color c = columns[i].getTermColor();
			g.setColor(c == null || !selected ? getForeground() : c);
			
			boolean withPlus = i > 0;
			columns[i].drawLinearPartString(row, horiz, baseline, withPlus, g);
			horiz += columns[i].maxLinearPartWidth(g, withPlus);
//			System.out.println("drawMatrixRow(), col " + i + ": width = " + columns[i].maxLinearPartWidth(g, withPlus));
		}
	}
}