package pairBlockProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;
import qnUtils.*;
import valueList.*;
import coreVariables.*;
import coreSummaries.*;
import imageGroups.*;
import imageUtils.*;

import test.*;
import pairBlock.*;


public class PairedTestApplet extends XApplet {
	static final private String RAW_AXIS_INFO_PARAM = "rawAxis";
	static final private String DIFF_AXIS_INFO_PARAM = "diffAxis";
	static final private String X1_NAME_PARAM = "x1Name";
	static final private String X2_NAME_PARAM = "x2Name";
	static final private String DIFF_NAME_PARAM = "diffName";
	static final private String X1_VALUES_PARAM = "x1Values";
	static final private String X2_VALUES_PARAM = "x2Values";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String MEANS_PARAM = "means";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String FORMULA_GIF_PARAM = "formulaGif";
	static final private String T_AXIS_INFO_PARAM = "tAxis";
	
	static final private Color kDarkGreenColor = new Color(0x006600);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private HypothesisTest test;
	private int testTail;
	private NumValue lowMeanDiff, highMeanDiff, meanDiffStep, rawMeanDiff;
	
	private PairedDotPlotView pairedView;
	
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	private XPanel sliderPanel;
	private CardLayout sliderPanelLayout;
	private SampStatSlider diffSlider;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		data = getData();
		
		test = readTestInfo(data, "diff");
		readParameters(data);
		
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.55, 10, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
			
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout());
			
				XPanel dataDisplayPanel = new XPanel();
				dataDisplayPanel.setLayout(new ProportionLayout(0.6, 5, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
			
				dataDisplayPanel.add(ProportionLayout.TOP, rawDataPanel(data));
				dataDisplayPanel.add(ProportionLayout.BOTTOM, diffDataPanel(data));
			
			leftPanel.add("Center", dataDisplayPanel);
			leftPanel.add("South", dataChoicePanel(data));
			
		add(ProportionLayout.LEFT, leftPanel);
		add(ProportionLayout.RIGHT, calcPanel(data, summaryData));
	}
	
	private DataSet getData() {
		DataSet theData = new DataSet();
		
			NumVariable x1Var = new NumVariable(getParameter(X1_NAME_PARAM));
			x1Var.readValues(getParameter(X1_VALUES_PARAM));
		theData.addVariable("x1", x1Var);
		
			NumVariable x2Var = new NumVariable(getParameter(X2_NAME_PARAM));
			x2Var.readValues(getParameter(X2_VALUES_PARAM));
		theData.addVariable("x2", x2Var);
		
			ScaledVariable scaledX2 = new ScaledVariable(x2Var.name, x2Var, "x2", 0.0,
																								1.0, x2Var.getMaxDecimals());
		theData.addVariable("x2Shift", scaledX2);
		
			SumDiffVariable diffVar = new SumDiffVariable(getParameter(DIFF_NAME_PARAM), theData, 
																														"x2Shift", "x1", SumDiffVariable.DIFF);
		theData.addVariable("diff", diffVar);
		
		return theData;
	}
	
	private void readParameters(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		
		lowMeanDiff = new NumValue(st.nextToken());
		highMeanDiff = new NumValue(st.nextToken());
		meanDiffStep = new NumValue(st.nextToken());
		
		NumVariable rawX1 = (NumVariable)data.getVariable("x1");
		ValueEnumeration x1e = rawX1.values();
		NumVariable rawX2 = (NumVariable)data.getVariable("x2");
		ValueEnumeration x2e = rawX2.values();
		double sd = 0.0;
		while (x1e.hasMoreValues())
			sd += (x2e.nextDouble() - x1e.nextDouble());
		int n = rawX1.noOfValues();
		
		rawMeanDiff = new NumValue(sd / n, meanDiffStep.decimals);
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet theSummaryData = new SummaryDataSet(data, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_DECIMALS_PARAM));
		int zDecimals = Integer.parseInt(st.nextToken());
		int pValueDecimals = Integer.parseInt(st.nextToken());
		
			StatisticValueVariable zValue = new StatisticValueVariable(translate("t statistic"), test, zDecimals);
		theSummaryData.addVariable("z", zValue);
		
			PValueVariable pValue = new PValueVariable(translate("p-value") + " = ", test, pValueDecimals);
		theSummaryData.addVariable("pValue", pValue);
		
			NumVariable yVar = (NumVariable)data.getVariable("x1");
			int n = yVar.noOfValues();
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), n - 1);
		theSummaryData.addVariable("zDistn", tDistn);
		
		theSummaryData.setSingleSummaryFromData();
		return theSummaryData;
	}
	
	private HypothesisTest readTestInfo(DataSet data, String yKey) {
		String tailString = getParameter(ALTERNATIVE_PARAM);
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		
		return new UnivarHypothesisTest(data, yKey, new NumValue(0, 0), testTail,
																							HypothesisTest.PAIRED_DIFF_MEAN, this);
	}
	
	private XPanel dataChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				dataSetChoice = new XChoice(this);
				dataSetChoice.addItem(translate("Actual Data"));
				dataSetChoice.addItem(translate("Modified Data"));
		
			choicePanel.add(dataSetChoice);
			
		thePanel.add("North", choicePanel);
		
			sliderPanel = new XPanel();
				sliderPanelLayout = new CardLayout();
			sliderPanel.setLayout(sliderPanelLayout);
			sliderPanel.add("Actual", new XPanel());
			
				XPanel localSliderPanel = new XPanel();
				localSliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
					diffSlider = new SampStatSlider(lowMeanDiff, highMeanDiff, meanDiffStep,
																						rawMeanDiff, SampStatSlider.MEAN_DIFF, this);
					diffSlider.setForeground(Color.blue);
				localSliderPanel.add(diffSlider);
				
			sliderPanel.add("Modified", localSliderPanel);
		
		thePanel.add("Center", sliderPanel);
		
		return thePanel;
	}
	
	private XPanel rawDataPanel(DataSet data) {
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(RAW_AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
		
		dataPanel.add("Bottom", horizAxis);
		
			CatVariable tempVar = new CatVariable("");
			tempVar.readLabels(getParameter(X1_NAME_PARAM) + " " + getParameter(X2_NAME_PARAM));
			
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.setCatLabels(tempVar);
		
		dataPanel.add("Left", vertAxis);
		
			pairedView = new PairedDotPlotView(data, this, "x1", "x2Shift", horizAxis, vertAxis, 1.0);
			pairedView.setRetainLastSelection(true);
			pairedView.setShowPairing(true);
			pairedView.lockBackground(Color.white);
			
		dataPanel.add("Center", pairedView);
		
		return dataPanel;
	}
	
	private XPanel diffDataPanel(DataSet data) {
		XPanel plotPanel = new XPanel();
		plotPanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(DIFF_AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
		
		plotPanel.add("Bottom", horizAxis);
		
			CatVariable tempVar = new CatVariable("");
			tempVar.readLabels(getParameter(DIFF_NAME_PARAM));
			
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.setCatLabels(tempVar);
		
		plotPanel.add("Left", vertAxis);
		
			DotPlotView dataView = new CentredDotPlotView(data, this, horizAxis, vertAxis, pairedView, 1.0);
			
			dataView.setRetainLastSelection(true);
			dataView.lockBackground(Color.white);
			dataView.setActiveNumVariable("diff");
			
		plotPanel.add("Center", dataView);
	
		return plotPanel;
	}
	
	private XPanel calcPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			XPanel tCalcPanel = new XPanel();
			tCalcPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_CENTER, 15));
				
				HypothesisView hypoth = new HypothesisView(test, this);
			tCalcPanel.add(hypoth);
		
				String formulaGif = getParameter(FORMULA_GIF_PARAM) + ".gif";
				OneValueImageView zCalculator = new OneValueImageView(summaryData, "z", this, formulaGif, 25);
			tCalcPanel.add(zCalculator);
		
		thePanel.add("North", tCalcPanel);
		
		thePanel.add("Center", tDistnPanel(summaryData));
			
			XPanel pValuePanel = new XPanel();
			pValuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
				OneValueView pValueView = new OneValueView(summaryData, "pValue", this);
				pValueView.setForeground(kDarkGreenColor);
			pValuePanel.add(pValueView);
			
		thePanel.add("South", pValuePanel);
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(getParameter(T_AXIS_INFO_PARAM));
		thePanel.add("Bottom", theHorizAxis);
