package inferenceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import distn.*;
import random.RandomNormal;
import coreGraphics.*;

import inference.*;


public class SampleIntervalApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final protected String RANDOM_NORMAL_PARAM = "random";
	static final protected String DATA_DECIMALS_PARAM = "dataDecimals";
	static final protected String MEAN_NAME_PARAM = "meanName";
	static final protected String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final protected String CONFIDENCE_LEVEL_PARAM = "confidenceLevel";
	static final protected String POPN_SD_PARAM = "knownPopnSD";
	static final protected String TARGET_PARAM = "target";
	
	static final private Color kNormalColor = new Color(0xDDDDFF);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private StackedPlusNormalView dataView;
	protected IntervalView summaryView;
	protected OneValueView ciValueView;
	protected CoverageValueView coverage;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck, summaryScaleCheck;
	
	private String sampleScaleInfo, summaryScaleInfo;
	private HorizAxis sampleAxis, summaryAxis;
	
	protected NumValue modelMean, modelSD;
	protected int noOfValues;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		summaryData.takeSample();
		
		sampleScaleInfo = getParameter(AXIS_INFO_PARAM);
		summaryScaleInfo = getParameter(SUMMARY_AXIS_INFO_PARAM);
		
		setLayout(new BorderLayout());
		
		XPanel displayPanel = new XPanel();
		displayPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		displayPanel.add(ProportionLayout.LEFT, samplePanel(data, "y", "model", summaryData));
		displayPanel.add(ProportionLayout.RIGHT, summaryPanel(summaryData, "ci", getTarget()));
		add("Center", displayPanel);
//		add("South", coveragePanel(summaryData, "ci"));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
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
		MeanCIVariable ci;
		
		String knownPopnSDString = getParameter(POPN_SD_PARAM);
		if (knownPopnSDString != null)
			ci = new MeanCIVariable(getParameter(MEAN_NAME_PARAM), 1.0,
										Double.parseDouble(knownPopnSDString), "y", decimals);
		else
			ci = new MeanCIVariable(getParameter(MEAN_NAME_PARAM), 1.0,
																					noOfValues - 1, "y", decimals);
		
		String levelString = getParameter(CONFIDENCE_LEVEL_PARAM);
		if (levelString != null) {
			double level = Double.parseDouble(levelString);
			ci.setTFromLevel(level);
		}
		summaryData.addVariable("ci", ci);
		
		return summaryData;
	}
	
	private NumValue getTarget() {
		String targetString = getParameter(TARGET_PARAM);
		if (targetString != null)
			return new NumValue(targetString);
		else
			return modelMean;
	}
	
	private XPanel samplePanel(DataSet data, String variableKey, String modelKey, DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dataPanel(data, variableKey, modelKey));
		thePanel.add("South", sampleControlPanel(summaryData));
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		sampleAxis = new HorizAxis(this);
		sampleAxis.readNumLabels(sampleScaleInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		sampleAxis.setAxisName(v.name);
		
		thePanel.add("Bottom", sampleAxis);
		
		dataView = new StackedPlusNormalView(data, this, sampleAxis, modelKey);
		dataView.setActiveNumVariable(variableKey);
		dataView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
		dataView.setDensityColor(kNormalColor);
		dataView.lockBackground(Color.white);
		
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet summaryData, String variableKey, NumValue target) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("Center", summaryDataPanel(summaryData, variableKey, target));
		thePanel.add("North", intervalValuePanel(summaryData, variableKey));
		thePanel.add("South", summaryControlPanel(summaryData, variableKey));
		
		return thePanel;
	}
	
	protected boolean onlyShowSummaryScale() {
		return false;
	}
	
	private XPanel summaryDataPanel(DataSet summaryData, String variableKey, NumValue target) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		summaryAxis = new HorizAxis(this);
		summaryAxis.readNumLabels(onlyShowSummaryScale() ? summaryScaleInfo : sampleScaleInfo);
		
		thePanel.add("Bottom", summaryAxis);
		
		summaryView = new IntervalView(summaryData, this, summaryAxis, variableKey, target);
		thePanel.add("Center", summaryView);
		summaryView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected XPanel sampleControlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 5));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel(translate("No of samples") + " =");
		thePanel.add(theCount);
		
		if (!onlyShowSummaryScale()) {
			XPanel checkPanel = new InsetPanel(0, 50, 0, 0);
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				summaryScaleCheck = new XCheckbox(translate("Expanded summary scale"), this);
			checkPanel.add(summaryScaleCheck);
			thePanel.add(checkPanel);
		}
		
		return thePanel;
	}
	
	private XPanel intervalValuePanel(DataSet summaryData, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
		return thePanel;
	}
	
	private XPanel summaryControlPanel(DataSet summaryData, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 15));
		
			ciValueView = new OneValueView(summaryData, variableKey, this);
//			ciValueView.setFont(getBigFont());
		thePanel.add(ciValueView);
		
			coverage = new CoverageValueView(summaryData, variableKey, this, getTarget());
//			coverage.setFont(getBigFont());
		thePanel.add(coverage);
		
		return thePanel;
	}
	
//	private XPanel coveragePanel(DataSet summaryData, String variableKey) {
//		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//		
//		return thePanel;
//	}
	
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
		else if (target == summaryScaleCheck) {
			summaryAxis.readNumLabels(summaryScaleCheck.getState() ? summaryScaleInfo : sampleScaleInfo);
			summaryAxis.repaint();
			summaryView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}