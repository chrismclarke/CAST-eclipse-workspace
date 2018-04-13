package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;


public class RotateEffectDistnApplet extends RotateApplet {
	static final private String EFFECT_DECIMALS_PARAM = "effectDecimals";
	
	private DataSet data;
	protected SummaryDataSet summaryData;
	
	private XButton sampleButton;
	private XChoice displayChoice;
	private int currentDisplayIndex = 0;
	
	protected DataSet readData() {
		data = new TwoFactorDataSet(this);
		
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new AnovaSummaryData(sourceData, "error");
		
		int decimals = Integer.parseInt(getParameter(EFFECT_DECIMALS_PARAM));
		summaryData.addVariable("xEffect", new FactorEffectVariable("X effect", "y", "ls",
																									FactorEffectVariable.X_EFFECT, decimals));
		summaryData.addVariable("zEffect", new FactorEffectVariable("Z effect", "y", "ls",
																									FactorEffectVariable.Z_EFFECT, decimals));
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		D3Axis xAxis = new D3Axis(getParameter(X_VAR_NAME_PARAM), D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xAxis.setCatScale(xVar);
		
		D3Axis yAxis = new D3Axis(getParameter(Y_VAR_NAME_PARAM), D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
		yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
		
		D3Axis zAxis = new D3Axis(getParameter(Z_VAR_NAME_PARAM), D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
		CatVariable zVar = (CatVariable)data.getVariable("z");
		zAxis.setCatScale(zVar);
		
		theView = new RotateEstimatesView(data, this, xAxis, yAxis, zAxis, "x", "y", "z", "ls",
																											RotateEstimatesView.X_EFFECT);
		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			XPanel displayTypePanel = new XPanel();
			displayTypePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				XLabel l = new XLabel("Display:", XLabel.LEFT, this);
				l.setFont(getStandardBoldFont());
			displayTypePanel.add(l);
			
				displayChoice = new XChoice(this);
				displayChoice.addItem("Estimates of effect of X");
				displayChoice.addItem("Estimates of effect of Z");
				displayChoice.addItem("Best model");
			displayTypePanel.add(displayChoice);
			
		thePanel.add(displayTypePanel);
		
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplayIndex) {
				currentDisplayIndex = newChoice;
				
				((RotateEstimatesView)theView).setDisplayEffect(
																	newChoice == 0 ? RotateEstimatesView.X_EFFECT
																	: newChoice == 1 ? RotateEstimatesView.Z_EFFECT
																	: RotateEstimatesView.MODEL);
				theView.repaint();
			}
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