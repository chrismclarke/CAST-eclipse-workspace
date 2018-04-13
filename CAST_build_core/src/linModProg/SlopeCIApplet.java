package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import models.*;

import inferenceProg.*;
import linMod.*;


public class SlopeCIApplet extends SampleIntervalApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String X_AXIS_PARAM = "horizAxis";
	static final private String Y_AXIS_PARAM = "vertAxis";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String RANDOM_SEED = "randomSeed";
	
	private int getValueCount(DataSet data) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		return xVar.noOfValues();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String seedString = getParameter(RANDOM_SEED);
		noOfValues = getValueCount(data);
		String randomParams = String.valueOf(noOfValues) + " 0.0 1.0 " + seedString + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(noOfValues);
		data.addVariable("error", error);
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		
		modelMean = yDistn.getSlope();
		
		ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model", 10);
		data.addVariable("y", yData);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		int decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slope distn", sourceData,
																							"x", "y", decimals);
		summaryData.addVariable("slopeDistn", slopeDistn);
		
		double level = Double.parseDouble(getParameter(CONFIDENCE_LEVEL_PARAM));
		SlopeCIVariable ci = new SlopeCIVariable(getParameter(MEAN_NAME_PARAM), level,
													noOfValues - 2, summaryData, "slopeDistn", decimals);
		summaryData.addVariable("ci", ci);
		
		return summaryData;
	}
	
	private XPanel yNamePanel(DataSet data, VertAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(X_AXIS_PARAM));
			Variable x = (Variable)data.getVariable("x");
			xAxis.setAxisName(x.name);
			plotPanel.add("Bottom", xAxis);
			
			VertAxis yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
			plotPanel.add("Left", yAxis);
			
			SampleLineView theView = new SampleLineView(data, this, xAxis, yAxis, "x", "y", "model");
			theView.setShowData(true);
			theView.lockBackground(Color.white);
			
			plotPanel.add("Center", theView);
		
		thePanel.add("Center", plotPanel);
		thePanel.add("North", yNamePanel(data, yAxis));
		
		return thePanel;
	}
	
	protected boolean onlyShowSummaryScale() {
		return true;
	}
	
	protected void doTakeSample() {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)summaryData.getVariable("slopeDistn");
		slopeDistn.resetSource();
		summaryData.takeSample();
	}
}