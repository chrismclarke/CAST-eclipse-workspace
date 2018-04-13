package normal;

import java.awt.*;

import dataView.*;
import distn.NormalDistnVariable;


class ValuePosInfo implements PositionInfo {
	ValuePosInfo(double value) {
		this.value = value;
	}
	
	public boolean equals(PositionInfo otherPos) {
		if (otherPos == null || !(otherPos instanceof ValuePosInfo))
			return false;
		ValuePosInfo other = (ValuePosInfo)otherPos;
		return value == other.value;
	}
	
	public double value;
}


public class NormalTableView extends DataView {
//	static final public String NORMAL_TABLE = "normalTable";
	
	static final public int kRows = 72;
	static final public int kCols = 10;
	
	static final public int kTopBottomGap = 4;
	static final public int kLeftRightGap = 6;
	static final public int kVertGap = 6;
	static final public int kHorizGap = 12;
	
	static final private double step[] = {0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
	
	private boolean initialised = false;
	
	private String yKey;
	private HorizHeading horizHeading;
	private VerticalHeading vertHeading;
	
	public int ascent, valWidth, idealHeight, idealWidth;
	public int leftOrigin, topOrigin;
	
	public NormalTableView(DataSet theData, XApplet applet, String yKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.yKey = yKey;
	}
	
	public void setHeadings(HorizHeading horizHeading, VerticalHeading vertHeading) {
												//		Table needs to know about headings so that heading
												//		values can be highlighted when selection is changed
		this.horizHeading = horizHeading;
		this.vertHeading = vertHeading;
	}
	
	public void setOrigin(int newLeftOrigin, int newTopOrigin, HorizHeading hh, VerticalHeading vh) {
		boolean horizChange = (newLeftOrigin != leftOrigin);
		boolean vertChange = (newTopOrigin != topOrigin);
		if (horizChange || vertChange) {
			leftOrigin = newLeftOrigin;
			topOrigin = newTopOrigin;
			repaint();
			if (horizChange)
				hh.repaint();
			if (vertChange)
				vh.repaint();
		}
	}
	
	public boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		valWidth = fm.stringWidth(".0000");
		
		idealHeight = kRows * ascent + (kRows - 1) * kVertGap + 2 * kTopBottomGap;
		idealWidth = kCols * valWidth + (kCols - 1) * kHorizGap + 2 * kLeftRightGap;
		
		topOrigin = - (kTopBottomGap + (kRows / 2 - 3) * (ascent + kVertGap));
		
		initialised = true;
		return true;
	}
	
	private void drawValue(Graphics g, double z, int colIndex, int baseline) {
		double prob = NormalDistnVariable.stdCumProb(z);
		int decimals = (int)Math.round(prob * 10000.0);
		String digits = String.valueOf(decimals);
		if (decimals >= 10000)
			digits = "1.000";
		else if (decimals >= 1000)
			digits = "." + digits;
		else if (decimals >= 100)
			digits = ".0" + digits;
		else if (decimals >= 10)
			digits = ".00" + digits;
		else if (decimals >= 1)
			digits = ".000" + digits;
		else
			digits = ".0000";
		g.drawString(digits, leftOrigin + kLeftRightGap + colIndex * (valWidth + kHorizGap),
																												baseline);
	}
	
	public Point selectedRowAndCol() {
		DistnVariable y = (DistnVariable)getVariable(yKey);
		double maxSelected = y.getMaxSelection();
		if (maxSelected > -3.6 && maxSelected < 3.6) {
			int hundredths = (int)Math.round((3.6 + maxSelected) * 100.0);
			int selectedRow = hundredths / 10;
			int selectedCol = hundredths % 10;
			if (maxSelected < 0.0) {
				selectedCol = 10 - selectedCol;
				if (selectedCol == 10) {
					selectedCol = 0;
					selectedRow --;
				}
			}
			if (selectedRow >= 0 && selectedRow < kRows && selectedCol >= 0 && selectedCol < kCols)
				return new Point(selectedCol, selectedRow);
		}
		return null;
	}
	
	public Point getVisibleRows() {
		int minVisibleRow = (-topOrigin - kTopBottomGap) / (kVertGap + ascent);
		int maxVisibleRow = (getSize().height - topOrigin - kTopBottomGap) / (kVertGap + ascent) + 1;
		if (maxVisibleRow >= kRows)
			maxVisibleRow = kRows - 1;
		return new Point(minVisibleRow, maxVisibleRow);
	}
	
	public Point getVisibleCols() {
		int minVisibleCol = (-leftOrigin - kLeftRightGap) / (valWidth + kHorizGap);
		int maxVisibleCol = (getSize().width - leftOrigin - kLeftRightGap) / (valWidth + kHorizGap) + 1;
		if (maxVisibleCol >= kCols)
			maxVisibleCol = kCols - 1;
		
		return new Point(minVisibleCol, maxVisibleCol);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		Point visVertRange = getVisibleRows();
		int minRowVisible = visVertRange.x;
		int maxRowVisible = visVertRange.y;
		
		Point visHorizRange = getVisibleCols();
		int minColVisible = visHorizRange.x;
		int maxColVisible = visHorizRange.y;
		
		g.setColor(Color.blue);
		int baseline = topOrigin + kTopBottomGap + ascent;
		double zBase = -3.6;
		for (int i=0 ; i<kRows/2 ; i++) {
			if (i >= minRowVisible && i <= maxRowVisible) {
				for (int j=minColVisible ; j<=maxColVisible ; j++)
					drawValue(g, zBase + step[10 - j], j, baseline);
			}
			baseline += kVertGap + ascent;
			zBase += 0.1;
		}
		
		g.setColor(Color.black);
		for (int i=kRows/2 ; i<kRows ; i++) {
			if (i >= minRowVisible && i <= maxRowVisible) {
				for (int j=minColVisible ; j<=maxColVisible ; j++)
					drawValue(g, zBase + step[j], j, baseline);
			}
			baseline += kVertGap + ascent;
			zBase += 0.1;
		}
		
		Point sel = selectedRowAndCol();
		if (sel != null) {
			g.setColor(Color.red);
			int left = leftOrigin + kLeftRightGap + sel.x * (valWidth + kHorizGap);
			int top = topOrigin + kTopBottomGap + sel.y * (ascent + kVertGap);
			g.drawRect(left - 5, top - 3, valWidth + 9, ascent + 5);
			g.drawRect(left - 4, top - 2, valWidth + 7, ascent + 3);
		}
	}
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		return new Dimension(idealWidth, idealHeight);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeSelection(Graphics g) {
		super.doChangeSelection(g);
		horizHeading.repaint();
		vertHeading.repaint();
	}
	
	public double valueFromRowCol(int row, int col) {
		double value = -3.6 + row * 0.1;
		if (row < kRows / 2)
			value += (10 - col) * 0.01;
		else
			value += col * 0.01;
		return value;
	}
	
	public void selectMax(ValuePosInfo newValue) {
		getData().setSelection(yKey, Double.NEGATIVE_INFINITY, newValue.value);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		int row = (y - kTopBottomGap - topOrigin) / (ascent + kVertGap);
		if (row >= kRows)
			row = kRows - 1;
		int col = (x - kLeftRightGap - leftOrigin) / (valWidth + kHorizGap);
		if (col >= kCols)
			col = kCols - 1;
		double value = valueFromRowCol(row, col);
		
		return new ValuePosInfo(value);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		selectMax((ValuePosInfo)startInfo);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			selectMax((ValuePosInfo)toPos);
	}
	
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (endPos != null)
			selectMax((ValuePosInfo)endPos);
	}
}
	
