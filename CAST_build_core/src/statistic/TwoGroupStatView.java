package statistic;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;



public class TwoGroupStatView extends DotPlotView {
	static final private int kMaxVertJitter = 30;
	static final private int kTopBorder = 4;
	static final private int kRightBorder = 10;
	
	static final private int kMedianArrowLength = 20;
	static final private int kRangeArrowHeight = 20;
	
	static final private Color kSeparatorColor = new Color(0xDDDDDD);
	static final private Color kGroupTextColor = new Color(0x666666);
	
	static final private Color kBoxFillColor = new Color(0xEEEEEE);
	
	private String kMedianString;
	
	private String y0Key, y1Key;
	private int statDecimals;
	
	private Font groupFont, statFont;
	
	private boolean initialised = false;
	private BoxInfo y0BoxInfo, y1BoxInfo;
	
	private Color centerColor, spreadColor;
	private boolean rangeNotIqr = false;
	
	public TwoGroupStatView(DataSet theData, XApplet applet, HorizAxis numAxis, String yKey,
															String groupKey, String y0Key, String y1Key, int statDecimals) {
		super(theData, applet, numAxis, 0.5);
		setActiveNumVariable(yKey);
		setActiveCatVariable(groupKey);
		this.y0Key = y0Key;
		this.y1Key = y1Key;
		this.statDecimals = statDecimals;
		groupFont = applet.getBigBoldFont();
		statFont = applet.getStandardBoldFont();
		
		kMedianString = applet.translate("Median");
	}
	
	public void setStatColors(Color centerColor, Color spreadColor) {
		this.centerColor = centerColor;
		this.spreadColor = spreadColor;
	}
	
	public void setRangeType(int rangeType) {
		rangeNotIqr = (rangeType == 1);
		repaint();
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
	
	private void doInitialisation(Graphics g) {
		y1BoxInfo = new BoxInfo();
		y1BoxInfo.setFillColor(kBoxFillColor);
		
		g.setFont(groupFont);
		int ascent = g.getFontMetrics().getAscent();
		int available = (getSize().height - 3) / 2 - currentJitter - ascent;
		y1BoxInfo.vertMidLine = currentJitter + available / 2;
		y1BoxInfo.boxBottom = y1BoxInfo.vertMidLine - y1BoxInfo.getBoxHeight() / 2;
		
		y0BoxInfo = new BoxInfo();
		y0BoxInfo.setFillColor(kBoxFillColor);
		
		y0BoxInfo.vertMidLine = y1BoxInfo.vertMidLine + (getSize().height + 3) / 2;
		y0BoxInfo.boxBottom = y1BoxInfo.boxBottom + (getSize().height + 3) / 2;
		
		NumVariable y0Var = (NumVariable)getVariable(y0Key);
		NumValue[] sortedY0 = y0Var.getSortedData();
		y0BoxInfo.initialiseBox(sortedY0, false, axis);
	}
	
	private void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	private void drawBox(BoxInfo boxInfo, NumValue[] sortedY, Graphics g) {
		g.setColor(getForeground());
		boxInfo.drawBoxPlot(g, this, sortedY, axis);
		g.setFont(statFont);
		int ascent = g.getFontMetrics().getAscent();
		
		int medianPos = boxInfo.boxPos[BoxInfo.MEDIAN];
		int boxBottom = boxInfo.boxBottom;
		
		Point bottomMedian = translateToScreen(medianPos, boxBottom, null);
		NumValue median = new NumValue(boxInfo.boxVal[BoxInfo.MEDIAN], statDecimals);
		LabelValue medianVal = new LabelValue(kMedianString + " = " + median);
		
		g.setColor(centerColor);
		medianVal.drawCentred(g, bottomMedian.x, bottomMedian.y + ascent + kMedianArrowLength);
		g.drawLine(bottomMedian.x, bottomMedian.y + 2, bottomMedian.x, bottomMedian.y + kMedianArrowLength - 2);
		g.drawLine(bottomMedian.x - 1, bottomMedian.y + 3, bottomMedian.x - 1, bottomMedian.y + kMedianArrowLength - 2);
		g.drawLine(bottomMedian.x + 1, bottomMedian.y + 3, bottomMedian.x + 1, bottomMedian.y + kMedianArrowLength - 2);
		
		g.drawLine(bottomMedian.x, bottomMedian.y + 2, bottomMedian.x - 4, bottomMedian.y + 6);
		g.drawLine(bottomMedian.x, bottomMedian.y + 2, bottomMedian.x + 4, bottomMedian.y + 6);
		g.drawLine(bottomMedian.x, bottomMedian.y + 3, bottomMedian.x - 4, bottomMedian.y + 7);
		g.drawLine(bottomMedian.x, bottomMedian.y + 3, bottomMedian.x + 4, bottomMedian.y + 7);
		
		int lowIndex = rangeNotIqr ? BoxInfo.LOW_EXT : BoxInfo.LOW_QUART;
		int highIndex = rangeNotIqr ? BoxInfo.HIGH_EXT : BoxInfo.HIGH_QUART;
		
		int lqPos = boxInfo.boxPos[lowIndex];
		int uqPos = boxInfo.boxPos[highIndex];
		int boxTop = boxInfo.boxBottom + boxInfo.getBoxHeight();
		
		Point topLq = translateToScreen(lqPos, boxTop, null);
		Point topUq = translateToScreen(uqPos, boxTop, null);
		
		NumValue iqr = new NumValue(boxInfo.boxVal[highIndex]
																					- boxInfo.boxVal[lowIndex], statDecimals);
		LabelValue iqrVal = new LabelValue((rangeNotIqr ? getApplet().translate("Range") : getApplet().translate("IQR"))
																																																				+ " = " + iqr);
		
		g.setColor(spreadColor);
		iqrVal.drawCentred(g, (topLq.x + topUq.x) / 2, topLq.y - kRangeArrowHeight);
		
		int rangeArrowVert = topLq.y - kRangeArrowHeight / 2;
		g.drawLine(topLq.x, rangeArrowVert, topUq.x, rangeArrowVert);
		g.drawLine(topLq.x + 1, rangeArrowVert - 1, topUq.x - 1, rangeArrowVert - 1);
		g.drawLine(topLq.x + 1, rangeArrowVert + 1, topUq.x - 1, rangeArrowVert + 1);
		
		for (int i=0 ; i<2 ; i++) {
			g.drawLine(topLq.x + i, rangeArrowVert, topLq.x + i + 4, rangeArrowVert + 4);
			g.drawLine(topLq.x + i, rangeArrowVert, topLq.x + i + 4, rangeArrowVert - 4);
			
			g.drawLine(topUq.x - i, rangeArrowVert, topUq.x - i - 4, rangeArrowVert + 4);
			g.drawLine(topUq.x - i, rangeArrowVert, topUq.x - i - 4, rangeArrowVert - 4);
		}
	}
	
	public void paintBackground(Graphics g) {
		initialise(g);
		
		NumVariable y1Var = (NumVariable)getVariable(y1Key);
		NumValue[] sortedY1 = y1Var.getSortedData();
		y1BoxInfo.initialiseBox(sortedY1, false, axis);
		drawBox(y1BoxInfo, sortedY1, g);
		
		NumVariable y0Var = (NumVariable)getVariable(y0Key);
		NumValue[] sortedY0 = y0Var.getSortedData();
		drawBox(y0BoxInfo, sortedY0, g);
		
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