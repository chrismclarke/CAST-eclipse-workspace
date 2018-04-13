package test;

import dataView.*;
import randomStat.*;


public class ProbPValueView extends DiscreteProbValueView {
	
	private String kPValueString;
	
	public ProbPValueView(DataSet theData, String variableKey, XApplet applet, int maxN) {
		super(theData, variableKey, applet, maxN);
		kPValueString = applet.translate("p-value") + " = ";
	}

//--------------------------------------------------------------------------------
	
	protected String getMaxLabelString() {
		return kPValueString + super.getMaxLabelString();
	}
	
	protected String getLabelString() {
		return kPValueString + super.getLabelString();
	}
}
