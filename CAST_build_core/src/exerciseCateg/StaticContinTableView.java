package exerciseCateg;

import java.awt.*;

import dataView.*;


public class StaticContinTableView extends CoreTwoWayTableView {
//	static final public String STATIC_CONTIN_TABLE = "staticContinTable";
	
	static final private Insets kCellInsets = new Insets(6, 20, 6, 20);
	static final private Insets kLeftLabelInsets = new Insets(6, 1, 6, 10);
	static final private Insets kTopLabelInsets = new Insets(1, 5, 3, 5);
	static final private Insets kRightMarginInsets = new Insets(6, 10, 6, 4);
	static final private Insets kBottomMarginInsets = new Insets(3, 20, 1, 20);
	static final private Insets kTotalInsets = new Insets(3, 5, 1, 4);
	
	static final private Color kCellHiliteColor = new Color(0xFF99FF);
	static final private Color kLightGrey = new Color(0xCCCCCC);
	
	static final public int NO_SELECTION = -1;
	
	private int numeratorX = -1, numeratorY = -1, denominatorX = -1, denominatorY = -1;
	
	private int selectedX = -1, selectedY = -1;
	private boolean hiliteRowCol = false;
	
	public StaticContinTableView(DataSet theData, XApplet applet, String yKey, String xKey) {
		super(theData, applet, yKey, xKey);
	}
	
	public void setPropnIndices(int numeratorX, int numeratorY, int denominatorX, int denominatorY) {
		this.numeratorX = numeratorX;
		this.numeratorY = numeratorY;
		this.denominatorX = denominatorX;
		this.denominatorY = denominatorY;
	}
	
	public void clearPropnIndices() {
		numeratorX = numeratorY = denominatorX = denominatorY = NO_SELECTION;
	}
	
	public void setSelectedRowCol(int selectedX, int selectedY) {
		this.selectedX = selectedX;
		this.selectedY = selectedY;
		hiliteRowCol = false;
	}
	
	public void showExpectedRowCol() {
		hiliteRowCol = true;
	}

//---------------------------------------------------------------------------------
	
	
	protected Dimension getMaxTopLabelSize(Graphics g) {
		return getMaxCatNameSize(g, xKey, kTopLabelInsets);
	}
	
	protected Dimension getMaxLeftLabelSize(Graphics g) {
		return getMaxCatNameSize(g, yKey, kLeftLabelInsets);
	}
	
	protected Dimension getMaxCellSize(Graphics g) {
		int[][] counts = getCounts();
		int max = 0;
		for (int i=0 ; i<counts.length ; i++)
			for (int j=0 ; j<counts[i].length ; j++)
				max = Math.max(max, counts[i][j]);
		return sizeOfInteger(g, max, kCellInsets);
	}
	
	protected Dimension getMaxRightMarginSize(Graphics g) {
		int[][] counts = getCounts();
		int maxMargin = 0;
		for (int j=0 ; j<counts[0].length ; j++)
			maxMargin = Math.max(maxMargin, yMarginTotal(counts, j));
		
		return sizeOfInteger(g, maxMargin, kRightMarginInsets);
	}
	
	protected Dimension getMaxBottomMarginSize(Graphics g) {
		int[][] counts = getCounts();
		int maxMargin = 0;
		for (int i=0 ; i<counts.length ; i++)
			maxMargin = Math.max(maxMargin, xMarginTotal(counts, i));
		
		return sizeOfInteger(g, maxMargin, kBottomMarginInsets);
	}
	
	protected Dimension getTotalSize(Graphics g) {
		int[][] counts = getCounts();
		int total = overallTotal(counts);
		return sizeOfInteger(g, total, kTotalInsets);
	}
	
	
	public int[][] getCounts() {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		return xVar.getCounts(yVar);
	}
	
	
	protected void drawTopLabel(Graphics g, int index, Rectangle cell) {
		drawCatName(g, xKey, cell, kTopLabelInsets, index, CENTER);
	}
	
	protected void drawLeftLabel(Graphics g, int index, Rectangle cell) {
		drawCatName(g, yKey, cell, kLeftLabelInsets, index, LEFT);
	}
	
	
	protected void drawCell(Graphics g, int i, int j, Rectangle cell, int[][] counts) {
		if (selectedX >= 0 && selectedY >= 0) {
			Color c = g.getColor();
			if (selectedX == i && selectedY == j) {
				g.setColor(Color.yellow);
				g.fillRect(cell.x, cell.y, cell.width, cell.height);
			}
			g.setColor(kLightGrey);
			drawOneCount(g, counts[i][j], cell, kCellInsets);
			g.setColor(c);
		}
		else
			drawOneCount(g, counts[i][j], cell, kCellInsets);
	}
	
	
	protected void drawRightMargin(Graphics g, int i, Rectangle cell, int[][] counts) {
		drawOneCount(g, yMarginTotal(counts, i), cell, kRightMarginInsets);
	}
	
	protected void drawBottomMargin(Graphics g, int i, Rectangle cell, int[][] counts) {
		drawOneCount(g, xMarginTotal(counts, i), cell, kBottomMarginInsets);
	}
	
	
	protected void drawTotal(Graphics g, Rectangle cell, int[][] counts) {
		drawOneCount(g, overallTotal(counts), cell, kTotalInsets);
	}
	
	protected void drawCellHighlight(Graphics g) {
		if (numeratorX >= 0 || numeratorY >= 0) {
			g.setColor(kCellHiliteColor);
			
			Rectangle r = getCellRectangle(numeratorX, numeratorY);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			r = getCellRectangle(denominatorX, denominatorY);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
		else if (hiliteRowCol) {
			g.setColor(kCellHiliteColor);
			
			Rectangle r = getCellRectangle(selectedX, -1);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			r = getCellRectangle(-1, selectedY);
			g.fillRect(r.x, r.y, r.width, r.height);
			
			r = getCellRectangle(-1, -1);
			g.fillRect(r.x, r.y, r.width, r.height);
		}
	}
	
}
