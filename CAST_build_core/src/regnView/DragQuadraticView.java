package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class DragQuadraticView extends DataView {
	
	static final private int NO_SELECTED_HANDLE = -1;
	private int selectedHandle = NO_SELECTED_HANDLE;
	protected int hitHandle = NO_SELECTED_HANDLE;
	
	static final private int kXHitSlop = 30;
	
	private String xKey, yKey, lineKey;
	private HorizAxis xAxis;
	private VertAxis yAxis;
	
	private boolean drawResiduals = false;
	
	public DragQuadraticView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.lineKey = lineKey;
		this.xKey = xKey;
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public void setDrawResiduals(boolean drawResiduals) {
		this.drawResiduals = drawResiduals;
		repaint();
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private Point getFittedPoint(NumValue x, LinearModel model, Point thePoint) {
		double y = model.evaluateMean(x.toDouble());
//		try {
			int vertPos = yAxis.numValToRawPosition(y);
			int horizPos = xAxis.numValToRawPosition(x.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
//		} catch (AxisException e) {
//			return null;
//		}
	}
	
	public void paintView(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		if (drawResiduals && xKey != null && yKey != null) {
			g.setColor(Color.blue);
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			Point dataPoint = null;
			Point fittedPoint = null;
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				NumValue xVal = (NumValue)xe.nextValue();
				NumValue yVal = (NumValue)ye.nextValue();
				dataPoint = getScreenPoint(xVal, yVal, dataPoint);
				fittedPoint = getFittedPoint(xVal, model, fittedPoint);
				
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, fittedPoint.y);
			}
		}
		
		Point handles[] = model.getHandles(this, xAxis, yAxis);
		for (int i=0 ; i<handles.length ; i++)
			ModelGraphics.drawHandle(g, handles[i], selectedHandle == i);
		
		g.setColor(Color.gray);
		model.drawMean(g, this, xAxis, yAxis);
		
		if (xKey != null && yKey != null) {
			g.setColor(getForeground());
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			Point thePoint = null;
			
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				NumValue xVal = (NumValue)xe.nextValue();
				NumValue yVal = (NumValue)ye.nextValue();
				thePoint = getScreenPoint(xVal, yVal, thePoint);
				if (thePoint != null)
					drawCross(g, thePoint);
			}
		}
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kXHitSlop || y < 0 || x >= getSize().width + kXHitSlop || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= yAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + yAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		LinearModel model = (LinearModel)getVariable(lineKey);
		Point handles[] = model.getHandles(this, xAxis, yAxis);
		for (int i=0 ; i<handles.length ; i++)
			if (Math.abs(handles[i].x - x) + Math.abs(handles[i].y - y) < kMinHitDistance)
				return new VertDragPosInfo(hitPos.y, i, y - handles[i].y);
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		hitOffset = dragPos.hitOffset;
		hitHandle = dragPos.index;
		selectedHandle = hitHandle;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedHandle = NO_SELECTED_HANDLE;
			repaint();
		}
		else {
			selectedHandle = hitHandle;
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newHandleValue = yAxis.positionToNumVal(newYPos);
				LinearModel model = (LinearModel)getVariable(lineKey);
				model.setHandle(selectedHandle, newHandleValue, xAxis);
				getData().variableChanged(lineKey);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedHandle = NO_SELECTED_HANDLE;
		repaint();
	}
}
	
