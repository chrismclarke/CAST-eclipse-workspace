package logistic;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class DragLogisticView extends BarNumView {
	
	static final private int NO_SELECTED_HANDLE = -1;
	
	protected int selectedHandle = NO_SELECTED_HANDLE;
	protected int hitHandle = NO_SELECTED_HANDLE;
	
	public DragLogisticView(DataSet theData, XApplet applet,
						VertAxis yAxis, HorizAxis xAxis, String xKey, String yKey, String modelKey) {
		super(theData, applet, yAxis, xAxis, xKey, yKey, modelKey);
		setShowPrediction(false, 0.0);
	}
	
	protected void drawHandles(Graphics g, LinearModel theModel) {
		Point handles[] = theModel.getHandles(this, horizAxis, vertAxis);
		for (int i=0 ; i<handles.length ; i++)
			ModelGraphics.drawHandle(g, handles[i], selectedHandle == i);
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
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= vertAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + vertAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		LinearModel model = (LinearModel)getVariable(modelKey);
		Point handles[] = model.getHandles(this, horizAxis, vertAxis);
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
				double newHandleValue = vertAxis.positionToNumVal(newYPos);
				LinearModel model = (LinearModel)getVariable(modelKey);
				model.setHandle(selectedHandle, newHandleValue, horizAxis);
				getData().variableChanged(modelKey);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedHandle = NO_SELECTED_HANDLE;
		repaint();
	}
}
	
