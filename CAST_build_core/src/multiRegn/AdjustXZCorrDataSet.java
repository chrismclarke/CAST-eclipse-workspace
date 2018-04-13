package multiRegn;

import dataView.*;
import models.*;


public class AdjustXZCorrDataSet extends MultiRegnDataSet {
	static final private String INITIAL_XZ_R2_PARAM = "initialXZR2";
	
	private double initialXZR2;
	
	public AdjustXZCorrDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addExplanVariables(XApplet applet) {
		super.addExplanVariables(applet);
		
		NumVariable rawZVar = (NumVariable)getVariable("zRaw");
		
		AdjustedSsqVariable adjZ = new RegnAdjustedSsqVariable(zVarName[0], this, "x", "zRaw",
																														rawZVar.getMaxDecimals());
		
		initialXZR2 = Double.parseDouble(applet.getParameter(INITIAL_XZ_R2_PARAM));
		adjZ.setR2(initialXZR2);
		addVariable("z", adjZ);
	}
	
	protected String getZDataKey() {
		return "zRaw";
	}
	
	public double getInitialXZR2() {
		return initialXZR2;
	}

}