package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import random.*;
import distn.*;
import qnUtils.*;
import coreSummaries.*;
import coreVariables.*;
import imageGroups.*;
import imageUtils.*;

import test.*;


public class ZPValueApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String TEST_PARAM = "testInfo";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String MEANS_PARAM = "means";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String FORMULA_GIF_PARAM = "formulaGif";
	
	static final private String kZAxisScale = "-3 3 -3 1";
	
	static final private Color kDarkGreenColor = new Color(0x006600);
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private RandomNormal generator;
	
	private HypothesisTest test;
	private int testTail;
	
	private NumValue lowMean, highMean, meanStep, rawMean;
	private NumValue testMean, testSD;
	
	private SampStatSlider meanSlider;
	private XChoice dataSetChoice;
	private int currentDataSet = 0;
	
	private int zDecimals, pValueDecimals;
	
	private XPanel controlPanel;
	private CardLayout controlPanelLayout;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		ScalesImages.loadScales(this);
		
		getData();
		test = readTestInfo(data, "y");
		getSummaryData(data);
		
		setLayout(new ProportionLayout(0.55, 8, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 20));
			
			leftPanel.add("Center", dataPlotPanel(data));
			
			leftPanel.add("South", dataChoicePanel(data));
		
		add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.5, 3, ProportionLayout.VERTICAL,
																									ProportionLayout.TOTAL));
			
			rightPanel.add(ProportionLayout.TOP, calcPanel(data, summaryData));
			
			rightPanel.add(ProportionLayout.BOTTOM, zDistnPanel(summaryData));
		
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	private void getData() {
		data = new DataSet();
		
		NumVariable rawY = new NumVariable(translate("Raw data"));;
		String valueString = getParameter(VALUES_PARAM);
		if (valueString == null) {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			rawY.setValues(vals);
		}
		else
			rawY.readValues(valueString);
		
		data.addVariable("raw", rawY);
		
		readParameters(data);
		
			FixedMeanSDVariable shiftedY = new FixedMeanSDVariable(getParameter(VAR_NAME_PARAM), data, "raw", rawMean.toDouble());
		data.addVariable("y", shiftedY);
		
		if (testSD != null) {
			NormalDistnVariable meanDistn = new NormalDistnVariable("distn of mean");
			meanDistn.setMean(testMean.toDouble());
			meanDistn.setSD(testSD.toDouble() / Math.sqrt(rawY.noOfValues()));
			
			data.addVariable("meanDistn", meanDistn);
		}
	}
	
	private void readParameters(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		
		lowMean = new NumValue(st.nextToken());
		highMean = new NumValue(st.nextToken());
		meanStep = new NumValue(st.nextToken());
		
		NumVariable rawY = (NumVariable)data.getVariable("raw");
		ValueEnumeration e = rawY.values();
		double sy = 0.0;
		while (e.hasMoreValues())
			sy += e.nextDouble();
		int n = rawY.noOfValues();
		
		rawMean = new NumValue(sy / n, meanStep.decimals);
		
		st = new StringTokenizer(getParameter(TEST_PARAM));
		testMean = new NumValue(st.nextToken());
		if (st.hasMoreTokens())
			testSD = new NumValue(st.nextToken());
	}
	
	private void getSummaryData(DataSet data) {
		summaryData = new SummaryDataSet(data, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_DECIMALS_PARAM));
		zDecimals = Integer.parseInt(st.nextToken());
		pValueDecimals = Integer.parseInt(st.nextToken());
		
		String testStatName = (testSD == null) ? translate("t statistic") : translate("z statistic");
		
		StatisticValueVariable zValue = new StatisticValueVariable(testStatName, test, zDecimals);
		PValueVariable pValue = new PValueVariable(translate("p-value") + " = ", test, pValueDecimals);
		
		if (testSD == null) {
			NumVariable yVar = (NumVariable)data.getVariable("y");
			int n = yVar.noOfValues();
			TDistnVariable tDistn = new TDistnVariable(translate("t distn"), n - 1);
			summaryData.addVariable("zDistn", tDistn);
		}
		else {
			NormalDistnVariable zDistn = new NormalDistnVariable("z distn");
			summaryData.addVariable("zDistn", zDistn);
		}
		
		summaryData.addVariable("z", zValue);
		summaryData.addVariable("pValue", pValue);
		
		summaryData.setSingleSummaryFromData();
	}
	
	private HypothesisTest readTestInfo(DataSet data, String yKey) {
		String tailString = getParameter(ALTERNATIVE_PARAM);
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		if (testSD == null)
			return new UnivarHypothesisTest(data, yKey, testMean, testTail,
																	HypothesisTest.MEAN, this);
		else
			return new UnivarHypothesisTest(data, yKey, testMean, testSD, testTail, this);
	}
	
	private XPanel dataPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			CoreVariable v = data.getVariable("y");
			theHorizAxis.setAxisName(v.name);
		
		thePanel.add("Bottom", theHorizAxis);
		
			String theoryKey = (testSD == null) ? null : "meanDistn";
			DataPlusMeanDistnView dataView = new DataPlusMeanDistnView(data, this, theHorizAxis, theoryKey, 1.0);
			dataView.setActiveNumVariable("y");
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel zDistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(kZAxisScale);
		thePanel.add("Bottom", theHorizAxis);
		CoreVariable v = summaryData.getVariable("z");
		theHorizAxis.setAxisName(v.name);
		
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
		
			controlPanel = new XPanel();
				controlPanelLayout = new CardLayout();
			controlPanel.setLayout(controlPanelLayout);
			controlPanel.add("Actual", new XPanel());
			
				XPanel sliderPanel = new XPanel();
				sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
					meanSlider = new SampStatSlider(lowMean, highMean, meanStep, rawMean, SampStatSlider.MEAN, this);
					meanSlider.setForeground(Color.blue);
				sliderPanel.add(meanSlider);
				
			controlPanel.add("Modified", sliderPanel);
		
		thePanel.add("Center", controlPanel);
		
		return thePanel;
	}
	
	private XPanel calcPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 2));
		
			HypothesisView hypoth = new HypothesisView(test, this);
		thePanel.add(hypoth);
		
			XPanel zpPanel = new XPanel();
			zpPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_SPACED, 2));
		
				String formulaGif = getParameter(FORMULA_GIF_PARAM) + ".gif";
				OneValueImageView zCalculator = new OneValueImageView(summaryData, "z", this, formulaGif, 25);
			zpPanel.add(zCalculator);
				
				OneValueView pValueView = new OneValueView(summaryData, "pValue", this);
				pValueView.setForeground(kDarkGreenColor);
			zpPanel.add(pValueView);
			
		thePanel.add(zpPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		FixedMeanSDVariable yVariable = (FixedMeanSDVariable)data.getVariable("y");
		
		if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (currentDataSet != newChoice) {
				currentDataSet = newChoice;
				if (currentDataSet == 0) {
					yVariable.reset();
					data.variableChanged("y");
					summaryData.redoLastSummary();
					
					controlPanelLayout.show(controlPanel, "Actual");
				}
				else {
					yVariable.setMean(meanSlider.getStat());
					data.variableChanged("y");
					summaryData.redoLastSummary();
					
					controlPanelLayout.show(controlPanel, "Modified");
				}
			}
			return true;
		}
		else if (target == meanSlider) {
			yVariable.setMean(meanSlider.getStat());
			data.variableChanged("y");
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