package models;

import java.util.*;

import dataView.*;
import utils.*;
import random.*;


abstract public class CoreModelDataSet extends DataSet {
	static final private String DATA_COUNT_PARAM = "noOfDataSets";
	
	static final private String DATA_NAME_PARAM = "dataName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_RANDOM_PARAM = "xRandom";
	
	static final private String LABEL_NAME_PARAM = "labelName";
	static final private String LABELS_PARAM = "labels";
	
	static final private String DESCRIPTION_PARAM = "description";
	static final private String QUESTION_PARAM = "question";
	
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String DECIMALS_PARAM = "decimals";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String X_AXIS_INFO_PARAM = "xAxis";
	
	static final private String SUMMARY_AXIS_PARAM = "summaryAxis";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	
	static final private String INITIAL_R2_PARAM = "initialR2";
	
	
	private String dataName[];
	private String yVarName[];
	private String xVarName[];
	
	private String yValueString[];
	protected String yLabelString[];
	private String xValueString[];
	private String xLabelString[];
	
	protected String xRandomDistn[];
	
	private String yAxisInfo[];
	private String xAxisInfo[];
	
	private String description[];
	private String question[];
	private int responseDecimals[];
	
	private String summaryAxisInfo[];
	private int summaryDecimals[];
	
	protected int noOfDataSets;
	protected int currentDataSetIndex = 0;
	
	protected String yKey = "y";
	
	public CoreModelDataSet(XApplet applet) {
		String countString = applet.getParameter(DATA_COUNT_PARAM);
		noOfDataSets = (countString == null) ? 1 : Integer.parseInt(countString);
		
		dataName = readStrings(DATA_NAME_PARAM, applet, noOfDataSets);
		yVarName = readStrings(Y_VAR_NAME_PARAM, applet, noOfDataSets);
		xVarName = readStrings(X_VAR_NAME_PARAM, applet, noOfDataSets);
		
		yValueString = readStrings(Y_VALUES_PARAM, applet, noOfDataSets);
		yLabelString = readStrings(Y_LABELS_PARAM, applet, noOfDataSets);
		xValueString = readStrings(X_VALUES_PARAM, applet, noOfDataSets);
		xLabelString = readStrings(X_LABELS_PARAM, applet, noOfDataSets);
		
		xRandomDistn = readStrings(X_RANDOM_PARAM, applet, noOfDataSets);
		
		yAxisInfo = readStrings(Y_AXIS_INFO_PARAM, applet, noOfDataSets);
		xAxisInfo = readStrings(X_AXIS_INFO_PARAM, applet, noOfDataSets);
		
		description = readStrings(DESCRIPTION_PARAM, applet, noOfDataSets);
		question = readStrings(QUESTION_PARAM, applet, noOfDataSets);
		
		responseDecimals = readInts(DECIMALS_PARAM, applet, noOfDataSets);
		
		summaryAxisInfo = readStrings(SUMMARY_AXIS_PARAM, applet, noOfDataSets);
		summaryDecimals = readInts(SUMMARY_DECIMALS_PARAM, applet, noOfDataSets);
		
		addExplanVariables(applet);
		
		String initialR2String = applet.getParameter(INITIAL_R2_PARAM);
		boolean adjustedY = (initialR2String != null);
		
		String dataYKey = adjustedY ? "rawY" : "y";
		
		if (yValueString == null)
			addResponseModel(dataYKey, applet);
		else
			addDataVariable(dataYKey, yVarName, yValueString, yLabelString);
		
		if (adjustedY) {
			double initialR2 = Double.parseDouble(initialR2String);
			addAdjustedVariable(yKey, initialR2, dataYKey, applet);
		}
		
		addLeastSquaresFit(applet);
		updateForNewSample();
		
		String labelName = applet.getParameter(LABEL_NAME_PARAM);
		if (labelName != null)
			addLabelVariable("label", labelName, applet.getParameter(LABELS_PARAM));
	}
	
