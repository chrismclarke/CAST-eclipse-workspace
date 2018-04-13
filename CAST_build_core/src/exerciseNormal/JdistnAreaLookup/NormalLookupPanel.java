package exerciseNormal.JdistnAreaLookup;

import dataView.*;
import axis.*;
import exercise2.*;

import exerciseNormalProg.*;


public class NormalLookupPanel extends CoreLookupPanel {
	static final public boolean SINGLE_DENSITY = true;
	static final public boolean MULTIPLE_DENSITIES = false;
	
	
	public NormalLookupPanel(DataSet data, String distnKey, CoreLookupApplet exerciseApplet,
																											boolean highAndLow, boolean singleDensity) {
		super(data, distnKey, exerciseApplet, highAndLow);
		if (singleDensity)
			((ContinDistnLookupView)distnView).setSingleDensity();
	}
	
	public NormalLookupPanel(DataSet data, String distnKey, CoreLookupApplet exerciseApplet,
																																							boolean highAndLow) {
		this(data, distnKey, exerciseApplet, highAndLow, MULTIPLE_DENSITIES);
	}
	
	protected CoreDistnLookupView getDistnLookupView(DataSet data, String distnKey,
																	ExerciseApplet exerciseApplet, HorizAxis theAxis, boolean highAndLow) {
		return new ContinDistnLookupView(data, exerciseApplet, theAxis, distnKey, highAndLow);
	}
	
	public double getPixError(NumValue lowLimit, NumValue highLimit) {
		ContinDistnLookupView normalView = (ContinDistnLookupView)distnView;
		double maxError = 0.0;
		if (lowLimit != null)
			maxError += normalView.getPixError(lowLimit.toDouble());
		if (highLimit != null)
			maxError += normalView.getPixError(highLimit.toDouble());
		return maxError;
	}
	
	public double twoPixelValue() {
		ContinDistnLookupView normalView = (ContinDistnLookupView)distnView;
		return normalView.twoPixelValue();
	}
	
}