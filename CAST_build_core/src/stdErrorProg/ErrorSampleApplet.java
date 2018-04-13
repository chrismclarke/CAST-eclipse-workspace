package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import valueList.*;
import distn.*;
import coreGraphics.*;
import coreVariables.*;
import corr.*;
import sampling.*;
import stdError.*;


public class ErrorSampleApplet extends XApplet {
	static final private String DATA_AXIS_INFO_PARAM = "dataAxis";
	static final protected String ERROR_NAME_PARAM = "errorName";
	static final protected String ERROR_AXIS_INFO_PARAM = "errorAxis";
	static final protected String RANDOM_PARAM = "random";
	static final private String PERCENTILE_PARAM = "percentile";
	static final protected String PARAM_NAME_PARAM = "paramName";
	static final protected String STATISTIC_NAME_PARAM = "statisticName";
	static final protected String DECIMALS_PARAM = "decimals";
	static final protected String UNITS_PARAM = "units";
	static final protected String MAX_BIAS_SE_PARAM = "maxBiasSe";
	static final protected String SHOW_BIAS_SE_PARAM = "showBiasSe";
	
	static final private Color kBiasSeBackground = new Color(0xEDF2FF);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private MeanSDDotPlotView errorView;
	
	protected RepeatingButton takeSampleButton;
	protected XCheckbox accumulateCheck, biasSECheck;
	
	protected double targetProb;
	protected NumValue target;
	
	private XPanel seBiasPanel;
	private CardLayout seBiasPanelLayout;
	
