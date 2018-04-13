package linModProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import test.*;
import qnUtils.*;
import models.*;
import formula.*;

import linMod.*;
import randomStatProg.*;


public class SlopePValueApplet extends SampleMeanApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String SLOPE_LIMITS_PARAM = "slopeLimits";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_AXIS_PARAM = "xAxis";
	static final protected String Y_AXIS_PARAM = "yAxis";
	static final private String RANDOM_SEED = "randomSeed";
	
	static final private String kPValueAxisInfo = "0 1 0.0 0.2";
	static final private int kSlopeDecimals = 10;		//		value does not matter
	
	private HypothesisTest test;
	private ParameterSlider slopeSlider;
	
	public void setupApplet() {
		RegnImages.loadRegn(this);
		
		data = getData();
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.5, 20));
		dataPanel.add(ProportionLayout.LEFT, leftPanel(data));
		dataPanel.add(ProportionLayout.RIGHT, pValuePanel(summaryData));
		add("Center", dataPanel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String seedString = getParameter(RANDOM_SEED);
		int noOfValues = getValueCount(data);
		String randomParams = String.valueOf(noOfValues) + " 0.0 1.0 " + seedString + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(noOfValues);
		data.addVariable("error", error);
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		
		ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model", 10);
		data.addVariable("y", yData);
		
		return data;
	}
	
	protected int getValueCount(DataSet data) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		return xVar.noOfValues();
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		SlopeDistnVariable slopeDistn = new SlopeDistnVariable("slope distn", sourceData,
																							"x", "y", kSlopeDecimals);
		summaryData.addVariable("slopeDistn", slopeDistn);
		
		test = new SlopeHypothesisTest(summaryData, "slopeDistn", new NumValue(0.0, 0),
																		HypothesisTest.HA_NOT_EQUAL, this);
		
		int pValueDecimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		PValueVariable pValue = new PValueVariable(translate("p-value"), test, pValueDecimals);
		
		summaryData.addVariable("p-value", pValue);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
	}
	
	protected XPanel leftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
		thePanel.add("Center", dataPanel(data));
		
		XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 8));
//			topPanel.add("North", new HypothesisView(test, this));
			
			StringTokenizer st = new StringTokenizer(getParameter(SLOPE_LIMITS_PARAM));
			String minSlopeString = st.nextToken();
			String maxSlopeString = st.nextToken();
			int noOfSlopeSteps = Integer.parseInt(st.nextToken());
			double startSlope = Double.parseDouble(st.nextToken());
			
			String beta1 = MText.expandText("#beta##sub1#");
			slopeSlider = new ParameterSlider(new NumValue(minSlopeString), new NumValue(maxSlopeString),
										new NumValue(startSlope), noOfSlopeSteps, beta1, this);
			topPanel.add("Center", slopeSlider);
		
		thePanel.add("North", topPanel);
		
		thePanel.add("South", topControlPanel(data, summaryData, "p-value"));
		
		return thePanel;
	}
	
	private XPanel yNamePanel(DataSet data, VertAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
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
	
	private XPanel pValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 30));
			thePanel.add("North", new HypothesisView(test, this));
			
			XPanel graphPanel = new XPanel();
			graphPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
				theHorizAxis.readNumLabels(kPValueAxisInfo);
				theHorizAxis.setAxisName(translate("p-value"));
				
				graphPanel.add("Bottom", theHorizAxis);
				
				boolean showCumulative = true;
				
				DotCumulativeView pValueView = new DotCumulativeView(summaryData, this, theHorizAxis, showCumulative);
				pValueView.setActiveNumVariable("p-value");
				graphPanel.add("Center", pValueView);
			thePanel.add("Center", graphPanel);
		return thePanel;
	}
	
	protected void doTakeSample() {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)summaryData.getVariable("slopeDistn");
		slopeDistn.resetSource();
		super.doTakeSample();
	}
	
	private boolean localAction(Object target) {
		if (target == slopeSlider) {
			summaryData.clearData();
			summaryData.variableChanged("p-value");
			
			double slope = slopeSlider.getParameter().toDouble();
			LinearModel model = (LinearModel)data.getVariable("model");
			model.setSlope(slope);
			
//			NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
//			error.clearSample();
			data.variableChanged("error");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}