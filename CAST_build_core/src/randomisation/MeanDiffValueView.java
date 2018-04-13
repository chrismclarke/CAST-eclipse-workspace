package randomisation;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class MeanDiffValueView extends ValueImageView {
//	static final private String MEANDIFFVIEW = "meanDiff";
	
	private TwoGroupDotView dotPlot;
	private NumValue maxDiff;
	
	public MeanDiffValueView(DataSet theData, XApplet applet, TwoGroupDotView dotPlot, NumValue maxDiff) {
		super(theData, applet, "statistics/diffMean.gif", 13);
		this.dotPlot = dotPlot;
		this.maxDiff = maxDiff;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return maxDiff.stringWidth(g);
	}
	
	protected String getValueString() {
		double mean[] = dotPlot.getMeans();
		return (new NumValue(mean[1] - mean[0], maxDiff.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
