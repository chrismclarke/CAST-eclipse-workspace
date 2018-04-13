package regnProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;
import coreGraphics.*;

import regn.*;
import regnView.*;


public class DragQuadraticApplet extends ScatterApplet {
	static final protected String INTERCEPT_PARAM = "interceptLimits";
	static final protected String SLOPE_PARAM = "slopeLimits";
	static final protected String CURVATURE_PARAM = "curvatureLimits";
	
	protected NumValue intMin, intMax, intStart, slopeMin, slopeMax, slopeStart;
	protected NumValue curveMin, curveMax, curveStart;
	
	private String xKey, yKey;
	
	private DragQuadraticView theView;
	private XCheckbox residCheck;
	private XButton lsButton;
	
	public void setupApplet() {
		StringTokenizer paramLimits = new StringTokenizer(getParameter(INTERCEPT_PARAM));
		intMin = new NumValue(paramLimits.nextToken());
		intMax = new NumValue(paramLimits.nextToken());
		intStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(SLOPE_PARAM));
		slopeMin = new NumValue(paramLimits.nextToken());
		slopeMax = new NumValue(paramLimits.nextToken());
		slopeStart = new NumValue(paramLimits.nextToken());
		
		paramLimits = new StringTokenizer(getParameter(CURVATURE_PARAM));
		curveMin = new NumValue(paramLimits.nextToken());
		curveMax = new NumValue(paramLimits.nextToken());
		curveStart = new NumValue(paramLimits.nextToken());
		
		super.setupApplet();
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String yValues = getParameter(Y_VALUES_PARAM);
		if (yValues != null) {
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
			yKey = "y";
		}
		String xValues = getParameter(X_VALUES_PARAM);
		if (xValues != null) {
			data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM));
			xKey = "x";
		}
		else {
			String xSequence = getParameter(X_SEQUENCE_PARAM);
			if (xSequence != null) {
				NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
				xVar.readSequence(xSequence);
				data.addVariable("x", xVar);
				xKey = "x";
			}
		}
		
		QuadraticModel modelVariable = new QuadraticModel("model", data, "x", intStart, slopeStart,
																																		curveStart, new NumValue(0.0, 0));
		data.addVariable("model", modelVariable);
		
		return data;
	}
	
	protected XPanel quadEqnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		thePanel.add(new QuadraticEquationView(data, this, "model", getParameter(Y_VAR_NAME_PARAM),
									getParameter(X_VAR_NAME_PARAM), intMin, intMax, slopeMin, slopeMax, curveMin, curveMax));
		return thePanel;
	}
	
	protected XPanel lsResidButtonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
			residCheck = new XCheckbox(translate("Show residuals"), this);
			residCheck.setState(false);
		thePanel.add(residCheck);
	
			lsButton = new XButton(translate("Least squares"), this);
		thePanel.add(lsButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		
		thePanel.add(quadEqnPanel(data));
		
		thePanel.add(lsResidButtonPanel());
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new DragQuadraticView(data, this, theHorizAxis, theVertAxis, xKey, yKey, "model");
		return theView;
	}
	
	protected void showResiduals(boolean showNotHide) {
		theView.setDrawResiduals(residCheck.getState());
	}
	
	protected void setLSParameters() {
		QuadraticModel model = (QuadraticModel)data.getVariable("model");
		model.setLSParams("y", intStart.decimals, slopeStart.decimals,
																						curveStart.decimals, 0);
		data.variableChanged("model");
	}

	
	private boolean localAction(Object target) {
		if (target == residCheck) {
			showResiduals(residCheck.getState());
			return true;
		}
		else if (target == lsButton) {
			setLSParameters();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}