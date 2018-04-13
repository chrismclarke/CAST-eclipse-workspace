package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class PredictAndErrorView extends SampleLineView {
//	static public final String PREDICT_AND_ERROR_PLOT = "predictAndErrorPlot";
	
	static final public int PREDICT_MEAN = PredErrorSummaryVariable.PREDICT_MEAN;
	static final public int PREDICT_NEW_VALUE = PredErrorSummaryVariable.PREDICT_NEW_VALUE;
	
	static final private Color kXColor = new Color(0xBBBBBB);
	static final private Color kPredictColor = Color.blue;
	static final private Color kActualColor = new Color(0x009900);
	
	static final private int kArrowSize = 3;
	static final private int kTextVertGap = 2;
	static final private int kTextHorizGap = 4;
	
	private LabelValue kPredictionLabel, kActualNewLabel, kMeanEstLabel, kActualMeanLabel;
	
	private LabelValue kErrorLabel;
	
	private int predictMode = PREDICT_MEAN;
	private double xErrorDisplay;
	
	public PredictAndErrorView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
						String modelKey, int predictMode, double xErrorDisplay) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		kErrorLabel = new LabelValue(applet.translate("Error"));
		setShowData(true);
		this.predictMode = predictMode;
		this.xErrorDisplay = xErrorDisplay;
		
		if (predictMode == PREDICT_MEAN) {
			kMeanEstLabel = new LabelValue(applet.translate("Estimate of mean"));
			kActualMeanLabel = new LabelValue(applet.translate("Actual mean"));
		}
		else {
			kPredictionLabel = new LabelValue(applet.translate("Predicted new value"));
			kActualNewLabel = new LabelValue(applet.translate("Actual new value"));
		}
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		drawPrediction(g);
	}
	
	private void drawArrow(Point p, Color c, Graphics g) {
		g.setColor(c);
		g.drawLine(0, p.y, p.x, p.y);
		g.drawLine(0, p.y, kArrowSize, p.y - kArrowSize);
		g.drawLine(0, p.y, kArrowSize, p.y + kArrowSize);
	}
	
	private void drawLines(double x0, double yPred, double yActual,
														LabelValue predLabel, LabelValue actualLabel, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		Point predictP = getScreenPos(x0, yPred);
		Point actualP = getScreenPos(x0, yActual);
		Point errorP = getScreenPos(xErrorDisplay, yPred);
		
		g.setColor(kXColor);
		g.drawLine(predictP.x, 0, predictP.x, getSize().height);
		
		drawArrow(predictP, kPredictColor, g);
		if (predictP.y < actualP.y)
			predLabel.drawCentred(g, errorP.x, predictP.y - descent - kTextVertGap);
		else
			predLabel.drawCentred(g, errorP.x, predictP.y + ascent + kTextVertGap);
		
		drawArrow(actualP, kActualColor, g);
		if (predictP.y < actualP.y)
			actualLabel.drawCentred(g, errorP.x, actualP.y + ascent + kTextVertGap);
		else
			actualLabel.drawCentred(g, errorP.x, actualP.y - descent - kTextVertGap);
		
		g.setColor(Color.red);
		g.drawLine(errorP.x, predictP.y, errorP.x, actualP.y);
		
		if (Math.abs(actualP.y - predictP.y) > kArrowSize + 1) {
			int dir = predictP.y < actualP.y ? 1 : -1;
			g.drawLine(errorP.x, predictP.y, errorP.x - kArrowSize, predictP.y + dir * kArrowSize);
			g.drawLine(errorP.x, predictP.y, errorP.x + kArrowSize, predictP.y + dir * kArrowSize);
		}
		
		if (Math.abs(actualP.y - predictP.y) > ascent + 2)
			kErrorLabel.drawLeft(g, errorP.x - kTextHorizGap, (predictP.y + actualP.y + ascent - descent) / 2);
	}
	
	
	private void drawPrediction(Graphics g) {
		ResponsePredictVariable yVar = (ResponsePredictVariable)getVariable(yKey);
		NumVariable xVar = (NumVariable)getVariable(xKey);
		int n = xVar.noOfValues();
		
		double x0 = xVar.doubleValueAt(n-1);
		double y0 = yVar.getNewY().toDouble();
		
		LinearModel model = (LinearModel)getVariable(modelKey);
		double y0Mean = model.evaluateMean(x0);
		
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		double y0Prediction = lse.getIntercept() + lse.getSlope() * x0;
		
		Point predictP = getScreenPos(x0, y0Prediction);
		g.setColor(Color.black);
		setCrossSize(MEDIUM_CROSS);
		drawBlob(g, predictP);
		
		if (predictMode == PREDICT_MEAN)
			drawLines(x0, y0Prediction, y0Mean, kMeanEstLabel, kActualMeanLabel, g);
		else {
			drawLines(x0, y0Prediction, y0, kPredictionLabel, kActualNewLabel, g);
			
			g.setColor(Color.red);
			setCrossSize(LARGE_CROSS);
			Point newP = getScreenPos(x0, y0);
			drawCross(g, newP);
			setCrossSize(MEDIUM_CROSS);
		}
	}
}
	
