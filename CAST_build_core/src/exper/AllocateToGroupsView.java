package exper;

import java.awt.*;

import dataView.*;

import map.*;


public class AllocateToGroupsView extends DataView {
//	static public final String ALLOCATE_TO_GROUPS = "allocateToGroups";
	
	static final public int kEndFrame = 40;
	static final private int kHeadingTopBottom = 2;
	static final private int kCircleRadius = 7;
	static final private int kArrowWidth = 30;
	static final private int kArrowHead = 6;
	static final private int kGroupGap = 10;
	
	static final private int kMinCircleHorizSpacing = 16 + 2 * kCircleRadius;
	static final private int kMinCircleVertSpacing = 20 + 2 * kCircleRadius;
	static final private int kTableLeftRightBorder = 8;
	static final private int kTableTopBottomBorder = 3;
	
//	static final private Color kLurkingColor[] = {Color.red, Color.blue};
	
	private String groupKey, lurkingKey;
	private NumKeyView lurkingKeyView;
	private LabelValue unitName;
	private int noOfCols;
	
	public AllocateToGroupsView(DataSet theData, XApplet applet, String groupKey, String lurkingKey,
																			NumKeyView lurkingKeyView, LabelValue unitName, int noOfCols) {
		super(theData, applet, new Insets(0,0,0,0));
		this.groupKey = groupKey;
		this.lurkingKey = lurkingKey;
		this.lurkingKeyView = lurkingKeyView;
		this.unitName = unitName;
		this.noOfCols = noOfCols;
	}
	
	private Point getIntermediatePoint(Point pFull, Point pGroup, Point p) {
		if (p ==  null)
			p = new Point(0, 0);
		p.x = pFull.x + (pGroup.x - pFull.x) * getCurrentFrame() / kEndFrame;
		p.y = pFull.y + (pGroup.y - pFull.y) * getCurrentFrame() / kEndFrame;
		return p;
	}
	
	private Point getCircleCenter(int index, int nRows, int nCols, Rectangle r, Point p) {
		if (p == null)
			p = new Point(0, 0);
		int rowIndex = index / nCols;
		int colIndex = index % nCols;
		p.x = r.x + kTableLeftRightBorder + (r.width - 2 * kTableLeftRightBorder)
																										* (2 * colIndex + 1) / (2 * nCols);
		p.y = r.y + kTableTopBottomBorder + (r.height - 2 * kTableTopBottomBorder)
																										* (2 * rowIndex + 1) / (2 * nRows);
		return p;
	}
	
	private void drawAllUnits(Graphics g, Rectangle r) {
		g.setColor(getForeground());
		unitName.drawCentred(g, r.x + r.width / 2, r.y - kHeadingTopBottom
																											- g.getFontMetrics().getDescent());
		g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		g.setColor(Color.white);
		g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
		g.setColor(getForeground());
		
		NumVariable lurkingVar = (NumVariable)getVariable(lurkingKey);
		int nValues = lurkingVar.noOfValues();
		int nRows = (nValues - 1) / noOfCols + 1;
		
		Point p = null;
		ValueEnumeration le = lurkingVar.values();
		int index = 0;
		while (le.hasMoreValues()) {
			double l = le.nextDouble();
			p = getCircleCenter(index, nRows, noOfCols, r, p);
			g.setColor(lurkingKeyView.findColor(l));
			g.fillOval(p.x - kCircleRadius, p.y - kCircleRadius, 2 * kCircleRadius, 2 * kCircleRadius);
			index ++;
		}
	}
	
