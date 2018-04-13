package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class TransResidView extends ScatterView {
//	static public final String TRANS_RESID_PLOT = "transResidPlot";
	
	protected String lineKey;
	
	public TransResidView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			double x = getNumVariable().doubleValueAt(index);
			LinearModel model = (LinearModel)getVariable(lineKey);
			double yHat = model.evaluateMean(x);
			if (Double.isNaN(yHat) || Double.isInfinite(yHat))
				return;
			
			try {
				int vertPos = yAxis.numValToPosition(yHat);
				int horizPos = axis.numValToPosition(x);
				Point fittedPoint = translateToScreen(horizPos, vertPos, null);
				
				Color oldColor = g.getColor();
				g.setColor(Color.red);
				g.drawLine(fittedPoint.x, fittedPoint.y, fittedPoint.x, thePoint.y);
				g.setColor(oldColor);
			} catch (AxisException e) {
			}
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		super.paintView(g);
	}
	
	protected void drawBackground(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
	}
		
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(xKey) || key.equals(yKey))
			repaint();
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

//-----------------------------------------------------------------------------------
	
	private PositionInfo previousPosition = null;
	
	protected boolean startDrag(PositionInfo startInfo) {
		previousPosition = startInfo;
		return super.startDrag(startInfo);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
			
		PositionInfo posInfo = super.getPosition(x, y);
		if (posInfo == null)
			return previousPosition;
		else {
			previousPosition = posInfo;
			return posInfo;
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		super.endDrag(startPos, endPos);
		previousPosition = null;
	}
}
	
