package variance;

import java.util.*;

import dataView.*;
import models.*;


public class PureRegnDataSet extends CoreModelDataSet {
	static final private String FACTOR_MODEL_PARAM = "factorModel";
	static final private String QUAD_R2_PARAM = "quadR2";				//	initial propn of explained Ssq that is quad
	static final private String TARGET_PARAM = "targetMeanSD";	//	only for adjusted variable
	static final private String FACTOR_EFFECTS_PARAM = "factorEffect";	//	base factor effects for adjusted variable
	
	static final public String kLinLsKey = "lsLin";
	static final public String kQuadLsKey = "lsQuad";
	static final public String kFactorLsKey = "lsFactor";
	
	public PureRegnDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void doXAdjustment(int dataIndex) {
		CatVariable xVar = (CatVariable)getVariable("x");
		double xCatValue[] = new double[xVar.noOfCategories()];
		double xValues[] = new double[xVar.noOfValues()];
		
		for (int i=0 ; i<xCatValue.length ; i++)
			xCatValue[i] = Double.parseDouble(xVar.getLabel(i).toString());
		
		for (int i=0 ; i<xValues.length ; i++) {
			int valueIndex = xVar.labelIndex(xVar.valueAt(i));
			xValues[i] = xCatValue[valueIndex];
		}
		
		NumVariable xNumVar = (NumVariable)getVariable("xNum");
		if (xNumVar == null) {
			xNumVar = new NumVariable(xVar.name);
			addVariable("xNum", xNumVar);
		}
		xNumVar.setValues(xValues);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			GroupsModelVariable yDistn = new GroupsModelVariable(getYVarName(), this, "x");
			yDistn.setParameters(applet.getParameter(FACTOR_MODEL_PARAM));
		addVariable("model", yDistn);
		
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, "x", "error",
																															"model", getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		LinearModel linFit = new LinearModel("linear least squares", this, "xNum");
		addVariable(kLinLsKey, linFit);
		
		QuadraticModel quadFit = new QuadraticModel("quadratic least squares", this, "xNum");
		addVariable(kQuadLsKey, quadFit);		//	Only needed when comparing factor and quad analyses
																				//	in QuadFactorCompareApplet
		
		GroupsModelVariable factorFit = new GroupsModelVariable("factor least squares", this, "x");
		addVariable(kFactorLsKey, factorFit);
	}
	
	public void updateForNewSample() {
		CoreModelVariable linFit = (CoreModelVariable)getVariable(kLinLsKey);
		if (linFit != null)
			linFit.updateLSParams(yKey);
		CoreModelVariable quadFit = (CoreModelVariable)getVariable(kQuadLsKey);
		if (quadFit != null)
			quadFit.updateLSParams(yKey);
		CoreModelVariable factorFit = (CoreModelVariable)getVariable(kFactorLsKey);
		if (factorFit != null)
			factorFit.updateLSParams(yKey);
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
		NumVariable rawY = (NumVariable)getVariable(rawYKey);
		
		StringTokenizer st = new StringTokenizer(applet.getParameter(TARGET_PARAM));
		double targetMean = Double.parseDouble(st.nextToken());
		double targetSD = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(applet.getParameter(FACTOR_EFFECTS_PARAM));
		int nEffects = st.countTokens();
		double baseEffects[] = new double[nEffects];
		for (int i=0 ; i<nEffects ; i++)
			baseEffects[i] = Double.parseDouble(st.nextToken());
		
		AdjustedPureVariable adjY = new AdjustedPureVariable(rawY.name, this, "xNum", "x", rawYKey,
																	baseEffects, targetMean, targetSD, rawY.getMaxDecimals());
		
		String initialQuadR2String = applet.getParameter(QUAD_R2_PARAM);	//	only used by QuadAdjustSsqApplet
		double initialQuadR2 = Double.parseDouble(initialQuadR2String);
		adjY.setR2(initialR2, initialQuadR2);
		addVariable(newYKey, adjY);
	}

}