package utils;

import java.awt.*;

import dataView.*;


public class ParameterSlider extends XSlider implements StatusInterface {
	static final public int NO_SHOW_MIN_MAX = 0;
	static final public int SHOW_MIN_MAX = 1;
	
	private NumValue minValue, maxValue;
	private int noOfSteps = 0;
	
	private boolean showValue = true;
	
	static private int valueInt(NumValue val) {
		double v = val.toDouble();
		for (int i=0 ; i<val.decimals ; i++)
			v *= 10.0;
		return (int)Math.round(v);
	}
	
	static private String minMaxString(String theString, int minMaxShow) {
		return (minMaxShow == SHOW_MIN_MAX) ? theString : null;
	}
	
	static private int initialStep(NumValue minValue, NumValue maxValue, double startValue,
																					int noOfSteps) {
		return (int)Math.round(noOfSteps * (startValue - minValue.toDouble())
																		/ (maxValue.toDouble() - minValue.toDouble()));
	}

//-----------------------------------------------------
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
													String paramName, int minMaxShow, int orientation, XApplet applet) {
		super(minMaxString(minValue.toString(), minMaxShow),
						minMaxString(maxValue.toString(), minMaxShow), paramName, 0,
						valueInt(maxValue) - valueInt(minValue),
						valueInt(startValue) - valueInt(minValue), orientation, applet, true);
		this.minValue = minValue;
	}
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
													String paramName, int minMaxShow, XApplet applet) {
		this(minValue, maxValue, startValue, paramName, minMaxShow, HORIZONTAL, applet);
	}
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
																		String paramName, XApplet applet) {
		this(minValue, maxValue, startValue, paramName, SHOW_MIN_MAX, applet);
	}
	
	
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
								int noOfSteps, String paramName, int minMaxShow, int orientation, XApplet applet) {
		super(minMaxString(minValue.toString(), minMaxShow),
						minMaxString(maxValue.toString(), minMaxShow), paramName, 0,
						noOfSteps, initialStep(minValue, maxValue, startValue.toDouble(), noOfSteps),
						orientation, applet, true);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.noOfSteps = noOfSteps;
	}
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
								int noOfSteps, String paramName, int minMaxShow, XApplet applet) {
		this(minValue, maxValue, startValue, noOfSteps, paramName, minMaxShow, HORIZONTAL, applet);
	}
	
	public ParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
											int noOfSteps, String paramName, XApplet applet) {
		this(minValue, maxValue, startValue, noOfSteps, paramName, SHOW_MIN_MAX, applet);
	}
	
	public void setShowValue(boolean showValue) {
		this.showValue = showValue;
	}

//-----------------------------------------------------
	
	public String getStatus() {
		return getParameter().toString();
	}
	
	@SuppressWarnings("deprecation")
	public void setStatus(String status) {
		setParameter(Double.parseDouble(status));
		postEvent(new Event(this, Event.ACTION_EVENT, null));
	}
	
	protected Value translateValue(int val) {
		return showValue ? getParameter(val) : null;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return showValue ? Math.max(translateValue(getMinValue()).stringWidth(g),
																translateValue(getMaxValue()).stringWidth(g)) : 0;
	}
	
	public NumValue getParameter() {
		return getParameter(getValue());
	}
	
	protected NumValue getParameter(int val) {
		double v = val;
		if (noOfSteps == 0)
			for (int i=0 ; i<minValue.decimals ; i++)
				v *= 0.1;
		else
			v = (maxValue.toDouble() - minValue.toDouble()) * v / noOfSteps;
		return new NumValue(minValue.toDouble() + v, minValue.decimals);
	}
	
	public void setParameter(double newValue) {
		if (noOfSteps == 0)
			setValue(valueInt(new NumValue(newValue, minValue.decimals)) - valueInt(minValue));
		else
			setValue(initialStep(minValue, maxValue, newValue, noOfSteps));
	}
	
	public void setParameter(double newValue, boolean postEvent) {
		setPostEvents(postEvent);
		setParameter(newValue);
		setPostEvents(true);
	}
	
	public void changeLimits(NumValue minValue, NumValue maxValue, NumValue startValue) {
		this.minValue = minValue;
		setMinMaxValues(minValue.toString(), 0, maxValue.toString(), valueInt(maxValue) - valueInt(minValue));
		setValue(valueInt(startValue) - valueInt(minValue));
	}
	
	public void changeLimits(NumValue minValue, NumValue maxValue, NumValue startValue,
								int noOfSteps) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		setMinValue(minValue.toString(), 0);
		setMaxValue(maxValue.toString(), noOfSteps);
		setValue(initialStep(minValue, maxValue, startValue.toDouble(), noOfSteps));
		setTickSpacing(0, noOfSteps);
	}
}