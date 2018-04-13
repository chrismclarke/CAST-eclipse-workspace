package regnView;

import java.awt.*;

import dataView.*;
import axis.*;

import regn.*;


public class ScaledResidPlotView extends TransResidView {
//	static public final String SCALED_RESID_PLOT = "scaledResidPlot";
	
	private double residSD = 0.0;
	private VertAxis originalYAxis;
	
	public ScaledResidPlotView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis residAxis, VertAxis originalYAxis, String xKey,
						String yKey, String lineKey) {
		super(theData, applet, xAxis, residAxis, xKey, yKey, lineKey);
		this.originalYAxis = originalYAxis;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		PowerLinearModel model = (PowerLinearModel)getVariable(lineKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		double y = yVariable.doubleValueAt(index);
		if (Double.isNaN(y) || Double.isNaN(y))
			return null;
		try {
			double x = theVal.toDouble();
			double yt = originalYAxis.transform(y);
			double yHatT = model.evaluateUntransformedMean(x);
			double sdResid = (yt - yHatT) / residSD;
			
			int vertPos = yAxis.numValToPosition(sdResid);
			int horizPos = axis.numValToPosition(theVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException e) {
			return null;
		}
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null) {
			double x = getNumVariable().doubleValueAt(index);
			
			try {
				int vertPos = yAxis.numValToPosition(0.0);
				int horizPos = axis.numValToPosition(x);
				Point zeroPoint = translateToScreen(horizPos, vertPos, null);
				
				Color oldColor = g.getColor();
				g.setColor(Color.red);
				g.drawLine(zeroPoint.x, zeroPoint.y, zeroPoint.x, thePoint.y);
				g.setColor(oldColor);
			} catch (AxisException e) {
			}
		}
	}
	
	private void setupSD() {
		NumVariable xVar = getNumVariable();
		ValueEnumeration xe = xVar.values();
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		PowerLinearModel model = (PowerLinearModel)getVariable(lineKey);
		
		double rss = 0.0;
		int n = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			double yt = originalYAxis.transform(y);
			double yHatT = model.evaluateUntransformedMean(x);
			double resid = yt - yHatT;
			
			rss += resid * resid;
			n ++;
		}
		
		residSD = Math.sqrt(rss / (n - 2));
	}
	
	public void paintView(Graphics g) {
		setupSD();
		
		super.paintView(g);
	}
	
	protected void drawBackground(Graphics g) {
		try {
			g.setColor(Color.gray);
			int zeroPos = yAxis.numValToPosition(0.0);
			Point p = translateToScreen(0, zeroPos, null);
			g.drawLine(0, p.y, getSize().width, p.y);
		} catch (AxisException e) {
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(lineKey))
			repaint();
		else
			super.doChangeVariable(g, key);
	}
}
	
