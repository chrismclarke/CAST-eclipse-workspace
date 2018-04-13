package sport;

import java.awt.*;

import dataView.*;
import utils.*;


public class AnonParameterSlider extends XSlider {
	
	private NumValue minValue;
	
	static private int valueInt(NumValue val) {
		double v = val.toDouble();
		for (int i=0 ; i<val.decimals ; i++)
			v *= 10.0;
		return (int)Math.round(v);
	}
	
	public AnonParameterSlider(NumValue minValue, NumValue maxValue, NumValue startValue,
													XApplet applet) {
		super(null, null, null, 0, valueInt(maxValue) - valueInt(minValue),
											valueInt(startValue) - valueInt(minValue), applet);
		this.minValue = minValue;
	}
	
	protected Value translateValue(int val) {
		return getParameter(val);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max(translateValue(getMinValue()).stringWidth(g),
											translateValue(getMaxValue()).stringWidth(g));
	}
	
	public NumValue getParameter() {
		return getParameter(getValue());
	}
	
	protected NumValue getParameter(int val) {
		double v = val;
		for (int i=0 ; i<minValue.decimals ; i++)
			v *= 0.1;
		return new NumValue(minValue.toDouble() + v, minValue.decimals);
	}
}