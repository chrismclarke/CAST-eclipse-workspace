package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import regnView.*;


public class LSMapApplet extends ChangeValuesApplet {
	static final protected String DECIMALS_PARAM = "decimals";
	
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	private XCheckbox change1Check;
	private XCheckbox change2Check;
	
	private int intDecs = 0;
	private int slopeDecs = 0;
	private int sdDecs = 0;
	@SuppressWarnings("unused")
	private int residDecs = 0;
	
	protected LinearModel createModel(DataSet data) {
		try {
			StringTokenizer decimals = new StringTokenizer(getParameter(DECIMALS_PARAM));
			intDecs = Integer.parseInt(decimals.nextToken());
			slopeDecs = Integer.parseInt(decimals.nextToken());
			sdDecs = Integer.parseInt(decimals.nextToken());
			residDecs = Integer.parseInt(decimals.nextToken());
		} catch (Exception e) {
		}
		
		LinearModel modelVariable = new LinearModel("model", data, "x");
		modelVariable.setLSParams("y", intDecs, slopeDecs, sdDecs);
		return modelVariable;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		return createPlotPanel(data, true, "x", "y", "label", getParameter(X_AXIS_INFO_PARAM),
																		getParameter(Y_AXIS_INFO_PARAM), 0);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel mapPanel = new XPanel();
		mapPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		mapPanel.add(new USAMapView(data, this, "z", "x"));
		
		thePanel.add("Center", mapPanel);
		
		XPanel checkPanel = new XPanel();
		checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		change1Check = new XCheckbox(getParameter(CHANGE1_NAME_PARAM), this);
		change1Check.setState(false);
		checkPanel.add(change1Check);
		change2Check = new XCheckbox(getParameter(CHANGE2_NAME_PARAM), this);
		change2Check.setState(false);
		checkPanel.add(change2Check);
		
		thePanel.add("South", checkPanel);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		return new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
	}
	
	protected void updateLSParams() {
		LinearModel modelVariable = (LinearModel)data.getVariable("model");
		modelVariable.setLSParams("y", intDecs, slopeDecs, sdDecs);
	}

	
	private boolean localAction(Object target) {
		if (target == change1Check) {
			boolean doChange = change1Check.getState();
			doChanges(change1, doChange);
			return true;
		}
		if (target == change2Check) {
			boolean doChange = change2Check.getState();
			doChanges(change2, doChange);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}