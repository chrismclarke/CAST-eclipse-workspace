package linModProg;

import java.awt.*;
import java.util.*;

import axis.*;
import utils.*;
import dataView.*;
import random.*;
import models.*;
import coreGraphics.*;

import regn.*;
import linMod.*;


public class SampleLineApplet extends ScatterApplet {
	static final private String REGN_MODEL_PARAM = "regnModel";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String RANDOM_X_PARAM = "randomX";
	static final protected String MAX_PARAM = "maxParams";
	
	private NumValue intMax, slopeMax;
	private boolean canSample;
	
	protected DataSet data;
	
	private XButton sampleButton;
	private RandomNormal generator, xGenerator;
	
	private SampleLineView theView;
	private LSEquationView lsLineEquation;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(MAX_PARAM));
		intMax = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String xValueString = getParameter(X_VALUES_PARAM);
		NumVariable xVar = new NumVariable(X_VAR_NAME_PARAM);
		if (xValueString == null) {
			xGenerator = new RandomNormal(getParameter(RANDOM_X_PARAM));
			double vals[] = xGenerator.generate();
			xVar.setValues(vals);
		}
		else
			xVar.readValues(xValueString);
		data.addVariable("x", xVar);
		
		String yValues = getParameter(Y_VALUES_PARAM);
		canSample = (yValues == null);
		if (canSample) {
			LinearModel yDistn = new LinearModel(getParameter(Y_VAR_NAME_PARAM), data, "x");
			yDistn.setParameters(getParameter(REGN_MODEL_PARAM));
			data.addVariable("model", yDistn);
			
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			generator = new RandomNormal(randomInfo);
			
			NumVariable error = new NumVariable(translate("Error"));
			data.addVariable("error", error);
			
			ResponseVariable yData = new ResponseVariable(getParameter(Y_VAR_NAME_PARAM),
																							data, "x", "error", "model", 10);
			data.addVariable("y", yData);
		}
		else
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValues);
		return data;
	}
	
	private XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
		if (canSample) {
			LinearEquationView modelEqn = new LinearEquationView(data, this, "model", null, null,
																										intMax, intMax, slopeMax, slopeMax);
			modelEqn.setFont(getBigFont());
			thePanel.add(modelEqn);
		}
		lsLineEquation = new LSEquationView(data, this, "y", "x", intMax, slopeMax);
		lsLineEquation.setFont(getBigFont());
		lsLineEquation.setForeground(Color.blue);
		if (!canSample)
			lsLineEquation.setShowData(true);
		thePanel.add(lsLineEquation);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		if (canSample) {
			thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
			
			XPanel samplePanel = new XPanel();
			samplePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			sampleButton = new XButton(translate("Take sample"), this);
			samplePanel.add(sampleButton);
			
			thePanel.add(ProportionLayout.LEFT, samplePanel);
			
			thePanel.add(ProportionLayout.RIGHT, equationPanel(data));
		}
		else {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			thePanel.add(equationPanel(data));
		}
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new SampleLineView(data, this, theHorizAxis, theVertAxis, "x", "y", "model");
		if (!canSample) {
			theView.setShowData(true);
			theView.setShowModel(false);
		}
		return theView;
	}
	
	private void takeSample() {
		double vals[] = generator.generate();
		((NumVariable)data.getVariable("error")).setValues(vals);
		if (xGenerator != null) {
			vals = xGenerator.generate();
			((NumVariable)data.getVariable("x")).setValues(vals);
		}
		theView.setShowData(true);
		lsLineEquation.setShowData(true);
		data.variableChanged("error");
	}
	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}