package scatterProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
import coreGraphics.*;



public class RandomScatterApplet extends ScatterApplet {
	static final private String RANDOM_PARAM = "random";
																// if xValues == null, then bivar normal params
																//	else univar normal error params
	static final private String REGN_MODEL_PARAM = "regnModel";
	
	private XButton sampleButton;
	private RandomBiNormal biNormGenerator;
	private RandomNormal normGenerator;
	
	private boolean randomX;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String xValuesString = getParameter(X_VALUES_PARAM);
		randomX = (xValuesString == null);
		if (randomX) {
			String randomInfo = getParameter(RANDOM_PARAM);
			biNormGenerator = new RandomBiNormal(randomInfo);
			double vals[][] = biNormGenerator.generate();
			data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), vals[0]);
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), vals[1]);
		}
		else {
			data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
			
			LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
			yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
			data.addVariable("model", yDistn);
			
			String randomInfo = getParameter(RANDOM_PARAM);
			normGenerator = new RandomNormal(randomInfo);
			
			NumVariable error = new NumVariable(translate("Error"));
			double vals[] = normGenerator.generate();
			error.setValues(vals);
			data.addVariable("error", error);
			
			ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																							data, "x", "error", "model", 10);
			data.addVariable("y", yData);
		}
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		sampleButton = new XButton(translate("Another sample"), this);
		thePanel.add(sampleButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			if (randomX) {
				NumVariable xVariable = (NumVariable)data.getVariable("x");
				NumVariable yVariable = (NumVariable)data.getVariable("y");
				double vals[][] = biNormGenerator.generate();
				synchronized (data) {
					xVariable.setValues(vals[0]);
					yVariable.setValues(vals[1]);
					data.variableChanged("x");
					data.variableChanged("y");
				}
			}
			else {
				double vals[] = normGenerator.generate();
				((NumVariable)data.getVariable("error")).setValues(vals);
				data.variableChanged("error");
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