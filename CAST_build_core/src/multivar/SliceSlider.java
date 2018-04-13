package multivar;

import java.awt.*;

import dataView.*;
import utils.*;


public class SliceSlider extends XSlider {
	private String sliceName[];
	
	public SliceSlider(String varName, int minVal, int maxVal, String sliceName[],
																	String minName, String maxName, XApplet applet) {
		super(minName, maxName, varName + " = ", minVal, maxVal, minVal, applet);
		this.sliceName = sliceName;
	}
	
	public SliceSlider(String varName, int minVal, int maxVal, String sliceName[],
																						XApplet applet) {
		this(varName, minVal, maxVal, sliceName, null, null, applet);
	}
	
	public SliceSlider(String varName, int minVal, int maxVal, XApplet applet) {
		this(varName, minVal, maxVal, null, applet);
	}
	
	protected Value translateValue(int val) {
		if (sliceName == null)
			return new NumValue(getValue(), 0);
		else
			return new LabelValue(sliceName[getValue() - getMinValue()]);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (sliceName == null)
			return Math.max(translateValue(getMinValue()).stringWidth(g),
									translateValue(getMaxValue()).stringWidth(g));
		else {
			int maxWidth = 0;
			FontMetrics fm = g.getFontMetrics();
			for (int i=0 ; i<sliceName.length ; i++)
				maxWidth = Math.max(maxWidth, fm.stringWidth(sliceName[i]));
			return maxWidth;
		}
	}
}