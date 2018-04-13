package regnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class Residual2PlotApplet extends ResidualPlotApplet {
	private XChoice modelChoice;
	private int selectedModel;
	
	protected LinearModel createModel(DataSet data) {
		QuadraticModel modelVariable = new QuadraticModel("model", data, "x");
		modelVariable.setLSLinearParams("y", 0, 0, 0, 0);
		return modelVariable;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		
		modelChoice = new XChoice(this);
		modelChoice.addItem(translate("Linear fit"));
		modelChoice.addItem(translate("Quadratic fit"));
		modelChoice.select(0);
		selectedModel = 0;
		thePanel.add(modelChoice);
		
		return thePanel;
	}
	
	protected void updateLSParams() {
		QuadraticModel modelVariable = (QuadraticModel)data.getVariable("model");
		if (modelChoice.getSelectedIndex() == 0)
			modelVariable.setLSLinearParams("y", 0, 0, 0, 0);
		else
			modelVariable.setLSParams("y", 0, 0, 0, 0);
	}

	
	private boolean localAction(Object target) {
		if (target == modelChoice) {
			if (modelChoice.getSelectedIndex() != selectedModel) {
				selectedModel = modelChoice.getSelectedIndex();
				QuadraticModel theModel = (QuadraticModel)data.getVariable("model");
				if (selectedModel == 0)
					theModel.setLSLinearParams("y", 0, 0, 0, 0);
				else
					theModel.setLSParams("y", 0, 0, 0, 0);
				data.variableChanged("model");
			}
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