package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
import graphics3D.*;

import linMod.*;


public class RotateModel2Applet extends RotateModelApplet {
	static final private String X_DISTN_NAME_PARAM = "xDistnName";
	static final private String X_DISTN_PARAM = "xDistn";
	
	private XChoice xChoice;
	private int currentXChoice = 0;
	
	private RandomRectangular xGenerator[] = new RandomRectangular[3];
	
	private void addXVariable(DataSet data, String xKey, int index) {
		String valueString = getParameter(X_VALUES_PARAM + (index + 1));
		if (valueString != null)
			data.addNumVariable(xKey, getParameter(X_VAR_NAME_PARAM), valueString);
		else {
			xGenerator[index] = new RandomRectangular(getParameter(X_DISTN_PARAM + (index + 1)));
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			double vals[] = xGenerator[index].generate();
			xVar.setValues(vals);
			data.addVariable(xKey, xVar);
		}
		
	}
	
	protected DataSet readData() {
		data = super.readData();
		
		addXVariable(data, "x2", 1);
		addXVariable(data, "x3", 2);
//		data.addNumVariable("x2", getParameter(X_VAR_NAME_PARAM),
//																			getParameter(X_VALUES_PARAM + "2"));
//		data.addNumVariable("x3", getParameter(X_VAR_NAME_PARAM),
//																			getParameter(X_VALUES_PARAM + "3"));
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateDragXView theView = new RotateDragXView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y", startX);
		theView.setBigHitRadius();
		return theView;
	}
	
	protected void addChoice(XPanel targetPanel) {
		xChoice = new XChoice(this);
		xChoice.addItem(getParameter(X_DISTN_NAME_PARAM));
		xChoice.addItem(getParameter(X_DISTN_NAME_PARAM + "2"));
		xChoice.addItem(getParameter(X_DISTN_NAME_PARAM + "3"));
		
		targetPanel.add(xChoice);
	}
	
	protected void changeChoice(int newChoice) {
		String newXKey = (newChoice == 0) ? "x" : (newChoice == 1) ? "x2" : "x3";
		
		((ResponseVariable)data.getVariable("y")).setXKey(newXKey);
		((RotateDragXView)theView).setXKey(newXKey);
		((RotateDragXView)theView).setShowData(false);
		
		data.variableChanged("y");
	}
	
	protected void takeSample() {
		if (xGenerator[currentXChoice] != null) {
			NumVariable xVar = (NumVariable)data.getVariable("x" + (currentXChoice + 1));
			double vals[] = xGenerator[currentXChoice].generate();
			xVar.setValues(vals);
		}
		super.takeSample();
	}
	
	private boolean localAction(Object target) {
		if (target == xChoice) {
			if (currentXChoice != xChoice.getSelectedIndex()) {
				currentXChoice = xChoice.getSelectedIndex();
				changeChoice(currentXChoice);
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