package matrix;

import java.awt.*;

import dataView.*;



public class ResidualMatrixView extends LinearPartMatrixView {
//	static public final String RESID_MATRIX_VIEW = "residualMatrixView";
	
	static final private int kMinusWidth = 24;
	
	private ModelTerm yColumn;
	
	private int linearWidth, yWidth;
	
	public ResidualMatrixView(DataSet theData, XApplet applet, String heading) {
		super(theData, applet, heading);
	}
	
	public void setResponseColumn(ModelTerm yColumn) {
		this.yColumn = yColumn;
	}
	
//----------------------------------
	
	protected int getContentWidth(Graphics g) {
		linearWidth = super.getContentWidth(g);
		
		yWidth = yColumn.maxLinearPartWidth(g, false);
		
		FontMetrics fm = g.getFontMetrics();
		
		return yWidth + kMinusWidth + fm.stringWidth("(") + linearWidth + fm.stringWidth("(");
	}
	
	protected void drawMatrixRow(int row, boolean selected, int horiz, int baseline,
																																					Graphics g) {
		Color c = yColumn.getTermColor();
		g.setColor(c == null ? getForeground() : c);
		yColumn.drawLinearPartString(row, horiz, baseline, false, g);
		horiz += yWidth;
		
		g.setColor(getForeground());
		g.drawLine(horiz + kMinusWidth / 2 - 3, baseline - 3,
																							horiz + kMinusWidth / 2 + 3, baseline - 3);
		horiz += kMinusWidth;
		
		g.drawString("(", horiz, baseline);
		horiz += g.getFontMetrics().stringWidth("(");
		
		super.drawMatrixRow(row, selected, horiz, baseline, g);
		horiz += linearWidth;
		
		g.setColor(getForeground());
		g.drawString(")", horiz, baseline);
	}
}