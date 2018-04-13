package linMod;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class LinePredictionView extends SampleLineView {
//	static public final String LINE_PREDICTION_PLOT = "linePredictionPlot";
	
	private XValueSlider xSlider;
	
	public LinePredictionView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey,
						String modelKey, XValueSlider xSlider) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.xSlider = xSlider;
	}
	
	protected void drawLSLine(Graphics g, double lowX, double highX) {
		super.drawLSLine(g, lowX, highX);
		
		double x0 = xSlider.getNumValue().toDouble();
		
		g.setColor(Color.blue);
		
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		
		double slope = lse.getSlope();
		double intercept = lse.getIntercept();
		
		double prediction = intercept + slope * x0;
		
		LinearModel model = (LinearModel)getData().getVariable(modelKey);
		
		double theoryMean = model.evaluateMean(x0);
		
		Point predictionPos = getScreenPos(x0, prediction);
		Point meanPos = getScreenPos(x0, theoryMean);
		
		g.setColor(Color.black);
		g.drawLine(0, meanPos.y, meanPos.x, meanPos.y);
		
		g.setColor(Color.blue);
		g.drawLine(0, predictionPos.y, predictionPos.x, predictionPos.y);
		
		g.setColor(getForeground());
	}
}
	
