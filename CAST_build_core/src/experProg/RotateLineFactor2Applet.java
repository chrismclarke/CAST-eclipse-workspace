package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import exper.*;


public class RotateLineFactor2Applet extends RotateLineFactorApplet {
//	static final private String[] xKey = {"treat1", "treat2"};
	
	private TreatmentLabelsView treat1Labels;
	
	private XCheckbox treat1Check, treat2Check, interactCheck;
	private boolean fitFactor[] = {true, true};
	private boolean fitInteraction;
	
	private XChoice linFactorChoice;
	private int currentChoice;
	
	public void setupApplet() {
		readEffects();
		
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		add("Center", rotatePanel(data));
		add("East", controlPanel(data));
		
		updateModel();
	}
	
	protected RotateTwoFactorView create3DView(DataSet data, D3Axis xAxis, D3Axis yAxis,
																													D3Axis zAxis, String yVarKey) {
		RotateTwoFactorView theView = super.create3DView(data, xAxis, yAxis, zAxis, yVarKey);
		theView.setDrawData(true);
//		theView.setResidualDisplay(RotateTwoFactorView.LINE_RESIDUALS);
		theView.setCrossColouring(RotateTwoFactorView.X_COLOURS);
		theView.setCrossSize(DataView.LARGE_CROSS);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 15));
			
			XPanel treat1Panel = new XPanel();
			treat1Panel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
				treat1Check = new XCheckbox("Main effect for " + data.getVariable("treat1").name, this);
				treat1Check.setState(false);
				treat1Check.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.BACKGROUND]);
				fitFactor[0] = false;
			treat1Panel.add(treat1Check);
			
				treat1Labels = new TreatmentLabelsView(data, this, "treat1");
			treat1Panel.add(treat1Labels);
		
		thePanel.add(treat1Panel);
			
			XPanel treat2Panel = new XPanel();
			treat2Panel.setLayout(new BorderLayout(0, 2));
			
				XPanel checkPanel = new XPanel();
				checkPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					treat2Check = new XCheckbox("Main effect for " + data.getVariable("treat2").name, this);
					treat2Check.setState(false);
					treat2Check.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.BACKGROUND]);
					fitFactor[1] = false;
				checkPanel.add(treat2Check);
				
			treat2Panel.add("North", checkPanel);
			
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 0));
				
				linFactorChoice = new XChoice(this);
				linFactorChoice.addItem("Factor");
				linFactorChoice.addItem("Linear effect");
				linFactorChoice.select(0);
				currentChoice = 0;
				
				choicePanel.add(linFactorChoice);
				
			treat2Panel.add("Center", choicePanel);
			
		thePanel.add(treat2Panel);
			
			interactCheck = new XCheckbox("Interaction", this);
			interactCheck.setState(false);
			interactCheck.disable();
			fitInteraction = false;
		thePanel.add(interactCheck);
		
		return thePanel;
	}
	
	private void updateModel() {
		FactorsModel model = (FactorsModel)data.getVariable("model");
		model.setLSParams("response", fitFactor, fitInteraction);
		data.variableChanged("model");
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
		else if (target == linFactorChoice) {
			if (linFactorChoice.getSelectedIndex() != currentChoice) {
				currentChoice = linFactorChoice.getSelectedIndex();
				if (!fitFactor[1]) {
					fitFactor[1] = true;
					treat2Check.setState(true);
					if (fitFactor[0])
						interactCheck.enable();
				}
				FactorsModel responseVar = (FactorsModel)data.getVariable("model");
				if (currentChoice == 0) {
					responseVar.setCatToNum(1, 2, null, meanZ);
					updateModel();
				}
				else {
					responseVar.setCatToNum(1, 2, zCatToNum, meanZ);
					updateModel();
				}
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}