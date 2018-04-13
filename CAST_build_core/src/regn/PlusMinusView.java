package regn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class PlusMinusView extends PredictionView {
//	static public final String PLUS_MINUS_VIEW = "plusMinus";
	static final public Color errorColor = new Color(0x009900);		//		dark green
	
	private String errorString;
	
	public PlusMinusView(DataSet theData, XApplet applet, String yKey, String lineKey,
																	DragExplanAxis xAxis, VertAxis yAxis, int predictionDecimals) {
		super(theData, applet, yKey, lineKey, xAxis, yAxis, predictionDecimals);
		setForeground(errorColor);
		errorString = applet.translate("Likely error") + " = ";
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(errorString);
	}
	
	protected String getValueString() {
		NumValue xValue = xAxis.getAxisVal();
		
		LinearModel theModel = (LinearModel)getVariable(lineKey);
		double plusMinus = 2.0 * theModel.evaluateSD(xValue).toDouble();
		
		return (new NumValue(plusMinus, predictionDecimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(errorString, startHoriz, baseLine);
	}
}
