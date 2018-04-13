package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import models.*;
import graphics3D.*;

import multiRegn.*;


public class PlaneSampleApplet extends CoreRegnPlaneApplet {
	static final private String ERROR_PARAM = "error";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String SPECIAL_Z_PARAM = "specialZ";
	static final private String SPECIAL_X_PARAM = "specialX";
	static final private String MIN_VAR_ROTATION_PARAM = "minVarRotation";
								//		remove // in RotateMap.setAngles() to see angles in console
	
	protected SummaryDataSet summaryData;
	
	private XButton takeSampleButton, minVarButton;
	private XCheckbox accumulateCheck;
	
	
	protected DataSet readData() {
		data = super.readData();
		
		data.addVariable("lsEvaluator", new MultipleRegnModel("LS", data, explanKey));
		
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
		
		ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																		data, explanKey, "error", "model", 10);
		data.addVariable("y", yData);
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		int[] decimals = new int[explanKey.length + 1];
		String decimalsString = getParameter(DECIMALS_PARAM);
		if (decimalsString != null) {
			StringTokenizer st = new StringTokenizer(decimalsString);
			for (int i=0 ; i<decimals.length ; i++)
				decimals[i] = Integer.parseInt(st.nextToken());
		}
		
		summaryData.addVariable("planes", new LSCoeffVariable("Planes", "lsEvaluator",
																	explanKey, "y", null, decimals));
		
		summaryData.takeSample();
		return summaryData;
	}
	
	
	protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis, D3Axis xAxis,
																									D3Axis zAxis) {
		SamplePlanesView theView = new SamplePlanesView(summaryData, this,
								xAxis, yAxis, zAxis, "planes", data, "model", explanKey, "y", "lsEvaluator");
		theView.setResidualShow(false);
		
		String specialZString = getParameter(SPECIAL_Z_PARAM);
		if (specialZString != null) {
			double specialZ = Double.parseDouble(specialZString);
			theView.setLineDisplay(SamplePlanesView.LINE_AT_Z, specialZ);
		}
		else {
			String specialXString = getParameter(SPECIAL_X_PARAM);
			if (specialXString != null) {
				double specialX = Double.parseDouble(specialXString);
				theView.setLineDisplay(SamplePlanesView.LINE_AT_X, specialX);
			}
		}
		return theView;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
		takeSampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		String specialRotateDirection = getParameter(MIN_VAR_ROTATION_PARAM);
		if (specialRotateDirection == null)
			return super.controlPanel(data);
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			thePanel.add(new RotateButton(RotateButton.XZ_ROTATE, theView, this));
			thePanel.add(new RotateButton(RotateButton.XYZ_ROTATE, theView, this));
			
				minVarButton = new XButton(translate("Show min variability"), this);
			thePanel.add(minVarButton);
			
				rotateButton = new XButton(translate("Spin"), this);
			thePanel.add(rotateButton);
			return thePanel;
		}
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			theView.setDrawData(true);
			summaryData.takeSample();
			return true;
		}
		else if (target == minVarButton) {
			StringTokenizer st = new StringTokenizer(getParameter(MIN_VAR_ROTATION_PARAM));
			double roundDens = Double.parseDouble(st.nextToken());
			double ofDens = Double.parseDouble(st.nextToken());
			theView.animateRotateTo(roundDens, ofDens);
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