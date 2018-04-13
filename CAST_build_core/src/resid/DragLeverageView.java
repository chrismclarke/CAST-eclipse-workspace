package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import regnView.*;


public class DragLeverageView extends LSScatterView {
//	static public final String DRAG_LEVERAGE_PLOT = "dragLeveragePlot";
	
	static final private Color kArrowColor = new Color(0xFFCCFF);
	static final private Color kCrossHiliteColor = Color.yellow;
	static final private Color kCrossDragColor = new Color(0xFF6666);
	
	private boolean adjustLS = false;
	private NumValue initialIntercept, initialSlope;
	
	public DragLeverageView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																						String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, lineKey);
		LinearModel lsLine = (LinearModel)getVariable(lineKey);
		initialSlope = lsLine.getSlope();
		initialIntercept = lsLine.getIntercept();
	}
	
	public void setAdjustLS(boolean adjustLS) {
		this.adjustLS = adjustLS;
		LinearModel lsLine = (LinearModel)getVariable(lineKey);
		if (adjustLS)
			lsLine.updateLSParams(yKey);
		else  {
			lsLine.setSlope(initialSlope);
			lsLine.setIntercept(initialIntercept);
		}
		NumVariable xVar = (NumVariable)getVariable(xKey);
		int finalIndex = xVar.noOfValues() - 1;
		getData().variableChanged(lineKey, finalIndex);
	}
	
	private void drawHorizArrow(Graphics g, int xCenter, int yCenter, int xSign) {
		int x = xCenter + xSign * 8;
		for (int i=0 ; i<11 ; i++) {
			g.drawLine(x, yCenter - i, x, yCenter + i);
			x += xSign;
		}
		
		for (int i=0 ; i<5 ; i++) {
			g.drawLine(x, yCenter - 5, x, yCenter + 5);
			x += xSign;
		}
	}
	
	private void drawVertArrow(Graphics g, int xCenter, int yCenter, int ySign) {
		int y = yCenter + ySign * 8;
		for (int i=0 ; i<11 ; i++) {
			g.drawLine(xCenter - i, y, xCenter + i, y);
			y += ySign;
		}
		
		for (int i=0 ; i<5 ; i++) {
			g.drawLine(xCenter - 5, y, xCenter + 5, y);
			y += ySign;
		}
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			g.setColor(kArrowColor);
			drawHorizArrow(g, thePoint.x, thePoint.y, +1);
			drawHorizArrow(g, thePoint.x, thePoint.y, -1);
			drawVertArrow(g, thePoint.x, thePoint.y, +1);
			drawVertArrow(g, thePoint.x, thePoint.y, -1);
			
			g.setColor(doingDrag ? kCrossDragColor : kCrossHiliteColor);
			g.fillRect(thePoint.x - 6, thePoint.y - 6, 13, 13);
		}
	}

//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 25;
	
	private boolean doingDrag = false;
//	private int selectedIndex = -1;
	private int xOffset, yOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		NumVariable xVar = getNumVariable();
		int finalIndex = xVar.noOfValues() - 1;
		Point actualPos = getScreenPoint(finalIndex, (NumValue)(xVar.valueAt(finalIndex)), null);
		
		int dist = (actualPos.x - x) * (actualPos.x - x) + (actualPos.y - y) * (actualPos.y - y);
		
		if (dist > kMinHitDist)
			return null;
		else
			return new DragPosInfo(x - actualPos.x, y - actualPos.y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int xOnAxis = hitPos.x - xOffset;
		int yOnAxis = hitPos.y - yOffset;
		if (xOnAxis >= 0 && xOnAxis < axis.getAxisLength() && yOnAxis >= 0
																							&& yOnAxis < yAxis.getAxisLength())
			return new DragPosInfo(xOnAxis, yOnAxis);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null && startPos instanceof DragPosInfo) {
			DragPosInfo posInfo = (DragPosInfo)startPos;
			xOffset = posInfo.x;
			yOffset = posInfo.y;
			
			doingDrag = true;
			repaint();
			return true;
		}
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			doingDrag = true;
			DragPosInfo dragPos = (DragPosInfo)toPos;
			
			try {
				double newX = axis.positionToNumVal(dragPos.x);
				NumVariable xVar = (NumVariable)getVariable(xKey);
				int finalIndex = xVar.noOfValues() - 1;
				((NumValue)xVar.valueAt(finalIndex)).setValue(newX);
				xVar.clearSortedValues();
				
				double newY = yAxis.positionToNumVal(dragPos.y);
				NumVariable yVar = (NumVariable)getVariable(yKey);
				((NumValue)yVar.valueAt(finalIndex)).setValue(newY);
				yVar.clearSortedValues();
				
				if (adjustLS) {
					LinearModel lsLine = (LinearModel)getVariable(lineKey);
					lsLine.updateLSParams(yKey);
				}
				
				getData().variableChanged(xKey, finalIndex);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}
	
