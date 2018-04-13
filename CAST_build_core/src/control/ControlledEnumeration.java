package control;

import java.awt.*;

import dataView.*;

public class ControlledEnumeration implements ValueEnumeration {
	static final private int kMaxRememberedVals = 9;
	
	static final public int ONE_OUTLIER = 1;
	static final public int ZONE_A_2_3 = 2;
	static final public int ZONE_AB_4_5 = 4;
	static final public int INCREASE_6 = 8;
	static final public int ONE_SIDE_9 = 16;
	static final public int ALL_PROBLEMS = ONE_OUTLIER | ZONE_A_2_3 | ZONE_AB_4_5
																							| INCREASE_6 | ONE_SIDE_9;
	
	static private String kOutlierString, kZoneAString, kZoneABString, kMonotonicString, kOneSideString;
	
	private ValueEnumeration e;
	private NumValue previous[] = new NumValue[kMaxRememberedVals];
	private int rememberedVals = 0;
	private double centre, lowerLimit, upperLimit, lowerABLimit, lowerBCLimit,
																						upperABLimit, upperBCLimit;
	private int problemFlags;
	
	static private void setupStrings(XApplet applet) {
		if (kOutlierString == null)
			kOutlierString = applet.translate("Value outside control limits");
		if (kZoneAString == null)
			kZoneAString = applet.translate("2 out of 3 values in Zone A");
		if (kZoneABString == null)
			kZoneABString = applet.translate("4 out of 5 values in Zones A or B on one side");
		if (kMonotonicString == null)
			kMonotonicString = applet.translate("6 values in a row increasing or decreasing");
		if (kOneSideString == null)
			kOneSideString = applet.translate("9 values on one side of centre");
	};
	
	static public int maxProblemWidth(Graphics g, int problemFlags, XApplet applet) {
		setupStrings(applet);
		FontMetrics fm = g.getFontMetrics();
		int maxWidth = 0;
		if ((problemFlags & ONE_OUTLIER) != 0)
			maxWidth = Math.max(maxWidth, fm.stringWidth(kOutlierString));
		if ((problemFlags & ZONE_A_2_3) != 0)
			maxWidth = Math.max(maxWidth, fm.stringWidth(kZoneAString));
		if ((problemFlags & ZONE_AB_4_5) != 0)
			maxWidth = Math.max(maxWidth, fm.stringWidth(kZoneABString));
		if ((problemFlags & INCREASE_6) != 0)
			maxWidth = Math.max(maxWidth, fm.stringWidth(kMonotonicString));
		if ((problemFlags & ONE_SIDE_9) != 0)
			maxWidth = Math.max(maxWidth, fm.stringWidth(kOneSideString));
		return maxWidth;
	}
	
	ControlledEnumeration(NumVariable variable, ControlLimitAxis controlAxis, int problemFlags,
																																		XApplet applet) {
		e = variable.values();
		this.problemFlags = problemFlags;
		
		setupStrings(applet);
		
		centre = controlAxis.getCentre();
		lowerLimit = controlAxis.getLowerLimit();
		upperLimit = controlAxis.getUpperLimit();
		lowerABLimit = controlAxis.getLowerABLimit();
		lowerBCLimit = controlAxis.getLowerBCLimit();
		upperABLimit = controlAxis.getUpperABLimit();
		upperBCLimit = controlAxis.getUpperBCLimit();
	}
	
	public boolean hasMoreValues() {
		return e.hasMoreValues();
	}
	
	public Value nextValue() {
		for (int i=kMaxRememberedVals-1 ; i>0 ; i--)
			previous[i] = previous[i-1];
		previous[0] = (NumValue)e.nextValue();
		if (rememberedVals < kMaxRememberedVals)
			rememberedVals++;
		return previous[0];
	}
	
	public double nextDouble() {
		return ((NumValue)nextValue()).toDouble();
	}
	
	public RepeatValue nextGroup() {
		return new RepeatValue(nextValue(), 1);
	}
	
	public ControlProblem getControlProblem() {
		ControlProblem problem = ((problemFlags & ONE_OUTLIER) != 0) ? getOutlierProblem() : null;
		if (problem == null && (problemFlags & ZONE_A_2_3) != 0)
			problem = getZoneAProblem();
		if (problem == null && (problemFlags & ZONE_AB_4_5) != 0)
			problem = getZoneABProblem();
		if (problem == null && (problemFlags & INCREASE_6) != 0)
			problem = getMonotonicProblem();
		if (problem == null && (problemFlags & ONE_SIDE_9) != 0)
			problem = getOneSizeProblem();
		return problem;
	}
	
	public ControlProblem getOutlierProblem() {
		if (previous[0].toDouble() > upperLimit || previous[0].toDouble() < lowerLimit)
			return new ControlProblem(kOutlierString, 0);
		else
			return null;
	}
	
	public ControlProblem getZoneAProblem() {
		if (rememberedVals < 3 || (previous[0].toDouble() <= upperABLimit && previous[0].toDouble() >= lowerABLimit))
			return null;
		
		if (previous[1].toDouble() > upperABLimit || previous[1].toDouble() < lowerABLimit
											|| previous[2].toDouble() > upperABLimit || previous[2].toDouble() < lowerABLimit)
			return new ControlProblem(kZoneAString, 2);
		else
			return null;
	}
	
	public ControlProblem getZoneABProblem() {
		if (rememberedVals < 5)
			return null;
		boolean topSide;
		if (previous[0].toDouble() > upperBCLimit)
			topSide = true;
		else if (previous[0].toDouble() < lowerBCLimit)
			topSide = false;
		else
			return null;
		
		int count = 1;
		for (int i=1 ; i<5 ; i++)
			if ((previous[i].toDouble() > upperBCLimit && topSide) || (previous[i].toDouble() < lowerBCLimit && !topSide))
				count++;
		
		if (count >= 4)
			return new ControlProblem(kZoneABString, 4);
		else
			return null;
	}
	
	public ControlProblem getMonotonicProblem() {
		if (rememberedVals < 6 || previous[0].toDouble() == previous[1].toDouble())
			return null;
		boolean increasing = previous[0].toDouble() > previous[1].toDouble();
		int run = 2;
		double lastValue = previous[1].toDouble();
		for (int i=2 ; i<rememberedVals ; i++)
			if (previous[i].toDouble()  != lastValue) {
				if (increasing == (lastValue > previous[i].toDouble())) {
					lastValue = previous[i].toDouble();
					run ++;
					if (run == 6)
						return new ControlProblem(kMonotonicString, 5);
				}
				else
					return null;
			}
		return null;
	}
	
	public ControlProblem getOneSizeProblem() {
		if (rememberedVals < 9)
			return null;
		boolean topSide = (previous[0].toDouble() > centre);
		for (int i=1 ; i<9 ; i++)
			if (topSide != (previous[i].toDouble() > centre))
				return null;
		
		return new ControlProblem(kOneSideString, 8);
	}
}