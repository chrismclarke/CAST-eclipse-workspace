package exerciseProb;

import java.awt.*;

import dataView.*;


public class CrossTableView extends DataView {
//	static final public String CROSS_TABLE = "crossTable";
	
	static final private int kCellLeftRightBorder = 10;
	static final private int kCellTopBottomBorder = 6;
	
	static final private Color kSelectedBackground = Color.yellow;
	
	private Value[] rowLabel;
	private Value[] colLabel;
	private Font rowFont, colFont;
	private Color rowColor[], colColor[];
	
	private boolean[][] isSelected = null;
	
	private int colLabelWidth, rowLabelWidth, cellWidth, tableWidth;
	private int rowAscent, colAscent, cellHeight, tableHeight;
	
	private boolean initialised = false;
	
	public CrossTableView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
	}
	
	public void setRowColLabels(Value[] rowLabel, Value[] colLabel, Font rowFont, Font colFont, Color[] rowColor,
																																														Color[] colColor) {
		this.rowLabel = rowLabel;
		this.colLabel = colLabel;
		this.rowFont = rowFont;
		this.colFont = colFont;
		this.rowColor = rowColor;
		this.colColor = colColor;
		isSelected = null;
		initialised = false;
	}
	
	public void setSelection(boolean[][] isSelected) {
		this.isSelected = isSelected;
	}
	
	protected void doInitialisation(Graphics g) {
		g.setFont(rowFont);
		rowAscent = g.getFontMetrics().getAscent();
		rowLabelWidth = 0;
		for (int i=0 ; i<rowLabel.length ; i++)
			rowLabelWidth = Math.max(rowLabelWidth, rowLabel[i].stringWidth(g));
		
		g.setFont(colFont);
		colAscent = g.getFontMetrics().getAscent();
		colLabelWidth = 0;
//		for (int i=0 ; i<colLabel.length ; i++)
//			colLabelWidth = Math.max(colLabelWidth, colLabel[i].stringWidth(g));		//	ignore '10' for cards
		colLabelWidth = Math.max(colLabelWidth, colLabel[0].stringWidth(g));
		
		cellHeight = rowAscent + 2 * kCellTopBottomBorder;
		tableHeight = rowLabel.length * cellHeight + 1 + colAscent + kCellTopBottomBorder;
		
		cellWidth = colLabelWidth + 2 * kCellLeftRightBorder;
		tableWidth = rowLabelWidth + kCellLeftRightBorder + colLabel.length * cellWidth + 1;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private Point getCellTopLeft(int row, int col, Point p) {
		if (p ==  null)
			p = new Point(0, 0);
		p.x = rowLabelWidth + kCellLeftRightBorder + col * cellWidth;
		p.y = colAscent + kCellTopBottomBorder + row * cellHeight;
		return p;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		Point p = getCellTopLeft(0, 0, null);
		g.setColor(Color.white);
		g.fillRect(p.x, p.y, cellWidth * colLabel.length, cellHeight * rowLabel.length);
		
		if (isSelected != null) {
			g.setColor(kSelectedBackground);
			for (int i=0 ; i<rowLabel.length ; i++)
				for (int j=0 ; j<colLabel.length ; j++)
					if (isSelected[i][j]) {
						p = getCellTopLeft(i, j, p);
						g.fillRect(p.x, p.y, cellWidth, cellHeight);
					}
		}
		
		g.setColor(getForeground());
		for (int i=0 ; i<=rowLabel.length ; i++){
			p = getCellTopLeft(i, 0, p);
			g.drawLine(p.x, p.y, p.x + cellWidth * colLabel.length, p.y);
		}
		
		for (int j=0 ; j<=colLabel.length ; j++){
			p = getCellTopLeft(0, j, p);
			g.drawLine(p.x, p.y, p.x, p.y + cellHeight * rowLabel.length);
		}
		
		for (int i=0 ; i<rowLabel.length ; i++) {
			p = getCellTopLeft(i, 0, p);
			g.setFont(rowFont);
			g.setColor(rowColor[i]);
			rowLabel[i].drawLeft(g, p.x - kCellLeftRightBorder, p.y + rowAscent + kCellTopBottomBorder);
		}
		
		for (int j=0 ; j<colLabel.length ; j++) {
			p = getCellTopLeft(0, j, p);
			g.setFont(colFont);
			g.setColor(colColor[j]);
			colLabel[j].drawCentred(g, p.x + cellWidth / 2, p.y - kCellTopBottomBorder);
		}
		
//		for (int i=0 ; i<rowLabel.length ; i++)
//			for (int j=0 ; j<colLabel.length ; j++) {
//				tempValue.setValue(getCellValue(i, j));
//				p = getCellTopLeft(i, j, p);
//				tempValue.drawCentred(g, p.x + cellWidth / 2, p.y + ascent + kCellTopBottomBorder);
//			}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(tableWidth, tableHeight);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}