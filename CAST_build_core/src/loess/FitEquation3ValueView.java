package loess;

import java.awt.*;

import dataView.*;
import valueList.*;
import models.*;


public class FitEquation3ValueView extends ValueView {
//	static public final String FIT_3_VALUE = "fit3Value";
	static final private int kHatHeight = 5;
	static final private int kSuperscriptOffset = 5;
	
	static final private String kEqualsString = " = ";
//	static final private String kTenString = "10";
	
	private String yKey, modelKey;
	private SelectionValueView selectedXView;
	private NumValue maxFitValue;
	private int factorDecimals;
	private Font superscriptFont;
	
	private boolean initialised = false;
	private double factor, power;
	private int mainStringWidth, maxExplanWidth, powerWidth;
	private String mainString;
	
	
	public FitEquation3ValueView(DataSet theData, String yKey,
						String modelKey, SelectionValueView selectedXView, NumValue maxFitValue,
						int factorDecimals, XApplet applet) {
		super(theData, applet);
		this.selectedXView = selectedXView;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.maxFitValue = maxFitValue;
		this.factorDecimals = factorDecimals;
		
		superscriptFont = applet.getSmallFont();
	}

//--------------------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (!initialised) {
			FontMetrics fm = g.getFontMetrics();
			
			LinearModel model = (LinearModel)getVariable(modelKey);
			factor = Math.pow(10.0, model.getIntercept().toDouble());
			power = model.getSlope().toDouble();
			String factorString = new NumValue(factor, factorDecimals).toString();
			mainString = getVariable(yKey).name + kEqualsString + factorString + " (";
			mainStringWidth = fm.stringWidth(mainString);
			
			maxExplanWidth = selectedXView.getMaxValue().stringWidth(g);
			
			Font oldFont = g.getFont();
			g.setFont(superscriptFont);
			powerWidth = model.getSlope().stringWidth(g);
			g.setFont(oldFont);
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	protected int getLabelAscent(Graphics g) {
		int mainAscent = super.getLabelAscent(g) + kHatHeight;
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		int superAscent = g.getFontMetrics().getAscent();
		g.setFont(oldFont);
		
		return Math.max(mainAscent, superAscent + kSuperscriptOffset);
	}
	
	protected int getLabelWidth(Graphics g) {
		initialise(g);
		return mainStringWidth + maxExplanWidth + powerWidth
																+ g.getFontMetrics().stringWidth(") = ");
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
		startHoriz += mainStringWidth;
		
		Color oldColor = g.getColor();
		g.setColor(selectedXView.getForeground());
		double selectedX = selectedXView.getValue();
		String xString;
		if (Double.isNaN(selectedX))
			xString = "???";
		else
			xString = (new NumValue(selectedX,
												selectedXView.getMaxValue().decimals)).toString();
		g.drawString(xString, startHoriz, baseline);
		startHoriz += g.getFontMetrics().stringWidth(xString);
		g.setColor(oldColor);
		g.drawString(")", startHoriz, baseline);
		startHoriz += g.getFontMetrics().stringWidth(")");
		
		Font oldFont = g.getFont();
		g.setFont(superscriptFont);
		LinearModel model = (LinearModel)getVariable(modelKey);
		String powerString = model.getSlope().toString();
		g.drawString(powerString, startHoriz, baseline - kSuperscriptOffset);
		startHoriz += g.getFontMetrics().stringWidth(powerString);
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
		double selectedX = selectedXView.getValue();
		if (Double.isNaN(selectedX))
			return Double.NaN;
		else
			return factor * Math.pow(selectedX, power);
	}
	
	public void redrawValue() {
		redrawAll();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
