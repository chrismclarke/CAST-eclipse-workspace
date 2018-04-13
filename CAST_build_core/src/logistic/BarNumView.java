package logistic;

import dataView.*;
import axis.*;


public class BarNumView extends BarPredictionView {
//	static final public String BAR_NUM_PLOT = "barNumPlot";
	
	private boolean showPrediction = true;
	protected double predictionX;
	
	public BarNumView(DataSet theData, XApplet applet, VertAxis vertAxis, HorizAxis horizAxis,
								String xKey, String yKey, String modelKey) {
		super(theData, applet, vertAxis, horizAxis, xKey, yKey, modelKey);
	}
	
	public void setShowPrediction(boolean showPrediction, double predictionX) {
		this.showPrediction = showPrediction;
		this.predictionX = predictionX;
	}
	
	protected boolean canShowPrediction() {
		return showPrediction;
	}
	
	protected double getPredictionX() {
		return predictionX;
	}

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
	
