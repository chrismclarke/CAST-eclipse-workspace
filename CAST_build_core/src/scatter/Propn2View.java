package scatter;

import java.awt.*;

import dataView.*;
import valueList.*;


public class Propn2View extends ValueView {
//	static public final String PROPN2_VIEW = "proportion2Value";
	static private final int decimals = 3;
	static private final String kZeroString = "0.000";
	
	protected ScatterTruncView theView;
	private String labelString;
	
	public Propn2View(DataSet theData, XApplet applet, ScatterTruncView theView,
																																String labelString) {
		super(theData, applet);
		this.theView = theView;
		this.labelString = labelString;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	protected String getValueString() {
		int noOfValues = getData().getSelection().getNoOfFlags();
		int totalCount = 0;
		int passCount = 0;
		for (int i=0 ; i<noOfValues ; i++)
			if (theView.isNotTruncated(i)) {
				totalCount ++;
				if (theView.isPass(i))
					passCount++;
			}
		if (totalCount == 0)
			return "??";
		NumValue propn = new NumValue(((double)passCount) / totalCount, decimals);
		return propn.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
