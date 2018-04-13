package normal;

import java.awt.*;

import dataView.*;


public class HorizHeading extends BufferedCanvas {
	static final private int kTopBottomGap = 5;
	static final private String headingVal[] = {".00", ".01", ".02", ".03", ".04", ".05", ".06", ".07", ".08", ".09"};
	
	private NormalTableView theTable;
	
	public HorizHeading(NormalTableView theTable, XApplet applet) {
		super(applet);
		this.theTable = theTable;
	}
	
	public void corePaint(Graphics g) {
		theTable.initialise(g);
		int ascent = g.getFontMetrics().getAscent();
		
		Point visHorizRange = theTable.getVisibleCols();
		int minColVisible = visHorizRange.x;
		int maxColVisible = visHorizRange.y;
		
		int baseline = kTopBottomGap + ascent;
		for (int i=minColVisible ; i<=maxColVisible ; i++)
			g.drawString(headingVal[i], theTable.leftOrigin + NormalTableView.kLeftRightGap
												+ i * (theTable.valWidth + NormalTableView.kHorizGap), baseline);
		
		Point sel = theTable.selectedRowAndCol();
		if (sel != null) {
			g.setColor(Color.red);
			int left = theTable.leftOrigin + NormalTableView.kLeftRightGap + sel.x * (theTable.valWidth
																							+ NormalTableView.kHorizGap);
			int top = kTopBottomGap;
			g.drawRect(left - 3, top - 3, theTable.valWidth + 5, ascent + 5);
			g.drawRect(left - 2, top - 2, theTable.valWidth + 3, ascent + 3);
		}
		
		g.setColor(Color.gray);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
	}
	
	public Dimension getMinimumSize() {
		theTable.initialise(theTable.getGraphics());
		int ascent = getGraphics().getFontMetrics().getAscent();
		
		return new Dimension(theTable.idealWidth, ascent + 2 * kTopBottomGap);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
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
		
		Point currentRowCol = theTable.selectedRowAndCol();
		int row = currentRowCol.y;
		
		int col = (x - NormalTableView.kLeftRightGap - theTable.leftOrigin) / (theTable.valWidth + NormalTableView.kHorizGap);
		if (col >= NormalTableView.kCols)
			col = NormalTableView.kCols - 1;
		
		double value = theTable.valueFromRowCol(row, col);
		return new ValuePosInfo(value);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		theTable.selectMax((ValuePosInfo)startInfo);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			theTable.selectMax((ValuePosInfo)toPos);
	}
	
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (endPos != null)
			theTable.selectMax((ValuePosInfo)endPos);
	}
	
}
	
