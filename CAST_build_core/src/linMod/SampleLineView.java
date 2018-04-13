package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class SampleLineView extends ScatterView {
//	static public final String SAMPLE_LINE_PLOT = "sampleLinePlot";
	
	static final private Color kLightGray = new Color(0xDDDDDD);
	
	private boolean showModel = true;
	private boolean showData = false;
	private boolean showResiduals = false;
	
	protected String modelKey;
	
	public SampleLineView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																						String xKey, String yKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.modelKey = modelKey;
	}
	
	public void setShowData(boolean showData) {
		this.showData = showData;
	}
	
	public void setShowModel(boolean showModel) {
		this.showModel = showModel;
	}
	
	public void setShowResiduals(boolean showResiduals) {
		this.showResiduals = showResiduals;
	}
	
	protected Point getScreenPos(double x, double y) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	private void drawModel(Graphics g, double lowX, double highX) {
		g.setColor(kLightGray);
		
		LinearModel model = (LinearModel)getData().getVariable(modelKey);
		
		double yOffset = 2.0 * model.evaluateSD().toDouble();
		
		double lowY1 = model.evaluateMean(new NumValue(lowX)) + yOffset;
		double highY1 = model.evaluateMean(new NumValue(highX)) + yOffset;
		
		Point startPos1 = getScreenPos(lowX, lowY1);
		Point endPos1 = getScreenPos(highX, highY1);
		
		double lowY2 = model.evaluateMean(new NumValue(lowX)) - yOffset;
		double highY2 = model.evaluateMean(new NumValue(highX)) - yOffset;
		
		Point startPos2 = getScreenPos(lowX, lowY2);
		Point endPos2 = getScreenPos(highX, highY2);
		
		int x[] = {startPos1.x, endPos1.x, endPos2.x, startPos2.x};
		int y[] = {startPos1.y, endPos1.y, endPos2.y, startPos2.y};
		
		g.fillPolygon(x, y, 4);
		g.drawLine(startPos1.x, startPos1.y, endPos1.x, endPos1.y);
		g.drawLine(startPos2.x, startPos2.y, endPos2.x, endPos2.y);
		
		g.setColor(Color.gray);
		model.drawMean(g, this, axis, yAxis);
		g.setColor(getForeground());
	}
	
	protected void drawLSLine(Graphics g, double lowX, double highX) {
		g.setColor(Color.blue);
		
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		
		double slope = lse.getSlope();
		double intercept = lse.getIntercept();
		
		double lowY = intercept + slope * lowX;
		double highY = intercept + slope * highX;
		
		Point startPos = getScreenPos(lowX, lowY);
		Point endPos = getScreenPos(highX, highY);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
		
		g.setColor(getForeground());
	}
	
	private void drawResiduals(Graphics g) {
		g.setColor(Color.red);
		
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		
		double slope = lse.getSlope();
		double intercept = lse.getIntercept();
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		Point p0 = null;
		Point p1 = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			double y = ye.nextDouble();
			double fit = intercept + slope * x;
			try {
				int xPos = axis.numValToPosition(x);
				int yPos = yAxis.numValToPosition(y);
				int fitPos = yAxis.numValToPosition(fit);
				p0 = translateToScreen(xPos, yPos, p0);
				p1 = translateToScreen(xPos, fitPos, p1);
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			} catch (AxisException ex) {
			}
		}
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		double lowX = axis.minOnAxis;
		double highX = axis.maxOnAxis;
		double xBorder = (highX - lowX) * 0.1;
		lowX -= xBorder;
		highX += xBorder;
		
		if (showModel)
			drawModel(g, lowX, highX);
		
		if (showData) {
			drawLSLine(g, lowX, highX);
			if (showResiduals)
				drawResiduals(g);
			super.paintView(g);
		}
	}
}
	
