package randomisation;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class MeanMedianValueView extends ValueImageView {
//	static final private String MEANMEDVIEW = "meanMedian";
	
	private RoundMedianDotView dotPlot;
	private NumValue maxDiff;
	
	public MeanMedianValueView(DataSet theData, XApplet applet, RoundMedianDotView dotPlot, NumValue maxDiff) {
		super(theData, applet, "statistics/meanMedDiff.gif", 12);
		this.dotPlot = dotPlot;
		this.maxDiff = maxDiff;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return maxDiff.stringWidth(g);
	}
	
	protected String getValueString() {
		return (new NumValue(dotPlot.getMean() - dotPlot.getMedian(),
																										maxDiff.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
