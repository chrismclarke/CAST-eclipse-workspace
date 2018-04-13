package multivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;


public class RotateApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "xAxis";
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final protected String Z_AXIS_INFO_PARAM = "zAxis";
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String Z_VALUES_PARAM = "zValues";
	static final protected String INITIAL_ROTATION_PARAM = "initialRotation";
	
	protected Rotate3DView theView;
	protected XButton rotateButton;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(2, 2));
		add("North", topPanel(data));
		add("Center", displayPanel(data));
		
		String initialRotationString = getParameter(INITIAL_ROTATION_PARAM);
		if (initialRotationString != null) {
			StringTokenizer theAngles = new StringTokenizer(initialRotationString);
			int roundDens = Integer.parseInt(theAngles.nextToken());
			int ofDens = Integer.parseInt(theAngles.nextToken());
			theView.setInitialRotation(roundDens, ofDens);
		}
		
		add("South", controlPanel(data));
		add("East", eastPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		String yValues = getParameter(Y_VALUES_PARAM);
		if (yValues != null)
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValues);
		String xValues = getParameter(X_VALUES_PARAM);
		if (xValues != null)
			data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), xValues);
		String zValues = getParameter(Z_VALUES_PARAM);
		if (zValues != null)
			data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), zValues);
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = new Rotate3DView(data, this, xAxis, yAxis, zAxis,"x", "y", "z");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected String getYAxisName() {
		return getParameter(Y_VAR_NAME_PARAM);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		XLabel yLabel = new XLabel("y:" + getYAxisName(), XLabel.LEFT, this);
		yLabel.setForeground(D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(yLabel);
		
		XLabel xLabel = new XLabel("x:" + getParameter(X_VAR_NAME_PARAM), XLabel.LEFT, this);
		xLabel.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(xLabel);
		
		XLabel zLabel = new XLabel("z:" + getParameter(Z_VAR_NAME_PARAM), XLabel.LEFT, this);
		zLabel.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(zLabel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == rotateButton) {
			theView.startAutoRotation();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}