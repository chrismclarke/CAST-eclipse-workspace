package multiRegn;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class PredictionXZView extends ValueView {
//	static final public String PREDICTION_XZ_VIEW = "predictionXZ";
	
	private String yName;
	private String modelKey;
	private String explanKey[];
	private NumValue minPrediction, maxPrediction;
	private int predictionDecimals;
	
	private NumValue tempVal[];
	
	public PredictionXZView(DataSet theData, XApplet applet, String yName, String[] explanKey, String modelKey,
					NumValue minPrediction, NumValue maxPrediction) {
		super(theData, applet);
		this.yName = yName;
		this.explanKey = explanKey;
		this.modelKey = modelKey;
		this.minPrediction = minPrediction;
		this.maxPrediction = maxPrediction;
		predictionDecimals = Math.max(minPrediction.decimals, maxPrediction.decimals);
		tempVal = new NumValue[explanKey.length];
	}

//--------------------------------------------------------------------------------
	
	private String getEquation() {
		MultipleRegnModel theModel = (MultipleRegnModel)getVariable(modelKey);
		
		String eqn = yName + " = " + theModel.getParameter(0).toString();
		for (int i=0 ; i<explanKey.length ; i++) {
			CoreVariable xVariable = getVariable(explanKey[i]);
			eqn += " + " + theModel.getParameter(i+1).toString() + " " + xVariable.name;
		}
		
		return eqn + " =";
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(getEquation());
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max(minPrediction.stringWidth(g), maxPrediction.stringWidth(g));
	}
	
	protected String getValueString() {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			for (int i=0 ; i<explanKey.length ; i++) {
				NumVariable xVariable = (NumVariable)getVariable(explanKey[i]);
				tempVal[i] = (NumValue)xVariable.valueAt(selectedIndex);
			}
			
			MultipleRegnModel theModel = (MultipleRegnModel)getVariable(modelKey);
			double prediction = theModel.evaluateMean(tempVal);
			
			return (new NumValue(prediction, predictionDecimals)).toString();
		}
		else
			return null;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(getEquation(), startHoriz, baseLine);
		FontMetrics fm = g.getFontMetrics();
		int halfYWidth = fm.stringWidth(yName) / 2;
		int ascent = fm.getAscent();
		g.drawLine(startHoriz, baseLine - ascent, startHoriz + halfYWidth, baseLine - ascent - 2);
		g.drawLine(startHoriz + halfYWidth, baseLine - ascent - 2, startHoriz + 2 * halfYWidth,
																										baseLine - ascent);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
