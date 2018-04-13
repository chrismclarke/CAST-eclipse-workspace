package sampling;

import java.awt.*;

import dataView.*;
import utils.*;


public class ValueSlider extends XSlider {
	private double min, max;
	private int decimals;
	
	public ValueSlider(double min, double max, int noOfSteps, int decimals, XApplet applet) {
		super(null, null, "Proportion less than ", 0, noOfSteps, 0, applet);
		this.min = min;
		this.max = max;
		this.decimals = decimals;
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getSliderValue(), decimals);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max((new NumValue(min, decimals)).stringWidth(g),
															(new NumValue(max, decimals)).stringWidth(g));
	}
	
	public double getSliderValue() {
		return getSliderValue(getValue());
	}
	
	protected double getSliderValue(int val) {
		return min + val * (max - min) / getMaxValue();
	}
}