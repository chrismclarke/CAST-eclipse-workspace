package percentile;

import java.awt.*;

import utils.*;
import dataView.*;


public class YearsSlider extends XSlider {
	static protected String[] makeArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	
	private String[] unitsArray;
	
	private int versionIndex = 0;		//	to allow units to change
	
	public YearsSlider(XApplet applet, int startYears, int maxYears, String[] unitsArray) {
		super("1", String.valueOf(maxYears), applet.translate("In") + " ", 1, maxYears, startYears, applet);
		this.unitsArray = unitsArray;
	}
	
	public YearsSlider(XApplet applet, int startYears, int maxYears, String units) {
		this(applet, startYears, maxYears, makeArray(units));
	}
	
	public void setVersionIndex(int versionIndex) {
		this.versionIndex = versionIndex;
		
		refreshValue();		//		changes text for the value label at top
		repaint();
	}
	
	protected Value translateValue(int val) {
		return new LabelValue(val + " " + unitsArray[versionIndex] + " . . .");
	}
	
	protected int getMaxValueWidth(Graphics g) {
		int oldVersionIndex = versionIndex;
		int maxWidth = 0;
		for (versionIndex=0 ; versionIndex<unitsArray.length ; versionIndex++)
			maxWidth = Math.max(maxWidth, translateValue(getMaxValue()).stringWidth(g));
			
		versionIndex = oldVersionIndex;
		return maxWidth;
	}
	
	public int getYears() {
		return getValue();
	}
}