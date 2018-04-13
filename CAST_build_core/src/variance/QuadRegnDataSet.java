package variance;

import java.util.*;

import dataView.*;
import models.*;


public class QuadRegnDataSet extends CoreModelDataSet {
	static final private String QUAD_MODEL_PARAM = "quadModel";
	static final private String QUAD_R2_PARAM = "quadR2";				//	initial propn of explained Ssq that is quad
	static final private String TARGET_PARAM = "targetMeanSD";	//	only for adjusted variable
	
	static final public String kLinLsKey = "lsLin";
	static final public String kQuadLsKey = "lsQuad";
	
	public QuadRegnDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			QuadraticModel yDistn = new QuadraticModel(getYVarName(), this, "x");
			yDistn.setParameters(applet.getParameter(QUAD_MODEL_PARAM));
		addVariable("model", yDistn);
		
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, "x", "error", "model",
																																			getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		LinearModel linFit = new LinearModel("linear least squares", this, "x");
		addVariable(kLinLsKey, linFit);
		
		QuadraticModel quadFit = new QuadraticModel("quadratic least squares", this, "x");
		addVariable(kQuadLsKey, quadFit);
	}
	
	public void updateForNewSample() {
		CoreModelVariable linFit = (CoreModelVariable)getVariable(kLinLsKey);
		if (linFit != null)
			linFit.updateLSParams(yKey);
		CoreModelVariable quadFit = (CoreModelVariable)getVariable(kQuadLsKey);
		if (quadFit != null)
			quadFit.updateLSParams(yKey);
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
		NumVariable rawY = (NumVariable)getVariable(rawYKey);
		
		StringTokenizer st = new StringTokenizer(applet.getParameter(TARGET_PARAM));
		double targetMean = Double.parseDouble(st.nextToken());
		double targetSD = Double.parseDouble(st.nextToken());
		
		AdjustedQuadVariable adjY = new AdjustedQuadVariable(rawY.name, this, "x", rawYKey,
																				targetMean, targetSD, rawY.getMaxDecimals());
		
		String initialQuadR2String = applet.getParameter(QUAD_R2_PARAM);	//	only used by QuadAdjustSsqApplet
		double initialQuadR2 = Double.parseDouble(initialQuadR2String);
		adjY.setR2(initialR2, initialQuadR2);
		addVariable(newYKey, adjY);
	}

}