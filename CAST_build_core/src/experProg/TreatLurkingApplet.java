package experProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreGraphics.*;
import random.*;
import models.*;
import coreSummaries.*;
import coreVariables.*;

import exper.*;


public class TreatLurkingApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_COEFF_PARAM = "yCoeffs";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String LURKING_VAR_NAME_PARAM = "lurkingVarName";
	static final private String LURKING_VALUES_PARAM = "lurkingValues";
	static final private String LURKING_AXIS_INFO_PARAM = "lurkingAxis";
	
	static final protected String TREAT_VAR_NAME_PARAM = "treatVarName";
	static final protected String TREAT_LABELS_PARAM = "treatLabels";
	static final protected String TREAT_VALUES_PARAM = "treatValues";
	
	static final protected String EFFECT_NAME_PARAM = "effectName";
	static final private String EFFECT_PARAM = "treatEffect";
	static final private String SUMMARY_AXIS_INFO_PARAM = "summaryAxis";
	static final protected String MAX_DIFF_PARAM = "maxDiff";
	static final private String RANDOM_SEED = "randomSeed";
	
	static final private String INDEX_VAR_NAME_PARAM = "indexVarName";
	
	static final private NumValue kMaxIndex = new NumValue(999, 0);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	protected NumValue maxDiff;
	
	private int currentLurkingIndex;
	private XChoice lurkingChoice;
	private RepeatingButton takeSampleButton;
	private ParameterSlider effectSlider;
	
	private TwoGroupDotPlotView lurkingView;
	private VertAxis yAxis;
	private MultiVertAxis lurkingAxis;
	
	private String lurkingVarName[];
	private String lurkingKey[];
	
	private NumValue effectMin, effectMax, effectStart;
	private String effectNameOnSlider;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(10, 10));
			
			leftPanel.add("West", lurkingPanel(data));
			leftPanel.add("Center", responsePanel(data));
			leftPanel.add("South", idPanel(data));
			
		add("Center", leftPanel);
		add("East", controlPanel(data, summaryData));
		
		
