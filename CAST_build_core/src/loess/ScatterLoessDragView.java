package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class ScatterLoessDragView extends ScatterView {
	
	static final private double kMinHitFraction = 0.02;
											//		min x-distance for hits as a fraction of axis length
	
	private String loessKey;
	
	public ScatterLoessDragView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String loessKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		setStickyDrag(true);
		this.loessKey = loessKey;
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		int vertPos = yAxis.numValToRawPosition(yVal.toDouble());
		int horizPos = axis.numValToRawPosition(xVal.toDouble());
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		LoessSmoothVariable loessVar = (LoessSmoothVariable)getVariable(loessKey);
		Point thePoint = null;
		
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			g.setColor(Color.orange);
			NumValue xValue = (NumValue)xVar.valueAt(selectedIndex);
			thePoint = getScreenPoint(selectedIndex, xValue, thePoint);
			g.drawLine(thePoint.x, 0, thePoint.x, getSize().height);
			
			NumValue smoothValue = (NumValue)loessVar.valueAt(selectedIndex);
			thePoint = getScreenPoint(xValue, smoothValue, thePoint);
			g.setColor(Color.red);
			doHilite(g, selectedIndex, thePoint);
		}
		
		g.setColor(getForeground());
		
		if (selectedIndex < 0) {
			ValueEnumeration e = xVar.values();
			int index = 0;
			while (e.hasMoreValues()) {
				NumValue nextVal = (NumValue)e.nextValue();
				thePoint = getScreenPoint(index, nextVal, thePoint);
				if (thePoint != null)
					drawMark(g, thePoint, groupIndex(index));
				index++;
			}
		}
		else {
			NumValue sortedX[] = xVar.getSortedData();
			int xIndex[] = xVar.getSortedIndex();
			int minIndex = loessVar.minInfluenceIndex(selectedIndex);
			int windowPoints = loessVar.getWindowPoints();
			for (int i=0 ; i<windowPoints ; i++) {
				thePoint = getScreenPoint(xIndex[minIndex+i], sortedX[minIndex+i], thePoint);
				if (thePoint != null)
					drawCross(g, thePoint);
			}
		}
	}
	
	private void drawBackground(Graphics g) {
		g.setColor(Color.blue);
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		LoessSmoothVariable loessVar = (LoessSmoothVariable)getVariable(loessKey);
		if (selectedIndex < 0)
			loessVar.drawCurve(g, this);
		else
			loessVar.drawLineForX(g, this, selectedIndex);
	}

//--------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		Point axisPoint = translateFromScreen(x, y, null);
		double xHit;
		try {
			xHit = axis.positionToNumVal(axisPoint.x);
		} catch (AxisException ex) {
			xHit = axis.minOnAxis;
		}
		NumVariable xVar = (NumVariable)getVariable("x");
		int hitIndex = -1;
		double hitDistance = Double.POSITIVE_INFINITY;
		ValueEnumeration e = xVar.values();
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			if (Math.abs(nextVal - xHit) < hitDistance) {
				hitIndex = index;
				hitDistance = Math.abs(nextVal - xHit);
			}
			index++;
		}
		
		if (hitDistance <= (axis.maxOnAxis - axis.minOnAxis) * kMinHitFraction)
			return new IndexPosInfo(hitIndex);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null)
			getData().setSelection(((IndexPosInfo)startInfo).itemIndex);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			getData().setSelection(((IndexPosInfo)toPos).itemIndex);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		getData().clearSelection();
	}
}
	
