package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;


public class DragStackedCrossView extends CoreDragStackedView {
//	static public final String DRAG_STACKED_CROSS = "dragStackedCross";
	
	static final private int kNameAxisGap = 4;
	static final private int kSmallTickLength = 2;
	static final private int kBigTickLength = 5;
	static final private int kTickLabelGap = 2;
	static final private int kDefaultTopGap = 10;
	
	static final private int kCrossBoxWidth = 12;

	private int firstLabelCol, ticksPerLabel, labelDecimals;
	
	private int varNameBaseline, axisLabelBaseline, axisVert, axisLength, axisLeft;
	
	
	public DragStackedCrossView(DataSet theData, XApplet applet, String yKey, String axisInfo) {
		super(theData, applet, yKey, axisInfo);
	}
	
	protected void readAxisInfo(Graphics g) {
		StringTokenizer st = new StringTokenizer(axisInfo);
		axisMin = Double.parseDouble(st.nextToken());
		axisMax = Double.parseDouble(st.nextToken());
		classWidth = Double.parseDouble(st.nextToken());
		firstLabelCol = Integer.parseInt(st.nextToken());			//	zero for label at axisMin
		ticksPerLabel = Integer.parseInt(st.nextToken());			//	often label at each 10th tick
		labelDecimals = Integer.parseInt(st.nextToken());
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		varNameBaseline = getSize().height - descent;
		axisLabelBaseline = varNameBaseline - ascent - kNameAxisGap;
		axisVert = axisLabelBaseline - ascent - kBigTickLength - kTickLabelGap;
		axisLength = (int)Math.round(Math.ceil((axisMax - axisMin) / classWidth)) * symbolAxisWidth;
		axisLeft = (getSize().width - axisLength) / 2;
	}
	
	protected void setSymbolSize(Graphics g) {
		symbolScreenWidth = symbolScreenHeight = symbolAxisWidth = symbolAxisHeight = kCrossBoxWidth;
	}
	
//-------------------------------------------------------------------
	
	protected void drawAxis(Graphics g) {
		g.drawLine(axisLeft, axisVert, axisLeft + axisLength, axisVert);
		
		int tickPos = axisLeft;
		while (tickPos <= axisLeft + axisLength) {
			g.drawLine(tickPos, axisVert, tickPos, axisVert + kSmallTickLength);
			tickPos += kCrossBoxWidth;
		}
		
		NumValue axisLabel = new NumValue(axisMin + firstLabelCol * classWidth, labelDecimals);
		tickPos = axisLeft + firstLabelCol * symbolScreenWidth;
		while (tickPos <= axisLeft + axisLength) {
			g.drawLine(tickPos, axisVert, tickPos, axisVert + kBigTickLength);
			axisLabel.drawCentred(g, tickPos, axisLabelBaseline);
			
			axisLabel.setValue(axisLabel.toDouble() + classWidth * ticksPerLabel);
			tickPos += symbolScreenWidth * ticksPerLabel;
		}
		
		LabelValue varName = new LabelValue(getVariable(yKey).name);
		varName.drawLeft(g, axisLeft + axisLength, varNameBaseline);
		
		g.setColor(Color.white);
		g.fillRect(axisLeft, 0, axisLength + 1, axisVert);
	}
	
	protected void setSymbolRect(int axisPix, int offAxisPix, Rectangle r) {
		r.x = axisLeft + axisPix;
		r.y = axisVert - offAxisPix;
		r.width = symbolScreenWidth;
		r.height = symbolScreenHeight;
	}
	
	protected void setDefaultSymbolRect(Rectangle r) {
		r.x = axisLeft + (axisLength - symbolScreenWidth) / 2;
		r.y = kDefaultTopGap;
		r.width = symbolScreenWidth;
		r.height = symbolScreenHeight;
	}
	
	protected int getStackIndex(int dragX, int dragY, boolean center) {
		if (center)
			dragX += symbolScreenWidth / 2;
		return (dragX - axisLeft) / symbolAxisWidth;
	}
	
	protected int getHighIndexOffset(int lowDragStackIndex) {
		return dragX - axisLeft - lowDragStackIndex * symbolAxisWidth;
	}
	
	protected int getDragStackPosition() {
		return Math.max((axisVert - dragY - symbolAxisHeight / 2) / symbolAxisHeight, 0);
	}
	
	protected void drawSymbol(Graphics g, Rectangle r, Color crossColor, NumValue y, int index) {
		g.setColor(crossColor);
		g.drawLine(r.x + 2, r.y + 2, r.x + r.width - 2, r.y + r.height - 2);
		g.drawLine(r.x + 2, r.y + r.height - 2, r.x + r.width - 2, r.y + 2);
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		if (x <= axisLeft || x >= axisLeft + axisLength)
			return null;
		
		return new DragPosInfo(x, y);
	}
	
}
	
