package time;

import java.awt.*;

import dataView.*;
import valueList.*;


public class ErrorSsqValueView extends ValueView {
//	static final private String ERRORVALUEVIEW = "ErrorView";
	
	static final private String kLabelString = "Error Ssq = ";
	
	private String valueKey, forecastKey;
	private NumValue maxErrorSsq;
	
	public ErrorSsqValueView(DataSet theData, XApplet applet,
											String valueKey, String forecastKey, NumValue maxErrorSsq) {
		super(theData, applet);
		this.valueKey = valueKey;
		this.forecastKey = forecastKey;
		this.maxErrorSsq = maxErrorSsq;
	}

	protected int getMaxValueWidth(Graphics g) {
		return maxErrorSsq.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable y = (NumVariable)getData().getVariable(valueKey);
		NumVariable yHat = (NumVariable)getData().getVariable(forecastKey);
		
		double errorSsq = 0.0;
		for (int i=0 ; i<y.noOfValues() ; i++) {
			double yVal = y.doubleValueAt(i);
			double yHatVal = yHat.doubleValueAt(i);
			if (!Double.isNaN(yVal) && !Double.isNaN(yHatVal))
				errorSsq += (yVal - yHatVal) * (yVal - yHatVal);
		}
		
		return new NumValue(errorSsq, maxErrorSsq.decimals).toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kLabelString);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(kLabelString, startHoriz, baseLine);
	}
}
