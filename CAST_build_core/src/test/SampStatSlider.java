package test;

import java.awt.*;

import dataView.*;
import utils.*;


public class SampStatSlider extends XSlider {
	static final public int MEAN = 0;
	static final public int SD = 1;
	static final public int MEAN_DIFF = 2;
	
	static final private String kMeanString = "x\u0305 = ";
	static final private String kSdString = "s = ";
	static final private String kMeanDiffString = "d\u0305 = ";
	
//	static final private int kGap = 4;
	
	static String getTitleString(int statType) {
		if (statType == MEAN)
			return kMeanString;
		else if (statType == SD)
			return kSdString;
		else
			return kMeanDiffString;
	}
	
	private NumValue lowStat, statStep;
	
	public SampStatSlider(NumValue lowStat, NumValue highStat, NumValue statStep,
													NumValue startStat, int statType, XApplet applet) {
		super(lowStat.toString(), highStat.toString(), getTitleString(statType), 0,
							(int)Math.round((highStat.toDouble() - lowStat.toDouble()) / statStep.toDouble()),
							(int)Math.round((startStat.toDouble() - lowStat.toDouble()) / statStep.toDouble()), applet);
		this.lowStat = lowStat;
		this.statStep = statStep;
	}
	
	public void setSliderValue(double newValue) {
		setValue((int)Math.round((newValue - lowStat.toDouble()) / statStep.toDouble()));
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getStat(val), statStep.decimals);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max(translateValue(getMinValue()).stringWidth(g),
															translateValue(getMaxValue()).stringWidth(g));
	}
	
	public double getStat() {
		return getStat(getValue());
	}
	
	protected double getStat(int val) {
		return lowStat.toDouble() + val * statStep.toDouble();
	}
}