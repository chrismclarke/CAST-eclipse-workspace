package randomStatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreSummaries.*;


public class SampleMeanApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String RANDOM_NORMAL_PARAM = "random";
	static final protected String DATA_DECIMALS_PARAM = "dataDecimals";
	static final protected String MEAN_NAME_PARAM = "meanName";
	static final protected String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	
	protected RandomNormal generator;
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected DataPlusDistnInterface dataView, summaryView;
	
	protected RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private NumValue modelMean, modelSD;
	protected int noOfValues;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(getDataViewProportion(), 10, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		dataPanel.add(ProportionLayout.TOP, dataPanel(data, "y", "model"));
		dataPanel.add(ProportionLayout.BOTTOM, summaryPanel(summaryData, "mean", "theory"));
		
		add("Center", dataPanel);
		add("South", controlPanel(data, summaryData, "mean"));
	}
	
	protected double getDataViewProportion() {
		return 0.5;
	}
	
	protected void generateInitialSample(SummaryDataSet summaryData) {
		setTheoryParameters(summaryData, "theory");
		summaryData.takeSample();
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, decimals);
		data.addVariable("y", y);
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
		noOfValues = Integer.parseInt(st.nextToken());
		modelMean = new NumValue(st.nextToken());
		modelSD = new NumValue(st.nextToken());
		dataDistn.setParams(modelMean.toString() + " " + modelSD.toString());
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		int decimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		MeanVariable mean = new MeanVariable(getParameter(MEAN_NAME_PARAM), "y", decimals);
		
		summaryData.addVariable("mean", mean);
		
		NormalDistnVariable meanDistn = new NormalDistnVariable("mean distn");
		summaryData.addVariable("theory", meanDistn);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
		NormalDistnVariable meanDistn = (NormalDistnVariable)summaryData.getVariable(theoryKey);
		NumValue meanSD = new NumValue(modelSD.toDouble() / Math.sqrt(noOfValues), modelSD.decimals + 6);
		meanDistn.setParams(modelMean.toString() + " " + meanSD.toString());
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = getAxis(data, variableKey);
		thePanel.add("Bottom", theHorizAxis);
		
		JitterPlusNormalView localView = getDataView(data, variableKey, modelKey, theHorizAxis,
																			DataPlusDistnInterface.CONTIN_DISTN);
		dataView = localView;
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = getAxis(data, variableKey);
		thePanel.add("Bottom", theHorizAxis);
		
		JitterPlusNormalView localView = getDataView(data, variableKey, modelKey, theHorizAxis,
																					DataPlusDistnInterface.NO_DISTN);
		summaryView = localView;
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected HorizAxis getAxis(DataSet data, String variableKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private JitterPlusNormalView getDataView(DataSet data, String variableKey,
										String modelKey, HorizAxis theHorizAxis, int densityDisplayType) {
		JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, theHorizAxis, modelKey, 1.0);
		dataView.setActiveNumVariable(variableKey);
		dataView.setShowDensity(densityDisplayType);
		return dataView;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
		thePanel.add(topControlPanel(data, summaryData, summaryKey));
		thePanel.add(bottomControlPanel(data, summaryData));
		
		return thePanel;
	}
	
	protected String getSampleButtonText() {
		return translate("Take sample");
	}
	
	protected XPanel topControlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		takeSampleButton = new RepeatingButton(getSampleButtonText(), this);
		topPanel.add(takeSampleButton);
		
		topPanel.add(new OneValueView(summaryData, summaryKey, this));
		return topPanel;
	}
	
	protected XPanel bottomControlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		bottomPanel.add(accumulateCheck);
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel(translate("No of samples") + " = ");
		bottomPanel.add(theCount);
		
		return bottomPanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}