//		CoreVariable v = summaryData.getVariable("z");
//		theHorizAxis.setAxisName(v.name);
		
		int tail = (testTail == HypothesisTest.HA_LOW) ? DistnTailView.LOW_TAIL
						: (testTail == HypothesisTest.HA_HIGH) ? DistnTailView.HIGH_TAIL
						: DistnTailView.TWO_TAIL;
		DistnTailView tView = new DistnTailView(summaryData, this, theHorizAxis, "zDistn", "z", tail);
		thePanel.add("Center", tView);
		
		tView.lockBackground(Color.white);
		tView.setForeground(Color.red);
		tView.setHighlightColor(kDarkGreenColor);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		ScaledVariable x2Variable = (ScaledVariable)data.getVariable("x2Shift");
		
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (currentDataSet != newChoice) {
				currentDataSet = newChoice;
				if (currentDataSet == 0) {
					x2Variable.setScale(0.0, 1.0, 10);
					data.variableChanged("x2Shift");
					summaryData.redoLastSummary();
					
					sliderPanelLayout.show(sliderPanel, "Actual");
				}
				else {
					diffSlider.setSliderValue(rawMeanDiff.toDouble());
					x2Variable.setScale(diffSlider.getStat() - rawMeanDiff.toDouble(), 1.0, 10);
					data.variableChanged("x2Shift");
					summaryData.redoLastSummary();
					
					sliderPanelLayout.show(sliderPanel, "Modified");
				}
			}
			return true;
		}
		else if (target == diffSlider) {
			x2Variable.setScale(diffSlider.getStat() - rawMeanDiff.toDouble(), 1.0, 10);
			data.variableChanged("x2Shift");
			summaryData.redoLastSummary();
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}