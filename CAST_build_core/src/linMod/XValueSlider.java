package linMod;

import java.awt.*;

import dataView.*;
import utils.*;


public class XValueSlider extends XSlider {
	static final public int SHOW_VALUES = 0;
	static final public int NO_SHOW_VALUES = 1;
	
	private NumValue minX, xStep;
	private int labelType;
	
	public XValueSlider(NumValue minX, NumValue maxX, NumValue xStep, NumValue startX,
																							XApplet applet) {
		this(minX, maxX, xStep, startX, SHOW_VALUES, applet);
	}
	
	public XValueSlider(NumValue minX, NumValue maxX, NumValue xStep, NumValue startX,
																int labelType, XApplet applet) {
		super((labelType == SHOW_VALUES) ? minX.toString() : null,
					(labelType == SHOW_VALUES) ? maxX.toString() : null,
					(labelType == SHOW_VALUES) ? "X = " : null,
					0, (int)Math.round((maxX.toDouble() - minX.toDouble()) / xStep.toDouble()),
					(int)Math.round((startX.toDouble() - minX.toDouble()) / xStep.toDouble()), applet);
		this.minX = minX;
		this.xStep = xStep;
		this.labelType = labelType;
	}
	
	protected Value translateValue(int val) {
		return (labelType == SHOW_VALUES) ? getNumValue() : null;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max(getNumValue(getMinValue()).stringWidth(g),
															getNumValue(getMaxValue()).stringWidth(g));
	}
	
	public NumValue getNumValue() {
		return getNumValue(getValue());
	}
	
	protected NumValue getNumValue(int val) {
		return new NumValue(minX.toDouble() + val * xStep.toDouble(), xStep.decimals);
	}
}