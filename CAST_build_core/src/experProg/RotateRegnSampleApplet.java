package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
//import coreVariables.*;

import multivarProg.*;


public class RotateRegnSampleApplet extends RotateApplet {
	static final private String MULTI_REGN_MODEL_PARAM = "yCoeffs";
	static final private String RANDOM_SEED = "randomSeed";
	
	static final private String kXKeys[] = {"x", "z"};
	
	private SummaryDataSet summaryData;
	
	private XButton sampleButton;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		createYData(data, getParameter(Y_VAR_NAME_PARAM));
		
		summaryData = new SummaryDataSet(data, "error");
		summaryData.takeSample();
		
		return data;
	}
	
	
	private int getValueCount(DataSet data) {
		NumVariable xVar = (NumVariable)data.getVariable("x");
		return xVar.noOfValues();
	}
	
	protected void createYData(DataSet data, String yName) {
		String seedString = getParameter(RANDOM_SEED);
		int count = getValueCount(data);
		String randomParams = String.valueOf(count) + " 0.0 1.0 " + seedString + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(count);
		data.addVariable("error", error);
		
			MultipleRegnModel model = new MultipleRegnModel("Model", data, kXKeys,
																												getParameter(MULTI_REGN_MODEL_PARAM));
		data.addVariable("model", model);
		
		ResponseVariable yData = new ResponseVariable(yName, data, kXKeys, "error", "model", 10);
		data.addVariable("y", yData);
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
			theView.setCrossSize(DataView.LARGE_CROSS);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			sampleButton = new XButton(translate("Repeat experiment"), this);
		thePanel.add(sampleButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
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