package regnProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import regnView.*;


public class PiecewiseScaleApplet extends LinearScaleApplet {
	static final private String TRANSFORM_PARAM = "transform";
	static final private String DECIMALS_PARAM = "scaledDecimals";
	
	private XButton resetButton;
	
	protected DataSet readData() {
		data = new DataSet();
		data.addNumVariable("x", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		PiecewiseLinearModel modelVariable = new PiecewiseLinearModel("model", data, "x",
																										getParameter(TRANSFORM_PARAM));
		data.addVariable("model", modelVariable);
		
		int scaledDecimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
		FittedValueVariable yVar = new FittedValueVariable(getParameter(SCALED_NAME_PARAM),
																										data, "x", "model", scaledDecimals);
		data.addVariable("scaled", yVar);
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		hasLabels = (labelName != null);
		if (hasLabels)
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
																			
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		return new PiecewiseScalingView(data, this, theHorizAxis, theVertAxis, "x", "model");
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			PiecewiseLinearModel modelVariable = (PiecewiseLinearModel)data.getVariable("model");
			modelVariable.reset();
			int selectedIndex = data.getSelection().findSingleSetFlag();
			data.variableChanged("model", selectedIndex);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}

