package residTwo;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;

import multiRegn.*;


public class DragOutlierView extends ModelDot3View {
	
	static final private Color kPaleRed = new Color(0xFF6666);
	
	public DragOutlierView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}

//--------------------------------------------------------------------------------
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		Point crossPos = null;
		double xVals[] = new double[explanKey.length];
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			double y = ((NumVariable)getVariable(yKey)).doubleValueAt(selectedIndex);
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = ((NumVariable)getVariable(explanKey[i])).doubleValueAt(selectedIndex);
//			double fit = model.evaluateMean(xVals);
			
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			Color redColor = Color.red;
			Color blackColor = Color.black;
//			if (shadeHandling == USE_OPAQUE && (y >= fit) != fromPlaneTop) {
//				redColor = kPaleRed;
//				blackColor = Color.gray;
//			}
			ModelGraphics.drawPointHandle(g, crossPos, doingDrag, redColor, blackColor);
		}
		
		g.setColor(Color.black);
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			double fit = model.evaluateMean(xVals);
			
			g.setColor((!(shadeHandling == USE_OPAQUE) || (y >= fit) == fromPlaneTop) ? 
						((index == selectedIndex) ? Color.red : Color.black)
						 : ((index == selectedIndex) ? kPaleRed : Color.gray));
			
			drawCross(g, crossPos);
			index ++;
		}
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------

	static final private int kMinCrossHitDistance = 16;
	
	private boolean doingDrag = false;
	private int yDragOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected void findScreenPositions(Point[] crossPos) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		int noOfVals = yVar.noOfValues();
		for (int i=0 ; i<noOfVals ; i++)
			crossPos[i] = getScreenPoint(xe[0].nextDouble(), ye.nextDouble(),
																	xe[1].nextDouble(), crossPos[i]);
	}
	
	protected int distance(int x, int y, Point crossPos) {
		int xDist = crossPos.x - x;
		int yDist = crossPos.y - y;
		return xDist * xDist + yDist * yDist;
	}
	
	protected int minHitDistance() {
		return kMinCrossHitDistance;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			Point p = getScreenPoint(((NumVariable)getVariable(explanKey[0])).doubleValueAt(selectedIndex),
								((NumVariable)getVariable(yKey)).doubleValueAt(selectedIndex),
								((NumVariable)getVariable(explanKey[1])).doubleValueAt(selectedIndex), null);
			int xDist = p.x - x;
			int yDist = p.y - y;
			if (xDist * xDist + yDist * yDist <= kMinCrossHitDistance)
				return new VertDragPosInfo(y, selectedIndex, yDist);
		}
		
		return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (doingDrag)
			return new VertDragPosInfo(y, 0, 0);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null || startInfo instanceof VertDragPosInfo) {
			VertDragPosInfo dragInfo = (VertDragPosInfo)startInfo;
			yDragOffset = dragInfo.hitOffset;
			
			setArrowCursor();
			doingDrag = true;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (doingDrag) {
			VertDragPosInfo dragInfo = (VertDragPosInfo)toPos;
			int newY = dragInfo.y - yDragOffset;
			Point p = translateToScreen(0, newY, null);
			
			try {
				int selectedIndex = getSelection().findSingleSetFlag();
				double xFract = xAxis.numValToPosition(((NumVariable)getVariable(explanKey[0])).doubleValueAt(selectedIndex));
				double zFract = zAxis.numValToPosition(((NumVariable)getVariable(explanKey[1])).doubleValueAt(selectedIndex));
				double y = Math.min(1.0, Math.max(0.0, map.mapToD(p, xFract, zFract)));
				
				double yMin = yAxis.getMinOnAxis();
				double yMax = yAxis.getMaxOnAxis();
				double yVal = yMin + y * (yMax - yMin);
				
				NumVariable yVar = (NumVariable)getVariable(yKey);
				((NumValue)yVar.valueAt(selectedIndex)).setValue(yVal);
				MultipleRegnModel ls = (MultipleRegnModel)getVariable(modelKey);
				ls.updateLSParams(yKey);
				getData().variableChanged(modelKey, selectedIndex);
			} catch (AxisException e) {
			}
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (doingDrag) {
			doingDrag = false;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
