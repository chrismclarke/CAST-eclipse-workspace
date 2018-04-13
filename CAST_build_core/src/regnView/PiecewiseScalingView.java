package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class PiecewiseScalingView extends DataView {
//	static public final String PIECE_SCALING_PLOT = "pieceScaling";
	
	static final private int kArrowSize = 5;
	
	private HorizAxis xAxis;
	private VertAxis yAxis;
	private String xKey, lineKey;
	
	public PiecewiseScalingView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																																String xKey, String lineKey) {
		super(theData, applet, new Insets(5,5,5,5));
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xKey = xKey;
		this.lineKey = lineKey;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		int selectedIndex = getData().getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			g.setColor(Color.red);
			PiecewiseLinearModel model = (PiecewiseLinearModel)getData().getVariable(lineKey);
			
			NumVariable xVar = (NumVariable)getData().getVariable(xKey);
			NumValue x = (NumValue)xVar.valueAt(selectedIndex);
			
			double y = model.evaluateMean(x);
			try {
				int vertPos = yAxis.numValToPosition(y);
				int horizPos = xAxis.numValToPosition(x.toDouble());
				Point p = translateToScreen(horizPos, vertPos, null);
				
				g.drawLine(0, p.y, p.x, p.y);
				g.drawLine(0, p.y, kArrowSize, p.y + kArrowSize);
				g.drawLine(0, p.y, kArrowSize, p.y - kArrowSize);
				g.drawLine(p.x, p.y, p.x, getSize().height);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void drawBackground(Graphics g) {
//		NumVariable xVariable = getNumVariable();
		PiecewiseLinearModel model = (PiecewiseLinearModel)getVariable(lineKey);
		
		Point handle = model.getHandle(this, xAxis, yAxis);
		PiecewiseLinearModel.drawBiHandle(g, handle, doingDrag);
		
		g.setColor(Color.gray);
		model.drawMean(g, this, xAxis, yAxis);
	}

//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 20;
	private int xOffset, yOffset;
	private boolean doingDrag = false;
	private Point pTemp = null;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		PiecewiseLinearModel model = (PiecewiseLinearModel)getVariable(lineKey);
		Point pTemp = model.getHandle(this, xAxis, yAxis);
		if (pTemp != null) {
			int xDist = pTemp.x - x;
			int yDist = pTemp.y - y;
			int dist = xDist*xDist + yDist*yDist;
			if (dist <= kMinHitDist)
				return new DragPosInfo(x - pTemp.x, y - pTemp.y);
		}
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return new DragPosInfo(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null && (startInfo instanceof DragPosInfo)) {
			DragPosInfo localInfo = (DragPosInfo)startInfo;
			doingDrag = true;
			xOffset = localInfo.x;
			yOffset = localInfo.y;
			repaint();
		}
		return true;
	}
	
	private double findValue(NumCatAxis axis, int pos) {
		try {
			return axis.positionToNumVal(pos);
		}
		catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				return axis.minOnAxis;
			else
				return axis.maxOnAxis;
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null && (toPos instanceof DragPosInfo)) {
			DragPosInfo localInfo = (DragPosInfo)toPos;
			pTemp = translateFromScreen(localInfo.x - xOffset, localInfo.y - yOffset, pTemp);
			double newX = findValue(xAxis, pTemp.x);
			double newY = findValue(yAxis, pTemp.y);
			PiecewiseLinearModel model = (PiecewiseLinearModel)getVariable(lineKey);
			model.setHandle(newX, newY);
			int selectedIndex = getData().getSelection().findSingleSetFlag();
			getData().variableChanged(lineKey, selectedIndex);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
			doingDrag = false;
			repaint();
	}
}
	
