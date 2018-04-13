package testProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import distn.*;
import qnUtils.*;
import coreGraphics.*;
import imageGroups.*;

import test.*;
import randomStatProg.*;


class ChangePSlider extends XNoValueSlider {
	static final private int kMaxVal = 50;
	
	public ChangePSlider(XApplet applet) {
		super(applet.translate("normal"), applet.translate("skew"), null, 0, kMaxVal, 0, applet);
	}
	
	protected double getChangeP() {
		return getChangeP(getValue());
	}
	
	protected double getChangeP(int val) {
		return 0.5 - (0.5 * val) / kMaxVal;
	}
}


public class ShapiroWilkesApplet extends SampleMeanApplet {
	static final private String MAX_PARAM = "maxValue";
	static final private String COUNT_PARAM = "noOfValues";
	
	static final private String kPValueAxisInfo = "0 1 0.0 0.2";
	
	private HypothesisTest test;
	private ChangePSlider changePSlider;
	
	public void setupApplet() {
		ScalesImages.loadScales(this);
		
		data = getData();
		test = readTestInfo(data, "y");
		summaryData = getSummaryData(data);
		generateInitialSample(summaryData);
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.5, 5));
		dataPanel.add(ProportionLayout.LEFT, leftPanel(data));
		dataPanel.add(ProportionLayout.RIGHT, pValuePanel(summaryData));
		add("Center", dataPanel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		double modelMax = (new NumValue(getParameter(MAX_PARAM))).toDouble();
		int noOfValues = Integer.parseInt(getParameter(COUNT_PARAM));
		
		RandomRectangular localGenerator = new RandomRectangular(noOfValues, 0.0, 1.0);
		NumVariable y = new NumSampleVariable("raw", localGenerator, 8);
		data.addVariable("raw", y);
		
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		SkewedVariable py = new SkewedVariable(getParameter(VAR_NAME_PARAM), y, "raw", "y",
																									decimals, modelMax);
		data.addVariable("y", py);
		
		SkewedDistnVariable dataDistn = new SkewedDistnVariable("data model");
		dataDistn.setParams(getParameter(MAX_PARAM));
		data.addVariable("model", dataDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "raw");
		
		int pValueDecimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		PValueVariable pValue = new PValueVariable(translate("p-value"), test, pValueDecimals);
		
		summaryData.addVariable("p-value", pValue);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
	}
	
	protected HypothesisTest readTestInfo(DataSet data, String yKey) {
		return new ShapiroWilkesTest(data, yKey, this);
	}
	
	protected XPanel leftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
		thePanel.add("Center", dataPanel(data));
		
		XPanel topPanel = new XPanel();
		topPanel.setLayout(new BorderLayout(0, 8));
		topPanel.add("North", new HypothesisView(test, HypothesisView.VERTICAL, this));
		
		changePSlider = new ChangePSlider(this);
//		meanSlider.setFont(kSliderFont);
		topPanel.add("Center", changePSlider);
		
		thePanel.add("North", topPanel);
		
		thePanel.add("South", topControlPanel(data, summaryData, "p-value"));
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = data.getVariable("y");
		theHorizAxis.setAxisName(v.name);
		
		thePanel.add("Bottom", theHorizAxis);
		
		JitterPlusNormalView dataView = new JitterPlusNormalView(data, this, theHorizAxis, "model", 1.0);
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
		
		DotCumulativeView pValueView = new DotCumulativeView(summaryData, this, theHorizAxis, true);
		pValueView.setActiveNumVariable("p-value");
		thePanel.add("Center", pValueView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == changePSlider) {
			double newChangeP = changePSlider.getChangeP();
			
			SkewedVariable pv = (SkewedVariable)data.getVariable("y");
			pv.setChangeP(newChangeP);
			data.variableChanged("y");
			
			SkewedDistnVariable distn = (SkewedDistnVariable)data.getVariable("model");
			distn.setChangeP(newChangeP);
			data.variableChanged("model");
			
			summaryData.setSingleSummaryFromData();
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