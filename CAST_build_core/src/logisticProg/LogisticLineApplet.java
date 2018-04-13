package logisticProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import regn.*;
import logistic.*;


class ParameterLimits {
	public NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart;
	
	ParameterLimits(String intercept, String slope) {
		StringTokenizer paramLimits = new StringTokenizer(intercept);
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(slope);
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
	}
}

public class LogisticLineApplet extends XApplet {
	static final protected String Y_AXIS_INFO_PARAM = "horizAxis";
	static final protected String PROPN_AXIS_INFO_PARAM = "vertAxis";
	
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String X_LABELS_PARAM = "xLabels";
	
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Y_VALUES_PARAM = "yValues";
	
	static final protected String INTERCEPT_PARAM = "interceptLimits";
	static final protected String SLOPE_PARAM = "slopeLimits";
	
	static final protected String HANDLE_VALUES_PARAM = "handleValues";
	
	protected LogisticModel logisticVariable;
	protected DataSet data;
	protected HorizAxis theHorizAxis;
	protected VertAxis theVertAxis;
	
	protected ParameterLimits logistic;
	private ParameterSlider interceptSlider, slopeSlider;
	protected String successName;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable xCatVariable = new CatVariable(getParameter(X_VAR_NAME_PARAM),
																							Variable.USES_REPEATS);
		xCatVariable.readLabels(getParameter(X_LABELS_PARAM));
		xCatVariable.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xCatVariable);
		
		successName = xCatVariable.getLabel(0).toString();
		
		NumVariable yNumVariable = new NumVariable(getParameter(Y_VAR_NAME_PARAM), Variable.USES_REPEATS);
		yNumVariable.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yNumVariable);
		
		logistic = new ParameterLimits(getParameter(INTERCEPT_PARAM), getParameter(SLOPE_PARAM));
		logisticVariable = new LogisticModel("model", data, "x", logistic.intStart, logistic.slopeStart);
		String handlesString = getParameter(HANDLE_VALUES_PARAM);
		if (handlesString != null) {
			StringTokenizer handles = new StringTokenizer(handlesString);
			double lowHandle = Double.parseDouble(handles.nextToken());
			double highHandle = Double.parseDouble(handles.nextToken());
			logisticVariable.setManualHandles(lowHandle, highHandle);
		}
		data.addVariable("model", logisticVariable);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel xVariateName = new XLabel(translate("Pr") + "(" + successName + ")", XLabel.LEFT, this);
		xVariateName.setFont(theVertAxis.getFont());
		thePanel.add(xVariateName);
		
		return thePanel;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		return new HorizAxis(this);
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		return new VertAxis(this);
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		BarNumView theView = new BarNumView(data, this, theVertAxis, theHorizAxis, null, null, "model");
		theView.setShowPrediction(true, 0.0);
		return theView;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = createHorizAxis(data);
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(getParameter(Y_VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
		theVertAxis = createVertAxis(data);
		labelInfo = getParameter(PROPN_AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theVertAxis);
		
		DataView theView = createDataView(data, theHorizAxis, theVertAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected LinearEquationView createEquationView(DataSet data) {
		return new LogisticEquationView(data, this, "model",
								successName, getParameter(Y_VAR_NAME_PARAM), logistic.intMin, logistic.intMax,
								logistic.slopeMin, logistic.slopeMax, Color.red, Color.blue);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		XPanel equationPanel = new XPanel();
		equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		equationPanel.add(createEquationView(data));
		
		thePanel.add("Center", equationPanel);
		
		XPanel sliderPanel = new InsetPanel(0, 10, 0, 0);
		sliderPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		interceptSlider = new ParameterSlider(logistic.intMin, logistic.intMax, logistic.intStart, translate("Intercept"), this);
		interceptSlider.setForeground(Color.red);
		sliderPanel.add(ProportionLayout.LEFT, interceptSlider);
		
		slopeSlider = new ParameterSlider(logistic.slopeMin, logistic.slopeMax, logistic.slopeStart, translate("Slope"), this);
		slopeSlider.setForeground(Color.blue);
		sliderPanel.add(ProportionLayout.RIGHT, slopeSlider);
		
		thePanel.add("South", sliderPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == interceptSlider) {
			logisticVariable.setIntercept(interceptSlider.getParameter());
			data.variableChanged("model");
			return true;
		}
		else if (target == slopeSlider) {
			logisticVariable.setSlope(slopeSlider.getParameter());
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