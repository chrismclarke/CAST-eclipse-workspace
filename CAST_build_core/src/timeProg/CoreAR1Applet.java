package timeProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import regn.*;
import time.*;


abstract public class CoreAR1Applet extends CoreActualResidApplet {
	static final private String MAX_AR1_PARAMS_PARAM = "maxAR1Params";
	
	static final protected boolean SHOW_ZERO_BUTTON = true;
	
	static final private Color kEqnBackground = new Color(0xDDDDEE);
	
	private NumValue maxAR1Intercept, maxAR1Slope;
	
	private XButton lsButton, zeroResButton, identityResButton;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_AR1_PARAMS_PARAM));
		maxAR1Intercept = new NumValue(st.nextToken());
		maxAR1Slope = new NumValue(st.nextToken());
		LinearModel ar1Model = new LinearModel("AR1Model", data, "time",
					new NumValue(0.0, maxAR1Intercept.decimals), new NumValue(1.0, maxAR1Slope.decimals));
		data.addVariable("ar1Model", ar1Model);
		
		return data;
	}
	
	abstract protected String getActualKey();
	abstract protected String getLagKey();
	
	protected TimeView getTimeView(DataSet data, IndexTimeAxis horizAxis, VertAxis vertAxis) {
		return new TimeView(data, this, horizAxis, vertAxis);
	}
	
	protected XPanel ar1ScatterPanel(DataSet data, String yKey, String lagKey, String modelKey, String smoothKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
		thePanel.add("Bottom", horizAxis);
		if (labelAxes)
			horizAxis.setAxisName(data.getVariable(lagKey).name);
		thePanel.add("Bottom", horizAxis);
		
		DragLineARView theView = new DragLineARView(data, this, horizAxis, vertAxis, lagKey, yKey, modelKey, smoothKey);
		theView.setRetainLastSelection(true);
		theView.setDrawResiduals(false);
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		thePanel = addYAxisName(thePanel, data, yKey, vertAxis, null);
		return thePanel;
	}
	
	protected XPanel getAREqn(DataSet data, String yName, String yLagName) {
		XPanel thePanel = new InsetPanel(10, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			LinearEquationView theEqn = new LinearEquationView(data, this, "ar1Model", yName,
											yLagName, maxAR1Intercept, maxAR1Intercept, maxAR1Slope, maxAR1Slope);
		
		thePanel.add(theEqn);
		thePanel.lockBackground(kEqnBackground);
		
		return thePanel;
	}
	
	protected XPanel arButtonPanel(boolean showZeroButton) {
		return arButtonPanel(showZeroButton, translate("Predict with") + ":", true);
	}
	
	protected XPanel arButtonPanel(boolean showZeroButton, String labelString, boolean horizontal) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 2));
		
			XLabel label = new XLabel(labelString, XLabel.LEFT, this);
			label.setFont(getStandardBoldFont());
		thePanel.add(label);
		
			XPanel buttonPanel = new XPanel();
			if (horizontal)
				buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			else
				buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 5));
			
			if (showZeroButton) {
				zeroResButton = new XButton(translate("Zero"), this);
				buttonPanel.add(zeroResButton);
			}
			
				identityResButton = new XButton(translate("Previous"), this);
			buttonPanel.add(identityResButton);
			
				lsButton = new XButton(translate("Least sqrs"), this);
			buttonPanel.add(lsButton);
			
		thePanel.add(buttonPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		LinearModel ar1Model = (LinearModel)data.getVariable("ar1Model");
		int selectedIndex = data.getSelection().findSingleSetFlag();
		if (target == lsButton) {
			ar1Model.setXKey(getLagKey());
			ar1Model.updateLSParams(getActualKey());
			data.variableChanged("ar1Model", selectedIndex);
			return true;
		}
		else if (target == zeroResButton) {
			ar1Model.setIntercept(0.0);
			ar1Model.setSlope(0.0);
			data.variableChanged("ar1Model", selectedIndex);
			return true;
		}
		else if (target == identityResButton) {
			ar1Model.setIntercept(0.0);
			ar1Model.setSlope(1.0);
			data.variableChanged("ar1Model", selectedIndex);
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