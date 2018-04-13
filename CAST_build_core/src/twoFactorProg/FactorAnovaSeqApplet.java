package twoFactorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import glmAnova.*;
import glmAnovaProg.*;
import twoFactor.*;


public class FactorAnovaSeqApplet extends AnovaTableSeqApplet implements SetLastExplanInterface {
	static final protected String Y_AXIS_INFO_PARAM = "yAxis";
	static final private String FACTOR_VARS_PARAM = "factorVariables";
	static final private String X_TYPES_PARAM = "xTypes";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	protected String factorKeys[];
	
	private RotateDragFactorsView theView;
	private XButton rotateButton;
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 10));
		
		add("Center", displayPanel(data));
		
		add("East", eastPanel(data));
			
			AnovaSeqTableView table = getAnovaTable(data);
			
		add("South", table);
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			factorKeys = new String[2];
			String factorVarString = getParameter(FACTOR_VARS_PARAM);
			if (factorVarString != null) {
				StringTokenizer st = new StringTokenizer(factorVarString);
				factorKeys[0] = st.nextToken();
				factorKeys[1] = st.nextToken();
			}
			else {
				factorKeys[0] = "x";
				factorKeys[1] = "z";
				data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
																												getParameter(X_LABELS_PARAM));
				data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
																												getParameter(Z_LABELS_PARAM));
			}
						
								//	need duplicate LS model of type TwoFactorModel for RotateDragFactorsView
			TwoFactorModel lsFit = new TwoFactorModel("least squares", data, factorKeys,
																TwoFactorModel.NONE, TwoFactorModel.NONE, false, 0.0);
		data.addVariable("ls", lsFit);
		
		return data;
	}
	
	protected AnovaSeqTableView getAnovaTable(DataSet data) {
		AnovaSeqTableView table = new AnovaSeqTableView(data, this,
								componentKeys, maxSsq, componentName, componentColor, variableName, null);
		table.setLinkedView(this);
		table.setFont(getBigFont());
		if (showTests)
			table.setShowTests(true, maxMsq, maxF);
	
		String initVarString = getParameter(INIT_VARIABLES_PARAM);
		if (initVarString != null) {
			int initVars = Integer.parseInt(initVarString);
			setLastExplanatory(initVars - 1);
			table.setLastSeparateX(initVars - 1);
		}
		return table;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
		CatVariable xVar = (CatVariable)data.getVariable(factorKeys[0]);
		D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		xAxis.setCatScale(xVar);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		D3Axis yAxis = new D3Axis(yVar.name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		CatVariable zVar = (CatVariable)data.getVariable(factorKeys[1]);
		D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		zAxis.setCatScale(zVar);
		
		theView = new RotateDragFactorsView(data, this, xAxis, yAxis, zAxis, factorKeys[0], "y", factorKeys[1], "ls");
		theView.setAllowDragParams(false);
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	public void setLastExplanatory(int lastSeparateX) {
		StringTokenizer st = new StringTokenizer(getParameter(X_TYPES_PARAM));
		boolean hasInteraction = false;
		int xType = TwoFactorModel.NONE;
		int zType = TwoFactorModel.NONE;
		for (int i=0 ; i<=lastSeparateX ; i++) {
			String s = st.nextToken();
			if (s.equals("Int"))
				hasInteraction = true;
			else {
				int type = (s.charAt(1) == 'L') ? TwoFactorModel.LINEAR : TwoFactorModel.FACTOR;
				if (s.charAt(0) == 'x')
					xType = type;
				else
					zType = type;
			}
		}
		
		TwoFactorModel lsFit = (TwoFactorModel)data.getVariable("ls");
		lsFit.setModelType(xType, zType, hasInteraction);
		lsFit.setLSParams("y");
		data.variableChanged("ls");
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
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