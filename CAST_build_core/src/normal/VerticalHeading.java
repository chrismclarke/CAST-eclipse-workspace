package normal;

import java.awt.*;

import dataView.*;


public class VerticalHeading extends BufferedCanvas {
	static final private int kLeftRightGap = 5;
	static final private int kRows = 72;
	
	static final private String wholeNumber[] = {"-3.", "-2.", "-1.", "-0.", "0.", "1.", "2.", "3."};
	static final private String decimal[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
	static final private String kBiggestLabel = "-3.5";
//	static final private String headingVal[] = generateHeadings();
	
	private NormalTableView theTable;
	
//	static private String[] generateHeadings() {
//		String[] s = new String[kRows];
//		int wholeIndex = 0;
//		int decimalIndex = 5;
//		for (int i=0 ; i<kRows/2 ; i++) {
//			s[i] = wholeNumber[wholeIndex] + decimal[decimalIndex];
//			decimalIndex --;
//			if (decimalIndex < 0) {
//				wholeIndex ++;
//				decimalIndex = 9;
//			}
//		}
//		decimalIndex = 0;
//		for (int i=kRows/2 ; i<kRows ; i++) {
//			s[i] = wholeNumber[wholeIndex] + decimal[decimalIndex];
//			decimalIndex ++;
//			if (decimalIndex > 9) {
//				wholeIndex ++;
//				decimalIndex = 0;
//			}
//		}
//		return s;
//	}
	
	public VerticalHeading(NormalTableView theTable, XApplet applet) {
		super(applet);
		this.theTable = theTable;
	}
	
	public void corePaint(Graphics g) {
		theTable.initialise(g);
//		int labelWidth = getGraphics().getFontMetrics().stringWidth(kBiggestLabel);
		
		Point visRange = theTable.getVisibleRows();
		int minRowVisible = visRange.x;
		int maxRowVisible = visRange.y;
		
		int wholeIndex = 0;
		int decimalIndex = 5;
		g.setColor(Color.blue);
		for (int i=0 ; i<kRows/2 ; i++) {
			if (i >= minRowVisible && i <= maxRowVisible)
				g.drawString(wholeNumber[wholeIndex] + decimal[decimalIndex], kLeftRightGap,
											theTable.topOrigin + NormalTableView.kTopBottomGap
											+ theTable.ascent + i * (theTable.ascent + NormalTableView.kVertGap));
			decimalIndex --;
			if (decimalIndex < 0) {
				wholeIndex ++;
				decimalIndex = 9;
			}
		}
		
		g.setColor(Color.black);
		decimalIndex = 0;
		for (int i=kRows/2 ; i<kRows ; i++) {
			if (i >= minRowVisible && i <= maxRowVisible)
				g.drawString(wholeNumber[wholeIndex] + decimal[decimalIndex], kLeftRightGap,
											theTable.topOrigin + NormalTableView.kTopBottomGap
											+ theTable.ascent + i * (theTable.ascent + NormalTableView.kVertGap));
			decimalIndex ++;
			if (decimalIndex > 9) {
				wholeIndex ++;
				decimalIndex = 0;
			}
		}
		
		Point sel = theTable.selectedRowAndCol();
		if (sel != null) {
			g.setColor(Color.red);
			int left = kLeftRightGap;
			int top = theTable.topOrigin + NormalTableView.kTopBottomGap
														+ sel.y * (theTable.ascent + NormalTableView.kVertGap);
			g.drawRect(left - 3, top - 3, getSize().width - 2 * kLeftRightGap + 5,
																					theTable.ascent + 5);
			g.drawRect(left - 2, top - 2, getSize().width - 2 * kLeftRightGap + 3,
																					theTable.ascent + 3);
		}
		
		g.setColor(Color.gray);
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
	}
	
	public Dimension getMinimumSize() {
		theTable.initialise(theTable.getGraphics());
		int labelWidth = getGraphics().getFontMetrics().stringWidth(kBiggestLabel);
		
		return new Dimension(labelWidth + 2 * kLeftRightGap, theTable.idealHeight);
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
		int col = currentRowCol.x;
		
		int row = (y - NormalTableView.kTopBottomGap - theTable.topOrigin) / (theTable.ascent + NormalTableView.kVertGap);
		if (row >= kRows)
			row = kRows - 1;
		
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
	
