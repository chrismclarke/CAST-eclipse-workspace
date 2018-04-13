package exper2;

import java.awt.*;

import dataView.*;

import exper2Prog.*;


public class MissingValuesView extends DataView {
	static final private int kLeftRightBorder = 20;
	static final private int kTopBottomBorder = 8;
	
	static final private int kMinHorizGap = 25;
	static final private int kValueVertGap = 6;
	
	private String yKey, xKey;
	
	private boolean initialised = false;
	
	private int ascent, headingAscent, headingDescent;
	private int valueWidth, headingWidth, coreColWidth;
	private int nGroups, maxGroupCount;
	private int group[];
	private int indexInGroup[];
	
	private int hitIndex = -1;
	private int selectedIndex = -1;
	
	public MissingValuesView(DataSet theData, XApplet applet, String yKey, String xKey) {
		super(theData, applet, new Insets(0,0,0,0));
		this.yKey = yKey;
		this.xKey = xKey;
	}
	
	public void changeMissing(int i) {
		MissingValueVariable yVar = (MissingValueVariable)getVariable(yKey);
		yVar.changeMissing(i);
		getData().variableChanged(yKey);
	}
	
	private Font getHeadingFont(Font f) {
		return new Font(f.getName(), Font.BOLD, f.getSize());
	}
	
	private boolean initialise(Graphics g) {
		if (!initialised) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			valueWidth = yVar.getMaxWidth(g);
			int n = yVar.noOfValues();
			
			ascent = g.getFontMetrics().getAscent();
		
			CatVariable xVar = (CatVariable)getVariable(xKey);
			nGroups = xVar.noOfCategories();
			
			Font f = g.getFont();
			g.setFont(getHeadingFont(f));
			headingAscent = g.getFontMetrics().getAscent();
			headingDescent = g.getFontMetrics().getDescent();
			headingWidth = 0;
			for (int i=0 ; i<nGroups ; i++)
				headingWidth = Math.max(headingWidth, xVar.getLabel(i).stringWidth(g));
			g.setFont(f);
			
			coreColWidth = Math.max(valueWidth, headingWidth);
			
			int noInGroup[] = new int[nGroups];
			group = new int[n];
			indexInGroup = new int[n];
			maxGroupCount = 0;
			
			for (int i=0 ; i<n ; i++) {
				int x = xVar.getItemCategory(i);
				group[i] = x;
				int index = (noInGroup[x] ++);
				indexInGroup[i] = index;
				maxGroupCount = Math.max(maxGroupCount, index + 1);
			}
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private void setValueRect(int col, int row, Rectangle r) {
		int valueHorizGap = (getSize().width - 2 * kLeftRightBorder - nGroups * valueWidth) / (nGroups - 1);
		int horizOffset = kLeftRightBorder - kMinHorizGap / 2;
		r.x = horizOffset + col * (valueWidth + valueHorizGap);
		int vertOffset = (headingAscent + headingDescent) + kTopBottomBorder - kValueVertGap / 2;
		r.y = vertOffset + row * (ascent + kValueVertGap);
		r.width = kMinHorizGap + valueWidth;
		r.height = kValueVertGap + ascent;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		nGroups = xVar.noOfCategories();
		
		Font f = g.getFont();
		g.setFont(getHeadingFont(f));
		Rectangle r = new Rectangle(0, 0, 0, 0);
		int headingBaseline = headingAscent;
		for (int i=0 ; i<nGroups ; i++) {
			setValueRect(i, 0, r);
			int center = r.x + r.width / 2;
			xVar.getLabel(i).drawCentred(g, center, headingBaseline);
		}
		
		g.setFont(f);
		
		g.setColor(Color.white);
		int bodyTop = headingAscent + headingDescent + (kTopBottomBorder - kValueVertGap) / 2;
		g.fillRect(0, bodyTop, getSize().width, getSize().height - bodyTop);
		g.setColor(getForeground());
		
//		int valueLineHt = ascent + kValueVertGap;
		
		MissingValueVariable yVar = (MissingValueVariable)getVariable(yKey);
		Color dimColor = dimColor(getForeground(), 0.8);
		
		for (int i=0 ; i<group.length ; i++) {
			NumValue y = yVar.baseValueAt(i);
			setValueRect(group[i], indexInGroup[i], r);
			
			if (i == selectedIndex) {
				g.setColor(Color.yellow);
				g.fillRect(r.x, r.y, r.width, r.height);
			}
			
			g.setColor(yVar.isMissing(i) ? dimColor : getForeground());
			
			int baseline = r.y + (r.height + ascent) / 2;
			int valRight = r.x + (r.width + valueWidth) / 2;
			
			y.drawLeft(g, valRight, baseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise(getGraphics());
		
		int width = 2 * kLeftRightBorder + nGroups * coreColWidth + (nGroups - 1) * kMinHorizGap;
		int height = headingAscent + headingDescent +  2 * kTopBottomBorder + maxGroupCount * ascent
																										+ (maxGroupCount - 1) * kValueVertGap;
		return new Dimension(width, height);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private int getHitColumn(int x) {
		int valueHorizGap = (getSize().width - 2 * kLeftRightBorder - nGroups * valueWidth) / (nGroups - 1);
		int horizOffset = kLeftRightBorder - kMinHorizGap / 2;
		return (x - horizOffset) / (valueWidth + valueHorizGap);
	}
	
	private int getHitRow(int y) {
		int vertOffset = (headingAscent + headingDescent) + kTopBottomBorder - kValueVertGap / 2;
		return (y - vertOffset) / (ascent + kValueVertGap);
	}
	
	private boolean canChange(int index) {
		MissingValueVariable yVar = (MissingValueVariable)getVariable(yKey);
		if (yVar.isMissing(index))
			return true;
//		boolean allMissing = true;
		int col = group[index];
		for (int i=0 ; i<yVar.noOfValues() ; i++)
			if (group[i] == col && i != index && !yVar.isMissing(i))
				return true;
		return false;
	}

	protected PositionInfo getPosition(int x, int y) {
		int row = getHitRow(y);
		int col = getHitColumn(x);
		
		for (int i=0 ; i<group.length ; i++)
			if (col == group[i] && row == indexInGroup[i])
				return canChange(i) ? new IndexPosInfo(i) : null;
		
		return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		IndexPosInfo indexPos = (IndexPosInfo)startInfo;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		if (indexPos.itemIndex < 0 || indexPos.itemIndex >= yVar.noOfValues())
			return false;
		else {
			hitIndex = selectedIndex = indexPos.itemIndex;
			repaint();
			return true;
		}
	}
	
	protected void doDrag(PositionInfo fromInfo, PositionInfo toInfo) {
		if (toInfo == null)
			selectedIndex = -1;
		else {
			IndexPosInfo toPos = (IndexPosInfo)toInfo;
			if (hitIndex == toPos.itemIndex)
				selectedIndex = hitIndex;
			else
				selectedIndex = -1;
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startInfo, PositionInfo endInfo) {
		if (endInfo != null) {
			IndexPosInfo endPos = (IndexPosInfo)endInfo;
			if (hitIndex == endPos.itemIndex)
				changeMissing(endPos.itemIndex);
			selectedIndex = -1;
			getData().variableChanged(yKey);
			((MissingValuesApplet)getApplet()).recalculateAnova();
		}
	}


}