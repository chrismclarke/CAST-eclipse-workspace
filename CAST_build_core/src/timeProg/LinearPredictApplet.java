package timeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;
import models.*;

import regn.*;
import time.*;


public class LinearPredictApplet extends BasicTimeApplet {
	static final private String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String TIME_VAR_NAME_PARAM = "timeName";
	
	private XChoice modelChoice;
	private int selectedModel = 0;
	
	private FitPredictValueView smooth;
	
	private XPanel eqnPanel;
	private CardLayout eqnCardLayout;
	
	protected int fitDecs;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		NumVariable timeVar = new NumVariable(getParameter(TIME_VAR_NAME_PARAM));
		timeVar.readSequence(getParameter(TIME_SEQUENCE_PARAM));
		data.addVariable("time", timeVar);
		
		StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		int intDecs = Integer.parseInt(st.nextToken());
		int slopeDecs = Integer.parseInt(st.nextToken());
		int curvatureDecs = Integer.parseInt(st.nextToken());
		fitDecs = Integer.parseInt(st.nextToken());
		LinearModel model = new LinearModel("linearLS", data, "time");
		model.setLSParams("y", intDecs, slopeDecs, 0);
		data.addVariable("linearLS", model);
		
		FittedValueVariable fit = new FittedValueVariable("Fit", data, "time", "linearLS", fitDecs);
		data.addVariable("linearFit", fit);
		
		QuadraticModel model2 = new QuadraticModel("quadraticLS", data, "time");
		model2.setLSParams("y", intDecs, slopeDecs, curvatureDecs, 0);
		data.addVariable("quadraticLS", model2);
		
		FittedValueVariable fit2 = new FittedValueVariable("Fit", data, "time", "quadraticLS", fitDecs);
		data.addVariable("quadraticFit", fit2);
		
		return data;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		OneValueView actual = new OneValueView(data, "y", this);
		actual.setForeground(kActualColor);
		actual.setLabel(translate("Actual") + " =");
		thePanel.add(actual);
		
		smooth = new FitPredictValueView(data, "linearFit", "y", this, translate("Fitted value") + " =",
																										translate("Prediction") + " =");
		smooth.setForeground(kSmoothedColor);
		thePanel.add(smooth);
		
		return thePanel;
	}
	
	private XPanel linearEqnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		String yName = data.getVariable("y").name;
		String timeName = data.getVariable("time").name;
		LinearModel model = (LinearModel)data.getVariable("linearLS");
		NumValue intercept = model.getIntercept();
		NumValue slope = model.getSlope();
		thePanel.add(new LinearEquationView(data, this, "linearLS", yName, timeName,
																			intercept, intercept, slope, slope));
		return thePanel;
	}
	
	private XPanel quadraticEqnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		String yName = data.getVariable("y").name;
		String timeName = data.getVariable("time").name;
		QuadraticModel model = (QuadraticModel)data.getVariable("quadraticLS");
		NumValue intercept = model.getIntercept();
		NumValue slope = model.getSlope();
		NumValue curvature = model.getCurvature();
		thePanel.add(new QuadraticEquationView(data, this, "quadraticLS", yName, timeName,
																		intercept, intercept, slope, slope, curvature, curvature));
		return thePanel;
	}
	
	protected XPanel lowerControlPanel(DataSet data) {
		eqnPanel = new XPanel();
		eqnCardLayout = new CardLayout();
		eqnPanel.setLayout(eqnCardLayout);
		
		eqnPanel.add("linear", linearEqnPanel(data));
		eqnPanel.add("quadratic", quadraticEqnPanel(data));
		eqnCardLayout.show(eqnPanel, "linear");
		return eqnPanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel menuPanel = new XPanel();
			menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			modelChoice = new XChoice(this);
			modelChoice.addItem(translate("Linear model"));
			modelChoice.addItem(translate("Quadratic model"));
			modelChoice.select(0);
			
			menuPanel.add(modelChoice);
			
		thePanel.add("North", menuPanel);
		
		thePanel.add("Center", lowerControlPanel(data));
		
		return thePanel;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"linearFit"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return true;
	}
	
	protected void changeModelType(int newModelType) {
		String newFitKey = (newModelType == 0) ? "linearFit" : "quadraticFit";
		getView().setSmoothedVariable(newFitKey);
		getView().repaint();
		if (smooth != null)
			smooth.setVariableKey(newFitKey);
		if (eqnCardLayout != null)
			eqnCardLayout.show(eqnPanel, (newModelType == 0) ? "linear" : "quadratic");
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newModelType = modelChoice.getSelectedIndex();
			if (newModelType != selectedModel) {
				selectedModel = newModelType;
				changeModelType(newModelType);
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