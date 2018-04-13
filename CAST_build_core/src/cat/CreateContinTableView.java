package cat;

import java.awt.*;

import dataView.*;


public class CreateContinTableView extends CoreCreateTableView {
//	static public final String CONTIN_TABLE_TALLIES = "continTableTallies";
	
	static private final int kMinColCatNameSpacing = 16;
	static private final int kTallyCountGap = 5;
	static private final int kCellTopBottomBorder = 5;
	static private final int kHeadingVertSpacing = 3;
	
	private String catColKey;
	private int selectedColCat = -1;
	private int noOfColCats;
	
	private int maxColCatWidth, colVarNameWidth, maxCountWidth, maxTallyWidth;
	private int cellWidth, cellHeight, tableLeft, tableTop;
	
	private int maxCount;
	
	private int counts[][];
	
	private Font bigFont;
	private int bigAscent;
	
	public CreateContinTableView(DataSet theData, XApplet applet, String catRowKey, String catColKey) {
		super(theData, applet, catRowKey);
		this.catColKey = catColKey;
		
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		CatVariable catColVar = (CatVariable)getVariable(catColKey);
		noOfColCats = catColVar.noOfCategories();
		
		int maxCounts[][] = catRowVar.getCounts(catColVar);
		maxCount = 0;
		for (int i=0 ; i<noOfRowCats ; i++)
			for (int j=0 ; j<noOfColCats ; j++)
				maxCount = Math.max(maxCount, maxCounts[i][j]);
		
		counts = new int[noOfRowCats][noOfColCats];
		
		bigFont = applet.getBigBoldFont();
	}
	
	public void clearCounts() {
		for (int i=0 ; i<noOfRowCats ; i++)
			for (int j=0 ; j<noOfColCats ; j++)
				counts[i][j] = 0;
		selectedRowCat = -1;
		selectedColCat = -1;
		repaint();
	}
	
	public void completeCounts() {
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		CatVariable catColVar = (CatVariable)getVariable(catColKey);
		counts = catRowVar.getCounts(catColVar);
		selectedRowCat = -1;
		selectedColCat = -1;
		repaint();
	}
	
	public void addCatValue(int i) {
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		int catRowIndex = catRowVar.getItemCategory(i);
		CatVariable catColVar = (CatVariable)getVariable(catColKey);
		int catColIndex = catColVar.getItemCategory(i);
		counts[catRowIndex][catColIndex] ++;
		selectedRowCat = catRowIndex;
		selectedColCat = catColIndex;
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (!super.initialise(g))
			return false;
		
		CatVariable catColVar = (CatVariable)getVariable(catColKey);
		
		Font standardFont = g.getFont();
		g.setFont(boldFont);
		FontMetrics fm = g.getFontMetrics();
		maxColCatWidth = 0;
		for (int i=0 ; i<noOfColCats ; i++)
			maxColCatWidth = Math.max(maxColCatWidth, catColVar.getLabel(i).stringWidth(g));
		
		colVarNameWidth = fm.stringWidth(catColVar.name);
		
		g.setFont(bigFont);
		fm = g.getFontMetrics();
		bigAscent = fm.getAscent();
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		maxCountWidth = fm.stringWidth(String.valueOf(maxCount));
		maxTallyWidth = maxCount * kTallyWidth;
		
		cellWidth = Math.max(maxColCatWidth + kMinColCatNameSpacing,
																											maxTallyWidth + 2 * kTallyLeftBorder);
		cellHeight = tallyHeight() + kTallyCountGap + 2 * kCellTopBottomBorder + bigAscent;
		
		tableLeft = 2 * kRowCatNameLeftRightBorder + Math.max(maxRowCatWidth, rowVarNameWidth);
		tableTop = 2 * (boldAscent + boldDescent) + 3 * kHeadingVertSpacing;
		
		return true;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
//		Font standardFont = g.getFont();
		g.setFont(boldFont);
		
		drawRowHeading(g);
		drawColHeading(g);
		
		g.setFont(bigFont);
		NumValue tempCount = new NumValue(0.0, 0);
		
		for (int row=0 ; row<noOfRowCats ; row++)
			for (int col=0 ; col<noOfColCats ; col++)
				drawCell(g, row, col, tempCount);
	}
	
	private void drawRowHeading(Graphics g) {
		CatVariable catRowVar = (CatVariable)getVariable(catRowKey);
		int varNameBaseline = tableTop - kHeadingVertSpacing - boldDescent;
		g.drawString(catRowVar.name, 0, varNameBaseline);
		
		int baseline = tableTop + (cellHeight + boldAscent - boldDescent) / 2;
		int horizStart = tableLeft - kRowCatNameLeftRightBorder - rowVarNameWidth;
		for (int i=0 ; i<noOfRowCats ; i++) {
			g.setColor(selectedRowCat == i ? Color.red : Color.blue);
			catRowVar.getLabel(i).drawRight(g, horizStart, baseline);
			baseline += cellHeight;
		}
		g.setColor(getForeground());
	}
	
	private void drawColHeading(Graphics g) {
		CatVariable catColVar = (CatVariable)getVariable(catColKey);
		int varNameBaseline = kHeadingVertSpacing + boldAscent;
		
		g.drawString(catColVar.name, tableLeft + (noOfColCats * cellWidth - colVarNameWidth) / 2,
																																		varNameBaseline);
		
		int baseline = varNameBaseline + kHeadingVertSpacing + boldAscent + boldDescent;
		int horizCentre = tableLeft + cellWidth / 2;
		for (int i=0 ; i<noOfColCats ; i++) {
			g.setColor(selectedColCat == i ? Color.red : Color.blue);
			catColVar.getLabel(i).drawCentred(g, horizCentre, baseline);
			horizCentre += cellWidth;
		}
		g.setColor(getForeground());
		
	}
	
	private void drawCell(Graphics g, int row, int col, NumValue tempCount) {
		int top = tableTop + row * cellHeight;
		int left = tableLeft + col * cellWidth;
		g.setColor(Color.white);
		g.fillRect(left, top, cellWidth, cellHeight);
		g.setColor(Color.lightGray);
		g.drawRect(left, top, cellWidth, cellHeight);
		
		boolean selected = (row == selectedRowCat) && (col == selectedColCat);
		drawTallies(g, counts[row][col], top + kCellTopBottomBorder,
																left + (cellWidth - maxTallyWidth) / 2, selected);
		
		int cellCount = counts[row][col];
		g.setColor(selected ? Color.red : (cellCount == 0) ? Color.lightGray : getForeground());
		int countRight = left + (cellWidth + maxCountWidth) / 2;
		
		tempCount.setValue(cellCount);
		tempCount.drawLeft(g, countRight, top + kCellTopBottomBorder + bigAscent + tallyHeight() + kTallyCountGap);
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int width = tableLeft + noOfColCats * cellWidth + 1;
		int height = tableTop + noOfRowCats * cellHeight + 1;
		
		return new Dimension(width, height);
	}
	
}