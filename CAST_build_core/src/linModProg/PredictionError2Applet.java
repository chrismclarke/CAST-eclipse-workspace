package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import coreGraphics.*;
import models.*;

import linMod.*;


public class PredictionError2Applet extends XApplet {
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String X_AXIS_PARAM = "horizAxis";
	static final private String Y_AXIS_PARAM = "vertAxis";
	static final private String ERROR_AXIS_PARAM = "errorAxis";
	static final private String RANDOM_SEED = "randomSeed";
	static final private String X_ERROR_DISPLAY_SEED = "xDisplayError";
	
	static final private Color kTitleColor = new Color(0x990000);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private XChoice sampleSizeChoice;
	private int sampleSize[] = null;
	private int currentSizeIndex = 0;
	
	private String xValues[];
	
	public void setupApplet() {
		readSampleSizes();
		
		data = readData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout());
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
			dataPanel.add(ProportionLayout.TOP, mainPanel(data, summaryData,
											PredErrorSummaryVariable.PREDICT_MEAN, translate("Estimating mean"),
											translate("Estimation error"), "meanError"));
			dataPanel.add(ProportionLayout.BOTTOM, mainPanel(data, summaryData,
											PredErrorSummaryVariable.PREDICT_NEW_VALUE, translate("Predicting new Y"),
											translate("Prediction error"), "newYError"));
		
		add("Center", dataPanel);
		
		add("South", controlPanel(data, summaryData));
	}
	
	private void readSampleSizes() {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int nx = st.countTokens();
		sampleSize = new int[nx];
		for (int i=0 ; i<nx ; i++)
			sampleSize[i] = Integer.parseInt(st.nextToken());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		xValues = new String[sampleSize.length];
		for (int i=0 ; i<sampleSize.length ; i++)
			xValues[i] = getParameter(X_VALUES_PARAM + i);
			
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(xValues[0]);
		data.addVariable("x", xVar);
		
			String seedString = getParameter(RANDOM_SEED);
			int count = xVar.noOfValues();
			String randomParams = String.valueOf(count) + " 0.0 1.0 " + seedString + " 3.0";
			RandomNormal generator = new RandomNormal(randomParams);
			NumSampleVariable error = new NumSampleVariable("error", generator, 10);
			error.setSampleSize(count);
		data.addVariable("error", error);
		
			LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
			yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		
		ResponsePredictVariable yData = new ResponsePredictVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model");
		data.addVariable("y", yData);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
			int decimals = 10;
			PredErrorSummaryVariable meanError = new PredErrorSummaryVariable("Error in mean", "x", "y",
																"model", decimals, PredErrorSummaryVariable.PREDICT_MEAN);
		summaryData.addVariable("meanError", meanError);
		
			PredErrorSummaryVariable newYError = new PredErrorSummaryVariable("Error in new Y", "x", "y",
																"model", decimals, PredErrorSummaryVariable.PREDICT_NEW_VALUE);
		summaryData.addVariable("newYError", newYError);
		
		return summaryData;
	}
	
	private XPanel mainPanel(DataSet data, SummaryDataSet summaryData, int errorType,
																String predictionName, String errorName, String errorKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XLabel nameLabel = new XLabel(predictionName, XLabel.LEFT, this);
			nameLabel.setForeground(kTitleColor);
			nameLabel.setFont(getBigBoldFont());
		thePanel.add("North", nameLabel);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.75, 10));
		
			dataPanel.add(ProportionLayout.LEFT, bivarDisplayPanel(data, errorType));
			dataPanel.add(ProportionLayout.RIGHT, errorPanel(summaryData, errorKey, errorName));
			
		thePanel.add("Center", dataPanel);
		
		return thePanel;
	}
	
	private XPanel yNamePanel(String yName, VertAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(yName, XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	private XPanel errorPanel(SummaryDataSet summaryData, String errorKey, String errorName) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(ERROR_AXIS_PARAM));
			plotPanel.add("Left", yAxis);
			
			DotPlotView dataView = new DotPlotView(summaryData, this, yAxis, 1.0);
			dataView.setActiveNumVariable(errorKey);
			dataView.lockBackground(Color.white);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
		thePanel.add("North", yNamePanel(errorName, yAxis));
		
		return thePanel;
	}
	
	private XPanel bivarDisplayPanel(DataSet data, int errorType) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
			CoreVariable x = data.getVariable("x");
			xAxis.setAxisName(x.name);
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			plotPanel.add("Left", yAxis);
			
			double xErrorDisplay = Double.parseDouble(getParameter(X_ERROR_DISPLAY_SEED));
			PredictAndErrorView dataView = new PredictAndErrorView(data, this, xAxis, yAxis, "x", "y",
																														"model", errorType, xErrorDisplay);
			dataView.lockBackground(Color.white);
			
			plotPanel.add("Center", dataView);
		
		thePanel.add("Center", plotPanel);
//		thePanel.add("North", yNamePanel(getParameter(Y_VAR_NAME_PARAM), yAxis));
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XPanel sampleSizePanel = new XPanel();
			sampleSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			sampleSizePanel.add(new XLabel(translate("Sample size") + ":", XLabel.LEFT, this));
				sampleSizeChoice = new XChoice(this);
				for (int i=0 ; i<sampleSize.length ; i++)
					sampleSizeChoice.addItem("" + sampleSize[i]);
			
			sampleSizePanel.add(sampleSizeChoice);
		thePanel.add(sampleSizePanel);
		
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newChoice) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.readValues(xValues[newChoice]);
		summaryData.changeSampleSize(sampleSize[newChoice] + 1);
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == sampleSizeChoice) {
			int newChoice = sampleSizeChoice.getSelectedIndex();
			if (newChoice != currentSizeIndex) {
				currentSizeIndex = newChoice;
				changeSampleSize(newChoice);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}