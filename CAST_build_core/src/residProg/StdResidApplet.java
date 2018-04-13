package residProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import models.*;

import regnProg.*;
import regnView.*;
import resid.*;


public class StdResidApplet extends MultipleScatterApplet {
	static final private String SAMPLE_SIZE_PARAM = "sampleSize";
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String RANDOM_SEED = "randomSeed";
	static final private String RESID_NAME_PARAM = "residName";
	
	static final private String kResidAxisInfo = "-4 4 -4 1";
	
	protected RepeatingButton takeSampleButton;
//	private XCheckbox accumulateCheck;
	
	private XChoice sampleSizeChoice;
	private int sampleSize[] = null;
	private int currentSizeIndex = 0;
	
	private String xValues[];
	
	private void readSampleSizes() {
		String sizeString = getParameter(SAMPLE_SIZE_PARAM);
		StringTokenizer st = new StringTokenizer(sizeString);
		int nx = st.countTokens();
		sampleSize = new int[nx];
		for (int i=0 ; i<nx ; i++)
			sampleSize[i] = Integer.parseInt(st.nextToken());
	}
	
	protected DataSet readData() {
		readSampleSizes();
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
		
			ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model", 10);
		data.addVariable("y", yData);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		summaryData.takeSample();
		
			LinearModel lsLine = new LinearModel("LS line", sourceData, "x");
			lsLine.updateLSParams("y");
		sourceData.addVariable("lsLine", lsLine);
		
			StdResidValueVariable resid = new StdResidValueVariable(getParameter(RESID_NAME_PARAM),
													sourceData, "x", "y", "lsLine", 3);
		sourceData.addVariable("resid", resid);
		
		return summaryData;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
			
		thePanel.add(ProportionLayout.LEFT, createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		
		thePanel.add(ProportionLayout.RIGHT, createPlotPanel(data, false, "x", "resid", null,
							getParameter(X_AXIS_INFO_PARAM), kResidAxisInfo, 1));
		
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0) {
			TransResidView theView = new TransResidView(data, this, theHorizAxis, theVertAxis, "x", "y", "lsLine");
			theView.setRetainLastSelection(true);
			return theView;
		}
		else {
			StdResidPlotView theView = new StdResidPlotView(data, this, theHorizAxis, theVertAxis, "x", "resid");
			theView.setRetainLastSelection(true);
			return theView;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
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
		
			takeSampleButton = new RepeatingButton(translate("Another data set"), this);
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	protected void changeSampleSize(int newChoice) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.readValues(xValues[newChoice]);
		summaryData.changeSampleSize(sampleSize[newChoice] + 1);
		LinearModel lsLine = (LinearModel)data.getVariable("lsLine");
		lsLine.updateLSParams("y");
		data.variableChanged("y");
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			LinearModel lsLine = (LinearModel)data.getVariable("lsLine");
			lsLine.updateLSParams("y");
			data.variableChanged("y");
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