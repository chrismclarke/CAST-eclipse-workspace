package loess;

import java.awt.*;

import dataView.*;
import valueList.*;
import models.*;

import regn.*;


public class FitEquationValueView extends ValueView {
//	static public final String FIT_VALUE = "fitValue";
	static final private int kHatHeight = 5;
	static final private int kSubscriptOffset = 3;
	
	static final private String kEqualsString = " = ";
	static final private String kTenString = "10";
	
	private String yKey, modelKey;
	private NumValue maxFitValue;
	private SelectionValueView selectedXView;
	private Font superscriptFont;
	
	private boolean initialised = false;
	private int responseWidth, mainEqualsWidth, maxExplanWidth, equalsWidth;
	private String mainEqualsString;
	
	public FitEquationValueView(DataSet theData,
					String yKey, String modelKey, SelectionValueView selectedXView,
					NumValue maxFitValue, XApplet applet) {
		super(theData, applet);
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.selectedXView = selectedXView;
		this.maxFitValue = maxFitValue;
		superscriptFont = applet.getSmallFont();
	}

//--------------------------------------------------------------------------------
	
	public NumValue getMaxValue() {
		return maxFitValue;
	}
	
	protected int getLabelAscent(Graphics g) {
		return super.getLabelAscent(g) + kHatHeight;
	}
	
	private int log10Width(Graphics g, int maxParamWidth) {
		FontMetrics fm = g.getFontMetrics();
		int width = fm.stringWidth("log()") + maxParamWidth;
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		width += g.getFontMetrics().stringWidth(kTenString);
		g.setFont(oldFont);
		return width;
	}
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			
			LinearModel model = (LinearModel)getVariable(modelKey);
			mainEqualsString = kEqualsString + model.getIntercept().toString() + " + "
																 	+ model.getSlope().toString();
			mainEqualsWidth = fm.stringWidth(mainEqualsString);
			equalsWidth = fm.stringWidth(kEqualsString);
			
			responseWidth = log10Width(g, fm.stringWidth(getVariable(yKey).name));
			maxExplanWidth = log10Width(g, selectedXView.getMaxValue().stringWidth(g));
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected int getLabelWidth(Graphics g) {
		initialise(g);
		return responseWidth + mainEqualsWidth + maxExplanWidth + equalsWidth;
	}
	
	private int drawLog10(Graphics g, String paramValue, int horizPos, int baseline,
																							Color xValueColor) {
		String logString = getApplet().translate("log");
		g.drawString(logString, horizPos, baseline);
		FontMetrics fm = g.getFontMetrics();
		horizPos += fm.stringWidth(logString);
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		g.drawString("10", horizPos, baseline + kSubscriptOffset);
		horizPos += g.getFontMetrics().stringWidth("10");
		g.setFont(oldFont);
		
		g.drawString("(", horizPos, baseline);
		horizPos += g.getFontMetrics().stringWidth("(");
		if (xValueColor == null)
			g.drawString(paramValue, horizPos, baseline);
		else {
			Color oldColor = g.getColor();
			g.setColor(xValueColor);
			g.drawString(paramValue, horizPos, baseline);
			g.setColor(oldColor);
		}
		horizPos += g.getFontMetrics().stringWidth(paramValue);
		g.drawString(")", horizPos, baseline);
		horizPos += g.getFontMetrics().stringWidth(")");
		
		return horizPos;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseline) {
		CoreVariable yVar = getVariable(yKey);
		int responseEnd = drawLog10(g, yVar.name, startHoriz, baseline,  null);
		int hatMid = (startHoriz + responseEnd) / 2;
		int hatBase = baseline - g.getFontMetrics().getAscent();
		g.drawLine(startHoriz, hatBase - 1, hatMid, hatBase - kHatHeight);
		g.drawLine(hatMid, hatBase - kHatHeight, responseEnd, hatBase - 1);
		startHoriz = responseEnd;
		
		g.drawString(mainEqualsString, startHoriz, baseline);
		startHoriz += mainEqualsWidth;
		
		double selectedX = selectedXView.getValue();
		String xString;
		if (Double.isNaN(selectedX))
			xString = "???";
		else
			xString = (new NumValue(selectedX, selectedXView.getMaxValue().decimals)).toString();
		startHoriz = drawLog10(g, xString, startHoriz, baseline, selectedXView.getForeground());
		g.drawString(kEqualsString, startHoriz, baseline);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxFitValue.stringWidth(g);
	}
	
	protected String getValueString() {
		double value = getValue();
		if (Double.isNaN(value))
			return null;
		else if (Double.isInfinite(value))
			return "-inf";
		else
			return new NumValue(value, maxFitValue.decimals).toString();
	}
	
	public double getValue() {
		double selectedX = selectedXView.getValue();
		if (Double.isNaN(selectedX))
			return Double.NaN;
		else {
			PowerLinearModel model = (PowerLinearModel)getVariable(modelKey);
			double intercept = model.getIntercept().toDouble();
			double slope = model.getSlope().toDouble();
			double logX = (selectedX <= 0.0) ? Double.NEGATIVE_INFINITY
														: Math.log(selectedX) / Math.log(10.0);
			return intercept + slope * logX;
		}
	}
	
	public void redrawValue() {
		redrawAll();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
