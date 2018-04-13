package inference;

import dataView.*;
import valueList.*;


public class ProportionBelowView extends ProportionView {
//	static public final String PROPORTION_BELOW = "proportionBelow";
	
	protected double targetValue = Double.NEGATIVE_INFINITY;
	
	public ProportionBelowView(DataSet theData, String variableKey, XApplet applet) {
		super(theData, variableKey, applet);
	}
	
	public void setTargetValue(double targetValue) {
		this.targetValue = targetValue;
		repaint();
	}
	
	protected String getValueString() {
		NumVariable variable = (NumVariable)getVariable(variableKey);
		
		int n = variable.noOfValues();
		if (n == 0)
			return null;
		
		int below = 0;
		
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			if (nextVal <= targetValue)
				below++;
		}
		
		NumValue propn = new NumValue(below / (double)n, 4);
		return propn.toString();
	}
}
