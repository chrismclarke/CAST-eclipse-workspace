package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class LineBoundsView extends ScatterView {
//	static public final String LINE_BOUNDS_PLOT = "lineBoundsPlot";
	
	static final private Color kPink = new Color(0xFF99CC);
	
	private double slope, intercept, errorSD;
	
	private int[] xBoundsCoord, yBoundsCoord;
	
	public LineBoundsView(DataSet data, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																															String xKey, String modelKey) {
		super(data, applet, xAxis, yAxis, xKey, null);
		LinearModel m = (LinearModel)data.getVariable(modelKey);
		intercept = m.getIntercept().toDouble();
		slope = m.getSlope().toDouble();
		errorSD = m.evaluateSD().toDouble();
	}
	
	private void drawBounds(Graphics g, double xMean, double sxx, int n) {
		double zs = errorSD * 1.96;
		double nInv = 1.0 / n;
		
		PredictionBoundFinder intervalFinder = new PredictionBoundFinder(intercept, slope, zs, nInv,
																					xMean, sxx, this, axis, yAxis);
		int[] intervalTopX = intervalFinder.getXCoords();
		int[] intervalTopY = intervalFinder.getYCoords();
		
		intervalFinder = new PredictionBoundFinder(intercept, slope, -zs, nInv,
																					xMean, sxx, this, axis, yAxis);
		int[] intervalBottomX = intervalFinder.getXCoords();
		int[] intervalBottomY = intervalFinder.getYCoords();
		
		int newPolyLength = intervalTopX.length + intervalBottomX.length;
		if (xBoundsCoord == null || xBoundsCoord.length != newPolyLength) {
			xBoundsCoord = new int[newPolyLength];
			yBoundsCoord = new int[newPolyLength];
		}
		
		for (int i=0 ; i<intervalTopX.length ; i++) {
			xBoundsCoord[i] = intervalTopX[i];
			yBoundsCoord[i] = intervalTopY[i];
		}
		for (int i=0 ; i<intervalBottomX.length ; i++) {
			xBoundsCoord[intervalTopX.length + i] = intervalBottomX[intervalBottomX.length - i - 1];
			yBoundsCoord[intervalTopY.length + i] = intervalBottomY[intervalBottomY.length - i - 1] + 1;
									//		add 1 since bottom of fill is closer to LS line than top
		}
		
		g.setColor(kPink);
		g.fillPolygon(xBoundsCoord, yBoundsCoord, xBoundsCoord.length);
	}
	
	private Point getScreenPos(double x, double y, Point p) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, p);
	}
	
	protected void drawMeanLine(Graphics g, double lowX, double highX) {
		g.setColor(Color.blue);
		
		double lowY = intercept + slope * lowX;
		double highY = intercept + slope * highX;
		
		Point startPos = getScreenPos(lowX, lowY, null);
		Point endPos = getScreenPos(highX, highY, null);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
	}
	
	public void paintView(Graphics g) {
		NumVariable xVar = getNumVariable();
		ValueEnumeration e = xVar.values();
		double sx = 0.0;
		double sxx = 0.0;
		int n = 0;
		boolean missingLast = false;
		while (e.hasMoreValues()) {
			double x = e.nextDouble();
			if (Double.isNaN(x))
				missingLast = true;
			else {
				sx += x;
				sxx += x * x;
				n ++;
			}
		}
		double xMean = sx / n;
		sxx -= sx * xMean;
		
		drawBounds(g, xMean, sxx, n);
		
		double lowX = axis.minOnAxis;
		double highX = axis.maxOnAxis;
		double xSlop = (highX - lowX) * 0.05;
		lowX -= xSlop;
		highX += xSlop;
		
		drawMeanLine(g, lowX, highX);
		
		g.setColor(Color.lightGray);
		Point p = null;
		e = xVar.values();
		while (e.hasMoreValues()) {
			double x = e.nextDouble();
			if (!Double.isNaN(x)) {
				n --;
				int horizPos = axis.numValToRawPosition(x);
				
				if (n == 0 && !missingLast) {
					double mean = intercept + slope * x;
					int topPos = yAxis.numValToRawPosition(mean + 2.0 * errorSD);
					int bottomPos = yAxis.numValToRawPosition(mean - 2.0 * errorSD);
					p = translateToScreen(horizPos, topPos, p);
					int topY = p.y;
					p = translateToScreen(horizPos, bottomPos, p);
					int bottomY = p.y;
					g.setColor(Color.red);
					
					g.drawLine(p.x - 1, topY + 1, p.x - 1, bottomY - 1);
					g.drawLine(p.x + 1, topY + 1, p.x + 1, bottomY - 1);
					for (int i=2 ; i<5 ; i++) {
						g.drawLine(p.x - i, topY + i, p.x + i, topY + i);
						g.drawLine(p.x - i, bottomY - i, p.x + i, bottomY - i);
					}
				}
				else
					p = translateToScreen(horizPos, 0, p);
				g.drawLine(p.x, 0, p.x, getSize().height);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean canDrag() {
		return false;
	}
}
	
