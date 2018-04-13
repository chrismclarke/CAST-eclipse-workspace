package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreGraphics.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class DragQuadXZApplet extends RotateApplet {
	static final private String INITIAL_PARAM_PARAM = "initialParams";
	static final private String PARAM_MINMAX_PARAM = "paramMinMax";
	static final private String SHADE_CUTOFF_PARAM = "shadeCutoff";
	
	static final protected String QUAD_X_PARAM = "quadX";
	static final protected String QUAD_Z_PARAM = "quadZ";
	static final protected String INTERACT_PARAM = "interact";
	
	static final private Color kOrange = new Color(0xFF6600);
	static final private Color kDarkGreen = new Color(0x009900);
	static final private Color kKeyColor[] = {kDarkGreen, kOrange, Color.blue};
	
	static final protected String kXZHandleKeys[] = {"xHandle", "zHandle"};
	static final protected String kYHandleKey = "yHandle";
	
	static final private String kHandleDataString = "0 0 0 0 0 0";
	
	protected DataSet data;
	
	protected String yVarName, xVarName, zVarName;
	
	protected ColourMap colourMap;
	
	protected NumValue[] minParam;
	protected NumValue[] maxParam;
	
	protected MultiLinearEqnView equationView;
	
	private XCheckbox xCurveCheck, zCurveCheck, interactCheck;
	protected boolean quadXAlwaysOn = false;
	protected boolean quadZAlwaysOn = false;
	protected boolean interactAlwaysOn = false;
	
	protected void setupColorMap() {
		double[] keyValues = getKeyValues();
		colourMap = new ColourMap(kKeyColor, keyValues);
	}
	
	private double[] getKeyValues() {
		double keyValue[] = new double[kKeyColor.length];
		StringTokenizer st = new StringTokenizer(getParameter(SHADE_CUTOFF_PARAM));
		for (int i=0 ; i<keyValue.length ; i++)
			keyValue[i] = Double.parseDouble(st.nextToken());
		return keyValue;
	}
	
	protected void readMinMaxParams() {
		String minMaxParamString = getParameter(PARAM_MINMAX_PARAM);
		if (minMaxParamString != null) {
			minParam = new NumValue[6];
			maxParam = new NumValue[6];
			StringTokenizer st = new StringTokenizer(minMaxParamString);
			for (int i=0 ; i<6 ; i++) {
				minParam[i] = new NumValue(st.nextToken());
				maxParam[i] = new NumValue(st.nextToken());
			}
		}
	}
	
	protected DataSet readData() {
		data = new DataSet();
												//	squares will be added in equationView
		yVarName = getParameter(Y_VAR_NAME_PARAM);
		xVarName = getParameter(X_VAR_NAME_PARAM);
		zVarName = getParameter(Z_VAR_NAME_PARAM);
		
		data.addNumVariable("yHandle", "yHandle", kHandleDataString);
		data.addNumVariable("xHandle", "xHandle", kHandleDataString);
		data.addNumVariable("zHandle", "zHandle", kHandleDataString);
		
		ResponseSurfaceModel model = new ResponseSurfaceModel("model", data, kXZHandleKeys,
																								getParameter(INITIAL_PARAM_PARAM));
		data.addVariable("model", model);
		
		readMinMaxParams();
		
		setupColorMap();
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
			
			String explanName[] = new String[5];
			explanName[0] = explanName[2] = xVarName;
			explanName[1] = explanName[3] = zVarName;
			explanName[4] = xVarName + zVarName;		//	for interaction, but only used in sub-classes
			equationView = new MultiLinearEqnView(data, this, "model", yVarName, explanName, minParam, maxParam);
			
			equationView.setSquaredExplan(2, true);
			equationView.setSquaredExplan(3, true);
//			equationView.setLastDrawParameter(3);		//	never show interaction term
			
		thePanel.add(equationView);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(xVarName, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(yVarName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(zVarName, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
			DragResponseSurfaceView localView = new DragResponseSurfaceView(data, this, xAxis, yAxis, zAxis,
																							"model", kXZHandleKeys, kYHandleKey);
			localView.setHandlesForModel();
			localView.setColourMap(colourMap);
								
			localView.lockBackground(Color.white);
			theView = localView;
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		DragResponseSurfaceView localView = (DragResponseSurfaceView)theView;
		
		String xCurveType = getParameter(QUAD_X_PARAM);
		if (xCurveType == null)
			localView.setAllowTerm(3, false);
		else {
			quadXAlwaysOn = xCurveType.equals("on");
			if (quadXAlwaysOn)
				localView.setAllowTerm(3, true);
			else {
				localView.setAllowTerm(3, false);
				if (xCurveType.equals("check")) {
					xCurveCheck = new XCheckbox(translate("Quadratic in X"), this);
					xCurveCheck.setState(false);
					thePanel.add(xCurveCheck);
				}
			}
		}
		
		String zCurveType = getParameter(QUAD_Z_PARAM);
		if (zCurveType == null)
			localView.setAllowTerm(4, false);
		else {
			quadZAlwaysOn = zCurveType.equals("on");
			if (quadZAlwaysOn)
				localView.setAllowTerm(4, true);
			else {
				localView.setAllowTerm(4, false);
				if (zCurveType.equals("check")) {
					zCurveCheck = new XCheckbox(translate("Quadratic in Z"), this);
					zCurveCheck.setState(false);
					thePanel.add(zCurveCheck);
				}
			}
		}
		
		String interactType = getParameter(INTERACT_PARAM);
		if (interactType == null)
			localView.setAllowTerm(5, false);
		else {
			interactAlwaysOn = interactType.equals("on");
			if (interactAlwaysOn)
				localView.setAllowTerm(5, true);
			else {
				localView.setAllowTerm(5, false);
				if (interactType.equals("check")) {
					interactCheck = new XCheckbox(translate("Interaction"), this);
					interactCheck.setState(false);
					thePanel.add(interactCheck);
				}
			}
		}
		
		if (equationView != null)
			setEquationParamDraw();
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected void setEquationParamDraw() {
		boolean drawParams[] = new boolean[6];
		drawParams[0] = drawParams[1] = drawParams[2] = true;
		drawParams[3] = quadXAlwaysOn || (xCurveCheck != null && xCurveCheck.getState());
		drawParams[4] = quadZAlwaysOn || (zCurveCheck != null && zCurveCheck.getState());
		drawParams[5] = interactAlwaysOn || (interactCheck != null && interactCheck.getState());
		equationView.setDrawParameters(drawParams);
	}
	
	private boolean localAction(Object target) {
		if (target == xCurveCheck) {
			setEquationParamDraw();
			((DragResponseSurfaceView)theView).setAllowTerm(3, xCurveCheck.getState());
			data.variableChanged("model");	
			return true;
		}
		else if (target == zCurveCheck) {
			setEquationParamDraw();
			((DragResponseSurfaceView)theView).setAllowTerm(4, zCurveCheck.getState());
			data.variableChanged("model");	
			return true;
		}
		else if (target == interactCheck) {
			setEquationParamDraw();
			((DragResponseSurfaceView)theView).setAllowTerm(5, interactCheck.getState());
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