package regnView;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;

import regn.*;


public class DragOnCurveView extends DragLocationView {
//	static public final String DRAG_ON_CURVE_PLOT = "dragOnCurve";
	
//	static final private NumValue kNaNValue = new NumValue("?");
	
	private String xKey, yKey, lineKey;
	private VertAxis yAxis;
	
	private Color lineColor = Color.lightGray;
	
	boolean finishedSetup = false;
	
	private int selectedIndex, hitOffset;
	
	public DragOnCurveView(DataSet theData, XApplet applet, DragValAxis xAxis,
												VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis);
		this.xKey = xKey;
		this.yKey = yKey;
		this.lineKey = lineKey;
		this.yAxis = yAxis;
	}
	
	public void setLineColor(Color newColor) {
		lineColor = newColor;
	}
	
	public void setFinishedSetup(boolean finishedSetup) {
		this.finishedSetup = finishedSetup;
		DragValAxis xAxis = (DragValAxis)axis;
		xAxis.setAllowDrag(finishedSetup);
		xAxis.repaint();
		yAxis.repaint();
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = axis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point thePoint = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			thePoint = getScreenPoint(xVal, yVal, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
		
		if (yAxis instanceof PredictionAxis) {
			((PredictionAxis)yAxis).checkPrediction();
			yAxis.repaint();
		}
	}
	
	private void drawBackground(Graphics g) {
		SmoothQuadModel model = (SmoothQuadModel)getVariable(lineKey);
		DragValAxis xAxis = (DragValAxis)axis;
		
		g.setColor(lineColor);
		model.drawMean(g, this, xAxis, yAxis);
		
		NumValue constValue = xAxis.getAxisVal();
		
		if (constValue == null)
			model.drawHandles(g, this, xAxis, yAxis, selectedIndex);
		else
			try {
				int xPos = xAxis.numValToPosition(constValue.toDouble());
				double prediction = model.evaluateMean(constValue.toDouble());
				int yPos = yAxis.numValToPosition(prediction);
				Point linePt = translateToScreen(xPos, yPos, null);
				
				if (selectedVal) {
					g.setColor(Color.yellow);
					g.fillRect(linePt.x - 2, 0, 5, getSize().height);
				}
				
				g.setColor(Color.red);
				g.drawLine(linePt.x, 0, linePt.x, getSize().height - 1);
				
				g.setColor(Color.blue);
				g.drawLine(linePt.x, linePt.y, 0, linePt.y);
				g.drawLine(0, linePt.y, 3, linePt.y + 3);
				g.drawLine(0, linePt.y, 3, linePt.y - 3);
			} catch (AxisException e) {
			}
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (((DragValAxis)axis).getAxisVal() != null)
			return super.getInitialPosition(x, y);
		
		SmoothQuadModel model = (SmoothQuadModel)getVariable(lineKey);
		Point p = translateFromScreen(x, y, null);
		return model.getSelectedHandle(p, axis, yAxis);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (((DragValAxis)axis).getAxisVal() != null)
			return super.getPosition(x, y);
			
		if (x < 0 || y < -30 || x >= getSize().width || y >= getSize().height + 30)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		hitPos.y -= hitOffset;
		if (hitPos.y < 0 || hitPos.y >= yAxis.getAxisLength())
			return null;
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (((DragValAxis)axis).getAxisVal() != null)
			return super.startDrag(startPos);
			
		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		hitOffset = dragPos.hitOffset;
		selectedIndex = dragPos.index;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (((DragValAxis)axis).getAxisVal() != null) {
			super.doDrag(fromPos, toPos);
			return;
		}
		
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			try {
				double newHandleValue = yAxis.positionToNumVal(dragPos.y);
				SmoothQuadModel model = (SmoothQuadModel)getVariable(lineKey);
				model.setHandle(selectedIndex, newHandleValue);
				repaint();
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (((DragValAxis)axis).getAxisVal() != null) {
			super.endDrag(startPos, endPos);
			return;
		}
		
		selectedIndex = -1;
		repaint();
	}

	public void keyPressed(KeyEvent e) {
		if (((DragValAxis)axis).getAxisVal() != null)
			super.keyPressed(e);
	}
	
}
	