	public void setResponseKey(String yKey) {
		this.yKey = yKey;
	}
	
	private String getSuffix(int dataIndex) {
		return (dataIndex == 0) ? "" : String.valueOf(dataIndex + 1);
	}
	
	protected String[] readStrings(String param, XApplet applet, int noOfDataSets) {
		String result[] = null;
		if (applet.getParameter(param) != null) {
			result = new String[noOfDataSets];
			for (int i=0 ; i<noOfDataSets ; i++)
				result[i] = applet.getParameter(param + getSuffix(i));
		}
		return result;
	}
	
	protected int[] readInts(String param, XApplet applet, int noOfDataSets) {
		int result[] = null;
		if (applet.getParameter(param) != null) {
			result = new int[noOfDataSets];
			for (int i=0 ; i<noOfDataSets ; i++) {
				String intString = applet.getParameter(param + getSuffix(i));
				result[i] = (intString == null) ? result[0] : Integer.parseInt(intString);
			}
		}
		return result;
	}
	
	protected void addExplanVariables(XApplet applet) {
		if (xValueString == null) {
			if (xLabelString == null) {
				RandomNormal generator = new RandomNormal(xRandomDistn[0]);
				NumSampleVariable xVar = new NumSampleVariable(xVarName[0], generator, 10);
				xVar.setSampleSize(generator.getSampleSize());
				xVar.generateNextSample();
				addVariable("x", xVar);
			}
			else {
				RandomMultinomial generator = new RandomMultinomial(xRandomDistn[0]);
				CatSampleVariable xVar = new CatSampleVariable(xVarName[0], generator, Variable.USES_REPEATS);
				xVar.setSampleSize(generator.getSampleSize());
				xVar.generateNextSample();
				addVariable("x", xVar);
			}
		}
		else
			addDataVariable("x", xVarName, xValueString, xLabelString);
		
		doXAdjustment(0);
	}
	
	protected void addDataVariable(String key, String[] varName, String[] valueString,
																																	String[] labelString) {
		String localVarName = null;
		if (varName != null)
			localVarName = varName[0];
		if (labelString == null)
			addNumVariable(key, localVarName, valueString[0]);
		else {
			CatVariable v = new CatVariable(localVarName, CatVariable.USES_REPEATS);
			v.readLabels(labelString[0]);
			v.readValues(valueString[0]);			
			addVariable(key, v);
		}
	}
	
	protected void addErrorVariable(XApplet applet) {
		String seedString = applet.getParameter(RANDOM_SEED_PARAM);
		int count = ((Variable)getVariable("x")).noOfValues();
		String randomParams = String.valueOf(count) + " 0.0 1.0 " + seedString + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(count);
		error.generateNextSample();
		addVariable("error", error);
	}
	
	public void addBasicComponents() {
		int decimals = getResponseDecimals();
			BasicComponentVariable totalComp = new BasicComponentVariable("total", this, "x", yKey,
											"ls", BasicComponentVariable.TOTAL, decimals);
		addVariable("total", totalComp);
			BasicComponentVariable explainedComp = new BasicComponentVariable("explained", this, "x", yKey,
											"ls", BasicComponentVariable.EXPLAINED, decimals);
		addVariable("explained", explainedComp);
			BasicComponentVariable residComp = new BasicComponentVariable("resid", this, "x", yKey,
											"ls", BasicComponentVariable.RESIDUAL, decimals);
		addVariable("resid", residComp);
	}
	
	abstract protected void addResponseModel(String yKey, XApplet applet);
	
	abstract protected void addLeastSquaresFit(XApplet applet);
	
	abstract protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet);
	
