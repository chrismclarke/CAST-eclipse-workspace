package exerciseCateg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import contin.*;
import exerciseCategProg.*;


public class BuildContinTableView extends CoreTwoWayTableView implements StatusInterface {
//	static final public String CONTIN_TABLE = "continTable";
	
	static final public int NO_SELECTION = -1;
	
	static final private Insets kCellInsets = new Insets(6, 20, 6, 20);
	static final private Insets kLeftLabelInsets = new Insets(6, 1, 6, 10);
	static final private Insets kTopLabelInsets = new Insets(1, 5, 3, 5);
	static final private Insets kRightMarginInsets = new Insets(6, 10, 6, 4);
	static final private Insets kBottomMarginInsets = new Insets(3, 20, 1, 20);
	static final private Insets kTotalInsets = new Insets(3, 5, 1, 4);
	
	@SuppressWarnings("unused")
	private CatValueScroll2List theList;
	
	private int[][] counts;
	private int targetTotal;
	
	protected int selectedX = NO_SELECTION;
	protected int selectedY = NO_SELECTION;
	
	private int[] xHistory;
	private int[] yHistory;
	
	private boolean doingDrag = false;
	
	public BuildContinTableView(DataSet theData, XApplet applet, String yKey,
																								String xKey, CatValueScroll2List theList) {
		super(theData, applet, yKey, xKey);
		this.theList = theList;
	}
	
	public String getStatus() {
		String s = "";
		int nx = counts.length;
		int ny = counts[0].length;
		for (int i=0 ; i<nx ; i++)
			for (int j=0 ; j<ny ; j++)
				s += counts[i][j] + " ";
		s += "*";
		
		for (int i=0 ; i<xHistory.length ; i++)
			s += xHistory[i] + " ";
		s += "*";
		
		for (int i=0 ; i<yHistory.length ; i++)
			s += yHistory[i] + " ";
		
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString, "*");
		
		StringTokenizer st2 = new StringTokenizer(st.nextToken());
		for (int i=0 ; i<counts.length ; i++)
			for (int j=0 ; j<counts[i].length ; j++)
				counts[i][j] = Integer.parseInt(st2.nextToken());
		
		st2 = new StringTokenizer(st.nextToken());
		for (int i=0 ; i<xHistory.length ; i++)
			xHistory[i] = Integer.parseInt(st2.nextToken());
		
		st2 = new StringTokenizer(st.nextToken());
		for (int i=0 ; i<yHistory.length ; i++)
			yHistory[i] = Integer.parseInt(st2.nextToken());
		
		repaint();
	}
	
	public void resetCounts() {
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int nx = xVar.noOfCategories();
		int ny = yVar.noOfCategories();
		targetTotal = xVar.noOfValues();
		
		if (counts == null || counts.length != nx || counts[0].length != ny)
			counts = new int[nx][ny];
		else
			for (int i=0 ; i<nx ; i++)
				for (int j=0 ; j<ny ; j++)
					counts[i][j] = 0;
		
		if (xHistory == null || xHistory.length != targetTotal)
			xHistory = new int[targetTotal];
			
		if (yHistory == null || yHistory.length != targetTotal)
			yHistory = new int[targetTotal];
		
		repaint();
	}
	
	public void backOneValue() {
		int currentTotal = overallTotal(counts);
		if (currentTotal > 0) {
			currentTotal --;
			counts[xHistory[currentTotal]][yHistory[currentTotal]] --;
			repaint();
		}
	}
	
	public int getCurrentTotal() {
		return overallTotal(counts);
	}
	
	public void completeTable() {
		for (int i=0 ; i<counts.length ; i++)
			for (int j=0 ; j<counts[i].length ; j++)
				counts[i][j] = 0;
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable yVar = (CatVariable)getVariable(yKey);
		for (int i=0 ; i<xVar.noOfValues() ; i++)
			addOne(xVar.getItemCategory(i), yVar.getItemCategory(i));
		repaint();
	}
	
	public int checkCounts() {
		if (getCurrentTotal() != targetTotal)
			return ExerciseConstants.ANS_INCOMPLETE;
		else {
			CatVariable xVar = (CatVariable)getVariable(xKey);
			CatVariable yVar = (CatVariable)getVariable(yKey);
			int[][] correctCounts = xVar.getCounts(yVar);
			for (int i=0 ; i<counts.length ; i++)
				for (int j=0 ; j<counts[i].length ; j++)
					if (counts[i][j] != correctCounts[i][j])
						return ExerciseConstants.ANS_WRONG;
			
			return ExerciseConstants.ANS_CORRECT;
		}
	}
	
	public void setSelection(int selectedX, int selectedY) {
		this.selectedX = selectedX;
		this.selectedY = selectedY;
		repaint();
	}

