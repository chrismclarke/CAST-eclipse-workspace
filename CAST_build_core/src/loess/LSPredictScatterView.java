package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class LSPredictScatterView extends ScatterView {
//	static public final String LS_PREDICT_PLOT = "lsPredictPlot";
	
	private String lineKey, selectedXKey;
	
//	static private final int kHitSlop = 4;
	private boolean selectedVal = false;
	
	
	public LSPredictScatterView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																	String xKey, String yKey, String lineKey, String selectedXKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		this.selectedXKey = selectedXKey;
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawBackground(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
		g.setColor(getForeground());
		
		SelectionVariable selectedXVar = (SelectionVariable)getVariable(selectedXKey);
		double x = selectedXVar.getMinSelection();
		
		if (!Double.isNaN(x))
			try {
				int xPos = axis.numValToPosition(x);
				double prediction = model.evaluateMean(x);
				int yPos = yAxis.numValToPosition(prediction);
				Point linePt = translateToScreen(xPos, yPos, null);
				
				if (selectedVal) {
					g.setColor(Color.yellow);
					g.fillRect(linePt.x - 2, 0, 5, getSize().height);
				}
				g.setColor(Color.blue);
				g.drawLine(linePt.x, 0, linePt.x, getSize().height - 1);
				
				g.setColor(Color.red);
				g.drawLine(linePt.x, linePt.y, 0, linePt.y);
				g.drawLine(0, linePt.y, 3, linePt.y + 3);
				g.drawLine(0, linePt.y, 3, linePt.y - 3);
				g.setColor(getForeground());
			} catch (AxisException e) {
			}
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (theAxis == yAxis || theAxis == axis) {
			LinearModel model = (LinearModel)getVariable(lineKey);
			model.updateLSParams(yKey);
			getData().variableChanged(lineKey);
		}
		reinitialiseAfterTransform();
		repaint();
	}
	
//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x < 0 || hitPos.x >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		doDrag(null, startPos);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedVal = false;
			repaint();
		}
		else {
			selectedVal = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			
			try {
				double newX = axis.positionToNumVal(dragPos.x);
				getData().setSelection(selectedXKey, newX, newX);
//				repaint();
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedVal = false;
		repaint();
	}
}
	
