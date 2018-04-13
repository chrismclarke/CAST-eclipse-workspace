package experProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;

import exper.*;


public class IdentifiabilityApplet extends CoreMultiFactorApplet {
	static final protected String RESPONSE_AXIS_INFO_PARAM = "responseAxis";
	static final private String MAX_EFFECT_PARAM = "maxEffect";
	
	static final private String effect1Keys[] = {"treat1"};
	
	protected XButton zeroEffectButton[][];
	protected XButton zeroMeanButton[];
	
	private NumValue maxEffect;
	
	public void setupApplet() {
//		readEffects();
		
		data = readData();
		
		setLayout(new BorderLayout(30, 0));
		add("Center", displayPanel(data));
		add("East", parameterPanel(data));
	}
	
	protected String[] getEffectKeys() {
		return effect1Keys;
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
			maxEffect = new NumValue(getParameter(MAX_EFFECT_PARAM));
			FactorsModel model = new FactorsModel(getParameter(RESPONSE_NAME_PARAM),
																		data, getEffectKeys(), constant, effects, maxEffect.decimals);
																										//	Should be OK with null effects[][]
			model.setIdentConstraints(FactorsModel.ZERO_MEAN_EFFECTS);
			model.setLSParams("response", null, false);
			
		data.addVariable("model", model);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				yAxis.readNumLabels(getParameter(RESPONSE_AXIS_INFO_PARAM));
			plotPanel.add("Left", yAxis);
			
				HorizAxis treatAxis = new HorizAxis(this);
				CatVariable factorVar = (CatVariable)data.getVariable("treat1");
				treatAxis.setCatLabels(factorVar);
				treatAxis.setAxisName(factorVar.name);
				
			plotPanel.add("Bottom", treatAxis);
			
				AdjustBaseTreatView yView = new AdjustBaseTreatView(data, this, yAxis, treatAxis, "response",
												"treat1", "model");
				yView.lockBackground(Color.white);
			plotPanel.add("Center", yView);
		
		thePanel.add("Center", plotPanel);
		
			NumVariable respVar = (NumVariable)data.getVariable("response");
			XLabel respLabel = new XLabel(respVar.name, XLabel.LEFT, this);
			respLabel.setFont(yAxis.getFont());
		thePanel.add("North", respLabel);
		
		return thePanel;
	}
	
	protected XPanel parameterPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 20));
		
		thePanel.add(basePanel(data));
		
			zeroEffectButton = new XButton[1][];
			zeroMeanButton = new XButton[1];
		thePanel.add(effectPanel(data, "treat1", 0, "Treatment effects"));
		
		return thePanel;
	}
	
	
	protected XPanel effectPanel(DataSet data, String factorKey, int factorIndex, String title) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 2));
			
			CatVariable factor = (CatVariable)data.getVariable(factorKey);
			int nTreats = factor.noOfCategories();
			
			XLabel heading = new XLabel(title, XLabel.LEFT, this);
			heading.setFont(getBigBoldFont());
		thePanel.add(heading);
		
			zeroEffectButton[factorIndex] = new XButton[nTreats];
		for (int i=0 ; i<nTreats ; i++)
			thePanel.add(oneEffectPanel(data, factorKey, factorIndex, i));
			
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				zeroMeanButton[factorIndex] = new XButton("Zero Mean Effect", this);
			
			buttonPanel.add(zeroMeanButton[factorIndex]);
		
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	protected XPanel oneEffectPanel(DataSet data, String factorKey, int factorIndex,
																																int treatIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
			
			EffectValueView effect = new EffectValueView(data, this, "model",
												factorKey, maxEffect, factorIndex, treatIndex);
			effect.setForeground(TreatEffectSliderView.getBaseBarColor(treatIndex));
		thePanel.add(effect);
		
			XButton zeroButton = new XButton("Zero", this);
			zeroEffectButton[factorIndex][treatIndex] = zeroButton;
		thePanel.add(zeroButton);
		
		return thePanel;
	}
	
	protected XPanel basePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 2));
			
			XLabel heading = new XLabel("Base value", XLabel.LEFT, this);
			heading.setFont(getBigBoldFont());
		thePanel.add(heading);
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
		
			valuePanel.add(new EffectValueView(data, this, "model", "treat1", maxEffect, -1, 0));
		
		thePanel.add(valuePanel);
		
		return thePanel;
	}
	
	public void finishAnimation() {
	}

	
	private boolean localAction(Object target) {
		for (int i=0 ; i<zeroMeanButton.length ; i++)
			if (target == zeroMeanButton[i]) {
				FactorsModel model = (FactorsModel)data.getVariable("model");
				model.setLSParams("response", null, false);
				
				data.variableChanged("response");
				return true;
			}
		for (int factorIndex=0 ; factorIndex<zeroEffectButton.length ; factorIndex++) {
			int nLevels = zeroEffectButton[factorIndex].length;
			for (int treatIndex=0 ; treatIndex<nLevels ; treatIndex++)
				if (target == zeroEffectButton[factorIndex][treatIndex]) {
					FactorsModel model = (FactorsModel)data.getVariable("model");
					double effects[] = model.getMainEffects(factorIndex);
					double oldEffect = effects[treatIndex];
					double oldBase = model.getConstant();
					
					model.setConstant(oldBase + oldEffect);
					for (int i=0 ; i<nLevels ; i++)
						effects[i] -= oldEffect;
					
					model.setMainEffect(factorIndex, effects);
					
					data.variableChanged("response");
					return true;
				}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}