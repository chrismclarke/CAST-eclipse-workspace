package valueList;

import java.awt.*;

import dataView.*;


public class FixedValueView extends ValueView {
//	static final public String FIXED_VALUE = "fixedValue";
	
	private String label;
	private Value maxValue, currentValue;
	
	public FixedValueView(String label, Value maxValue, double currentValue,
																																		XApplet applet) {
		this(label, maxValue, new NumValue(currentValue, ((NumValue)maxValue).decimals), applet);
	}
	
	public FixedValueView(String label, Value maxValue, Value currentValue,
																																		XApplet applet) {
		super(new DataSet(), applet);
		this.label = label;
		this.maxValue = maxValue;
		this.currentValue = currentValue;
	}
	
	public void setValue(double newValue) {
		if (currentValue == null)
			currentValue = new NumValue(newValue, ((NumValue)maxValue).decimals);
		else {
			((NumValue)currentValue).setValue(newValue);
			((NumValue)currentValue).decimals = ((NumValue)maxValue).decimals;
		}
		redrawValue();
	}
	
	public void setValue(Value newValue) {
		currentValue = newValue;
		redrawValue();
	}
	
	public void clearValue() {
		currentValue = null;
		redrawValue();
	}
	
	public void setMaxValue(Value maxValue) {
		this.maxValue = maxValue;
		revalidate();
	}
	
	protected int getLabelWidth(Graphics g) {
		return (label == null) ? 0 : g.getFontMetrics().stringWidth(label);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		return (currentValue == null) ? "" : currentValue.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (label != null)
			g.drawString(label, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
