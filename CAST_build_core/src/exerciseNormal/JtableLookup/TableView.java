package exerciseNormal.JtableLookup;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;

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


public class TableView extends DataView {
//	static final public String NORMAL_TABLE = "normalTable";
	
	static final public int kRows = 72;
	static final public int kCols = 10;
	
	static final public int kTopBottomGap = 4;
	static final public int kLeftRightGap = 6;
	static final public int kVertGap = 6;
	static final public int kHorizGap = 12;
	
	static final private double step[] = {0.0, 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1};
	
	static final public Color kMinColor = Color.red;
	static final public Color kMaxColor = new Color(0x009900);		//	dark green
//	static final private Color kSelectedColor = new Color(0xFFAAAA);
	static final private Color kSelectedColor = Color.yellow;
	
	private boolean initialised = false;
	private boolean pendingScroll = true;
	
	private String zKey;
	private TablePanel thePanel;
	private TableHorizHeading horizHeading;
	private TableVertHeading vertHeading;
	
	public int ascent, valWidth, idealHeight, idealWidth;
	public int leftOrigin, topOrigin;
	
	public TableView(DataSet theData, XApplet applet, String zKey, TablePanel thePanel) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.zKey = zKey;
		this.thePanel = thePanel;
	}
	
	public void setHeadings(TableHorizHeading horizHeading, TableVertHeading vertHeading) {
												//		Table needs to know about headings so that heading
												//		values can be highlighted when selection is changed
		this.horizHeading = horizHeading;
		this.vertHeading = vertHeading;
	}
	
	public void setOrigin(int newLeftOrigin, int newTopOrigin, TableHorizHeading hh, TableVertHeading vh) {
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
		int valLeft = leftOrigin + kLeftRightGap + colIndex * (valWidth + kHorizGap);
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
		g.drawString(digits, valLeft, baseline);
	}
	
	private Point getRowCol(double z) {
		if (z > -3.6 && z < 3.6) {
			int hundredths = (int)Math.round((3.6 + z) * 100.0);
			int selectedRow = hundredths / 10;
			int selectedCol = hundredths % 10;
			if (z < 0.0) {
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
	
	public Point maxSelectedRowCol() {
		DistnVariable y = (DistnVariable)getVariable(zKey);
		return getRowCol(y.getMaxSelection());
	}
	
	public Point minSelectedRowCol() {
		DistnVariable y = (DistnVariable)getVariable(zKey);
		return getRowCol(y.getMinSelection());
	}
	
	public Rectangle findTableRectangle(Point p) {
		if (p == null)
			return null;
		int left = kLeftRightGap - 6 + p.x * (valWidth + kHorizGap);
		int width = valWidth + 6;
		int top = kTopBottomGap - 4 + p.y * (kVertGap + ascent);
		int height = ascent + 8;
		return new Rectangle(left, top, width, height);
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
	
	private void shadeSelection(Graphics g, Point minSel, Point maxSel) {
		g.setColor(kSelectedColor);
		if (minSel != null && maxSel != null) {
			int top = topOrigin + kTopBottomGap - 3 + minSel.y * (kVertGap + ascent);
			if (minSel.y < maxSel.y) {
				if (minSel.y < kRows / 2) {
					int right = leftOrigin + kLeftRightGap + (minSel.x + 1) * (valWidth + kHorizGap) + 5 - kHorizGap;
					g.fillRect(0, top, right, ascent + 6);
				}
				else {
					int left = leftOrigin + kLeftRightGap - 6 + minSel.x * (valWidth + kHorizGap);
					g.fillRect(left, top, getSize().width - left, ascent + 6);
				}
				
				top += (kVertGap + ascent);
				if (maxSel.y > minSel.y + 1) {	//		shade full rows
					int height = (maxSel.y - minSel.y - 1) * (kVertGap + ascent) - kVertGap + 6;
					g.fillRect(0, top, getSize().width, height);
				}
				
				top += (kVertGap + ascent) * (maxSel.y - minSel.y - 1);
				if (maxSel.y < kRows / 2) {
					int left = leftOrigin + kLeftRightGap - 6 + maxSel.x * (valWidth + kHorizGap);
					g.fillRect(left, top, getSize().width - left, ascent + 6);
				}
				else {
					int right = leftOrigin + kLeftRightGap + (maxSel.x + 1) * (valWidth + kHorizGap) + 5 - kHorizGap;
					g.fillRect(0, top, right, ascent + 6);
				}
			}
			else {
				int lowX = Math.min(minSel.x, maxSel.x);
				int highX = Math.max(minSel.x, maxSel.x);
				int left = leftOrigin + kLeftRightGap - 6 + lowX * (valWidth + kHorizGap);
				int width = (highX - lowX + 1) * (valWidth + kHorizGap) - kHorizGap + 12;
				g.fillRect(left, top, width, ascent + 6);
			}
		}
	
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		if (pendingScroll) {
			pendingScroll = false;
			thePanel.scrollToSelection();
		}
		
		Point visVertRange = getVisibleRows();
		int minRowVisible = visVertRange.x;
		int maxRowVisible = visVertRange.y;
		
		Point visHorizRange = getVisibleCols();
		int minColVisible = visHorizRange.x;
		int maxColVisible = visHorizRange.y;
		
		Point minSel = minSelectedRowCol();
		Point maxSel = maxSelectedRowCol();
		
		shadeSelection(g, minSel, maxSel);
		
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
		
		if (minSel != null) {
			g.setColor(kMinColor);
			int left = leftOrigin + kLeftRightGap + minSel.x * (valWidth + kHorizGap);
			int top = topOrigin + kTopBottomGap + minSel.y * (ascent + kVertGap);
			g.drawRect(left - 5, top - 3, valWidth + 9, ascent + 5);
			g.drawRect(left - 4, top - 2, valWidth + 7, ascent + 3);
		}
		
		if (maxSel != null) {
			g.setColor(kMaxColor);
			int left = leftOrigin + kLeftRightGap + maxSel.x * (valWidth + kHorizGap);
			int top = topOrigin + kTopBottomGap + maxSel.y * (ascent + kVertGap);
			if (minSel == null || minSel.x != maxSel.x || minSel.y != maxSel.y) {
				g.drawRect(left - 5, top - 3, valWidth + 9, ascent + 5);
				g.drawRect(left - 4, top - 2, valWidth + 7, ascent + 3);
			}
		}
	}
	
	public double getRoundingError(double x, double eps) {
		DistnVariable distn = (DistnVariable)getVariable(zKey);
		double low = Math.floor(x / eps) * eps;
		double high = low + eps;
		return distn.getCumulativeProb(high) - distn.getCumulativeProb(low);
	}
	
	public double inverseError(double prob) {
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(zKey);
		double exact = distn.getQuantile(prob);
		double low = exact;
		double cum;
		do {
			low -= 0.01;
			cum = distn.getCumulativeProb(low);
		} while (low >= -3.5 && Math.abs(cum - prob) < 0.0001);
		
		double high = exact;
		do {
			high += 0.01;
			cum = distn.getCumulativeProb(high);
		} while (high <= 3.5 && Math.abs(cum - prob) < 0.0001);
		
		return high - low;
	}
	
	public double[] correctZBounds(double prob, boolean interpolate, double slop) {
		double bounds[] = new double[2];
		
		int intProb = (int)Math.round(prob * 10000);				//	digits to look for in table
		ContinDistnVariable distn = (ContinDistnVariable)getVariable(zKey);
		double exactZ = distn.getQuantile(prob);
//		int tableInt;
		
		double low = Math.floor(exactZ * 100) / 100;			//	first tabulated z-value below
		while (low >= -3.5 && (int)Math.round(distn.getCumulativeProb(low - 0.01) * 10000) == intProb) {
			low -= 0.01;
		}
			//		low is now first z-value in tables below perfect z with same or lower prob
		
		double high = Math.ceil(exactZ * 100) / 100;			//	first tabulated z-value above
		while (high <= 3.5 && (int)Math.round(distn.getCumulativeProb(high + 0.01) * 10000) == intProb) {
			high += 0.01;
		}
			//		high is now first z-value in tables above perfect z with same or higher prob
		
		if (high - low > 0.011) {		//	several z-values give same prob
			bounds[0] = low;
			bounds[1] = high;
		}
		else {
			if (interpolate)
				bounds[0] = exactZ - 0.002;
			else if (high - exactZ <= exactZ - low)		//	nearest z would give higher value on table
				bounds[0] = exactZ - slop;
			else
				bounds[0] = low - slop;
				
			if (interpolate)
				bounds[1] = exactZ + 0.002;
			else if (high - exactZ >= exactZ - low)		//	nearest z would give lower value on table
				bounds[1] = exactZ + slop;
			else
				bounds[1] = high + slop;
		}
		return bounds;
	}

//-----------------------------------------------------------------------------------
	
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
	
	public void selectValue(ValuePosInfo newValue) {
		getData().setSelection(zKey, newValue.value, newValue.value);
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
		doDrag(null, startInfo);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			selectValue((ValuePosInfo)toPos);
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doDrag(null, endPos);
	}
}
	
