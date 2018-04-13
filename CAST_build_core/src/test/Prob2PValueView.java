package test;

import dataView.*;
import randomStat.*;


public class Prob2PValueView extends DiscreteProbValueView {
	
	private String kPValueString;
	
	public Prob2PValueView(DataSet theData, String variableKey, XApplet applet, int maxN) {
		super(theData, variableKey, applet, maxN);
		kPValueString = applet.translate("p-value") + " = 2 * ";
	}

//--------------------------------------------------------------------------------
	
	protected String getMaxLabelString() {
		return kPValueString + super.getMaxLabelString();
	}
	
	protected String getLabelString() {
		return kPValueString + super.getLabelString();
	}
	
	public double getValue() {
		return Math.min(1.0, super.getValue() * 2);
	}
}
