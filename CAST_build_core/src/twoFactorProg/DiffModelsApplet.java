package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;


public class DiffModelsApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String INTERACTION_PARAM = "interaction";
	static final private String BLOCK_FACTOR_PARAM = "blockFactor";
	
	static final private String kXZKeys[] = {"x", "z"};
	
	static final private Color kSsqBackground = new Color(0xFFEEBB);
	static final private Color kVarNameColor = new Color(0x990000);
	
	
	private XLabel ssqLabel[] = new XLabel[4];
	
	private XCheckbox showDataCheck;
	
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
													getParameter(X_LABELS_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
													getParameter(Z_LABELS_PARAM));
		
			TwoFactorModel model1 = new TwoFactorModel(getParameter(Y_VAR_NAME_PARAM), data, kXZKeys,
																		TwoFactorModel.NONE, TwoFactorModel.NONE, false, 0.0);
		data.addVariable("model1", model1);
		
			TwoFactorModel model2 = new TwoFactorModel(getParameter(Y_VAR_NAME_PARAM), data, kXZKeys,
																		TwoFactorModel.NONE, TwoFactorModel.NONE, false, 0.0);
		data.addVariable("model2", model2);
		
			ResidValueVariable residVar = new ResidValueVariable(translate("Residual"), data, kXZKeys, "y",
																																		"model", 9);
		data.addVariable("resid", residVar);
		
		return data;
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
		
			theView = new RotateDiffModelsView(data, this, xAxis, yAxis, zAxis,
																										"x", "y", "z", "model1", "model2");
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.HORIZONTAL);
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
				
			for (int i=0 ; i<ssqLabel.length ; i++) {
				ssqLabel[i] = new XLabel("", XLabel.CENTER, this);
				ssqLabel[i].setFont(getStandardBoldFont());
				if (i == 1 || i == 3)
					ssqLabel[i].setForeground(kVarNameColor);
			}
			
			String interactString = getParameter(INTERACTION_PARAM);
			boolean showInteraction = (interactString != null) && interactString.equals("true");
			
			String blockFactorString = getParameter(BLOCK_FACTOR_PARAM);
			boolean blockFactorMode = (blockFactorString != null) && blockFactorString.equals("true");
		
		thePanel.add(new SelectModelDiffView(data, this, data.getVariable("x").name,
														data.getVariable("z").name, "model1", "model2", "y",
														(RotateDiffModelsView)theView, ssqLabel, showInteraction, blockFactorMode));
			
			XPanel outerPanel = new XPanel();
			outerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel ssqPanel = new InsetPanel(10, 4);
				ssqPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, -2));
					
				for (int i=0 ; i<ssqLabel.length ; i++)
					ssqPanel.add(ssqLabel[i]);
					
					XPanel ssqValuePanel = new InsetPanel(0, 3);
					ssqValuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
						NumValue biggestSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
						ExplainedSsqView ssqView = new ExplainedSsqView(data, this, "y", "model1",
																												"model2", "x", "z", biggestSsq);
					ssqValuePanel.add(ssqView);
				ssqPanel.add(ssqValuePanel);
				
				ssqPanel.lockBackground(kSsqBackground);
			
			outerPanel.add(ssqPanel);
		
		thePanel.add(outerPanel);
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				showDataCheck = new XCheckbox(translate("Show data"), this);
				showDataCheck.setState(true);
			checkPanel.add(showDataCheck);
		
		thePanel.add(checkPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showDataCheck) {
			theView.setDrawData(showDataCheck.getState());
			theView.repaint();
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