//		setLayout(new ProportionLayout(0.5, 10));
//		
//			XPanel leftPanel = new XPanel();
//			leftPanel.setLayout(new ProportionLayout(0.35, 10));
//			
//			leftPanel.add(ProportionLayout.LEFT, lurkingPanel(data));
//			leftPanel.add(ProportionLayout.RIGHT, responsePanel(data));
//		
//		add(ProportionLayout.LEFT, leftPanel);
//		add(ProportionLayout.RIGHT, controlPanel(data, summaryData));
	}
	
	private DataSet getData() {
		data = new DataSet();
		
		int nx = 0;
		while (getParameter(LURKING_VAR_NAME_PARAM + nx) != null)
			nx++;
		
		lurkingVarName = new String[nx];
		lurkingKey = new String[nx];
		for (int i=0 ; i<nx ; i++) {
			lurkingVarName[i] = getParameter(LURKING_VAR_NAME_PARAM + i);
			lurkingKey[i] = "x" + i;
			data.addNumVariable(lurkingKey[i], lurkingVarName[i], getParameter(LURKING_VALUES_PARAM + i));
		}
		
			MultipleRegnModel model = new MultipleRegnModel(getParameter(Y_VAR_NAME_PARAM), data, lurkingKey,
																														getParameter(Y_COEFF_PARAM));
		data.addVariable("model", model);
		
			LabelEnumeration e = new LabelEnumeration(getParameter(EFFECT_PARAM));
			effectMin = new NumValue((String)e.nextElement());
			effectMax = new NumValue((String)e.nextElement());
			effectStart = new NumValue((String)e.nextElement());
			effectNameOnSlider = (String)e.nextElement();
		
			String seedString = getParameter(RANDOM_SEED);
			int count = ((Variable)data.getVariable("x0")).noOfValues();
			String randomParams = String.valueOf(count) + " 0.0 1.0 " + seedString + " 3.0";
			RandomNormal generator = new RandomNormal(randomParams);
			NumSampleVariable error = new NumSampleVariable("error", generator, 10);
			error.setSampleSize(count);
		data.addVariable("error", error);
		
		addTreatment(data);
		
			ResponseVariable resp = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
								data, lurkingKey, "error", "model", "treat", effectStart.toDouble(), 10);
		data.addVariable("y", resp);
		
		String indexName = getParameter(INDEX_VAR_NAME_PARAM);
		if (indexName != null) {
			NumVariable x0Var = (NumVariable)data.getVariable("x0");
			data.addVariable("index", new IndexVariable(indexName, x0Var.noOfValues()));
		}
		
		return data;
	}
	
	protected void addTreatment (DataSet data) {
		data.addCatVariable("treat", getParameter(TREAT_VAR_NAME_PARAM),
												getParameter(TREAT_VALUES_PARAM), getParameter(TREAT_LABELS_PARAM));
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
			maxDiff = new NumValue(getParameter(MAX_DIFF_PARAM));
			DiffSummaryVariable diff = new DiffSummaryVariable(getParameter(EFFECT_NAME_PARAM),
																														"y", "treat", maxDiff.decimals);
		
		summaryData.addVariable("est", diff);
		summaryData.takeSample();
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private XPanel responsePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", responsePlotPanel(data));
		thePanel.add("North", responseLabelPanel(data));
		
		return thePanel;
	}
	
	private XPanel responseLabelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		return thePanel;
	}
	
	private XPanel responsePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			yAxis = new VertAxis(this);
			yAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		thePanel.add("Left", yAxis);
		
			HorizAxis treatAxis = new HorizAxis(this);
			treatAxis.setCatLabels((CatVariable)data.getVariable("treat"));
			
		thePanel.add("Bottom", treatAxis);
		
			TreatDotView yView = new TreatDotView(data, this, yAxis, treatAxis, "y", "treat", 1.0);
			yView.lockBackground(Color.white);
		thePanel.add("Center", yView);
		
		return thePanel;
	}
	
	private XPanel lurkingPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", lurkingChoicePanel(data));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new FixedSizeLayout(100, 0));
			
			plotPanel.add(lurkingPlotPanel(data));
		
		thePanel.add("Center", plotPanel);
		
		return thePanel;
	}
	
	private XPanel lurkingChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			lurkingChoice = new XChoice(this);
			for (int i=0 ; i<lurkingVarName.length ; i++)
				lurkingChoice.addItem(lurkingVarName[i]);
			lurkingChoice.select(0);
			currentLurkingIndex = 0;
			
		thePanel.add(lurkingChoice);
		
		return thePanel;
	}
	
	private XPanel lurkingPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			lurkingAxis = new MultiVertAxis(this, lurkingVarName.length);
			lurkingAxis.readNumLabels(getParameter(LURKING_AXIS_INFO_PARAM + 0));
			for (int i=1 ; i<lurkingVarName.length ; i++)
				lurkingAxis.readExtraNumLabels(getParameter(LURKING_AXIS_INFO_PARAM + i));
			lurkingAxis.setChangeMinMax(true);
			
		thePanel.add("Left", lurkingAxis);
		
			lurkingView = new TwoGroupDotPlotView(data, this, lurkingAxis);
			lurkingView.setActiveNumVariable("x0");
			lurkingView.setActiveCatVariable("treat");
			lurkingView.lockBackground(Color.white);
			
		thePanel.add("Center", lurkingView);
		
		return thePanel;
	}
	
	private XPanel idPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																							VerticalLayout.VERT_CENTER, 0));
		thePanel.add(new OneValueView(data, "index", this, kMaxIndex));
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 10));
		
			effectSlider = new ParameterSlider(effectMin, effectMax, effectStart,
																															effectNameOnSlider, this);
			effectSlider.setFont(getStandardBoldFont());
		thePanel.add(effectSlider);
		
			XPanel estPanel = new XPanel();
			estPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			estPanel.add(new OneValueView(summaryData, "est", this, maxDiff));
			
		thePanel.add(estPanel);
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				takeSampleButton = new RepeatingButton("Repeat exper", this);
			samplePanel.add(takeSampleButton);
			
		thePanel.add(samplePanel);
		
		thePanel.add(summaryDistPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel summaryDistPanel(SummaryDataSet summaryData) {
		XPanel outerPanel = new XPanel();
//		outerPanel.setLayout(new FixedSizeLayout(280, 150));
		outerPanel.setLayout(new FixedSizeLayout(280, 100));
		
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new AxisLayout());
			
				HorizAxis summaryAxis = new HorizAxis(this);
				summaryAxis.readNumLabels(getParameter(SUMMARY_AXIS_INFO_PARAM));
				summaryAxis.setAxisName(summaryData.getVariable("est").name);
			thePanel.add("Bottom", summaryAxis);
			
//				StackedDotPlotView theView = new StackedDotPlotView(summaryData, null, this,
//																											summaryAxis, DataView.BUFFERED);
				DotPlotView theView = new DotPlotView(summaryData, this, summaryAxis, 1.0);
				theView.lockBackground(Color.white);
				
			thePanel.add("Center", theView);
		
		outerPanel.add(thePanel);
		return outerPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lurkingChoice) {
			int newXIndex = lurkingChoice.getSelectedIndex();
			if (newXIndex != currentLurkingIndex) {
				currentLurkingIndex = newXIndex;
				lurkingAxis.setAlternateLabels(newXIndex);
				lurkingAxis.repaint();
				lurkingView.setActiveNumVariable(lurkingKey[newXIndex]);
				lurkingView.repaint();
			}
			return true;
		}
		else if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == effectSlider) {
			NumValue effectVal = effectSlider.getParameter();
			ResponseVariable model = (ResponseVariable)data.getVariable("y");
			model.setEffect(effectVal.toDouble());
			data.variableChanged("y");
			summaryData.setSingleSummaryFromData();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}