package qnUtils;

import java.awt.*;

import dataView.*;
import utils.*;


public class DFSlider extends XSlider {
	static final public boolean MEAN = true;
	static final public boolean SD = false;
	
	static final private int kMaxVal = 61;
	
	public DFSlider(int startDF, XApplet applet) {
		super(null, null, applet.translate("Degrees of freedom") + " = ", 1, kMaxVal, startDF, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getDF(val), 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(String.valueOf(getDF(getMaxValue())));
	}
	
	public int getDF() {
		return getDF(getValue());
	}
	
	protected int getDF(int val) {
		return (val <= 30) ? val :
					(val <= 40) ? 30 + (val - 30) * 2 :
					(val <= 50) ? 50 + (val - 40) * 5 :
					(val <= 60) ? 100 + (val - 50) * 10 :
					300;
	}
}