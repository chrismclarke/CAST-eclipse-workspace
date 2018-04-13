package twoFactorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import graphics3D.*;

import multivarProg.*;
import regn.*;
import twoFactor.*;


public class RotateDragFactorsApplet extends RotateApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String X_MODEL_TERMS_PARAM = "xModelTerms";
	
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String Z_MODEL_TERMS_PARAM = "zModelTerms";
	
	static final private String INTERACTION_PARAM = "interaction";
	static final private String INIT_MODEL_PARAM = "initialModel";
	static final protected String MAX_RSS_PARAM = "maxRss";
	static final private String FACTOR_NOT_GROUPS_PARAM = "factorNotGroups";
	
	static final private String FACTOR_CHECKS_PARAM = "factorChecks";
	
	static final private String kXZKeys[] = {"x", "z"};
	
	static final protected Color kRssBackgroundColor = new Color(0xFFEEBB);
	
	private String kTermName[];
	
	private DataSet data;
	private int xModelTerms[];
	private int zModelTerms[];
	
	private boolean dataOnly, alwaysLS;
	private XButton lsButton;
	
	private XChoice xTermChoice, zTermChoice;
	private XCheckbox xFactorCheck, zFactorCheck;			//	alternative to XChoice for specifying model factors
	private int currentXIndex = 0;
	private int currentZIndex = 0;
	private XCheckbox interactionCheck;
	
	private boolean hasInteraction = false;
	
	public void setupApplet() {
		kTermName = new String[3];
		kTermName[0] = translate("None");
		kTermName[1] = translate("Linear");
		kTermName[2] = "-group";
		super.setupApplet();
	}
	
	protected DataSet readData() {
		data = new DataSet();
		
		String yValuesString = getParameter(Y_VALUES_PARAM);
		if (yValuesString != null)
			data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), yValuesString);
		
		data.addCatVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM),
													getParameter(X_LABELS_PARAM));
		data.addCatVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM),
													getParameter(Z_LABELS_PARAM));
		
		String initModelString = getParameter(INIT_MODEL_PARAM);
		dataOnly = initModelString.equals("none");
		if (!dataOnly) {
			alwaysLS = initModelString.equals("ls");
			double initMean = (alwaysLS) ? 0.0 : Double.parseDouble(initModelString);
			xModelTerms = getModelTerms(getParameter(X_MODEL_TERMS_PARAM));
			zModelTerms = getModelTerms(getParameter(Z_MODEL_TERMS_PARAM));
			
//			String groupsString = getParameter(FACTOR_NOT_GROUPS_PARAM);
			
			TwoFactorModel model = new TwoFactorModel(getParameter(Y_VAR_NAME_PARAM), data, kXZKeys,
																		xModelTerms[0], zModelTerms[0], false, initMean);
			if (yValuesString != null && alwaysLS)
				model.setLSParams("y");
			
			data.addVariable("model", model);
		}
		
		if (yValuesString != null && !dataOnly && alwaysLS) {
			ResidValueVariable residVar = new ResidValueVariable(translate("Residual"), data, kXZKeys, "y",
																																			"model", 9);
			data.addVariable("resid", residVar);
		}
		
		return data;
	}
	
	private int[] getModelTerms(String termString) {
		StringTokenizer st = new StringTokenizer(termString);
		int terms[] = new int[st.countTokens()];
		int index = 0;
		while (st.hasMoreTokens()) {
			String term = st.nextToken();
			if (term.equals("linear"))
				terms[index++] = TwoFactorModel.LINEAR;
			else if (term.equals("factor"))
				terms[index++] = TwoFactorModel.FACTOR;
			else
				terms[index++] = TwoFactorModel.NONE;
		}
		
		return terms;
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
		
		RotateDragFactorsView localView = new RotateDragFactorsView(data, this, xAxis, yAxis, zAxis,
																										"x", "y", "z", dataOnly ? null : "model");
		localView.setAllowDragParams(!alwaysLS);
		localView.setCrossColouring(RotateDragFactorsView.COLOURS);
		if (data.getVariable("y") == null)
			localView.setDrawData(false);
		theView = localView;
		theView.lockBackground(Color.white);
		if (dataOnly)
			theView.setCrossSize(DataView.LARGE_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XChoice termChoice(int[] options, CatVariable x) {
		if (options.length <= 1)
			return null;
		else {
			XChoice theChoice = new XChoice(this);
			for (int i=0 ; i<options.length ; i++) {
				String menuItem = kTermName[options[i]];
				if (options[i] == 2) {
					String factorString = getParameter(FACTOR_NOT_GROUPS_PARAM);
					if (factorString != null && factorString.equals("true"))
						menuItem = translate("Factor");
					else
						menuItem = x.noOfCategories() + menuItem;
				}
				theChoice.addItem(menuItem);
			}
			
			return theChoice;
		}
	}
	
	private XPanel termChoicePanel(XChoice termChoice, String termName) {
		if (termChoice == null)
			return null;
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																										VerticalLayout.VERT_CENTER, 0));
				XLabel termLabel = new XLabel(termName, XLabel.LEFT, this);
				termLabel.setFont(getStandardBoldFont());
			thePanel.add(termLabel);
					
			thePanel.add(termChoice);
			
			return thePanel;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		CatVariable zVar = (CatVariable)data.getVariable("z");
		
		String interactionString = getParameter(INTERACTION_PARAM);
		boolean canShowInteraction = interactionString.equals("true");
		
		XPanel thePanel = new InsetPanel(20, 10, 20, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		if (dataOnly)
			return thePanel;
		
		String checksString = getParameter(FACTOR_CHECKS_PARAM);
		if (checksString == null || !checksString.equals("true"))
			thePanel.add("Center", choiceControlPanel(xVar, zVar, canShowInteraction));
		else
			thePanel.add("Center", checkControlPanel(xVar, zVar, canShowInteraction));
		
		checkInteraction();
		
		return thePanel;
	}
	
	private XPanel choiceControlPanel(CatVariable xVar, CatVariable zVar, boolean canShowInteraction) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			xTermChoice = termChoice(xModelTerms, xVar);
		if (xTermChoice != null)
			thePanel.add(termChoicePanel(xTermChoice, xVar.name + ":"));
		
			zTermChoice = termChoice(zModelTerms, zVar);
		if (zTermChoice != null)
			thePanel.add(termChoicePanel(zTermChoice, zVar.name + ":"));
		
		if (canShowInteraction) {
			interactionCheck = new XCheckbox(translate("Interaction"), this);
			thePanel.add(interactionCheck);
		}
		
		return thePanel;
	}
	
	private XPanel checkControlPanel(CatVariable xVar, CatVariable zVar, boolean canShowInteraction) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel termsLabel = new XLabel(translate("Terms in model") + ":", XLabel.RIGHT, this);
				termsLabel.setFont(getStandardBoldFont());
			leftPanel.add(termsLabel);
			
		thePanel.add(leftPanel);
			
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
			if (xModelTerms.length == 1) {
				if (xModelTerms[0] != TwoFactorModel.NONE) {		//	always on
					xFactorCheck = new XCheckbox(xVar.name, this);
					xFactorCheck.setState(true);
					xFactorCheck.disable();
					
					rightPanel.add(xFactorCheck);
				}
			}
			else if (xModelTerms[0] == TwoFactorModel.NONE) {
				xFactorCheck = new XCheckbox(xVar.name, this);
				rightPanel.add(xFactorCheck);
			}
			
			if (zModelTerms.length == 1) {
				if (zModelTerms[0] != TwoFactorModel.NONE) {		//	always on
					zFactorCheck = new XCheckbox(zVar.name, this);
					zFactorCheck.setState(true);
					zFactorCheck.disable();
					
					rightPanel.add(zFactorCheck);
				}
			}
			else if (zModelTerms[0] == TwoFactorModel.NONE) {
				zFactorCheck = new XCheckbox(zVar.name, this);
				rightPanel.add(zFactorCheck);
			}
			
			if (canShowInteraction) {
				interactionCheck = new XCheckbox(translate("Interaction"), this);
				rightPanel.add(interactionCheck);
			}
			
		thePanel.add(rightPanel);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		
		CoreVariable yVar = data.getVariable("y");
		if (yVar != null && !dataOnly && alwaysLS) {
			XPanel rotatePanel = thePanel;
			
			thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
			
			thePanel.add(rotatePanel);
			
				XPanel rssPanel = new InsetPanel(10, 5, 10, 5);
				rssPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					NumValue maxRss = new NumValue(getParameter(MAX_RSS_PARAM));
					ResidSsq2View rssValue = new ResidSsq2View(data, this, "resid", maxRss, "xEquals/residualSsqBlack.png", 13);
			
					rssValue.setForeground(Color.red);
				rssPanel.add(rssValue);
				rssPanel.lockBackground(kRssBackgroundColor);
			
			thePanel.add(rssPanel);
		}
		else if (!dataOnly && yVar != null && !alwaysLS) {
			XPanel rotatePanel = thePanel;
			
			thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 60));
			
			thePanel.add(rotatePanel);
			
				lsButton = new XButton(translate("Least squares"), this);
			thePanel.add(lsButton);
		}
		return thePanel;
	}
	
	private void checkInteraction() {
		if (interactionCheck != null) {
			boolean canHaveInteraction = xModelTerms[currentXIndex] != TwoFactorModel.NONE
																		&& zModelTerms[currentZIndex] != TwoFactorModel.NONE;
			if (!canHaveInteraction) {
				hasInteraction = false;
				interactionCheck.setState(false);
				interactionCheck.disable();
			}
			else
				interactionCheck.enable();
		}
	}
	
	private void setLeastSquares() {
		TwoFactorModel model = (TwoFactorModel)data.getVariable("model");
		model.updateLSParams("y");
	}
	
	private void updateModel() {
		TwoFactorModel model = (TwoFactorModel)data.getVariable("model");
		model.setModelType(xModelTerms[currentXIndex], zModelTerms[currentZIndex],
																																	hasInteraction);
		CoreVariable yVar = data.getVariable("y");
		if (yVar != null && alwaysLS)
			model.setLSParams("y");
		data.variableChanged("model");
	}

	
	private boolean localAction(Object target) {
		if (target == lsButton) {
			setLeastSquares();
			data.variableChanged("model");
			return true;
		}
		else if (target == xTermChoice) {
			int newChoice = xTermChoice.getSelectedIndex();
			if (newChoice != currentXIndex) {
				currentXIndex = newChoice;
				checkInteraction();
				updateModel();
			}
			return true;
		}
		else if (target == xFactorCheck) {
			currentXIndex = xFactorCheck.getState() ? 1 : 0;
			checkInteraction();
			updateModel();
			return true;
		}
		else if (target == zTermChoice) {
			int newChoice = zTermChoice.getSelectedIndex();
			if (newChoice != currentZIndex) {
				currentZIndex = newChoice;
				checkInteraction();
				updateModel();
			}
			return true;
		}
		else if (target == zFactorCheck) {
			currentZIndex = zFactorCheck.getState() ? 1 : 0;
			checkInteraction();
			updateModel();
			return true;
		}
		else if (target == interactionCheck) {
			hasInteraction = interactionCheck.getState();
			updateModel();
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