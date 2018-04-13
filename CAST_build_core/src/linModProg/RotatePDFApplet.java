package linModProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
import graphics3D.*;

import linMod.*;


public class RotatePDFApplet extends RotateHistoApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	protected DataSet data;
	
	private XButton sampleButton;
	private XCheckbox showModelCheck;
	private RandomNormal generator;
	
	protected DataSet readData() {
		data = new DataSet();
		
		LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
		data.addVariable("model", yDistn);
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		
		NumVariable error = new NumVariable(translate("Error"));
		data.addVariable("error", error);
		
		ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																						data, "x", "error", "model", 10);
		data.addVariable("y", yData);
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotatePDFView theView = new RotatePDFView(data, this, xAxis, yAxis,
															densityAxis, "model", "x", "y", getParameter(SORTED_X_PARAM));
		theView.setBigHitRadius();
		return theView;
	}
	
	protected void addSampleButton(XPanel samplingPanel) {
		sampleButton = new XButton(translate("Take sample"), this);
		samplingPanel.add(sampleButton);
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, rotationPanel());
		
		XPanel samplingPanel = new XPanel();
		samplingPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		addSampleButton(samplingPanel);
		
		showModelCheck = new XCheckbox(translate("Show model"), this);
		showModelCheck.setState(true);
		showModelCheck.disable();
		samplingPanel.add(showModelCheck);
		
		thePanel.add(ProportionLayout.BOTTOM, samplingPanel);
		
		return thePanel;
	}
	
	protected void takeSample() {
		if (theView instanceof RotatePDFView)
			((RotatePDFView)theView).setPopNotSamp(false);
		double vals[] = generator.generate();
		((NumVariable)data.getVariable("error")).setValues(vals);
		data.variableChanged("error");
		if (showModelCheck != null) {
			showModelCheck.setState(false);
			showModelCheck.enable();
		}
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			takeSample();
			return true;
		}
		else if (target == showModelCheck) {
			((RotatePDFView)theView).setPopNotSamp(showModelCheck.getState());
			theView.repaint();
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