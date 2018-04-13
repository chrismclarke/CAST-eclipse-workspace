package pairBlockProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import random.*;
import valueList.*;
import coreVariables.*;
import coreSummaries.*;

import pairBlock.*;


public class DiffGroupApplet extends XApplet {
	static final private String GROUP1_MEANS_PARAM = "group1Means";
	static final private String GROUP2_MEANS_PARAM = "group2Means";
	static final protected String GROUP_NAMES_PARAM = "groupNames";
	static final private String GROUP_VALUES_PARAM = "groupValues";
	static final private String Y_NAME_PARAM = "yName";
	static final private String SD_PARAM = "sd";
	static final private String Y_AXIS_PARAM = "yAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String INDEX_NAME_PARAM = "indexName";
	static final protected String MAX_DIFF_PARAM = "maxDiff";
	
	static final private String[] kDataKeys = {"y1", "y2"};
	
//	static final private NumValue kZero = new NumValue(0.0, 0);
	static final protected NumValue kMaxPValue = new NumValue(1.0, 4);
	
	static final private Color kInferenceBackground = new Color(0xEDF2FF);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected NumValue maxDiff;
	
	private RepeatingButton takeSampleButton;
	protected XCheckbox joinPointsCheck;
	
	private VertAxis yAxis;
	private TwoGroupPairedView theView;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(16, 4));
		
		add("Center", displayPanel(data));
		add("South", controlPanel(summaryData));
		add("East", rightPanel(data));
		add("North", topPanel(data, yAxis));
		
		summaryData.takeSample();
	}
	
	protected DataSet readCoreData() {
		DataSet data = new DataSet();
		
			NumVariable yMean1 = new NumVariable("y1Mean");
			yMean1.readValues(getParameter(GROUP1_MEANS_PARAM));
		data.addVariable("yMean1", yMean1);
		
			NumVariable yMean2 = new NumVariable("y2Mean");
			yMean2.readValues(getParameter(GROUP2_MEANS_PARAM));
		data.addVariable("yMean2", yMean2);
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			double errorSd = Double.parseDouble(getParameter(SD_PARAM));
			RandomNormal generator1 = new RandomNormal(yMean1.noOfValues(), 0.0, errorSd, 3.0);
			NumSampleVariable yError1 = new NumSampleVariable("yError1", generator1, decimals);
		
		data.addVariable("yError1", yError1);
		
			RandomNormal generator2 = new RandomNormal(yMean2.noOfValues(), 0.0, errorSd, 3.0);
			NumSampleVariable yError2 = new NumSampleVariable("yError1", generator2, decimals);
		
		data.addVariable("yError2", yError2);
		
			SumDiffVariable y1Var = new SumDiffVariable(getParameter(Y_NAME_PARAM), data, "yMean1", "yError1",
																																						SumDiffVariable.SUM);
		data.addVariable("y1", y1Var);
		
			SumDiffVariable y2Var = new SumDiffVariable(getParameter(Y_NAME_PARAM), data, "yMean2", "yError2",
																																						SumDiffVariable.SUM);
		data.addVariable("y2", y2Var);
		
		data.addVariable("errors", new BiSampleVariable(data, "yError1", "yError2"));
		
		data.addVariable("index", new IndexVariable(getParameter(INDEX_NAME_PARAM), yMean1.noOfValues()));
		
		return data;
	}
	
	protected DataSet readData() {
		DataSet data = readCoreData();
		
			RandomisedCatVariable groupVar = new RandomisedCatVariable("Group");
			groupVar.readLabels(getParameter(GROUP_NAMES_PARAM));
			groupVar.readValues(getParameter(GROUP_VALUES_PARAM));
			
		data.addVariable("group", groupVar);
		
		data.addVariable("random", new BiSampleVariable(data, "errors", "group"));
		
		data.addVariable("y", new SwitchNumVariable(getParameter(Y_NAME_PARAM), data, kDataKeys, "group"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "random");
		
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
		summaryData.addVariable("ci", new TwoGroupCIVariable(translate("95% CI") + " =",
																									sourceData, "y", "group", 0.95, maxDiff.decimals));
		summaryData.addVariable("pValue", new TwoGroupPValueVariable(translate("p-value") + " =",
																											sourceData, "y", "group", kMaxPValue.decimals));
		
		return summaryData;
	}
	
	private XPanel topPanel(DataSet data, VertAxis yAxis) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(getParameter(Y_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 0));
		
			joinPointsCheck = new XCheckbox(translate("Show paired values"), this);
			joinPointsCheck.setState(false);
		thePanel.add(joinPointsCheck);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT,
																											VerticalLayout.VERT_CENTER, 5));
				OneValueView indexView = new OneValueView(data, "index", this);
			dataPanel.add(indexView);
			
				OneValueView groupView = new OneValueView(data, "group", this);
				groupView.setLabel("");
			dataPanel.add(groupView);
			
				OneValueView yView = new OneValueView(data, "y", this);
			dataPanel.add(yView);
			
		thePanel.add(dataPanel);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
		
			XPanel inferencePanel = new InsetPanel(10, 4);
			inferencePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
				XPanel ciPanel = new XPanel();
				ciPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
				
					XLabel estLabel = new XLabel(translate("Estimate of difference"), XLabel.LEFT, this);
					estLabel.setFont(getStandardBoldFont());
				ciPanel.add(estLabel);
					
					IntervalValue maxCILabel = new IntervalValue(maxDiff.toDouble(), 0.0, 0.0, maxDiff.decimals);
					OneValueView ciView = new OneValueView(summaryData, "ci", this, maxCILabel);
				ciPanel.add(ciView);
				
			inferencePanel.add(ciPanel);
			
				XPanel testPanel = new XPanel();
				testPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
				
					XLabel testLabel = new XLabel(translate("Equal means") + "?", XLabel.LEFT, this);
					testLabel.setFont(getStandardBoldFont());
				testPanel.add(testLabel);
					
					OneValueView pView = new OneValueView(summaryData, "pValue", this, kMaxPValue);
				testPanel.add(pView);
			
			inferencePanel.add(testPanel);
			
			inferencePanel.lockBackground(kInferenceBackground);
		thePanel.add(inferencePanel);
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	protected TwoGroupPairedView getDataView(DataSet data, VertAxis yAxis,
																															HorizAxis theGroupAxis) {
		return new TwoGroupPairedView(data, this, "y1", "y2",  "group", yAxis, theGroupAxis, 0.5);
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis theGroupAxis = new HorizAxis(this);
			
			CatVariable dummyGroupVar = new CatVariable("Group");
			dummyGroupVar.readLabels(getParameter(GROUP_NAMES_PARAM));
			theGroupAxis.setCatLabels(dummyGroupVar);
		thePanel.add("Bottom", theGroupAxis);
		
			theView = getDataView(data, yAxis, theGroupAxis);
			theView.setRetainLastSelection(true);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			data.clearSelection();
			summaryData.takeSample();
			return true;
		}
		else if (target == joinPointsCheck) {
			theView.setShowPairing(joinPointsCheck.getState());
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}