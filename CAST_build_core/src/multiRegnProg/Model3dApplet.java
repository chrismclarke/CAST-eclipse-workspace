package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class Model3dApplet extends CoreRegnPlaneApplet {
	static final private String ERROR_PARAM = "error";
	
	protected SummaryDataSet summaryData;
	
	private XButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	
	protected DataSet readData() {
		data = super.readData();
		
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	protected void createYData(DataSet data, String yName) {
//		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		
		String seedString = getParameter(ERROR_PARAM);
		StringTokenizer st = new StringTokenizer(seedString);
		int noOfValues = Integer.parseInt(st.nextToken());
		String randomParams = noOfValues + " 0.0 1.0 " + st.nextToken() + " 3.0";
		RandomNormal generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.setSampleSize(noOfValues);
		data.addVariable("error", error);
		
		ResponseVariable yData = new ResponseVariable(yName, data, explanKey, "error",
																									"model", 10);
		data.addVariable("y", yData);
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		summaryData.takeSample();
		return summaryData;
	}
	
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis, D3Axis zAxis) {
		Model3XView theView = new Model3XView(data, this, xAxis, yAxis, zAxis, "model",
																													explanKey, "y", Model3XView.DRAW_PLANE);
		theView.setDrawData(false);
		return theView;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = super.topPanel(data);
		theEqn.setLastDrawParameter(2);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			theView.setDrawData(true);
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
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