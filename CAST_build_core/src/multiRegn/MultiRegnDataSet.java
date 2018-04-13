package multiRegn;

import java.util.*;

import dataView.*;
import random.*;
import models.*;


public class MultiRegnDataSet extends CoreModelDataSet {
	static final protected String MULTI_REGN_MODEL_PARAM = "multiRegnModel";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String Z_AXIS_INFO_PARAM = "zAxis";
	static final private String Z_RANDOM_PARAM = "zRandom";
	static final protected String LS_DECIMALS_PARAM = "lsDecimals";
	
	static final public String xKeys[] = {"x", "z"};
	
	static final private double kXOnlyConstraints[] = {Double.NaN, Double.NaN, 0.0};
	static final private double kZOnlyConstraints[] = {Double.NaN, 0.0, Double.NaN};
	
	protected String zVarName[];
	protected String zValueString[];
	protected String zLabelString[];
	protected String zAxisInfo[];
	protected String zRandomDistn[];
	
	public MultiRegnDataSet(XApplet applet) {
		super(applet);
	}
	
	public double[] getXOnlyConstraints() {
		return kXOnlyConstraints;
	}
	
	public double[] getZOnlyConstraints() {
		return kZOnlyConstraints;
	}
	
	protected void readZStrings(XApplet applet) {
		zVarName = readStrings(Z_VAR_NAME_PARAM, applet, noOfDataSets);
		zValueString = readStrings(Z_VALUES_PARAM, applet, noOfDataSets);
		zLabelString = readStrings(Z_LABELS_PARAM, applet, noOfDataSets);
		zAxisInfo = readStrings(Z_AXIS_INFO_PARAM, applet, noOfDataSets);
		zRandomDistn = readStrings(Z_RANDOM_PARAM, applet, noOfDataSets);
	}
	
	protected void addExplanVariables(XApplet applet) {
		readZStrings(applet);
		
		if (zValueString == null) {
			if (zLabelString == null) {
				RandomNormal generator = new RandomNormal(zRandomDistn[0]);
				NumSampleVariable zVar = new NumSampleVariable(zVarName[0], generator, 10);
				zVar.setSampleSize(generator.getSampleSize());
				zVar.generateNextSample();
				addVariable(getZDataKey(), zVar);
			}
			else {
				RandomMultinomial generator = new RandomMultinomial(zRandomDistn[0]);
				CatSampleVariable zVar = new CatSampleVariable(zVarName[0], generator, Variable.USES_REPEATS);
				zVar.setSampleSize(generator.getSampleSize());
				zVar.generateNextSample();
				addVariable(getZDataKey(), zVar);
			}
		}
		else
			addDataVariable(getZDataKey(), zVarName, zValueString, zLabelString);
		
		super.addExplanVariables(applet);
	}
	
	protected String getZDataKey() {
		return "z";						//	Overridden by AdjustXZCorrDataSet to "zRaw"
	}
	
	protected String[] xKeys() {
		return xKeys;
	}
	
	protected int noOfParams() {
		int n = 1;
		for (int i=0 ; i<xKeys().length ; i++)
			if (getVariable(xKeys()[i]) instanceof CatVariable)
				n += ((CatVariable)getVariable(xKeys()[i])).noOfCategories() - 1;
			else
				n ++;
		return n;
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			MultipleRegnModel yDistn = new MultipleRegnModel(getYVarName(), this, xKeys());
			yDistn.setParameters(applet.getParameter(MULTI_REGN_MODEL_PARAM));
		addVariable("model", yDistn);
		
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, xKeys(), "error", "model",
																																			getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		int coeffDecimals[] = new int[noOfParams()];
		for (int i=0 ; i<noOfParams() ; i++)
			coeffDecimals[i] = 9;
		String lsDecimalString = applet.getParameter(LS_DECIMALS_PARAM);
		if (lsDecimalString != null) {
			StringTokenizer st = new StringTokenizer(lsDecimalString);
			for (int i=0 ; i<noOfParams() ; i++)
				coeffDecimals[i] = Integer.parseInt(st.nextToken());
		}
		
		NumValue b[] = new NumValue[noOfParams()];
		for (int i=0 ; i<noOfParams() ; i++)
			b[i] = new NumValue(0.0, coeffDecimals[i]);
		NumValue s = new NumValue(0.0, 9);
		MultipleRegnModel lsFit = new MultipleRegnModel("least squares", this, xKeys(), b, s);
		addVariable("ls", lsFit);
	}
	
	protected void doXAdjustment(int dataIndex) {
		if (zValueString == null) {
			SampleInterface zVar = (SampleInterface)getVariable(getZDataKey());
			StringTokenizer st = new StringTokenizer(zRandomDistn[dataIndex]);
			zVar.setSampleSize(Integer.parseInt(st.nextToken()));
			zVar.generateNextSample();
		}
		else
			updateOneVariable(getZDataKey(), zVarName, zValueString, zLabelString);
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
	}
	
	public void addBasicComponents() {
		int decimals = getResponseDecimals();
			BasicComponentVariable totalComp = new BasicComponentVariable("total", this, xKeys(), yKey,
											"ls", BasicComponentVariable.TOTAL, decimals);
		addVariable("total", totalComp);
			BasicComponentVariable explainedComp = new BasicComponentVariable("explained", this, xKeys(), yKey,
											"ls", BasicComponentVariable.EXPLAINED, decimals);
		addVariable("explained", explainedComp);
			BasicComponentVariable residComp = new BasicComponentVariable("resid", this, xKeys(), yKey,
											"ls", BasicComponentVariable.RESIDUAL, decimals);
		addVariable("resid", residComp);
	}
	
	public String getZVarName() {
		if (zVarName == null)
			return "";
		String result = zVarName[currentDataSetIndex];
		return (result == null) ? zVarName[0] : result;
	}
	
	public String getZAxisInfo() {
		if (zAxisInfo == null)
			return null;
		String result = zAxisInfo[currentDataSetIndex];
		return (result == null) ? zAxisInfo[0] : result;
	}
	
	public void updateForNewSample() {
		super.updateForNewSample();
		MultipleRegnModel lsXFit = (MultipleRegnModel)getVariable("lsX");
		if (lsXFit != null)
			lsXFit.updateLSParams(yKey, getXOnlyConstraints());
		MultipleRegnModel lsZFit = (MultipleRegnModel)getVariable("lsZ");
		if (lsZFit != null)
			lsZFit.updateLSParams(yKey, getZOnlyConstraints());
	}

}