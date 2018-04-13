package regn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class PredictionEView extends PredictionView {
//	static public final String PREDICTION_E_VIEW = "predictionE";
	
	private String xKey;
	
	public PredictionEView(DataSet theData, XApplet applet, String xKey, String yKey,
						String lineKey, DragExplanAxis xAxis, VertAxis yAxis, int predictionDecimals) {
		super(theData, applet, yKey, lineKey, xAxis, yAxis, predictionDecimals);
		this.xKey = xKey;
	}

//--------------------------------------------------------------------------------
	
	private String getEquation() {
		CoreVariable xVariable = getVariable(xKey);
		CoreVariable yVariable = getVariable(yKey);
		LinearModel theModel = (LinearModel)getVariable(lineKey);
		return yVariable.name + " = " + theModel.getIntercept().toString()
										+ " + " + theModel.getSlope().toString()
											+ " " + xVariable.name + " =";
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(getEquation());
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(getEquation(), startHoriz, baseLine);
		CoreVariable yVariable = getVariable(yKey);
		FontMetrics fm = g.getFontMetrics();
		int halfYWidth = fm.stringWidth(yVariable.name) / 2;
		int ascent = fm.getAscent();
		g.drawLine(startHoriz, baseLine - ascent, startHoriz + halfYWidth, baseLine - ascent - 2);
		g.drawLine(startHoriz + halfYWidth, baseLine - ascent - 2, startHoriz + 2 * halfYWidth,
																										baseLine - ascent);
	}
}
