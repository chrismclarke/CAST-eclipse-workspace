package logisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import regn.*;
import models.*;

import logistic.*;
import utils.*;


public class LinesApplet extends LogisticLineApplet {
	static final protected String LIN_INTERCEPT_PARAM = "linearInterceptLimits";
	static final protected String LIN_SLOPE_PARAM = "linearSlopeLimits";
	
	static final private int kPredictionDecimals = 3;
	
	protected ParameterLimits linear;
	
	private BarPredictionView theView;
	
	private XChoice modelChoice;
	private int currentSelection = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		linear = new ParameterLimits(getParameter(LIN_INTERCEPT_PARAM),
																					getParameter(LIN_SLOPE_PARAM));
		LinearModel linearVariable = new LinearModel("model2", data, "x", linear.intStart, linear.slopeStart);
		data.addVariable("model2", linearVariable);
		
		return data;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		return new DragExplanAxis(data.getVariable("y").name, this);
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		return new PredictionAxis(this);
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new BarPredictionView(data, this, (PredictionAxis)theVertAxis, (DragExplanAxis)theHorizAxis,
																									"x", "y", "model2", kPredictionDecimals);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		modelChoice = new XChoice(this);
		modelChoice.addItem(translate("Linear model"));
		modelChoice.addItem(translate("Nonlinear model"));
		thePanel.add(modelChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			int newSelection = modelChoice.getSelectedIndex();
			if (currentSelection != newSelection) {
				String newModelKey = (newSelection == 0) ? "model2" : "model";
				theView.setModel(newModelKey);
				((PredictionAxis)theVertAxis).setModel(data, "y", newModelKey,
															(DragExplanAxis)theHorizAxis, kPredictionDecimals);
				data.variableChanged(newModelKey);
				currentSelection = newSelection;
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