package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import test.*;
import distn.*;
import qnUtils.*;
import valueList.*;
import coreVariables.*;
import formula.*;
import imageGroups.*;


class PopnMeanSlider extends XSlider {
	
	private NumValue lowMean, meanStep;
	
	public PopnMeanSlider(NumValue lowMean, NumValue highMean, NumValue meanStep,
																NumValue startMean, XApplet applet) {
		super(lowMean.toString(), highMean.toString(), applet.translate("Real value of") + MText.expandText(" #mu# = "),
							0, (int)Math.round((highMean.toDouble() - lowMean.toDouble()) / meanStep.toDouble()),
							(int)Math.round((startMean.toDouble() - lowMean.toDouble()) / meanStep.toDouble()), applet);
		this.lowMean = lowMean;
		this.meanStep = meanStep;
	}
	
	protected Value translateValue(int val) {
		return new NumValue(getMean(val), meanStep.decimals);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return Math.max(translateValue(getMinValue()).stringWidth(g),
															translateValue(getMaxValue()).stringWidth(g));
	}
	
	protected double getMean() {
		return getMean(getValue());
	}
	
	protected double getMean(int val) {
		return lowMean.toDouble() + val * meanStep.toDouble();
	}
}


public class MeanPValueApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DATA_DECIMALS_PARAM = "dataDecimals";
//	static final private String MEAN_NAME_PARAM = "meanName";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	
	static final private String TEST_PARAM = "testInfo";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String MEANS_PARAM = "means";
	static final private String CUMULATIVE_PARAM = "cumulative";
	
	static final private String kPValueAxisInfo = "0 1 0.0 0.2";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	private int decimals;
	
	private ScaledVariable scaledY;
	
	private RepeatingButton takeSampleButton;
	
//	private DataPlusDistnInterface dataView, summaryView;
	
	private HypothesisTest test;
	private PopnMeanSlider meanSlider;
	
	public void setupApplet() {
		ScalesImages.loadScales(this);
		
		data = getData();
		test = readTestInfo(data, "y");
		summaryData = getSummaryData(data);
		
		summaryData.takeSample();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", new HypothesisView(test, this));
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 5));
			dataPanel.add(ProportionLayout.LEFT, leftPanel(data));
			dataPanel.add(ProportionLayout.RIGHT, pValuePanel(summaryData));
		
		add("Center", dataPanel);
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			RandomNormal generator = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM));
			generator.setMean(0.0);
			decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
			NumVariable y = new NumSampleVariable("raw", generator, decimals);
		data.addVariable("yRaw", y);
		
			NormalDistnVariable dataDistn = new NormalDistnVariable("data model");
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_NORMAL_PARAM));
			@SuppressWarnings("unused")
			int noOfValues = Integer.parseInt(st.nextToken());
			NumValue modelMean = new NumValue(st.nextToken());
			NumValue modelSD = new NumValue(st.nextToken());
			dataDistn.setParams(modelMean.toString() + " " + modelSD.toString());
		data.addVariable("model", dataDistn);
		
			scaledY = new ScaledVariable(getParameter(VAR_NAME_PARAM), y, "yRaw", modelMean.toDouble(),
																							1.0, decimals);
		data.addVariable("y", scaledY);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "yRaw");
		
		int pValueDecimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		PValueVariable pValue = new PValueVariable(translate("p-value"), test, pValueDecimals);
		
		summaryData.addVariable("p-value", pValue);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private HypothesisTest readTestInfo(DataSet data, String yKey) {
		NumValue testParam = new NumValue(getParameter(TEST_PARAM));
		
		String tailString = getParameter(ALTERNATIVE_PARAM);
		int testTail;
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		return new UnivarHypothesisTest(data, yKey, testParam, testTail, HypothesisTest.MEAN, this);
	}
	
	private XPanel leftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
		thePanel.add("Center", dataPanel(data));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 8));
//			topPanel.add("North", new HypothesisView(test, this));
			
				StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
				NumValue lowMean = new NumValue(st.nextToken());
				NumValue highMean = new NumValue(st.nextToken());
				NumValue meanStep = new NumValue(st.nextToken());
				NormalDistnVariable dataDistn = (NormalDistnVariable)data.getVariable("model");
				
				meanSlider = new PopnMeanSlider(lowMean, highMean, meanStep, dataDistn.getMean(), this);
				meanSlider.setFont(getBigBoldFont());
			topPanel.add("Center", meanSlider);
		
		thePanel.add("North", topPanel);
		
		thePanel.add("South", controlPanel(data, summaryData, "p-value"));
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data, SummaryDataSet summaryData, String summaryKey) {
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		topPanel.add(takeSampleButton);
		
			OneValueView pValueView = new OneValueView(summaryData, summaryKey, this);
			
			pValueView.setFont(getBigFont());
			
		topPanel.add(pValueView);
		return topPanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = data.getVariable("y");
		theHorizAxis.setAxisName(v.name);
		
		thePanel.add("Bottom", theHorizAxis);
		
		StackTestMeanView dataView = new StackTestMeanView(data, this, theHorizAxis, "model", test);
		dataView.setActiveNumVariable("y");
		thePanel.add("Center", dataView);
		dataView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel pValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(kPValueAxisInfo);
		theHorizAxis.setAxisName(translate("p-value"));
		
		thePanel.add("Bottom", theHorizAxis);
		
		String cumulativeString = getParameter(CUMULATIVE_PARAM);
		boolean showCumulative = (cumulativeString != null) && cumulativeString.equals("true");
		
		DotCumulativeView pValueView = new DotCumulativeView(summaryData, this, theHorizAxis, showCumulative);
		pValueView.setActiveNumVariable("p-value");
		thePanel.add("Center", pValueView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		if (target == meanSlider) {
			double mean = meanSlider.getMean();
			scaledY.setScale(mean, 1.0, decimals);
			data.variableChanged("y");
			
			if (summaryData.getSelection().getNoOfFlags() == 1)
				summaryData.redoLastSummary();
			else {
				summaryData.setSingleSummaryFromData();
				summaryData.getSelection().checkSize(1);
				summaryData.setSelection(0);
			}
			summaryData.variableChanged("p-value", 0);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}