package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import distn.*;

import linMod.*;


public class TransPIView extends ScatterView {
//	static public final String TRANS_PI_PLOT = "transPIPlot";
	
	static final private Color kPaleGray = new Color(0xCCCCCC);
	static final private Color kPink = new Color(0xFF99CC);
	
	static final public Color kIntervalColor = new Color(0x990033);
	
	private XValueSlider xSlider;
	
	private boolean initialised = false;
	private boolean showCI = true;
	
	private double slope, intercept;
	private double xMean, sxx, errorSD;
	private int n;
	
	private int[] xCICoord, yCICoord, xPICoord, yPICoord, xMeanCoord, yMeanCoord;
	
	private NumCatAxis xCalcAxis, yCalcAxis;
	
	public TransPIView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
																								String xKey, String yKey, XValueSlider xSlider) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.xSlider = xSlider;
		xCalcAxis = xAxis;
		yCalcAxis = yAxis;
	}
	
	public void setCalcAxes(HorizAxis xCalcAxis, VertAxis yCalcAxis) {
		this.xCalcAxis = xCalcAxis;
		this.yCalcAxis = yCalcAxis;
	}
	
	public void setShowCI(boolean showCI) {
		this.showCI = showCI;
	}
	
	private void doLS(DataSet theData, String xKey, String yKey) {
		n=0;
		double sx = 0.0;
		sxx = 0.0;
		double sy = 0.0;
		double syy = 0.0;
		double sxy = 0.0;
		
		NumVariable y = (NumVariable)theData.getVariable(yKey);
		NumVariable x = (NumVariable)theData.getVariable(xKey);
		ValueEnumeration ye = y.values();
		ValueEnumeration xe = x.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double yv = yCalcAxis.transform(ye.nextDouble());
			double xv = xCalcAxis.transform(xe.nextDouble());
			if (!Double.isNaN(yv) && !Double.isNaN(xv)) {
				sx += xv;
				sxx += xv * xv;
				sy += yv;
				syy += yv * yv;
				sxy += xv * yv;
				n ++;
			}
		}
		
		xMean = sx / n;
		
		sxx -= sx * xMean;
		sxy -= xMean * sy;
		syy -= sy * sy / n;
		
		slope = sxy / sxx;
		intercept = (sy - sx * slope) / n;
		
		double residSsq = syy - sxy * slope;
		errorSD = Math.sqrt(residSsq / (n - 2));
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		doLS(getData(), xKey, yKey);
		
		TransPIFinder meanFinder = new TransPIFinder(intercept, slope, 0.0, 1.0,
														xMean, sxx, this, axis, yAxis, xCalcAxis, yCalcAxis);
		xMeanCoord = meanFinder.getXCoords();
		yMeanCoord = meanFinder.getYCoords();
		
		double ts = errorSD * TTable.quantile(0.975, n - 2);
		double nInv = 1.0 / n;
		
		if (showCI) {
			TransPIFinder ciFinder = new TransPIFinder(intercept, slope, ts, nInv,
														xMean, sxx, this, axis, yAxis, xCalcAxis, yCalcAxis);
			int[] ciTopX = ciFinder.getXCoords();
			int[] ciTopY = ciFinder.getYCoords();
			
			ciFinder = new TransPIFinder(intercept, slope, -ts, nInv,
														xMean, sxx, this, axis, yAxis, xCalcAxis, yCalcAxis);
			int[] ciBottomX = ciFinder.getXCoords();
			int[] ciBottomY = ciFinder.getYCoords();
			
			xCICoord = new int[ciTopX.length + ciBottomX.length];
			yCICoord = new int[ciTopY.length + ciBottomY.length];
			
			for (int i=0 ; i<ciTopX.length ; i++) {
				xCICoord[i] = ciTopX[i];
				yCICoord[i] = ciTopY[i];
			}
			for (int i=0 ; i<ciBottomX.length ; i++) {
				xCICoord[ciTopX.length + i] = ciBottomX[ciBottomX.length - i - 1];
				yCICoord[ciTopY.length + i] = ciBottomY[ciBottomY.length - i - 1] + 1;
										//		add 1 since bottom of fill is closer to LS line than top
			}
		}
		
		nInv += 1.0;
		
		TransPIFinder piFinder = new TransPIFinder(intercept, slope, ts, nInv,
														xMean, sxx, this, axis, yAxis, xCalcAxis, yCalcAxis);
		int[] piTopX = piFinder.getXCoords();
		int[] piTopY = piFinder.getYCoords();
		
		piFinder = new TransPIFinder(intercept, slope, -ts, nInv,
														xMean, sxx, this, axis, yAxis, xCalcAxis, yCalcAxis);
		int[] piBottomX = piFinder.getXCoords();
		int[] piBottomY = piFinder.getYCoords();
		
		xPICoord = new int[piTopX.length + piBottomX.length];
		yPICoord = new int[piTopY.length + piBottomY.length];
		
		for (int i=0 ; i<piTopX.length ; i++) {
			xPICoord[i] = piTopX[i];
			yPICoord[i] = piTopY[i];
		}
		for (int i=0 ; i<piBottomX.length ; i++) {
			xPICoord[piTopX.length + i] = piBottomX[piBottomX.length - i - 1];
			yPICoord[piTopY.length + i] = piBottomY[piBottomY.length - i - 1] + 1;
									//		add 1 since bottom of fill is closer to LS line than top
		}
		
		initialised = true;
		return true;
	}
	
	protected Point getScreenPos(double x, double y) {
		int vertPos = yAxis.numTransValToRawPosition(y);
		int horizPos = axis.numTransValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	protected void drawLSLine(Graphics g, double lowX, double highX) {
		g.setColor(Color.blue);
		
		for (int i=1 ; i<xMeanCoord.length ; i++)
			g.drawLine(xMeanCoord[i-1], yMeanCoord[i-1], xMeanCoord[i], yMeanCoord[i]);
		
		g.setColor(getForeground());
	}
	
	public double getPredictionBound(boolean highNotLow) {
		double x = xCalcAxis.transform(xSlider.getNumValue().toDouble());
		double mean = intercept + slope * x;
		double plusMinus = errorSD * TTable.quantile(0.975, n - 2)
											* Math.sqrt(1.0 + 1.0 / n + (x - xMean) * (x - xMean) / sxx);
		return yCalcAxis.inverseTransform(mean + (highNotLow ? plusMinus : -plusMinus));
	}
	
	private void drawInterval(Graphics g) {
		g.setColor(kIntervalColor);
		
		double x = xCalcAxis.transform(xSlider.getNumValue().toDouble());
		double mean = intercept + slope * x;
		double plusMinus = errorSD * TTable.quantile(0.975, n - 2)
											* Math.sqrt(1.0 + 1.0 / n + (x - xMean) * (x - xMean) / sxx);
		
		double yHighTrans = mean + plusMinus;
		double yLowTrans = mean - plusMinus;
		if (yCalcAxis != yAxis) {
			yHighTrans = yAxis.transform(yCalcAxis.inverseTransform(yHighTrans));
			yLowTrans = yAxis.transform(yCalcAxis.inverseTransform(yLowTrans));
		}
		
		double xDraw = axis.transform(xSlider.getNumValue().toDouble());
		
		Point startPos = getScreenPos(xDraw, yHighTrans);
		Point endPos = getScreenPos(xDraw, yLowTrans);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
		
		g.drawLine(startPos.x, startPos.y, 0, startPos.y);
		g.drawLine(0, startPos.y, 3, startPos.y - 3);
		g.drawLine(0, startPos.y, 3, startPos.y + 3);
		
		g.drawLine(endPos.x, endPos.y, 0, endPos.y);
		g.drawLine(0, endPos.y, 3, endPos.y - 3);
		g.drawLine(0, endPos.y, 3, endPos.y + 3);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		g.setColor(kPink);
		g.fillPolygon(xPICoord, yPICoord, xPICoord.length);
		
		if (showCI) {
			g.setColor(kPaleGray);
			g.fillPolygon(xCICoord, yCICoord, xCICoord.length);
		}
		
		if (xSlider != null)
			drawInterval(g);
		
		double lowX = axis.transform(axis.minOnAxis);
		double highX = axis.transform(axis.maxOnAxis);
		double xSlop = (highX - lowX) * 0.05;
		lowX -= xSlop;
		highX += xSlop;
		
		drawLSLine(g, lowX, highX);
		super.paintView(g);
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		initialised = false;
		repaint();
	}
}
	
