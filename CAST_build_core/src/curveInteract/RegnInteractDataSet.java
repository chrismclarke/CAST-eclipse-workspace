package curveInteract;

import dataView.*;
import models.*;
import coreVariables.*;

import multiRegn.*;


public class RegnInteractDataSet extends MultiRegnDataSet {
	static final public String xInteractKeys[] = {"x", "z", "xz"};
	
	static final public double kXOnlyInteractConstraints[] = {Double.NaN, Double.NaN, 0.0, 0.0};
	static final public double kZOnlyInteractConstraints[] = {Double.NaN, 0.0, Double.NaN, 0.0};
	static final public double kNoInteractConstraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0};
	
	public RegnInteractDataSet(XApplet applet) {
		super(applet);
	}
	
	protected String[] xKeys() {
		return xInteractKeys;
	}
	
	protected int noOfParams() {
		return 4;
	}
	
	protected void addExplanVariables(XApplet applet) {
		super.addExplanVariables(applet);
		
		addVariable("xz", new ProductVariable("xz", this, "x", "z"));
	}
	
	public void updateForNewSample() {
		super.updateForNewSample();
		MultipleRegnModel lsXFit = (MultipleRegnModel)getVariable("lsX");
		if (lsXFit != null)
			lsXFit.updateLSParams("y", kXOnlyInteractConstraints);
		MultipleRegnModel lsZFit = (MultipleRegnModel)getVariable("lsZ");
		if (lsZFit != null)
			lsZFit.updateLSParams("y", kZOnlyInteractConstraints);
		MultipleRegnModel lsXZFit = (MultipleRegnModel)getVariable("lsXZ");
		if (lsXZFit != null)
			lsXZFit.updateLSParams("y", kNoInteractConstraints);
	}

}