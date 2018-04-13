package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import valueList.*;
import models.*;

import regn.*;
import regnView.*;


public class LinearScaleApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "horizAxis";
	static final protected String Y_AXIS_INFO_PARAM = "vertAxis";
	static final private String INTERCEPT_PARAM = "interceptLimits";
	static final private String SLOPE_PARAM = "slopeLimits";
	static final protected String SCALED_NAME_PARAM = "scaledName";
	static final private String MIN_MAX_PARAM = "minMax";
	
	static final private NumValue kMaxScaled = new NumValue(100.0, 1);
	
	static final private Color kRawColor = Color.blue;
	static final private Color kScaledColor = new Color(0x006600);
	
	private NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart;
	protected boolean hasLabels;
	
	protected DataSet data;
	private XCheckbox interceptCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("North", topPanel(data));
		add("Center", displayPanel(data));
		
		XPanel theControlPanel = controlPanel(data);
		if (theControlPanel != null)
			add("South", theControlPanel);
	}
	
	protected DataSet readData() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(INTERCEPT_PARAM));
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SLOPE_PARAM));
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
		
		data = new DataSet();
		data.addNumVariable("x", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		LinearModel modelVariable = new LinearModel("model", data, "x", intStart, slopeStart,
																																	new NumValue(0.0, 0));
		modelVariable.setFixedIntercept(true);
		data.addVariable("model", modelVariable);
		
		FittedValueVariable yVar = new FittedValueVariable(getParameter(SCALED_NAME_PARAM),
																										data, "x", "model", kMaxScaled.decimals);
		data.addVariable("scaled", yVar);
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		hasLabels = (labelName != null);
		if (hasLabels)
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
																			
		return data;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			if (hasLabels)
				valuePanel.add(new OneValueView(data, "label", this));
			
			OneValueView rawVal = new OneValueView(data, "x", this);
			rawVal.setForeground(kRawColor);
			valuePanel.add(rawVal);
			
			OneValueView scaledVal = new OneValueView(data, "scaled", this, kMaxScaled);
			scaledVal.setForeground(kScaledColor);
			valuePanel.add(scaledVal);
			
		thePanel.add(valuePanel);
		
			XLabel scaledLabel = new XLabel(getParameter(SCALED_NAME_PARAM), XLabel.LEFT, this);
			scaledLabel.setForeground(kScaledColor);
		thePanel.add(scaledLabel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			interceptCheck = new XCheckbox(translate("Zero unchanged"), this);
			interceptCheck.setState(true);
		equationPanel.add(interceptCheck);
		
		equationPanel.add(new LinearEquationView(data, this, "model", getParameter(SCALED_NAME_PARAM),
										getParameter(VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax));
		
		return equationPanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
		horizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		horizAxis.setForeground(kRawColor);
		thePanel.add("Bottom", horizAxis);
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(Y_AXIS_INFO_PARAM));
		vertAxis.setForeground(kScaledColor);
		thePanel.add("Left", vertAxis);
		
		DotPlotView scaledMarginView = new DotPlotView(data, this, vertAxis, 1.0);
		scaledMarginView.setActiveNumVariable("scaled");
		scaledMarginView.setRetainLastSelection(true);
		scaledMarginView.setForeground(kScaledColor);
		thePanel.add("LeftMargin", scaledMarginView);
		
		DotPlotView rawMarginView = new DotPlotView(data, this, horizAxis, 1.0);
		rawMarginView.setActiveNumVariable("x");
		rawMarginView.setRetainLastSelection(true);
		rawMarginView.setForeground(kRawColor);
		thePanel.add("BottomMargin", rawMarginView);
		
		DataView theView = createDataView(data, horizAxis, vertAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		StringTokenizer st = new StringTokenizer(getParameter(MIN_MAX_PARAM));
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return new LinearScalingView(data, this, theHorizAxis, theVertAxis, "x", "model", min, max);
	}

	
	private boolean localAction(Object target) {
		if (target == interceptCheck) {
			LinearModel modelVariable = (LinearModel)data.getVariable("model");
			if (interceptCheck.getState()) {
				modelVariable.setIntercept(0.0);
				modelVariable.setSlope(1.0);
			}
				
			modelVariable.setFixedIntercept(interceptCheck.getState());
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}

