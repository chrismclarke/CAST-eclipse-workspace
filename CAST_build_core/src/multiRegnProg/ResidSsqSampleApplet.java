package multiRegnProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import random.*;


public class ResidSsqSampleApplet extends ResidSsq3dApplet {
	static final private String RANDOM_SEED = "randomSeed";
	static final private String MULTI_REGN_MODEL_PARAM = "multiRegnModel";
	
	static final private String xKeys[] = {"x", "z"};
	
	private SummaryDataSet summaryData;
	private XButton sampleButton;
	
	
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
		
		MultipleRegnModel yDistn = new MultipleRegnModel(getParameter(Y_VAR_NAME_PARAM),
																																						data, xKeys);
		yDistn.setParameters(getParameter(MULTI_REGN_MODEL_PARAM));
		data.addVariable("genModel", yDistn);
		
		ResponseVariable yData = new ResponseVariable(yName, data, xKeys, "error", "genModel", 10);
		data.addVariable("y", yData);
		
		summaryData = new SummaryDataSet(data, "error");
		summaryData.takeSample();
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				sampleButton = new XButton(translate("Another data set"), this);
			samplePanel.add(sampleButton);
			
		thePanel.add(samplePanel);
		
		thePanel.add(super.eastPanel(data));
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			for (int i=currentDragParam ; i>0 ; i--)
				zeroParameter(i);
				
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