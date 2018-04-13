package twoFactorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;


public class RotateAdjustSDApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String JITTERING_PARAM = "jittering";
	static final private String ROOT_MEAN_RSS_PARAM = "rootMeanRss";
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	private DataSet data;
	
	private NumValue minValue, maxValue, startValue;
	
	private XButton lsButton;
	private ParameterSlider sSlider;
	
	protected DataSet readData() {
		data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
			CatVariable xCatVar = new CatVariable(getParameter(X_VAR_NAME_PARAM));
			xCatVar.readLabels(getParameter(X_LABELS_PARAM));
			xCatVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xCatVar);
		
			StringTokenizer st = new StringTokenizer(getParameter(ROOT_MEAN_RSS_PARAM));
			minValue = new NumValue(st.nextToken());
			maxValue = new NumValue(st.nextToken());
			startValue = new NumValue(st.nextToken());
			
			GroupsModelVariable model = new GroupsModelVariable("Factor", data, "x");
			model.setUsePooledSd(true);
			model.updateLSParams("y");
			model.setSD(startValue);
		data.addVariable("model", model);
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			CatVariable xCat = (CatVariable)data.getVariable("x");
			xAxis.setCatScale(xCat);
		
			D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			
			D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(kDefaultZAxis);
			
			double jitterPropn = Double.parseDouble(getParameter(JITTERING_PARAM));
			theView = new RotateFactorModelView(data, this, xAxis, yAxis, zAxis, "model", "x", "y", jitterPropn);
			((RotateFactorModelView)theView).setShowSDBand(true);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(20, 10, 20, 0);
		thePanel.setLayout(new BorderLayout(30, 0));
		
			sSlider = new ParameterSlider(minValue, maxValue, startValue,
																			translate("Estimate of error sd"), this);
			sSlider.setFont(getStandardBoldFont());
		thePanel.add("Center", sSlider);
		
			XPanel lsPanel = new XPanel();
			lsPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				lsButton = new XButton(translate("Best estimate"), this);
			lsPanel.add(lsButton);
			
		thePanel.add("East", lsPanel);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.create2DRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	private void setLeastSquares() {
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		model.updateLSParams("y");
		sSlider.setParameter(model.evaluateSD().toDouble());
	}

	
	private boolean localAction(Object target) {
		if (target == sSlider) {
			GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
			NumValue s = sSlider.getParameter();
			model.setSD(s);
			data.variableChanged("model");
			return true;
		}
		else if (target == lsButton) {
			setLeastSquares();
			data.variableChanged("model");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}