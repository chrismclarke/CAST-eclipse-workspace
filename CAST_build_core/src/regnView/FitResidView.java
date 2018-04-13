package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class FitResidView extends ScatterView {
	
	protected String lineKey;
	static final public Color darkGreen = new Color(0x006600);
	
	public FitResidView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
		setRetainLastSelection(true);
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			double x = getNumVariable().doubleValueAt(index);
			LinearModel model = (LinearModel)getVariable(lineKey);
			double yHat = model.evaluateMean(x);
			
			try {
				int vertPos = yAxis.numValToPosition(yHat);
				int horizPos = axis.numValToPosition(x);
				Point fittedPoint = translateToScreen(horizPos, vertPos, null);
				
				Color oldColor = g.getColor();
				g.setColor(darkGreen);
				g.drawLine(0, fittedPoint.y, fittedPoint.x, fittedPoint.y);
				g.drawLine(0, fittedPoint.y, 3, fittedPoint.y + 3);
				g.drawLine(0, fittedPoint.y, 3, fittedPoint.y - 3);
				g.setColor(Color.blue);
				g.drawLine(0, thePoint.y, thePoint.x, thePoint.y);
				g.drawLine(0, thePoint.y, 3, thePoint.y + 3);
				g.drawLine(0, thePoint.y, 3, thePoint.y - 3);
				
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
	
	private void drawBackground(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
	}
	
//-----------------------------------------------------------------------------------
	
	private PositionInfo previousPosition = null;
	
	protected boolean startDrag(PositionInfo startInfo) {
		previousPosition = startInfo;
		return super.startDrag(startInfo);
	}
	
	protected PositionInfo getPosition(int x, int y) {
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
	
