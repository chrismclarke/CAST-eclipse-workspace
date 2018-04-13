package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


abstract public class CoreRegnPlaneApplet extends RotateApplet {
	static final protected String MODEL_PARAM_PARAM = "modelParams";
	static final private String PARAM_MINMAX_PARAM = "paramMinMax";
	
	static final private String W_VAR_NAME_PARAM = "wVarName";
	static final private String W_VALUES_PARAM = "wValues";
	
	static final private String SHORT_VAR_NAMES_PARAM = "shortVarNames";
	
	static final protected int ZERO = 0;
	static final protected int DRAG = 1;
	static final protected int BEST = 2;
	
	protected DataSet data;
	protected String explanName[];
	protected String[] explanKey;
	protected String yName;
	
	protected NumValue[] minParam;
	protected NumValue[] maxParam;
	
	protected MultiLinearEqnView theEqn;
	
	protected DataSet readData() {
		data = new DataSet();
		
		yName = getParameter(Y_VAR_NAME_PARAM);
		
		String wName = getParameter(W_VAR_NAME_PARAM);
		if (wName == null) {
			explanName = new String[2];
			explanKey = new String[2];
		}
		else {
			explanName = new String[3];
			explanKey = new String[3];
			explanName[2] = wName;
			explanKey[2] = "w";
			NumVariable wVar = new NumVariable(wName);
			wVar.readValues(getParameter(W_VALUES_PARAM));
			data.addVariable("w", wVar);
		}
		
		explanName[0] = getParameter(X_VAR_NAME_PARAM);
		explanKey[0] = "x";
		String xValues = getParameter(X_VALUES_PARAM);
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), xValues);
			
		explanName[1] = getParameter(Z_VAR_NAME_PARAM);
		explanKey[1] = "z";
		String zValues = getParameter(Z_VALUES_PARAM);
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), zValues);
		
		data.addVariable("model", new MultipleRegnModel("Model", data, explanKey, getParameter(MODEL_PARAM_PARAM)));
		
		int noOfParams = explanKey.length + 1;
		
		String minMaxParamString = getParameter(PARAM_MINMAX_PARAM);
		if (minMaxParamString != null) {
			minParam = new NumValue[noOfParams];
			maxParam = new NumValue[noOfParams];
			StringTokenizer st = new StringTokenizer(minMaxParamString);
			for (int i=0 ; i<noOfParams ; i++) {
				minParam[i] = new NumValue(st.nextToken());
				maxParam[i] = new NumValue(st.nextToken());
			}
		}
		
		createYData(data, yName);
		
		return data;
	}
	
	protected void createYData(DataSet data, String yName) {
		String yValues = getParameter(Y_VALUES_PARAM);
		if (yValues != null)
			data.addNumVariable("y", yName, yValues);
	}
	
	protected String getShortYName() {
		String shortNameStrings = getParameter(SHORT_VAR_NAMES_PARAM);
		if (shortNameStrings == null)
			return yName;
		StringTokenizer st = new StringTokenizer(shortNameStrings);
		return st.nextToken();
	}
	
	protected String[] getShortXNames() {
		String shortNameStrings = getParameter(SHORT_VAR_NAMES_PARAM);
		if (shortNameStrings == null)
			return explanName;
		
		String shortName[] = new String[explanName.length];
		StringTokenizer st = new StringTokenizer(shortNameStrings);
		st.nextToken();		//for Y
		for (int i=0 ; i<explanName.length ; i++)
			shortName[i] = st.nextToken();
		return shortName;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		theEqn = new MultiLinearEqnView(data, this, "model", getShortYName(), getShortXNames(), minParam, maxParam);
		theEqn.setLastDrawParameter(0);
		thePanel.add(theEqn);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(explanName[0], D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		D3Axis yAxis = new D3Axis(yName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis(explanName[1], D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = getRotatingView(data, yAxis, xAxis, zAxis);
		CoreVariable y = data.getVariable("y");
		if (y != null) {
			int noOfValues = ((NumVariable)y).noOfValues();
			if (noOfValues > 50)
				theView.setCrossSize(DataView.SMALL_CROSS);
		}
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	abstract protected Rotate3DView getRotatingView(DataSet data, D3Axis yAxis,
																			D3Axis xAxis, D3Axis zAxis);
	
}