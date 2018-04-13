package loess;

import java.awt.*;

import dataView.*;
import valueList.*;


public class FitEquation2ValueView extends ValueView {
//	static public final String FIT_2_VALUE = "fit2Value";
	static final private int kHatHeight = 5;
	static final private int kSuperscriptOffset = 5;
	
	static final private String kEqualsString = " = ";
	static final private String kTenString = "10";
	
	private String yKey;
	private FitEquationValueView logFitView;
	private NumValue maxFitValue;
	private Font superscriptFont;
	
	private String mainString;
	
	public FitEquation2ValueView(DataSet theData, String yKey,
								FitEquationValueView logFitView, NumValue maxFitValue, XApplet applet) {
		super(theData, applet);
		this.logFitView = logFitView;
		this.yKey = yKey;
		this.maxFitValue = maxFitValue;
		
		CoreVariable yVar = getVariable(yKey);
		mainString = yVar.name + kEqualsString + kTenString;
		superscriptFont = applet.getSmallFont();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelAscent(Graphics g) {
		int mainAscent = super.getLabelAscent(g) + kHatHeight;
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		int superAscent = g.getFontMetrics().getAscent();
		g.setFont(oldFont);
		
		return Math.max(mainAscent, superAscent + kSuperscriptOffset);
	}
	
	protected int getLabelWidth(Graphics g) {
//		CoreVariable yVar = getVariable(yKey);
		int theWidth = g.getFontMetrics().stringWidth(mainString + kEqualsString);
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		theWidth += logFitView.getMaxValue().stringWidth(g);
		g.setFont(oldFont);
		
		return theWidth;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseline) {
		CoreVariable yVar = getVariable(yKey);
		g.drawString(mainString, startHoriz, baseline);
		FontMetrics fm = g.getFontMetrics();
		int responseEnd = fm.stringWidth(yVar.name);
		int hatMid = (startHoriz + responseEnd) / 2;
		int hatBase = baseline - fm.getAscent();
		g.drawLine(startHoriz, hatBase - 1, hatMid, hatBase - kHatHeight);
		g.drawLine(hatMid, hatBase - kHatHeight, responseEnd, hatBase - 1);
		startHoriz += fm.stringWidth(mainString);
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		Color oldColor = g.getColor();
		g.setColor(logFitView.getForeground());
		double logFit = logFitView.getValue();
		NumValue maxLogFit = logFitView.getMaxValue();
		String logFitString = (Double.isNaN(logFit)) ? "???"
									: (Double.isInfinite(logFit)) ? "-inf"
								: (new NumValue(logFit, maxLogFit.decimals)).toString();
		g.drawString(logFitString, startHoriz, baseline - kSuperscriptOffset);
		startHoriz += g.getFontMetrics().stringWidth(logFitString);
		g.setColor(oldColor);
		g.setFont(oldFont);
		
		g.drawString(kEqualsString, startHoriz, baseline);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxFitValue.stringWidth(g);
	}
	
	protected String getValueString() {
		double value = getValue();
		if (Double.isNaN(value))
			return null;
		else
			return new NumValue(value, maxFitValue.decimals).toString();
	}
	
	public double getValue() {
		double logFit = logFitView.getValue();
		if (Double.isNaN(logFit))
			return Double.NaN;
		else if (Double.isInfinite(logFit))
			return 0.0;
		else
			return Math.pow(10.0, logFit);
	}
	
	public void redrawValue() {
		redrawAll();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