//---------------------------------------------------------------------------------
	
	
	protected Dimension getMaxTopLabelSize(Graphics g) {
		return getMaxCatNameSize(g, xKey, kTopLabelInsets);
	}
	
	protected Dimension getMaxLeftLabelSize(Graphics g) {
		return getMaxCatNameSize(g, yKey, kLeftLabelInsets);
	}
	
	protected Dimension getMaxCellSize(Graphics g) {
		return sizeOfInteger(g, targetTotal, kCellInsets);
	}
	
	protected Dimension getMaxRightMarginSize(Graphics g) {
		return sizeOfInteger(g, targetTotal, kRightMarginInsets);
	}
	
	protected Dimension getMaxBottomMarginSize(Graphics g) {
		return sizeOfInteger(g, targetTotal, kBottomMarginInsets);
	}
	
	protected Dimension getTotalSize(Graphics g) {
		return sizeOfInteger(g, targetTotal, kTotalInsets);
	}
	
	
	public int[][] getCounts() {
		return counts;
	}
	
	
	protected void drawTopLabel(Graphics g, int index, Rectangle cell) {
		drawCatName(g, xKey, cell, kTopLabelInsets, index, CENTER);
	}
	
	protected void drawLeftLabel(Graphics g, int index, Rectangle cell) {
		drawCatName(g, yKey, cell, kLeftLabelInsets, index, LEFT);
	}
	
	
	protected void drawCell(Graphics g, int i, int j, Rectangle cell, int[][] counts) {
		boolean highlight = (selectedX >= 0 || selectedY >= 0) && (selectedX == i || selectedX < 0)
																														&& (selectedY == j || selectedY < 0);
		if (highlight)
			g.setFont(boldFont);
		drawOneCount(g, counts[i][j], cell, kCellInsets);
		if (highlight && doingDrag) {
			g.setColor(Color.red);
			drawPlusOne(g, cell, kCellInsets);
			g.setColor(getForeground());
			g.setFont(standardFont);
		}
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
		if (selectedX >= 0 && selectedY >= 0) {
			g.setColor(Color.yellow);
			Rectangle r = getCellRectangle(selectedX, selectedY);
			
			g.fillRect(r.x, r.y, r.width, r.height);
		}
	}


//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		int total = overallTotal(counts);
		
		return total < targetTotal;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		x -= (leftLabelWidth + 1);
		y -= (topLabelHeight + 1);
		
		if (x < 0 || y < 0)
			return null;
		
		int xCat = x / cellWidth;
		int yCat = y / cellHeight;
		
		if (xCat >= nXCats || yCat >= nYCats)
			return null;
		
		return new ContinCatInfo(xCat, yCat);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		ContinCatInfo catInfo = (ContinCatInfo)startInfo;
		int newX = (catInfo == null) ? NO_SELECTION : catInfo.xIndex;
		int newY = (catInfo == null) ? NO_SELECTION : catInfo.yIndex;
		if (newX != selectedX || newY != selectedY) {
			selectedX = newX;
			selectedY = newY;
			doingDrag = true;
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	private void addOne(int xCat, int yCat) {
		counts[xCat][yCat] ++;
		int lastIndex = overallTotal(counts) - 1;
		xHistory[lastIndex] = xCat;
		yHistory[lastIndex] = yCat;	
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		ContinCatInfo catInfo = (ContinCatInfo)endPos;
		if (catInfo != null) {
			addOne(catInfo.xIndex, catInfo.yIndex);
			((BuildContinTableApplet)getApplet()).moveToNextValue();
		}
		selectedX = NO_SELECTION;
		selectedY = NO_SELECTION;
		doingDrag = false;
		repaint();
	}
}
