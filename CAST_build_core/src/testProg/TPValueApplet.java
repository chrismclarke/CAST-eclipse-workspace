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
import coreGraphics.*;
import coreSummaries.*;
import coreVariables.*;
import imageGroups.*;
import imageUtils.*;

import test.*;
import sampling.*;


class SDOfMeanValueView extends ValueImageView {
	
	private String distnKey;
	
	SDOfMeanValueView(DataSet theData, XApplet applet, String distnKey) {
		super(theData, applet, "test/sdOfMeanFormula.gif", 18);
		this.distnKey = distnKey;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		DistnVariable y = (DistnVariable)getVariable(distnKey);
		return y.getSD().stringWidth(g);
	}
	
	protected String getValueString() {
		DistnVariable y = (DistnVariable)getVariable(distnKey);
		NumValue value = y.getSD();
		return value.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}


public class TPValueApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String TEST_PARAM = "testInfo";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String MEANS_PARAM = "means";
	static final private String SDS_PARAM = "sds";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	
	static final private String kTAxisScale = "-3 3 -3 1";
	
	static final protected Color kPinkColor = new Color(0xFFCCCC);
	static final protected Color kDarkGreenColor = new Color(0x006600);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected HypothesisTest test;
	private int testTail;
	
	protected NumValue testParam;
	protected NumValue lowMean, highMean, meanStep, startMean;
	protected NumValue lowSD, highSD, sdStep, startSD;
	private SampStatSlider meanSlider, sdSlider;
	
	private int meanDecimals, tDecimals, pValueDecimals;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		readMeanShiftRange();
		readSDShiftRange();
		
		getData();
		test = readTestInfo(data, "y");
		getSummaryData(data);
		
		setLayout(new ProportionLayout(0.3333, 3, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		add(ProportionLayout.TOP, dataPanel(data));
		
		XPanel summaryPanel = new XPanel();
		summaryPanel.setLayout(new ProportionLayout(0.5, 3, ProportionLayout.VERTICAL,
																								ProportionLayout.TOTAL));
		summaryPanel.add(ProportionLayout.TOP, meanPanel(summaryData));
		summaryPanel.add(ProportionLayout.BOTTOM, tPanel(summaryData));
		
		add(ProportionLayout.BOTTOM, summaryPanel);
	}
	
	protected void getData() {
		data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		data.addNumVariable("raw", translate("Raw data"), vals);
		
		FixedMeanSDVariable shiftedY = new FixedMeanSDVariable(getParameter(VAR_NAME_PARAM), data, "raw", startMean.toDouble());
		shiftedY.setSD(startSD.toDouble());
		
		data.addVariable("y", shiftedY);
	}
	
	protected void getSummaryData(DataSet data) {
		summaryData = new SummaryDataSet(data, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_DECIMALS_PARAM));
		meanDecimals = Integer.parseInt(st.nextToken());
		tDecimals = Integer.parseInt(st.nextToken());
		pValueDecimals = Integer.parseInt(st.nextToken());
		
		MeanVariable mean = new MeanVariable("mean of data", "y", meanDecimals);
		StatisticValueVariable tValue = new StatisticValueVariable(translate("t statistic"), test, tDecimals);
		PValueVariable pValue = new PValueVariable(translate("p-value") + " = ", test, pValueDecimals);
		
		NormalDistnVariable meanDistn = new NormalDistnVariable("mean distn");
		meanDistn.setDecimals(meanDecimals);
		summaryData.addVariable("meanTheory", meanDistn);
		
		TDistnVariable tDistn = new TDistnVariable("mean distn", 1);
		summaryData.addVariable("tDistn", tDistn);
		
		summaryData.addVariable("mean", mean);
		summaryData.addVariable("t", tValue);
		summaryData.addVariable("pValue", pValue);
		
		setSummaryDistns();
		summaryData.setSingleSummaryFromData();
	}
	
	protected void readMeanShiftRange() {
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		
		lowMean = new NumValue(st.nextToken());
		highMean = new NumValue(st.nextToken());
		meanStep = new NumValue(st.nextToken());
		
		startMean = new NumValue(st.nextToken());
	}
	
	protected void readSDShiftRange() {
		StringTokenizer st = new StringTokenizer(getParameter(SDS_PARAM));
		
		lowSD = new NumValue(st.nextToken());
		highSD = new NumValue(st.nextToken());
		sdStep = new NumValue(st.nextToken());
		
		startSD = new NumValue(st.nextToken());
	}
	
