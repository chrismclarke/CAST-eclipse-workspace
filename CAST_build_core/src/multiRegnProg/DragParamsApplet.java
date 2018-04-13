package multiRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;


public class DragParamsApplet extends RotateApplet {
	static final private String INITIAL_PARAM_PARAM = "initialParams";
	static final private String PARAM_MINMAX_PARAM = "paramMinMax";
	
	static final private String xKeys[] = {"x", "z"};
	
	protected DataSet data;
	
	private String explanName[];
	private String yVarName;
	
	private NumValue[] minParam;
	private NumValue[] maxParam;
	
	protected ColoredLinearEqnView equationView;
	private XButton zeroButton;
	
	protected DataSet readData() {
		data = new DataSet();
		
		explanName = new String[2];
		explanName[0] = getParameter(X_VAR_NAME_PARAM);
		explanName[1] = getParameter(Z_VAR_NAME_PARAM);
		yVarName = getParameter(Y_VAR_NAME_PARAM);
		
		data.addVariable("model", new MultipleRegnModel("Model", data, xKeys, getParameter(INITIAL_PARAM_PARAM)));
		
		String minMaxParamString = getParameter(PARAM_MINMAX_PARAM);
		if (minMaxParamString != null) {
			minParam = new NumValue[3];
			maxParam = new NumValue[3];
			StringTokenizer st = new StringTokenizer(minMaxParamString);
			for (int i=0 ; i<3 ; i++) {
				minParam[i] = new NumValue(st.nextToken());
				maxParam[i] = new NumValue(st.nextToken());
			}
		}
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		thePanel.add(getEquationView(data));
		
			zeroButton = new XButton("Zero Slopes", this);
		thePanel.add(zeroButton);
		return thePanel;
	}
	
	protected ColoredLinearEqnView getEquationView(DataSet data) {
		equationView = new ColoredLinearEqnView(data, this, "model", yVarName, explanName, minParam, maxParam);
		return equationView;
	}
	
	protected Rotate3DView getDataView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String modelKey) {
		return new DragParam3View(data, this, xAxis, yAxis, zAxis, "model", equationView);
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(explanName[0], D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
		D3Axis yAxis = new D3Axis(yVarName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		D3Axis zAxis = new D3Axis(explanName[1], D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
		
		theView = getDataView(data, xAxis, yAxis, zAxis, "model");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == zeroButton) {
			MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
			model.setParameter(1, 0.0);
			model.setParameter(2, 0.0);
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