	private void drawGroupUnits(Graphics g, CatVariable groupVar, int group, int groupCount,
																							Rectangle groupRect, Rectangle fullRect) {
		g.setColor(getForeground());
		groupVar.getLabel(group).drawCentred(g, groupRect.x + groupRect.width / 2,
												groupRect.y - kHeadingTopBottom - g.getFontMetrics().getDescent());
		g.drawRect(groupRect.x, groupRect.y, groupRect.width - 1, groupRect.height - 1);
		g.setColor(Color.white);
		g.fillRect(groupRect.x + 1, groupRect.y + 1, groupRect.width - 2, groupRect.height - 2);
		g.setColor(getForeground());
		
		if (getCurrentFrame() > 1) {
			int nGroupRows = (groupCount - 1) / noOfCols + 1;
			NumVariable lurkingVar = (NumVariable)getVariable(lurkingKey);
			int nFullRows = (lurkingVar.noOfValues() - 1) / noOfCols + 1;
			
			Point pFull = null;
			Point pGroup = null;
			Point p = null;
			ValueEnumeration le = lurkingVar.values();
			int index = 0;
			int groupIndex = 0;
			while (le.hasMoreValues()) {
				double l = le.nextDouble();
				int gr = groupVar.getItemCategory(index);
				if (gr == group) {
					pFull = getCircleCenter(index, nFullRows, noOfCols, fullRect, pFull);
					pGroup = getCircleCenter(groupIndex, nGroupRows, noOfCols, groupRect, pGroup);
					p = getIntermediatePoint(pFull, pGroup, p);
					g.setColor(lurkingKeyView.findColor(l));
					g.fillOval(p.x - kCircleRadius, p.y - kCircleRadius, 2 * kCircleRadius, 2 * kCircleRadius);
					groupIndex ++;
				}
				index ++;
			}
		}
	}
	
	private void drawArrow(Graphics g, int left, int right, int vert) {
		g.setColor(getForeground());
		g.drawLine(left, vert, right, vert);
		g.drawLine(right, vert, right - kArrowHead, vert - kArrowHead);
		g.drawLine(right, vert, right - kArrowHead, vert + kArrowHead);
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int headingSize = fm.getAscent() + fm.getDescent() + 2 * kHeadingTopBottom;
		int tableWidth = (getSize().width - kArrowWidth) / 2;
		Rectangle fullRect = new Rectangle(0, headingSize, tableWidth, getSize().height - headingSize);
		drawAllUnits(g, fullRect);
		
		CatVariable groupVar = (CatVariable)getVariable(groupKey);
		int nGroups = groupVar.noOfCategories();
		int groupCounts[] = groupVar.getCounts();
		int groupRows[] = new int[nGroups];
		int totalRows = 0;
		for (int i=0 ; i<nGroups ; i++) {
			groupRows[i] = (groupCounts[i] - 1) / noOfCols + 1;
			totalRows += groupRows[i];
		}
		int vertForRows = getSize().height - nGroups * headingSize - (nGroups - 1) * kGroupGap;
		
		Rectangle groupRect = new Rectangle(tableWidth + kArrowWidth, 0, tableWidth, 0);
		for (int i=0 ; i<nGroups ; i++) {
			groupRect.y += headingSize;
			groupRect.height = vertForRows * groupRows[i] / totalRows;
			drawGroupUnits(g, groupVar, i, groupCounts[i], groupRect, fullRect);
			
			drawArrow(g, tableWidth, groupRect.x - 2, groupRect.y + groupRect.height / 2);
			
			groupRect.y += groupRect.height + kGroupGap;
		}
	}
	
	public void doGroupingAnimation() {
		animateFrames(1, kEndFrame - 1, 16, null);
	}

//-----------------------------------------------------------------------------------
	
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(groupKey) || key.equals(lurkingKey))
			repaint();
	}
	
	public Dimension getMinimumSize() {
		Graphics g = getGraphics();
		FontMetrics fm = g.getFontMetrics();
		
			int circleTableWidth = noOfCols * kMinCircleHorizSpacing + 2 * kTableLeftRightBorder;
			
			CatVariable groupVar = (CatVariable)getVariable(groupKey);
			int nGroups = groupVar.noOfCategories();
			int maxLabelWidth = 0;
			for (int i=0 ; i<nGroups ; i++)
				maxLabelWidth = Math.max(maxLabelWidth, groupVar.getLabel(i).stringWidth(g));
			
		int minWidth = Math.max(circleTableWidth, unitName.stringWidth(g))
												+ kArrowWidth + Math.max(circleTableWidth, maxLabelWidth);
		
			int groupCounts[] = groupVar.getCounts();
		int minHeight = nGroups * (fm.getAscent() + fm.getDescent() + 2 * kHeadingTopBottom);
		for (int i=0 ; i<groupCounts.length ; i++) {
			int nGroupRows = (groupCounts[i] - 1) / noOfCols + 1;
			minHeight += nGroupRows * kMinCircleVertSpacing + 2 * kTableTopBottomBorder;
		}
		return new Dimension(minWidth, minHeight);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}