	private HypothesisTest readTestInfo(DataSet data, String yKey) {
		NumValue testParam = new NumValue(getParameter(TEST_PARAM));
		
		String tailString = getParameter(ALTERNATIVE_PARAM);
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		return new UnivarHypothesisTest(data, yKey, testParam, testTail, HypothesisTest.MEAN, this);
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		thePanel.add(ProportionLayout.LEFT, dataPlotPanel(data));
		
		thePanel.add(ProportionLayout.RIGHT, sliderPanel());
		
		return thePanel;
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
		
		DotPlotView dataView = new DotPlotView(data, this, theHorizAxis, 1.0);
		dataView.setActiveNumVariable("y");
		dataView.setForeground(Color.blue);
		thePanel.add("Center", dataView);
		dataView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
//		thePanel.setLayout(new ProportionLayout(0.5, 4, ProportionLayout.HORIZONTAL,
//																							ProportionLayout.TOTAL));
		
		meanSlider = new SampStatSlider(lowMean, highMean, meanStep, startMean, SampStatSlider.MEAN, this);
		meanSlider.setForeground(Color.blue);
		thePanel.add(ProportionLayout.LEFT, meanSlider);
		
		sdSlider = new SampStatSlider(lowSD, highSD, sdStep, startSD, SampStatSlider.SD, this);
		sdSlider.setForeground(Color.blue);
		thePanel.add(ProportionLayout.RIGHT, sdSlider);
		
		return thePanel;
	}
	
	private XPanel meanPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		thePanel.add(ProportionLayout.LEFT, meanPlotPanel(summaryData));
		thePanel.add(ProportionLayout.RIGHT, fittedPanel());
		
		return thePanel;
	}
	
	private XPanel meanPlotPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		CoreVariable v = summaryData.getVariable("mean");
		theHorizAxis.setAxisName(v.name);
		
		JitterPlusNormalView meanView = new JitterPlusNormalView(summaryData, this, theHorizAxis, "meanTheory", 0.0);
		meanView.setActiveNumVariable("mean");
		meanView.setDensityColor(kPinkColor);
		thePanel.add("Center", meanView);
		
		meanView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel fittedPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		SummaryView popnMean = new SummaryView(summaryData, this, "meanTheory", null, SummaryView.MEAN,
																															meanDecimals, SummaryView.POPULATION);
		popnMean.setForeground(Color.red);
		thePanel.add(popnMean);
		
		SDOfMeanValueView sampSD = new SDOfMeanValueView(summaryData, this, "meanTheory");
		sampSD.setForeground(Color.red);
		thePanel.add(sampSD);
		
		return thePanel;
	}
	
	private XPanel tPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
		thePanel.add(ProportionLayout.LEFT, tTailPanel(summaryData));
		thePanel.add(ProportionLayout.RIGHT, pValuePanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel tTailPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(kTAxisScale);
		thePanel.add("Bottom", theHorizAxis);
		CoreVariable v = summaryData.getVariable("t");
		theHorizAxis.setAxisName(v.name);
		
		int tail = (testTail == HypothesisTest.HA_LOW) ? DistnTailView.LOW_TAIL
						: (testTail == HypothesisTest.HA_HIGH) ? DistnTailView.HIGH_TAIL
						: DistnTailView.TWO_TAIL;
		DistnTailView tView = new DistnTailView(summaryData, this, theHorizAxis, "tDistn", "t", tail);
		thePanel.add("Center", tView);
		
		tView.lockBackground(Color.white);
		tView.setHighlightColor(kDarkGreenColor);
		
		return thePanel;
	}
	
	private XPanel pValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		OneValueView tView = new OneValueImageView(summaryData, "t", this, "test/tFormula.gif", 21);
		tView.setForeground(kDarkGreenColor);
		thePanel.add(tView);
		
		OneValueView pValueView = new OneValueView(summaryData, "pValue", this);
		pValueView.setForeground(kDarkGreenColor);
		thePanel.add(pValueView);
		
		return thePanel;
	}
	
	private void setSummaryDistns() {
		double sum = 0.0;
		double sum2 = 0.0;
		int count = 0;
		ValueEnumeration ye = ((Variable)data.getVariable("y")).values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sum += y;
			sum2 += y * y;
			count ++;
		}
		
		NormalDistnVariable meanDistn = (NormalDistnVariable)summaryData.getVariable("meanTheory");
		meanDistn.setMean(0.0);
		meanDistn.setSD(Math.sqrt((sum2 - sum * sum / count) / (count - 1) / count));
		
		TDistnVariable tDistn = (TDistnVariable)summaryData.getVariable("tDistn");
		tDistn.setDF(count - 1);
	}

	
	private boolean localAction(Object target) {
		if (target == meanSlider) {
			FixedMeanSDVariable yVariable = (FixedMeanSDVariable)data.getVariable("y");
			yVariable.setMean(meanSlider.getStat());
			data.variableChanged("y");
			setSummaryDistns();
			summaryData.variableChanged("meanTheory");
			summaryData.redoLastSummary();
			
			return true;
		}
		else if (target == sdSlider) {
			FixedMeanSDVariable yVariable = (FixedMeanSDVariable)data.getVariable("y");
			yVariable.setSD(sdSlider.getStat());
			data.variableChanged("y");
			setSummaryDistns();
			summaryData.variableChanged("meanTheory");
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