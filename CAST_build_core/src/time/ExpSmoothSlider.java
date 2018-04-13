package time;

import java.awt.*;

import dataView.*;
import utils.*;


public class ExpSmoothSlider extends XSlider {
	public ExpSmoothSlider(double initialConst, XApplet applet) {
		super("0.0", "1.0", applet.translate("Smoothing const") + " = ", 0, 100, (int)Math.round(initialConst * 100), applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getExpSmoothConst(val), 2);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
	
	public double getExpSmoothConst() {
		return getExpSmoothConst(getValue());
	}
	
	public double getExpSmoothConst(int val) {
		return val * 0.01;
	}
}