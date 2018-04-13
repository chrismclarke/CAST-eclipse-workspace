package utils;

import java.awt.*;

import dataView.*;

public class CatKey extends DataView {
	static private final int kTopBottomSpacing = 2;
	static private final int kCrossGap = 3;
	static private final int kEndGap = 3;
	static private final int kBorder = 1;
	
	static public final int HORIZ = 0;
	static public final int VERT = 1;
	
//	static public final String CAT_KEY = "catKey";
	
	private int orientation;
	private int itemHeight = 0;
	private int itemWidth;
	private int textOffset;
	private Dimension bestDimension;
	
	private boolean canSelectGroups = false;
	private int selectedCat = -1;
	
	public CatKey(DataSet theData, String variableKey, XApplet applet, int orientation) {
		super(theData, applet, null);
		this.orientation = orientation;
		setCrossSize(LARGE_CROSS);
		setActiveCatVariable(variableKey);
	}
	
	public void setCanSelectGroups(boolean canSelectGroups) {
		this.canSelectGroups = canSelectGroups;
	}
	
	private void findSpacingInfo() {
		CatVariable variable = getCatVariable();
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		itemHeight = Math.max(fm.getAscent() + fm.getDescent(), 2*LARGE_CROSS + 2)
																								+ 2 * kTopBottomSpacing;
		itemWidth = 2 * kCrossGap + kEndGap + 2*LARGE_CROSS + 2 + variable.getMaxWidth(g);
		textOffset = (itemHeight + fm.getAscent() - fm.getDescent()) / 2;
		if (orientation == HORIZ)
			bestDimension = new Dimension(2 * kBorder + variable.noOfCategories() * itemWidth,
																					2 * kBorder + itemHeight);
		else
			bestDimension = new Dimension(2 * kBorder + itemWidth,
															2 * kBorder + itemHeight * variable.noOfCategories());
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int index) {
		for (int i=0 ; i<2 ; i++)
			for (int j=0 ; j<2 ; j++)
				drawMark(g, new Point(x + i, y + j), index);
	}
	
	private void drawOneItem(Graphics g, int index, int x, int y) {
		CatVariable variable = getCatVariable();
		drawSymbol(g, x + kCrossGap + LARGE_CROSS, y + itemHeight / 2, index);
		int horiz = x + 2 * (kCrossGap + LARGE_CROSS) + 2;
		int vert = y + textOffset;
		variable.getLabel(index).drawRight(g, horiz, vert);
	}
	
	public void paintView(Graphics g) {
		if (itemHeight == 0)
			findSpacingInfo();
		
		g.drawRect(0, 0, bestDimension.width - 1, bestDimension.height - 1);
		Color oldColor = g.getColor();
		g.setColor(Color.white);
		g.fillRect(1, 1, bestDimension.width - 2, bestDimension.height - 2);
		
		if (canSelectGroups && selectedCat >= 0) {
			g.setColor(Color.yellow);
			g.fillRect(1, kBorder + selectedCat * itemHeight, bestDimension.width - 2, itemHeight);
		}
		
		g.setColor(oldColor);
		
		CatVariable variable = getCatVariable();
		if (orientation == HORIZ)
			for (int i=0 ; i<variable.noOfCategories() ; i++)
				drawOneItem(g, i, kBorder + itemWidth * i, kBorder);
		else
			for (int i=0 ; i<variable.noOfCategories() ; i++)
				drawOneItem(g, i, kBorder, kBorder + itemHeight * i);
	}
	
	public Dimension getMinimumSize() {
		findSpacingInfo();
		return bestDimension;
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	protected void doChangeSelection(Graphics g) {
	}
	
	protected void doChangeValue(Graphics g, int index) {
	}
	
	protected void doChangeVariable(Graphics g, String key) {
	}

//-----------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canSelectGroups;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x > getSize().width || y > getSize().height)
			return null;
		
		CatVariable variable = getCatVariable();
		int group = (y - kBorder) / itemHeight;
		group = Math.min(group, variable.noOfCategories() - 1);
		return new CatPosInfo(group, true);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos == null) {
			getData().clearSelection();
			selectedCat = -1;
			repaint();
		}
		else {
			CatPosInfo dragPos = (CatPosInfo)startPos;
			selectedCat = dragPos.catIndex;
			
			CatVariable variable = getCatVariable();
			boolean selection[] = new boolean[variable.noOfValues()];
			for (int i=0 ; i<selection.length ; i++)
				selection[i] = variable.getItemCategory(i) == selectedCat;
			getData().setSelection(selection);
			
			repaint();
		}
		
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		startDrag(null);
	}
}
