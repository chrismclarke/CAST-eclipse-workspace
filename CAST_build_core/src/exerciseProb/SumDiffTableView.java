package exerciseProb;

import java.awt.*;

import dataView.*;


public class SumDiffTableView extends DataView {
//	static final public String SUM_DIFF_TABLE = "sumDiffTable";
	
	static final public int SUM = 0;
	static final public int DIFF = 1;		//	row - col
	static final public int ABS_DIFF = 2;
	
	static final public int GREATER = 0;
	static final public int GREATER_EQUAL = 1;
	static final public int LESS = 2;
	static final public int LESS_EQUAL = 3;
	static final public int EQUAL = 4;
	
	static final public boolean WITH_REPLACEMENT = true;
	static final public boolean WITHOUT_REPLACEMENT = false;
	
	static final private int kCellLeftRightBorder = 10;
	static final private int kCellTopBottomBorder = 6;
	
	static final private Color kIllegalBackground = new Color(0xBBBBBB);
	static final private Color kIllegalTextColor = new Color(0x555555);
	static final private Color kSelectedBackground = Color.yellow;
	
	private NumValue[] rowColValues = null;
	
	private NumValue tempValue = new NumValue(0,0);
	
	private boolean isWithReplacement = WITH_REPLACEMENT;
	private int sumDiffType = SUM;
	private boolean showPopSamp = false;
	
	private double cutoff;
	private int direction;
	
	private int maxValueWidth, cellWidth, tableWidth;
	private int ascent, cellHeight, tableHeight;
	
	private boolean initialised = false;
	
	public SumDiffTableView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
	}
	
	public void setRowColValues(NumValue[] rowColValues) {
		this.rowColValues = rowColValues;
		initialised = false;
	}
	
	public void setWithReplacement(boolean isWithReplacement) {
		this.isWithReplacement = isWithReplacement;
	}
	
	public void setSumDiffType(int sumDiffType) {
		this.sumDiffType = sumDiffType;
	}
	
	public void setSelection(int direction, double cutoff) {
		this.direction = direction;
		this.cutoff = cutoff;
	}
	
	public void setShowPopSamp(boolean showPopSamp) {
		this.showPopSamp = showPopSamp;
	}
	
	public int countSelected() {
		int count = 0;
		for (int i=0 ; i<rowColValues.length ; i++)
			for (int j=0 ; j<rowColValues.length ; j++)
				if (isSelected(i, j))
					count ++;
		return count;
	}
	
	public int countPopn() {
		int count = rowColValues.length * rowColValues.length;
		if (!isWithReplacement)
			count -= rowColValues.length;
		return count;
	}
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
		cellHeight = ascent + 2 * kCellTopBottomBorder;
		tableHeight = (rowColValues.length + 1) * cellHeight + 1;
		
		int maxDecimals = 0;
		double maxValue = 0.0;
		for (int i=0 ; i<rowColValues.length ; i++) {
			maxDecimals = Math.max(maxDecimals, rowColValues[i].decimals);
			maxValue = Math.max(maxValue, rowColValues[i].toDouble());
		}
		if (sumDiffType == SUM)
			maxValue *= 2;
		else
			maxValue = -maxValue;
		tempValue.setValue(maxValue);
		tempValue.decimals = maxDecimals;
		
		maxValueWidth = tempValue.stringWidth(g);
		cellWidth = maxValueWidth + 2 * kCellLeftRightBorder;
		tableWidth = (rowColValues.length + 1) * cellWidth + 1;
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
			p = new Point(0,0);
		p.x = (row + 1) * cellWidth;
		p.y = (col + 1) * cellHeight;
		return p;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		Point p = getCellTopLeft(0, 0, null);
		g.setColor(Color.white);
		g.fillRect(p.x, p.y, cellWidth * rowColValues.length, cellHeight * rowColValues.length);
		
		if (showPopSamp) {
			if (!isWithReplacement) {
				g.setColor(kIllegalBackground);
				for (int i=0 ; i<rowColValues.length ; i++) {
					p = getCellTopLeft(i, i, p);
					g.fillRect(p.x, p.y, cellWidth, cellHeight);
				}
			}
			
			g.setColor(kSelectedBackground);
			for (int i=0 ; i<rowColValues.length ; i++)
				for (int j=0 ; j<rowColValues.length ; j++)
					if (isSelected(i, j)) {
						p = getCellTopLeft(i, j, p);
						g.fillRect(p.x, p.y, cellWidth, cellHeight);
					}
		}
		
		g.setColor(getForeground());
		for (int i=0 ; i<=rowColValues.length ; i++){
			p = getCellTopLeft(i, 0, p);
			g.drawLine(p.x, p.y, p.x, p.y + cellHeight * rowColValues.length);
			
			p = getCellTopLeft(0, i, p);
			g.drawLine(p.x, p.y, p.x + cellWidth * rowColValues.length, p.y);
		}
		
		for (int i=0 ; i<rowColValues.length ; i++) {
			p = getCellTopLeft(0, i, p);
			rowColValues[i].drawLeft(g, p.x - kCellLeftRightBorder, p.y + ascent + kCellTopBottomBorder);
			
			p = getCellTopLeft(i, 0, p);
			rowColValues[i].drawCentred(g, p.x + cellWidth / 2, p.y - kCellTopBottomBorder);
		}
		
		for (int i=0 ; i<rowColValues.length ; i++)
			for (int j=0 ; j<rowColValues.length ; j++) {
				tempValue.setValue(getCellValue(i, j));
				p = getCellTopLeft(i, j, p);
				if (showPopSamp && !isWithReplacement && i == j)
					g.setColor(kIllegalTextColor);
				else
					g.setColor(getForeground());
				tempValue.drawCentred(g, p.x + cellWidth / 2, p.y + ascent + kCellTopBottomBorder);
			}
	}
	
	private boolean isSelected(int row, int col) {
		if (!isWithReplacement && (row == col))
			return false;
		
		double x = getCellValue(row, col);
		switch (direction) {
			case GREATER:
				return x > cutoff;
			case GREATER_EQUAL:
				return x >= cutoff;
			case LESS:
				return x < cutoff;
			case LESS_EQUAL:
				return x <= cutoff;
			case EQUAL:
			default:
				return x == cutoff;
		}
	}
	
	private double getCellValue(int row, int col) {
		double rowVal = rowColValues[row].toDouble();
		double colVal = rowColValues[col].toDouble();
		switch (sumDiffType) {
			case SUM:
				return rowVal + colVal;
			case DIFF:
				return rowVal - colVal;
			case ABS_DIFF:
			default:
				return Math.abs(rowVal - colVal);
		}
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