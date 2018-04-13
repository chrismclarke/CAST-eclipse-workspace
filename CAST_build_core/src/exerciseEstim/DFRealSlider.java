package exerciseEstim;

import java.awt.*;

import dataView.*;
import utils.*;
import formula.*;


public class DFRealSlider extends XSlider {
	static final public boolean MEAN = true;
	static final public boolean SD = false;
	
	static final private int kMaxVal = 126;
	
	public DFRealSlider(int startDF, XApplet applet) {
		super(null, null, "t distn with df = ", 1, kMaxVal, startDF, applet);
	}
	
	protected Value translateValue(int val) {
		Value v;
		if (val == kMaxVal)
			v = new LabelValue(MText.translateUnicode("infinity"));
		else
			v = new NumValue(getDF(val), 0);
		return v;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(String.valueOf(getDF(kMaxVal - 1)));
	}
	
	public double getDF() {
		return getDF(getValue());
	}
	
//	public void setInfinityDf() {
//		setValue(kMaxVal);
//	}
	
	public void setDf(double df) {
		int dfIndex = dfToValue(df);
		setValue(dfIndex);
	}
	
	public boolean isCorrect(double df) {
		return dfToValue(df) == getValue();
	}
	
	private int dfToValue(double df) {
		int dfIndex = 0;
		if (Double.isInfinite(df))
			dfIndex = kMaxVal;
		else {
			while (getDF(dfIndex + 1) < df)
				dfIndex ++;
			double distance0 = df - getDF(dfIndex);
			double distance1 = getDF(dfIndex + 1) - df;
			if (distance1 < distance0)
				dfIndex ++;
		}
		return dfIndex;
	}
	
	protected double getDF(int val) {
		return (val <= 60) ? val :
					(val <= 80) ? 60 + (val - 60) * 2 :
					(val <= 100) ? 100 + (val - 80) * 5 :
					(val <= 110) ? 200 + (val - 100) * 10 :
					(val <= 124) ? 300 + (val - 110) * 50 :
					(val == 125) ? 2000 :
					Double.POSITIVE_INFINITY;
	}
}