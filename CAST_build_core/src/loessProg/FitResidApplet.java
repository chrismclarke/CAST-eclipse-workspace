package loessProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import models.*;

import regnProg.*;
import regn.*;
import loess.*;


public class FitResidApplet extends MultipleScatterApplet {
	static final private String RESID_AXIS_INFO_PARAM = "residAxis";
	static final private String FIT_RESID_DECIMALS_PARAM = "fitResidDecimals";
	static final private String MAX_BETA_PARAM = "maxSlopeIntercept";
	static final private String LOESS_POINTS_PARAM = "loessPoints";
	
	private NumValue maxIntercept, maxSlope;
	private int loessWindowWidth;
	
	private XCheckbox loessCheck;
	
	private HiliteResidualView xyView, residView;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_BETA_PARAM));
		maxIntercept = new NumValue(st.nextToken());
		maxSlope = new NumValue(st.nextToken());
		
		LinearModel modelVariable = new LinearModel("model", data, "x");
		modelVariable.setLSParams("y", maxIntercept.decimals, maxSlope.decimals, 0);
		data.addVariable("model", modelVariable);
		
		int fitResidDecimals = Integer.parseInt(getParameter(FIT_RESID_DECIMALS_PARAM));
		data.addVariable("resid", new ResidValueVariable(translate("Residual"), data, "x", "y", "model",
																							fitResidDecimals));
		data.addVariable("fit", new FittedValueVariable("Fitted", data, "x", "model",
																							fitResidDecimals));
		String loessString = getParameter(LOESS_POINTS_PARAM);
		if (loessString == null)
			loessWindowWidth = -1;
		else
			loessWindowWidth = Integer.parseInt(loessString);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		
		thePanel.add("Left", createPlotPanel(data, false, "x", "y", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(Y_AXIS_INFO_PARAM), 0));
		thePanel.add("Right", createPlotPanel(data, false, "x", "resid", null,
							getParameter(X_AXIS_INFO_PARAM), getParameter(RESID_AXIS_INFO_PARAM), 1));
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
																									int plotIndex) {
		String loessKey = null;
		if (plotIndex == 0) {
			if (loessWindowWidth > 0) {
				LoessSmoothVariable yLoessVar = new LoessSmoothVariable("Y loess", data, "x", "y");
				data.addVariable("yLoess", yLoessVar);
				yLoessVar.setAxes(theHorizAxis, theVertAxis);
				yLoessVar.initialise(loessWindowWidth);
				loessKey = "yLoess";
			}
			
			xyView = new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "y", "model", loessKey);
			return xyView;
		}
		else {
			if (loessWindowWidth > 0) {
				LoessSmoothVariable rLoessVar = new LoessSmoothVariable("Resid loess", data, "x", "resid");
				data.addVariable("rLoess", rLoessVar);
				rLoessVar.setAxes(theHorizAxis, theVertAxis);
				rLoessVar.initialise(loessWindowWidth);
				loessKey = "rLoess";
			}
			
			residView = new HiliteResidualView(data, this, theHorizAxis, theVertAxis, "x", "resid", null, loessKey);
			return residView;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			OneValueView xView = new OneValueView(data, "x", this);
			valuePanel.add(xView);
			
			OneValueView yView = new OneValueView(data, "y", this);
			yView.setForeground(Color.blue);
			valuePanel.add(yView);
			
			OneValueView fitView = new OneValueView(data, "fit", this);
			fitView.setForeground(HiliteResidualView.darkGreen);
			valuePanel.add(fitView);
			
			OneValueView residView = new OneValueView(data, "resid", this);
			residView.setForeground(Color.red);
			valuePanel.add(residView);
		
		thePanel.add(valuePanel);
		
		XPanel equationPanel = new XPanel();
			equationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
			String yName = data.getVariable("y").name;
			String xName = data.getVariable("x").name;
			LinearEquationView theEquation = new LinearEquationView(data, this, "model", yName, xName,
													maxIntercept, maxIntercept, maxSlope, maxSlope, Color.black, Color.black);
			equationPanel.add(theEquation);
			
			if (loessWindowWidth > 0) {
				loessCheck = new XCheckbox(translate("Show lowess smooth"), this);
				equationPanel.add(loessCheck);
			}
			
		thePanel.add(equationPanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == loessCheck) {
			xyView.setShowLoess(loessCheck.getState());
			residView.setShowLoess(loessCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}