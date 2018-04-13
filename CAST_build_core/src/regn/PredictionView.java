package regn;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.ValueView;
import models.*;


public class PredictionView extends ValueView {
//	static final public String PREDICTION_VIEW = "prediction";
	static final private String kPredictionString = "Predicted ";
	
	protected String yKey, lineKey;
	protected int predictionDecimals;
	protected DragExplanAxis xAxis;
	private VertAxis yAxis;
	
	public PredictionView(DataSet theData, XApplet applet, String yKey, String lineKey,
														DragExplanAxis xAxis, VertAxis yAxis, int predictionDecimals) {
		super(theData, applet);
		this.yKey = yKey;
		this.lineKey = lineKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.predictionDecimals = predictionDecimals;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		CoreVariable variable = getVariable(yKey);
		return g.getFontMetrics().stringWidth(kPredictionString + variable.name + " = ");
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max((new NumValue(yAxis.maxOnAxis, predictionDecimals)).stringWidth(g),
								(new NumValue(yAxis.minOnAxis, predictionDecimals)).stringWidth(g));
	}
	
	protected String getValueString() {
		if (xAxis.getAxisVal() == null)
			return null;
		double xValue = xAxis.getAxisVal().toDouble();
		
		LinearModel theModel = (LinearModel)getVariable(lineKey);
		double prediction = theModel.evaluateMean(xValue);
		
		return (new NumValue(prediction, predictionDecimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		CoreVariable variable = getVariable(yKey);
		g.drawString(kPredictionString + variable.name + " = ", startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
