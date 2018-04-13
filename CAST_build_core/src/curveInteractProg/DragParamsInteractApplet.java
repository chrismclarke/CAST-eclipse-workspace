package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import multiRegn.*;
import curveInteract.*;


public class DragParamsInteractApplet extends RotateApplet {
	static final private String INITIAL_PARAM_PARAM = "initialParams";
	static final private String PARAM_MINMAX_PARAM = "paramMinMax";
	
	static final protected String basicXKeys[] = {"x", "z"};
	static final private String interactXKeys[] = {"x", "z", "xz"};
	
	protected DataSet data;
	
	protected String explanName[];
	private String yVarName;
	
	private NumValue[] minParam;
	private NumValue[] maxParam;
	
	protected ColoredLinearEqnView equationView;
	private XButton zeroButton;
	
	protected DataSet readData() {
		data = new DataSet();
		
		explanName = new String[3];
		explanName[0] = getParameter(X_VAR_NAME_PARAM);
		explanName[1] = getParameter(Z_VAR_NAME_PARAM);
		explanName[2] = explanName[0] + "." + explanName[1];
		yVarName = getParameter(Y_VAR_NAME_PARAM);
		
		MultipleRegnModel model = new MultipleRegnModel("model", data, interactXKeys, getParameter(INITIAL_PARAM_PARAM));
		data.addVariable("model", model);
		
		addBasicModel(data, model);
		
		String minMaxParamString = getParameter(PARAM_MINMAX_PARAM);
		if (minMaxParamString != null) {
			minParam = new NumValue[4];
			maxParam = new NumValue[4];
			StringTokenizer st = new StringTokenizer(minMaxParamString);
			for (int i=0 ; i<4 ; i++) {
				minParam[i] = new NumValue(st.nextToken());
				maxParam[i] = new NumValue(st.nextToken());
			}
		}
		
		return data;
	}
	
	protected void addBasicModel(DataSet data, MultipleRegnModel model) {
		MultipleRegnModel noInteractModel = new MultipleRegnModel("noInteractModel", data, basicXKeys);
		for (int i=0 ; i<3 ; i++)
			noInteractModel.setParameter(i, model.getParameter(i));
															//	NOT a copy. Both models use the SAME NumValue.
															//	When the no-interact model params are changed,
															//	so are the interact ones.
		data.addVariable("noInteractModel", noInteractModel);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		thePanel.add(getEquationView(data));
		
			zeroButton = new XButton(translate("Zero slopes"), this);
		thePanel.add(zeroButton);
		return thePanel;
	}
	
	protected ColoredLinearEqnView getEquationView(DataSet data) {
		equationView = new ColoredLinearEqnView(data, this, "model", yVarName, explanName, minParam, maxParam);
		return equationView;
	}
	
	protected Rotate3DView getDataView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String modelKey) {
		return new DragParamInteractView(data, this, xAxis, yAxis, zAxis, "noInteractModel", "model", equationView);
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
			model.setParameter(3, 0.0);
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