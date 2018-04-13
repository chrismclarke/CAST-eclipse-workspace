package test;

import dataView.*;
import axis.*;


public class DragPValueAxis extends DragValAxis {
	private String kPValueString;
	
	public DragPValueAxis(XApplet applet) {
		super(applet);
		kPValueString = applet.translate("p-value");
	}
	
	protected String getConstName() {
		return kPValueString;
	}
	
	public NumValue positionToNeatNumVal(int thePosition) throws AxisException {
		NumValue newVal = super.positionToNeatNumVal(thePosition);
		newVal.setValue(Math.max(0.0, Math.min(1.0, newVal.toDouble())));
		return newVal;
	}
}