package statistic2;

import dataView.*;
import coreGraphics.*;


public class CenterCalculator extends StatCalculator {
	static public final int MEDIAN = 0;
	static public final int MEAN = 1;
	
	public CenterCalculator(int centerStatistic) {
		super(centerStatistic);
	}
	
	public double evaluateStat(NumVariable variable, BoxInfo theBoxInfo) {
		int n = variable.noOfValues();
		switch (getStat()) {
			case MEDIAN:
				return theBoxInfo.boxVal[BoxInfo.MEDIAN];
			case MEAN:
				double sum = 0.0;
				ValueEnumeration e = variable.values();
				while (e.hasMoreValues())
					sum += ((NumValue)e.nextValue()).toDouble();
				return sum / n;
			default:
				return 0.0;
		}
	}
	
	protected SpreadLimits findSpreadLimits(NumVariable variable, BoxInfo theBoxInfo) {
		return null;
	}
	
	public String getName(XApplet applet) {
		return getName(getStat(), applet);
	}
	
	static public String getName(int i, XApplet applet) {
		switch (i) {
			case MEDIAN:
				return applet.translate("Median");
			case MEAN:
				return applet.translate("Mean");
			default:
				return "";
		}
	}
}
