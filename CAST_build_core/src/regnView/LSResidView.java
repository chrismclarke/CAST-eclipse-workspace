package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class LSResidView extends ScatterView {
//	static public final String LS_RESID_PLOT = "lsResidPlot";
	
	protected String lineKey;
	private boolean visibleResids;
	
	public LSResidView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.lineKey = lineKey;
	}
	
	public void setVisibleResids(boolean visibleResids) {
		this.visibleResids = visibleResids;
		repaint();
	}
	
	public void paintView(Graphics g) {
		if (visibleResids) {
			drawBackground(g);
			g.setColor(getForeground());
		}
		
		super.paintView(g);
	}
	
	private Point getScreenPoint(double x, double y, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(y);
			int horizPos = axis.numValToPosition(x);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected void drawBackground(Graphics g) {
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		LinearModel model = (LinearModel)getVariable(lineKey);
		
		g.setColor(Color.red);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point dataPoint = null;
		Point fittedPoint = null;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			double fit = model.evaluateMean(x);
			dataPoint = getScreenPoint(x, y, dataPoint);
			fittedPoint = getScreenPoint(x, fit, fittedPoint);
			
			if (dataPoint != null && fittedPoint != null)
				g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, fittedPoint.y);
		}
		
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
	}
}
	
