package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import distn.*;


public class PredictionIntervalView extends ScatterView {
//	static public final String PREDICTION_INTERVAL_PLOT = "predictionIntervalPlot";
	
	static final private Color kPaleGray = new Color(0xCCCCCC);
	static final private Color kPink = new Color(0xFF99CC);
	
	private boolean initialised = false;
	private XValueSlider xSlider;
	
	private boolean showCI = true;
	
	private double slope, intercept;
	private double xMean, sxx, errorSD;
	private int n;
	
	private int[] xCICoord, yCICoord, xPICoord, yPICoord;
	
	public PredictionIntervalView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, XValueSlider xSlider) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
		this.xSlider = xSlider;
	}
	
	public void setShowCI(boolean showCI) {
		this.showCI = showCI;
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		
		slope = lse.getSlope();
		intercept = lse.getIntercept();
		sxx = lse.getSxx();
		xMean = lse.getXMean();
		n = lse.getN();
		errorSD = lse.getErrorSD();
		
		double ts = errorSD * TTable.quantile(0.975, n - 2);
		double nInv = 1.0 / n;
		
		if (showCI) {
			PredictionBoundFinder ciFinder = new PredictionBoundFinder(intercept, slope, ts, nInv,
																						xMean, sxx, this, axis, yAxis);
			int[] ciTopX = ciFinder.getXCoords();
			int[] ciTopY = ciFinder.getYCoords();
			
			ciFinder = new PredictionBoundFinder(intercept, slope, -ts, nInv,
																						xMean, sxx, this, axis, yAxis);
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
		PredictionBoundFinder ciFinder = new PredictionBoundFinder(intercept, slope, ts, nInv,
																					xMean, sxx, this, axis, yAxis);
		int[] ciTopX = ciFinder.getXCoords();
		int[] ciTopY = ciFinder.getYCoords();
		
		ciFinder = new PredictionBoundFinder(intercept, slope, -ts, nInv,
																					xMean, sxx, this, axis, yAxis);
		int[] ciBottomX = ciFinder.getXCoords();
		int[] ciBottomY = ciFinder.getYCoords();
		
		xPICoord = new int[ciTopX.length + ciBottomX.length];
		yPICoord = new int[ciTopY.length + ciBottomY.length];
		
		for (int i=0 ; i<ciTopX.length ; i++) {
			xPICoord[i] = ciTopX[i];
			yPICoord[i] = ciTopY[i];
		}
		for (int i=0 ; i<ciBottomX.length ; i++) {
			xPICoord[ciTopX.length + i] = ciBottomX[ciBottomX.length - i - 1];
			yPICoord[ciTopY.length + i] = ciBottomY[ciBottomY.length - i - 1] + 1;
									//		add 1 since bottom of fill is closer to LS line than top
		}
		
		initialised = true;
		return true;
	}
	
	protected Point getScreenPos(double x, double y) {
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = axis.numValToRawPosition(x);
		return translateToScreen(horizPos, vertPos, null);
	}
	
	protected void drawLSLine(Graphics g, double lowX, double highX) {
		g.setColor(Color.blue);
		
		double lowY = intercept + slope * lowX;
		double highY = intercept + slope * highX;
		
		Point startPos = getScreenPos(lowX, lowY);
		Point endPos = getScreenPos(highX, highY);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
		
		g.setColor(getForeground());
	}
	
	private void drawInterval(Graphics g) {
		g.setColor(Color.red);
		
		double x = xSlider.getNumValue().toDouble();
		double mean = intercept + slope * x;
		double plusMinus = errorSD * TTable.quantile(0.975, n - 2)
											* Math.sqrt(1.0 + 1.0 / n + (x - xMean) * (x - xMean) / sxx);
		
		Point startPos = getScreenPos(x, mean + plusMinus);
		Point endPos = getScreenPos(x, mean - plusMinus);
		g.drawLine(startPos.x, startPos.y, endPos.x, endPos.y);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		g.setColor(kPink);
		g.fillPolygon(xPICoord, yPICoord, xPICoord.length);
		
		if (showCI) {
			g.setColor(kPaleGray);
			g.fillPolygon(xCICoord, yCICoord, xCICoord.length);
		}
		
		drawInterval(g);
		
		double lowX = axis.minOnAxis;
		double highX = axis.maxOnAxis;
		double xSlop = (highX - lowX) * 0.05;
		lowX -= xSlop;
		highX += xSlop;
		
		drawLSLine(g, lowX, highX);
		super.paintView(g);
	}
}
	
