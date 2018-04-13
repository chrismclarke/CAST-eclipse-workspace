package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import twoGroup.*;


public class RotateAnovaPDFApplet extends RotateApplet {
	static final protected String JITTER_PARAM = "jitterPropn";
	
	static final protected String kDefaultZAxis = "0.0 1.0 2.0 1.0";
	
	protected DataSet data;
	
	private XButton sampleButton;
	
	protected DataSet readData() {
		data = new GroupsDataSet(this);
		return data;
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		double jitterPropn = Double.parseDouble(getParameter(JITTER_PARAM));
		return new RotateAnovaPDFView(data, this, xAxis, yAxis, densityAxis, "model", "x", "y", jitterPropn);
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis("", D3Axis.Y_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(kDefaultZAxis);
		
		theView = getView(data, xAxis, yAxis, zAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
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
		
		thePanel.add(ProportionLayout.BOTTOM, samplingPanel);
		
		return thePanel;
	}
	
	protected int rotationPanelOrientation() {
		return RotateButton.VERTICAL;
	}
	
	protected XPanel rotationPanel() {
		XPanel thePanel = RotateButton.create2DRotationPanel(theView, this, rotationPanelOrientation());
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected void takeSample() {
		NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
		error.generateNextSample();
		((RotateAnovaPDFView)theView).setShowData(true);
		((RotateAnovaPDFView)theView).resetJittering();
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
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}