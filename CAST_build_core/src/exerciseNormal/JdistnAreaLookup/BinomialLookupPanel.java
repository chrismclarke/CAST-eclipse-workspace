package exerciseNormal.JdistnAreaLookup;

import dataView.*;
import axis.*;
import exercise2.*;

import exerciseNormalProg.*;


public class BinomialLookupPanel extends CoreLookupPanel {
	
	public BinomialLookupPanel(DataSet data, String distnKey, CoreLookupApplet exerciseApplet,
																																	boolean highAndLow) {
		super(data, distnKey, exerciseApplet, highAndLow);
	}
	
	protected CoreDistnLookupView getDistnLookupView(DataSet data, String distnKey,
																	ExerciseApplet exerciseApplet, HorizAxis theAxis, boolean highAndLow) {
		return new DiscreteDistnLookupView(data, exerciseApplet, theAxis, distnKey, highAndLow);
	}
}