	protected double topProportion() {
		return 0.55;
	}
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(topProportion(), 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				topPanel.add(ProportionLayout.TOP, dataPanel(data, summaryData));
				topPanel.add(ProportionLayout.BOTTOM, summaryPanel(summaryData));
		
		add("Center", topPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(topProportion(), 10, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
				rightPanel.add(ProportionLayout.TOP, sampleControlPanel(summaryData));
				
				String showBiasSeString = getParameter(SHOW_BIAS_SE_PARAM);
				boolean showBiasSe = (showBiasSeString == null) || showBiasSeString.equals("true");
				rightPanel.add(ProportionLayout.BOTTOM, showBiasSe ? errorSummaryPanel(summaryData)
																									: new XPanel());
				
		add("East", rightPanel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomNormal generator = new RandomNormal(getParameter(RANDOM_PARAM));
		NumVariable y = new NumSampleVariable(getParameter(VAR_NAME_PARAM), generator, 10);
		data.addVariable("y", y);
		
		NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
		StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
		@SuppressWarnings("unused")
		int noOfValues = Integer.parseInt(st.nextToken());
		String modelMean = st.nextToken();
		String modelSD = st.nextToken();
		dataDistn.setParams(modelMean + " " + modelSD);
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	protected NumValue getTarget(DataSet data, double targetProb, int decimals) {
		ContinDistnVariable popnDistn = (ContinDistnVariable)data.getVariable("model");
		double percentile = popnDistn.getQuantile(targetProb);
		return new NumValue(percentile, decimals);
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			targetProb = Double.parseDouble(getParameter(PERCENTILE_PARAM));
			target = getTarget(data, targetProb, decimals);
			
			PercentileVariable estimator = new PercentileVariable("Estimate", "y",
																																	targetProb, decimals);
			
		summaryData.addVariable("est", estimator);
		
			ScaledVariable error = new ScaledVariable(getParameter(ERROR_NAME_PARAM), estimator,
																									"est", -target.toDouble(), 1.0, decimals);
		
		summaryData.addVariable("error", error);
		
		return summaryData;
	}
	
	protected HorizAxis getAxis(DataSet data, String variableKey, String axisInfoParam) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(axisInfoParam);
		theHorizAxis.readNumLabels(labelInfo);
		Variable v = (Variable)data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	protected StackedPlusNormalView getDataView(DataSet data, SummaryDataSet summaryData,
														HorizAxis horizAxis, String paramName, String statisticName) {
		ParamAndStatView theView = new ParamAndStatView(data, this, horizAxis, "model", null,
																summaryData, "est", "error", target, paramName, statisticName);
		return theView;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(data, "y", DATA_AXIS_INFO_PARAM);
		thePanel.add("Bottom", theHorizAxis);
		
			StackedPlusNormalView dataView = getDataView(data, summaryData, theHorizAxis,
												getParameter(PARAM_NAME_PARAM), getParameter(STATISTIC_NAME_PARAM));
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(summaryData, "error", ERROR_AXIS_INFO_PARAM);
			theHorizAxis.setForeground(Color.red);
		thePanel.add("Bottom", theHorizAxis);
		
			errorView = new MeanSDDotPlotView(summaryData, this, theHorizAxis);
			errorView.setAllowDrawMeanSD(false);
			errorView.setActiveNumVariable("error");
			errorView.lockBackground(Color.white);
			errorView.setForeground(Color.red);
		thePanel.add("Center", errorView);
		
		return thePanel;
	}
	
	protected XPanel sampleControlPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
			ValueCountView theCount = new ValueCountView(summaryData, this);
			theCount.setLabel(translate("Samples") + " =");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	protected XPanel errorSummaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				biasSECheck = new XCheckbox(translate("Estimate s.e. & bias"), this);
				biasSECheck.disable();
			checkPanel.add(biasSECheck);
			
		thePanel.add(checkPanel);
		
			seBiasPanel = new XPanel();
			seBiasPanelLayout = new CardLayout();
			seBiasPanel.setLayout(seBiasPanelLayout);
			
			seBiasPanel.add("blank", new XPanel());
			
			seBiasPanel.add("seBias", seBiasValuePanel(summaryData));
			
		thePanel.add(seBiasPanel);
		
		return thePanel;
	}
	
	private XPanel seBiasValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(5, 5);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			NumValue maxBiasSe = new NumValue(getParameter(MAX_BIAS_SE_PARAM));
		
			MeanView biasValueView = new MeanView(summaryData, "error", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			biasValueView.setLabel(translate("Bias"));
			biasValueView.setForeground(Color.blue);
			biasValueView.setFont(getBigFont());
			String unitsString = getParameter(UNITS_PARAM);
			if (unitsString != null)
				biasValueView.setUnitsString(unitsString);
			biasValueView.setMaxValue(maxBiasSe);
		thePanel.add(biasValueView);
		
			StDevnView seValueView = new StDevnView(summaryData, "error", MeanView.GENERIC_TEXT_FORMULA, 0, this);
			seValueView.setLabel(translate("Standard error"));
			seValueView.setForeground(Color.red);
			seValueView.setFont(getBigFont());
			if (unitsString != null)
				seValueView.setUnitsString(unitsString);
			seValueView.setMaxValue(maxBiasSe);
		thePanel.add(seValueView);
		
		thePanel.lockBackground(kBiasSeBackground);
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
		if (biasSECheck != null) {
			if (accumulateCheck.getState())
				biasSECheck.enable();
		}
	}
	
	protected void doChangeAccumulate() {
		summaryData.setAccumulate(accumulateCheck.getState());
		if (biasSECheck != null) {
			biasSECheck.setState(false);
			biasSECheck.disable();
			seBiasPanelLayout.show(seBiasPanel, "blank");
			if (errorView != null) {
				errorView.setAllowDrawMeanSD(false);
				errorView.repaint();
			}
		}
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			doChangeAccumulate();
			return true;
		}
		else if (target == biasSECheck) {
			seBiasPanelLayout.show(seBiasPanel, biasSECheck.getState() ? "seBias" : "blank");
			if (errorView != null) {
				errorView.setAllowDrawMeanSD(biasSECheck.getState());
				errorView.repaint();
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