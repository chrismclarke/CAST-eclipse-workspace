package experProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import exper.*;


public class RotateInteractionApplet extends XApplet {
	static final private String TREAT1_NAME_PARAM = "treat1Name";
	static final private String TREAT1_VALUES_PARAM = "treat1Values";
	static final private String TREAT1_LABELS_PARAM = "treat1Labels";
	
	static final private String TREAT2_NAME_PARAM = "treat2Name";
	static final private String TREAT2_VALUES_PARAM = "treat2Values";
	static final private String TREAT2_LABELS_PARAM = "treat2Labels";
	
	static final private String RESPONSE_NAME_PARAM = "responseName";
	static final private String RESPONSE_VALUES_PARAM = "responseValues";
	static final private String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String INITIAL_ROTATION_PARAM = "initialRotation";
	
	static final private String MODEL_EQN_FILE_PARAM = "modelEqnFile";
	
	static final private String[] xKey = {"treat1", "treat2"};
	
	protected DataSet data;
	
	protected RotateTwoFactorView theView;
	protected TreatmentLabelsView treat1Labels, treat2Labels;
	
	private XCheckbox treat1Check, treat2Check, interactCheck, residualCheck;
	private boolean fitFactor[] = {true, true};
	private boolean fitInteraction;
	
	private MultiImageCanvas eqnCanvas;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 5));
		
		add("Center", rotatePanel(data));
		add("East", controlPanel(data));
		add("North", headingPanel(data));
		
		updateModel();
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			CatVariable treat1Var = new CatVariable(getParameter(TREAT1_NAME_PARAM));
			treat1Var.readLabels(getParameter(TREAT1_LABELS_PARAM));
			treat1Var.readValues(getParameter(TREAT1_VALUES_PARAM));
		data.addVariable("treat1", treat1Var);
		
			CatVariable treat2Var = new CatVariable(getParameter(TREAT2_NAME_PARAM));
			treat2Var.readLabels(getParameter(TREAT2_LABELS_PARAM));
			treat2Var.readValues(getParameter(TREAT2_VALUES_PARAM));
		data.addVariable("treat2", treat2Var);
		
		data.addNumVariable("response", getParameter(RESPONSE_NAME_PARAM), getParameter(RESPONSE_VALUES_PARAM));
			
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			FactorsModel model = new FactorsModel(getParameter(RESPONSE_NAME_PARAM), data, xKey, decimals);
			
		data.addVariable("model", model);
		
		return data;
	}
	
	private XPanel rotatePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(2, 2));
		thePanel.add("Center", threeDPanel(data));
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
			
			buttonPanel.add(RotateButton.createRotationPanel(theView, this));
			buttonPanel.add(namePanel(data));
		thePanel.add("South", buttonPanel);
		
		String initialRotationString = getParameter(INITIAL_ROTATION_PARAM);
		if (initialRotationString != null) {
			StringTokenizer theAngles = new StringTokenizer(initialRotationString);
			int roundDens = Integer.parseInt(theAngles.nextToken());
			int ofDens = Integer.parseInt(theAngles.nextToken());
			theView.rotateTo(roundDens, ofDens);
		}
		return thePanel;
	}
	
	protected XPanel threeDPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			CatVariable xVar = (CatVariable)data.getVariable("treat1");
			D3Axis xAxis = new D3Axis(xVar.name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setCatScale(xVar);
			D3Axis yAxis = new D3Axis(getVarName("model"), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(RESPONSE_AXIS_INFO_PARAM));
			CatVariable zVar = (CatVariable)data.getVariable("treat2");
			D3Axis zAxis = new D3Axis(zVar.name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setCatScale(zVar);
			
			theView = new RotateTwoFactorView(data, this, xAxis, yAxis, zAxis, "treat1", "response", "treat2", "model");
//			theView.setDrawData(false);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private String getVarName(String key) {
		return data.getVariable(key).name;
	}
	
	private XPanel namePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		XLabel yLabel = new XLabel("y:" + getVarName("model"), XLabel.LEFT, this);
		yLabel.setForeground(D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(yLabel);
		
		XLabel xLabel = new XLabel("x:" + getVarName("treat1"), XLabel.LEFT, this);
		xLabel.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(xLabel);
		
		XLabel zLabel = new XLabel("z:" + getVarName("treat2"), XLabel.LEFT, this);
		zLabel.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]);
		thePanel.add(zLabel);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
			XPanel effectPanel = new XPanel();
			effectPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 6));
				
				treat1Check = new XCheckbox(translate("Main effect for") + " " + data.getVariable("treat1").name, this);
				treat1Check.setState(false);
				treat1Check.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.BACKGROUND]);
				fitFactor[0] = false;
			effectPanel.add(treat1Check);
			
				treat1Labels = new TreatmentLabelsView(data, this, "treat1");
			effectPanel.add(treat1Labels);
				
				treat2Check = new XCheckbox(translate("Main effect for") + " " + data.getVariable("treat2").name, this);
				treat2Check.setState(false);
				treat2Check.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.BACKGROUND]);
				fitFactor[1] = false;
			effectPanel.add(treat2Check);
			
				treat2Labels = new TreatmentLabelsView(data, this, "treat2");
			effectPanel.add(treat2Labels);
				
				interactCheck = new XCheckbox(translate("Interaction"), this);
				interactCheck.setState(false);
				interactCheck.disable();
				fitInteraction = false;
			effectPanel.add(interactCheck);
			
		thePanel.add(ProportionLayout.TOP, effectPanel);
		
		thePanel.add(ProportionLayout.BOTTOM, lowerControlPanel(data));
		
		return thePanel;
	}
	
	protected XPanel lowerControlPanel(DataSet data) {
		XPanel residPanel = new XPanel();
		residPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 6));
		
			residualCheck = new XCheckbox(translate("Show residuals"), this);
			residualCheck.setState(true);
			theView.setResidualDisplay(RotateTwoFactorView.LINE_RESIDUALS);
		residPanel.add(residualCheck);
		return residPanel;
	}
	
	private XPanel headingPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			eqnCanvas = new MultiImageCanvas("exper/" + getParameter(MODEL_EQN_FILE_PARAM), ".gif", 4, this);
			setEqnVisibility();
		thePanel.add(eqnCanvas);
		
		return thePanel;
	}
	
	private void setEqnVisibility() {
		boolean vis[] = {true, fitFactor[0], fitFactor[1], fitInteraction};
		eqnCanvas.setVisible(vis);
	}
	
	private void updateModel() {
		FactorsModel model = (FactorsModel)data.getVariable("model");
		model.setLSParams("response", fitFactor, fitInteraction);
		data.variableChanged("model");
		setEqnVisibility();
	}
	
	private boolean localAction(Object target) {
		if (target == treat1Check) {
			fitFactor[0] = treat1Check.getState();
			if (fitFactor[1]) {
				if (fitFactor[0])
					interactCheck.enable();
				else
					interactCheck.disable();
			}
			updateModel();
			return true;
		}
		else if (target == treat2Check) {
			fitFactor[1] = treat2Check.getState();
			if (fitFactor[0]) {
				if (fitFactor[1])
					interactCheck.enable();
				else
					interactCheck.disable();
			}
			updateModel();
			return true;
		}
		else if (target == interactCheck) {
			fitInteraction = interactCheck.getState();
			if (fitInteraction) {
				treat1Check.disable();
				treat2Check.disable();
			}
			else {
				treat1Check.enable();
				treat2Check.enable();
			}
			updateModel();
			return true;
		}
		else if (target == residualCheck) {
			theView.setResidualDisplay(residualCheck.getState() ? RotateTwoFactorView.LINE_RESIDUALS
																														: RotateTwoFactorView.NO_RESIDUALS);
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}