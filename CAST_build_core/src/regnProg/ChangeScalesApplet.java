package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import models.*;

import regn.*;
import regnView.*;
import utils.*;


public class ChangeScalesApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "horizAxis";
	static final protected String Y_AXIS_INFO_PARAM = "vertAxis";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String X_AXIS_SCALE_PARAM = "horiz2Scale";
	static final protected String Y_AXIS_SCALE_PARAM = "vert2Scale";
	
	private DataSet data;
	
	private ScatterLinesView theView;
	
	private ZoomSlider zoomer;
	private XChoice modelChoice;
	
	public void setupApplet() {
		data = readData();
		
		zoomer = new ZoomSlider(this, 100);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		QuadraticModel quadModelVariable = new QuadraticModel("model", data, "x");
		quadModelVariable.setLSParams("y", 0, 0, 0, 0);
		data.addVariable("model", quadModelVariable);
		
		LogLinearModel logLinModelVariable = new LogLinearModel("model2", data, "x");
		logLinModelVariable.setLSParams("y", 0, 0, 0);
		data.addVariable("model2", logLinModelVariable);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = zoomer.createHorizAxis(data, getParameter(X_AXIS_INFO_PARAM), getParameter(X_AXIS_SCALE_PARAM), this);
				NumVariable xVar = (NumVariable)data.getVariable("x");
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = zoomer.createVertAxis(data, getParameter(Y_AXIS_INFO_PARAM), getParameter(Y_AXIS_SCALE_PARAM), this);
			plotPanel.add("Left", vertAxis);
			
				theView = new ScatterLinesView(data, this, horizAxis, vertAxis, "x", "y", "model", "model2");
				zoomer.setControlledView(theView);
				theView.setCrossSize(DataView.LARGE_CROSS);
				theView.lockBackground(Color.white);
			plotPanel.add("Center", theView);
			
			zoomer.setScaleFraction(1.0);			//	start zoomed in
		
		thePanel.add("Center", plotPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			NumVariable yVar = (NumVariable)data.getVariable("y");
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(vertAxis.getFont());
			topPanel.add(yVariateName);
			
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		modelChoice = new XChoice(this);
		modelChoice.addItem(translate("No model"));
		modelChoice.addItem(translate("Quadratic model"));
		modelChoice.addItem(translate("Log linear model"));
		modelChoice.addItem(translate("Both models"));
		modelChoice.select(0);
		thePanel.add(modelChoice);
		
		thePanel.add(zoomer);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int modelSelected = modelChoice.getSelectedIndex();
			boolean showModel0 = modelSelected == 1 || modelSelected == 3;
			boolean showModel1 = modelSelected == 2 || modelSelected == 3;
			synchronized (data) {
				theView.showLine(0, showModel0);
				theView.showLine(1, showModel1);
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