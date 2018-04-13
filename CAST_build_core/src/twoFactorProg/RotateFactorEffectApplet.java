package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import valueList.*;
import graphics3D.*;

import multivarProg.*;
import twoFactor.*;


public class RotateFactorEffectApplet extends RotateApplet {
	static final private String MAX_EFFECT_PARAM = "maxEffect";
	
	static final private Color kEffectBackgroundColor = new Color(0xEEEEFF);
	static final private Color kXEffectColor = new Color(0x007700);
	static final private Color kZEffectColor = Color.blue;
	
	private DataSet data;
	protected SummaryDataSet summaryData;
	private XChoice dataSetChoice;
	
	private NumValue maxEffect;
	
	private XChoice displayChoice;
	private int currentDisplayIndex = 0;
	
	private XPanel effectPanel;
	private CardLayout effectPanelLayout;
	
	protected DataSet readData() {
		data = new TwoFactorDataSet(this);
		
		summaryData = getSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new AnovaSummaryData(sourceData, "error");
		
		maxEffect = new NumValue(getParameter(MAX_EFFECT_PARAM));
		int decimals = maxEffect.decimals;
		summaryData.addVariable("xEffect", new FactorEffectVariable(translate("Estimate") + " =", "y", "ls",
																									FactorEffectVariable.X_EFFECT, decimals));
		summaryData.addVariable("zEffect", new FactorEffectVariable(translate("Estimate") + " =", "y", "ls",
																									FactorEffectVariable.Z_EFFECT, decimals));
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
		dataSetChoice = ((CoreModelDataSet)data).dataSetChoice(this);
		if (dataSetChoice != null)
			thePanel.add(dataSetChoice);
			
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
//		theView.setCrossSize(DataView.LARGE_CROSS);
		theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(5, 0, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 70));
			
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
			
				XLabel choiceLabel = new XLabel(translate("Effect of changing") + "...", XLabel.LEFT, this);
				choiceLabel.setFont(getStandardBoldFont());
			choicePanel.add(choiceLabel);
				
				displayChoice = new XChoice(this);
				displayChoice.addItem(data.getVariable("x").name);
				displayChoice.addItem(data.getVariable("z").name);
			choicePanel.add(displayChoice);
		
		thePanel.add(choicePanel);
		
			effectPanel = new XPanel();
			effectPanelLayout = new CardLayout();
			effectPanel.setLayout(effectPanelLayout);
			
			effectPanel.add("x", oneEffectPanel(data, "x", "z", "xEffect", kXEffectColor));
			effectPanel.add("z", oneEffectPanel(data, "z", "x", "zEffect", kZEffectColor));
			effectPanelLayout.show(effectPanel, "x");
			
		thePanel.add(effectPanel);
		
		return thePanel;
	}
	
	private XPanel oneEffectPanel(DataSet data, String effectKey, String sliceKey,
																						String summaryEffectKey, Color effectColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
		
			XPanel partialEffectPanel = new InsetPanel(10, 5);
			partialEffectPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 10));
			
				XPanel slice0Panel = new XPanel();
				slice0Panel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
					CatVariable sliceVar = (CatVariable)data.getVariable(sliceKey);
					XLabel slice0Label = new XLabel(translate("For") + " " + sliceVar.name + " = " + sliceVar.getLabel(0).toString(), XLabel.LEFT, this);
					slice0Label.setFont(getStandardBoldFont());
					slice0Label.setForeground(effectColor);
				slice0Panel.add(slice0Label);
				
					SliceEffectValueView estimate0 = new SliceEffectValueView(data, this, "y", sliceKey, 0, effectKey, maxEffect);
					estimate0.setForeground(effectColor);
				slice0Panel.add(estimate0);
			
			partialEffectPanel.add(slice0Panel);
			
				XPanel slice1Panel = new XPanel();
				slice1Panel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					XLabel slice1Label = new XLabel(translate("For") + " " + sliceVar.name + " = " + sliceVar.getLabel(1).toString(), XLabel.LEFT, this);
					slice1Label.setFont(getStandardBoldFont());
					slice1Label.setForeground(effectColor);
				slice1Panel.add(slice1Label);
					
					SliceEffectValueView estimate1 = new SliceEffectValueView(data, this, "y", sliceKey, 1, effectKey, maxEffect);
					estimate1.setForeground(effectColor);
				slice1Panel.add(estimate1);
			
			partialEffectPanel.add(slice1Panel);
		
			partialEffectPanel.lockBackground(kEffectBackgroundColor);
		thePanel.add(partialEffectPanel);
			
			XPanel overallEffectPanel = new InsetPanel(10, 5);
			overallEffectPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel overallLabel = new XLabel(translate("Overall"), XLabel.LEFT, this);
				overallLabel.setFont(getStandardBoldFont());
				overallLabel.setForeground(effectColor);
			overallEffectPanel.add(overallLabel);
			
				OneValueView effectView = new OneValueView(summaryData, summaryEffectKey, this, maxEffect);
				effectView.setForeground(effectColor);
				effectView.setHighlightSelection(false);
			overallEffectPanel.add(effectView);
			
			overallEffectPanel.lockBackground(kEffectBackgroundColor);
		thePanel.add(overallEffectPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.7, 0));
		
			XPanel rotatePanel = RotateButton.createRotationPanel(theView, this, RotateButton.HORIZONTAL);
				rotateButton = new XButton(translate("Spin"), this);
			rotatePanel.add(rotateButton);
		
		thePanel.add(ProportionLayout.LEFT, rotatePanel);
		thePanel.add(ProportionLayout.RIGHT, new XPanel());
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplayIndex) {
				currentDisplayIndex = newChoice;
				
				((RotateEstimatesView)theView).setDisplayEffect(
																	newChoice == 0 ? RotateEstimatesView.X_EFFECT
																	: RotateEstimatesView.Z_EFFECT);
				theView.repaint();
				effectPanelLayout.show(effectPanel, newChoice == 0 ? "x" : "z");
			}
			return true;
		}
		else if (target == dataSetChoice) {
			if (((CoreModelDataSet)data).changeDataSet(dataSetChoice.getSelectedIndex())) {
				data.variableChanged("y");
				summaryData.setSingleSummaryFromData();
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