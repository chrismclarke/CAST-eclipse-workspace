package loess;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import valueList.*;

import linMod.*;


public class PIValueView extends ValueView {
//	static public final String PREDICTION_VIEW = "PIValue";
	
	static final private String kEqualsString = " = ";
	
	private String kIsString, kToString;
	
	private NumValue maxX, maxPrediction;
	private String predictionYString, predictionXString;
	private XValueSlider xSlider;
	private TransPIView theView;
	
	public PIValueView(DataSet theData, XApplet applet, String xKey, NumValue maxX,
								NumValue maxPrediction, XValueSlider xSlider, TransPIView theView,
								String predictionName) {
		super(theData, applet);
		this.maxX = maxX;
		this.maxPrediction = maxPrediction;
		this.xSlider = xSlider;
		this.theView = theView;
		
		kToString = " " + applet.translate("to") + " ";
		
		StringTokenizer st = new StringTokenizer(applet.translate("95% P.I. for * with * is"), "*");
		String kPIString = st.nextToken();
		String kWithString = st.nextToken();
		kIsString = st.nextToken() + " ";
		predictionYString = kPIString + predictionName + kWithString;
		predictionXString = getVariable(xKey).name + kEqualsString;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(predictionYString + predictionXString) + maxX.stringWidth(g)
																	+ fm.stringWidth(kIsString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(kToString) + 2 * fm.stringWidth(maxPrediction.toString());
	}
	
	protected String getValueString() {
		NumValue lowLimit = new NumValue(theView.getPredictionBound(false), maxPrediction.decimals);
		NumValue highLimit = new NumValue(theView.getPredictionBound(true), maxPrediction.decimals);
		return lowLimit.toString() + kToString + highLimit.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		FontMetrics fm = g.getFontMetrics();
		
		g.drawString(predictionYString, startHoriz, baseLine);
		startHoriz += fm.stringWidth(predictionYString);
		
		g.setColor(Color.blue);
		String valueString = predictionXString + xSlider.getNumValue().toString();
		g.drawString(valueString, startHoriz, baseLine);
		startHoriz += fm.stringWidth(valueString);
		
		g.setColor(getForeground());
		g.drawString(kIsString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		repaint();
	}
}
