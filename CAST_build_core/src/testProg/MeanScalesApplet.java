package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import qnUtils.*;
import coreVariables.*;
import imageGroups.*;

import test.*;


class MeanSlider extends XSlider {
	private NumValue lowMean, meanStep;
	
	public MeanSlider(NumValue lowMean, NumValue highMean, NumValue meanStep, NumValue startMean,
																							XApplet applet) {
		super(null, null, applet.translate("Sample mean") + " = ", 0,
							(int)Math.round((highMean.toDouble() - lowMean.toDouble()) / meanStep.toDouble()),
							(int)Math.round((startMean.toDouble() - lowMean.toDouble()) / meanStep.toDouble()),
							applet);
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


public class MeanScalesApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String TEST_PARAM = "testInfo";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String MEANS_PARAM = "means";
	static final private String ALTERNATIVE_PARAM = "alternative";
	static final private String AXIS_LABEL_PARAM = "axisLabel";
	static final private String HYPOTH_DISPLAY_PARAM = "hypothDisplay";
	
//	static final private String kPValueAxisInfo = "0 1 0.0 0.2";
	
	protected DataSet data;
	private FixedMeanSDVariable shiftedY;
	
	protected HypothesisTest test;
	private int testTail;
	
	protected NumValue testParam;
	protected NumValue lowMean, highMean, meanStep, startMean;
	private MeanSlider meanSlider;
	
	protected PValueAxis axis;
	
	protected int axisLabelType;
	private int hypothDisplayType;
	
	public void setupApplet() {
		ScalesImages.loadScales(this);
		
		readTestInfo();
		data = getData();
		
		test = new UnivarHypothesisTest(data, "y", testParam, testTail, testParameter(), this);
		test.setCompositeNull(true);
		
		setLayout(new BorderLayout(0, 10));
		add("Center", dataPanel(data));
		add("South", pValuePanel(data));
	}
	
	protected int testParameter() {
		return HypothesisTest.MEAN;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomNormal generator = new RandomNormal(randomInfo);
		double vals[] = generator.generate();
		data.addNumVariable("raw", translate("Raw data"), vals);
		
		shiftedY = new FixedMeanSDVariable(getParameter(VAR_NAME_PARAM), data, "raw", startMean.toDouble());
		data.addVariable("y", shiftedY);
		
		return data;
	}
	
	protected void readShiftRange() {
		StringTokenizer st = new StringTokenizer(getParameter(MEANS_PARAM));
		
		lowMean = new NumValue(st.nextToken());
		highMean = new NumValue(st.nextToken());
		meanStep = new NumValue(st.nextToken());
		
		startMean = new NumValue(st.nextToken());
	}
	
	private void readTestInfo() {
		testParam = new NumValue(getParameter(TEST_PARAM));
		
		readShiftRange();
		
		String tailString = getParameter(ALTERNATIVE_PARAM);
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		
		String axisLabelString = getParameter(AXIS_LABEL_PARAM);
		if (axisLabelString.equals("standard"))
			axisLabelType = PValueAxis.STANDARD_TEXT;
		else if (axisLabelString.equals("pValue"))
			axisLabelType = PValueAxis.P_VALUE_TEXT;
		else
			axisLabelType = PValueAxis.SIMPLE_TEXT;
		
		String hypothDisplayString = getParameter(HYPOTH_DISPLAY_PARAM);
		if (hypothDisplayString.equals("param"))
			hypothDisplayType = HypothesisTest.PARAM_DRAW;
		else
			hypothDisplayType = HypothesisTest.GENERIC_DRAW;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dataPlotPanel(data));
		
		meanSlider = new MeanSlider(lowMean, highMean, meanStep, startMean, this);
		thePanel.add("South", meanSlider);
		
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
		
		DotPlotTestMeanView dataView = new DotPlotTestMeanView(data, this, theHorizAxis, test);
		dataView.setActiveNumVariable("y");
		thePanel.add("Center", dataView);
		dataView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel pValuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		axis = new PValueAxis(axisLabelType, test, hypothDisplayType, this);
		thePanel.add("East", axis);
		
		PValueScalesView pValueView = new PValueScalesView(data, this, axis, test, hypothDisplayType);
		axis.setScalesView(pValueView);
		thePanel.add("Center", pValueView);
		pValueView.lockBackground(Color.white);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == meanSlider) {
			FixedMeanSDVariable yVariable = (FixedMeanSDVariable)data.getVariable("y");
			yVariable.setMean(meanSlider.getMean());
			data.variableChanged("y");
			if (axisLabelType == PValueAxis.P_VALUE_TEXT)
				axis.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}