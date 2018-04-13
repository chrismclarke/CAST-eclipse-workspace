package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;

import regnProg.*;


public class DragLineView extends ScatterView {
	
	static final private int NO_SELECTED_HANDLE = -1;
	static final private int kXHitSlop = 30;
	
	private XApplet applet;
	protected String lineKey;
	protected int selectedHandle = NO_SELECTED_HANDLE;
	protected int hitHandle = NO_SELECTED_HANDLE;
	
	private boolean drawResiduals = true;
	
	private Color residualColor = Color.blue;
	
	public DragLineView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																									String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		this.applet = applet;
	}
	
	public void setDrawResiduals(boolean drawResiduals) {
		this.drawResiduals = drawResiduals;
		repaint();
	}
	
	public void setResidualColor(Color residualColor) {
		this.residualColor = residualColor;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		super.paintView(g);
	}
	
	protected Point getFittedPoint(NumValue x, LinearModel model, Point thePoint) {
		double y = model.evaluateMean(x.toDouble());
		try {
			int vertPos = yAxis.numValToPosition(y);
			int horizPos = axis.numValToPosition(x.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected void drawBackground(Graphics g) {
		NumVariable xVariable = getNumVariable();
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		if (drawResiduals) {
			g.setColor(residualColor);
			ValueEnumeration xe = xVariable.values();
			Point dataPoint = null;
			Point fittedPoint = null;
			int index = 0;
			while (xe.hasMoreValues()) {
				NumValue x = (NumValue)xe.nextValue();
				dataPoint = getScreenPoint(index, x, dataPoint);
				fittedPoint = getFittedPoint(x, model, fittedPoint);
				
				if (dataPoint != null && fittedPoint != null)
					g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, fittedPoint.y);
				index++;
			}
		}
		
		Point handles[] = model.getHandles(this, axis, yAxis);
		for (int i=0 ; i<handles.length ; i++)
			ModelGraphics.drawHandle(g, handles[i], selectedHandle == i);
		
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
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
		Point handles[] = model.getHandles(this, axis, yAxis);
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
		if (applet instanceof ErrorSdEstApplet)
			((ErrorSdEstApplet)applet).hideErrorSdEst();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedHandle = NO_SELECTED_HANDLE;
			repaint();
		}
		else {
			int selectedIndex = getData().getSelection().findSingleSetFlag();
			selectedHandle = hitHandle;
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newHandleValue = yAxis.positionToNumVal(newYPos);
				LinearModel model = (LinearModel)getVariable(lineKey);
				model.setHandle(selectedHandle, newHandleValue, axis);
				getData().variableChanged(lineKey, selectedIndex);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedHandle = NO_SELECTED_HANDLE;
		repaint();
	}
}
	
