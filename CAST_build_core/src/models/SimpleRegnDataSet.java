package models;

import dataView.*;


public class SimpleRegnDataSet extends CoreModelDataSet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	
	public SimpleRegnDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			LinearModel yDistn = new LinearModel(getYVarName(), this, "x");
			yDistn.setParameters(applet.getParameter(REGN_MODEL_PARAM));
		addVariable("model", yDistn);
		
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, "x", "error", "model",
																																			getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		LinearModel lsFit = new LinearModel("least squares", this, "x");
		addVariable("ls", lsFit);
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
		NumVariable rawY = (NumVariable)getVariable(rawYKey);
		AdjustedSsqVariable adjY = new RegnAdjustedSsqVariable(rawY.name, this, "x", rawYKey,
																														rawY.getMaxDecimals());
		adjY.setR2(initialR2);
		addVariable(newYKey, adjY);
	}

}