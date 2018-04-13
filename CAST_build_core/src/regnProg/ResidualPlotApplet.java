package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import regnView.*;


public class ResidualPlotApplet extends ChangeValuesApplet {
	static final protected String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String VERTICAL_PARAM = "vertical";
	
	private XCheckbox changeCheck;
	
	protected LinearModel createModel(DataSet data) {
		LinearModel modelVariable = new LinearModel("model", data, "x");
		modelVariable.setLSParams("y", 0, 0, 0);
		return modelVariable;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addVariable("resid", new ResidValueVariable(translate("Residual") + ", e", data, "x", "y", "model",
																												0));
		data.addVariable("zero", new LinearModel("Zero", data, "x", LinearModel.kZero, LinearModel.kZero,
																									LinearModel.kZero));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		String verticalParam = getParameter(VERTICAL_PARAM);
		if (verticalParam != null && verticalParam.equals("true")) {
			thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL, ProportionLayout.TOTAL));
			
			thePanel.add("Top", createPlotPanel(data, false, "x", "y", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			thePanel.add("Bottom", createPlotPanel(data, false, "x", "resid", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		}
		else {
			thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
			
			thePanel.add("Left", createPlotPanel(data, false, "x", "y", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
			thePanel.add("Right", createPlotPanel(data, false, "x", "resid", null,
								getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		}
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		if (plotIndex == 0)
			return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		else
			return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "resid", "zero");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		String checkString = getParameter(CHANGE1_NAME_PARAM);
		if (checkString != null) {
			changeCheck = new XCheckbox(getParameter(CHANGE1_NAME_PARAM), this);
			changeCheck.setState(false);
			thePanel.add(changeCheck);
		}
		
		return thePanel;
	}
	
	protected void updateLSParams() {
		LinearModel modelVariable = (LinearModel)data.getVariable("model");
		modelVariable.setLSParams("y", 0, 0, 0);
	}

	
	private boolean localAction(Object target) {
		if (target == changeCheck) {
			boolean doChange = changeCheck.getState();
			doChanges(change1, doChange);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}