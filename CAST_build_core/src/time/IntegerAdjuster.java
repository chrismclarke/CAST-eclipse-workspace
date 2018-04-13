package time;

import java.awt.*;

import dataView.*;


public class IntegerAdjuster extends XValueAdjuster {
	public IntegerAdjuster(String title, int minVal, int maxVal, int startVal, XApplet applet) {
		super(title, minVal, maxVal, startVal, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(val, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
}