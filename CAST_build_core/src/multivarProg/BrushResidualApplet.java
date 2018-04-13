package multivarProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;


import regnProg.*;
//import regn.*;
import regnView.*;
import multivar.*;


public class BrushResidualApplet extends ResidualPlotApplet {
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	private XCheckbox changeCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		return data;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		DataView theView;
		if (plotIndex == 0)
			theView = new LSScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		else
			theView = new BrushExtremesView(data, this, theHorizAxis, theVertAxis, "x", "resid");
//		theView.setCrossSize(DataView.SMALL_CROSS);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel mapPanel = new XPanel();
		mapPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		mapPanel.add(new USAMapView(data, this, "z", "x"));
		
		thePanel.add("Center", mapPanel);
		
		XPanel checkPanel = new XPanel();
		checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
		OneValueView labelView = new OneValueView(data, "label", this);
		checkPanel.add(labelView);
		
		changeCheck = new XCheckbox(getParameter(CHANGE1_NAME_PARAM), this);
		changeCheck.setState(false);
		checkPanel.add(changeCheck);
		
		thePanel.add("East", checkPanel);
		return thePanel;
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