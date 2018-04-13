package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class TwoGroupDotView extends DotPlotView {
	static final private int kMaxVertJitter = 30;
	static final private int kTopBorder = 4;
	static final private int kRightBorder = 10;
	
	static final private Color kSeparatorColor = new Color(0xDDDDDD);
	static final private Color kGroupTextColor = new Color(0x990000);
	
	private Font groupFont;
	
	public TwoGroupDotView(DataSet theData, XApplet applet, HorizAxis numAxis, String yKey, String groupKey) {
		super(theData, applet, numAxis, 0.5);
		setActiveNumVariable(yKey);
		setActiveCatVariable(groupKey);
		groupFont = applet.getBigBoldFont();
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = getCatVariable().getItemCategory(index);
			if (groupIndex == 0)
				newPoint.y -= getSize().height / 2;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = getCatVariable().noOfCategories();
		return Math.min(kMaxVertJitter,
							(getSize().height - getViewBorder().top - getViewBorder().bottom) / noOfGroups / 2);
	}
	
	
	public void paintBackground(Graphics g) {
		CatVariable groupVar = getCatVariable();
		
		g.setColor(kSeparatorColor);
		int vert = getSize().height / 2;
		for (int i=-1 ; i<2 ; i++)
			g.drawLine(0, vert + i, getSize().width, vert + i);
		
		g.setColor(kGroupTextColor);
		g.setFont(groupFont);
		int ascent = g.getFontMetrics().getAscent();
		for (int i=0 ; i<2 ; i++) {
			int baseline = ascent + kTopBorder + i * (getSize().height / 2);
			Value groupLabel = groupVar.getLabel(i);
			
			groupLabel.drawLeft(g, getSize().width - kRightBorder, baseline);
		}
	}
	
	
	public void paintView(Graphics g) {
		paintBackground(g);
		super.paintView(g);
	}
}