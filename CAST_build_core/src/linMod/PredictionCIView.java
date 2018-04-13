package linMod;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import distn.*;


public class PredictionCIView extends ValueView {
//	static public final String PREDICTION_CI_VIEW = "predictionCIValue";
	
	static final private String kXEqualsString = "x = ";
	
	static final public int CONFIDENCE_INTERVAL = 0;
	static final public int PREDICTION_INTERVAL = 1;
	
	private String kToString;
	private String kCIString, kPIString, kCIIsString, kPIIsString;
	
	private String yKey, xKey;
	private NumValue maxX, maxPrediction;
	private XValueSlider xSlider;
	private int intervalType;
	
	public PredictionCIView(DataSet theData, XApplet applet, String yKey, String xKey, NumValue maxX,
								NumValue maxPrediction, int intervalType, XValueSlider xSlider) {
		super(theData, applet);
		this.yKey = yKey;
		this.xKey = xKey;
		this.maxX = maxX;
		this.maxPrediction = maxPrediction;
		this.intervalType = intervalType;
		this.xSlider = xSlider;
		
		kToString = "  " + applet.translate("to") + "  ";
		
		StringTokenizer st = new StringTokenizer(applet.translate("95% CI for mean at * is"), "*");
		kCIString = st.nextToken();
		kCIIsString = st.nextToken();
		st = new StringTokenizer(applet.translate("95% prediction at * is"), "*");
		kPIString = st.nextToken();
		kPIIsString = st.nextToken();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int width = (intervalType == CONFIDENCE_INTERVAL) ? fm.stringWidth(kCIString + kCIIsString + " ")
																																: fm.stringWidth(kPIString + kPIIsString + " ");
		return width + fm.stringWidth(kXEqualsString) + maxX.stringWidth(g);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.stringWidth(kToString) + 2 * fm.stringWidth(maxPrediction.toString());
	}
	
	protected String getValueString() {
		LSEstimate lse = new LSEstimate(getData(), xKey, yKey);
		
		double slope = lse.getSlope();
		double intercept = lse.getIntercept();
		double sxx = lse.getSxx();
		double xMean = lse.getXMean();
		int n = lse.getN();
		double errorSD = lse.getErrorSD();
		
		double nInv = 1.0 / n;
		if (intervalType != CONFIDENCE_INTERVAL)
			nInv += 1.0;
		double x = xSlider.getNumValue().toDouble();
		double mean = intercept + slope * x;
		double plusMinus = errorSD * TTable.quantile(0.975, n - 2)
													* Math.sqrt(nInv + (x - xMean) * (x - xMean) / sxx);
		
		NumValue lowLimit = new NumValue(mean - plusMinus, maxPrediction.decimals);
		NumValue highLimit = new NumValue(mean + plusMinus, maxPrediction.decimals);
		return lowLimit.toString() + kToString + highLimit.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		FontMetrics fm = g.getFontMetrics();
		
		String label = (intervalType == CONFIDENCE_INTERVAL) ? kCIString : kPIString;
		g.drawString(label, startHoriz, baseLine);
		startHoriz += fm.stringWidth(label);
		
		g.setColor(Color.red);
		label = kXEqualsString + xSlider.getNumValue().toString();
		g.drawString(label, startHoriz, baseLine);
		startHoriz += fm.stringWidth(label);
		
		g.setColor(getForeground());
		String isString = (intervalType == CONFIDENCE_INTERVAL) ? kCIIsString : kPIIsString;
		g.drawString(isString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