//---------------------------------------------------------------
	
	public XChoice dataSetChoice(XApplet applet) {
		if (dataName == null || dataName.length <= 1)
			return null;
		XChoice theChoice = new XChoice(applet);
		for (int i=0 ; i<dataName.length ; i++)
			theChoice.addItem(dataName[i]);
		return theChoice;
	}
	
	protected void updateOneVariable(String key, String[] varName, String[] valueString,
																																	String[] labelString) {
		Variable v = (Variable)getVariable(key);
		if (labelString != null && labelString[currentDataSetIndex] != null)
			((CatVariable)v).readLabels(labelString[currentDataSetIndex]);
		v.readValues(valueString[currentDataSetIndex]);
		if (varName[currentDataSetIndex] != null)
			v.name = varName[currentDataSetIndex];
	}
	
	public void updateForNewSample() {
		CoreModelVariable lsFit = (CoreModelVariable)getVariable("ls");
		if (lsFit != null)
			lsFit.updateLSParams(yKey);
	}
	
	public boolean changeDataSet(int dataIndex) {
		if (dataIndex != currentDataSetIndex) {
			currentDataSetIndex = dataIndex;
			
			if (xValueString == null) {
				SampleInterface xVar = (SampleInterface)getVariable("x");
				StringTokenizer st = new StringTokenizer(xRandomDistn[dataIndex]);
				xVar.setSampleSize(Integer.parseInt(st.nextToken()));
			}
			else
				updateOneVariable("x", xVarName, xValueString, xLabelString);
			doXAdjustment(dataIndex);
			
			if (yValueString == null) {
				Variable xVar = (Variable)getVariable("x");
				NumSampleVariable error = (NumSampleVariable)getVariable("error");
				error.setSampleSize(xVar.noOfValues());
				error.generateNextSample();
			}
			else
				updateOneVariable(yKey, yVarName, yValueString, yLabelString);
			
			updateForNewSample();
			
			return true;
		}
		else
			return false;
	}
	
	protected void doXAdjustment(int dataIndex) {
										//	Only used by PureRegnDataSet to create or change Num version of "x"
										//	and by MultiRegnDataSet to change "z"
	}
	
	public boolean changeDataSet(int dataIndex, XTextArea descriptionArea) {
		if (changeDataSet(dataIndex)) {
			if (descriptionArea != null)
				descriptionArea.setText(currentDataSetIndex);
			return true;
		}
		else
			return false;
	}
	
	public boolean changeDataSet(int dataIndex, XTextArea descriptionArea, XTextArea questionArea) {
		if (changeDataSet(dataIndex, descriptionArea)) {
			if (questionArea != null)
				questionArea.setText(currentDataSetIndex);
			return true;
		}
		else
			return false;
	}
	
//---------------------------------------------------------------
	
	public String getDataName() {
		if (dataName == null)
			return null;
		String result = dataName[currentDataSetIndex];
		return (result == null) ? dataName[0] : result;
	}
	
	public String getYVarName() {
		if (yVarName == null)
			return "";
		String result = yVarName[currentDataSetIndex];
		return (result == null) ? yVarName[0] : result;
	}
	
	public String getXVarName() {
		if (xVarName == null)
			return "";
		String result = xVarName[currentDataSetIndex];
		return (result == null) ? xVarName[0] : result;
	}
	
	public String[] getDescriptionStrings() {
		return description;
	}
	
	public String[] getQuestionStrings() {
		return question;
	}
	
	public String getYAxisInfo() {
		if (yAxisInfo == null)
			return null;
		String result = yAxisInfo[currentDataSetIndex];
		return (result == null) ? yAxisInfo[0] : result;
	}
	
	public String getXAxisInfo() {
		if (xAxisInfo == null)
			return null;
		String result = xAxisInfo[currentDataSetIndex];
		return (result == null) ? xAxisInfo[0] : result;
	}
	
	public String getSummaryAxisInfo() {
		String result = summaryAxisInfo[currentDataSetIndex];
		return (result == null) ? summaryAxisInfo[0] : result;
	}
	
	public int getSummaryDecimals() {
		return summaryDecimals[currentDataSetIndex];
	}
	
	public int getResponseDecimals() {
		if (responseDecimals == null)
			return 0;
		return responseDecimals[currentDataSetIndex];
	}

}