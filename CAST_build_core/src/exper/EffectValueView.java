package exper;

import java.awt.*;

import dataView.*;
import valueList.*;
import models.*;


public class EffectValueView extends ValueView {
//	static public final String EFFECT_VIEW = "effectValue";
	
	private String modelKey, factorKey;
	private NumValue maxEffect;
	private int factorIndex, treatIndex;
	
	private int maxWidth;
	private LabelValue tempLabel = new LabelValue("");
	private LabelValue constLabel;
	private NumValue tempValue;
	
	public EffectValueView(DataSet theData, XApplet applet, String modelKey, String factorKey,
													NumValue maxEffect, int factorIndex, int treatIndex) {
		super(theData, applet);
		this.modelKey = modelKey;
		this.factorKey = factorKey;
		this.maxEffect = maxEffect;
		this.factorIndex = factorIndex;
		this.treatIndex = treatIndex;
		tempValue = new NumValue(0.0, maxEffect.decimals);
	}
	
	public void setConstantLabel(String label) {
		constLabel = new LabelValue(label);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		CatVariable treat = (CatVariable)getVariable(factorKey);
		FontMetrics fm = g.getFontMetrics();
		
		maxWidth = 0;
		if (constLabel != null)
			maxWidth = constLabel.stringWidth(g);
		for (int i=0 ; i<treat.noOfCategories() ; i++)
			maxWidth = Math.max(maxWidth, treat.getLabel(i).stringWidth(g));
		maxWidth += fm.stringWidth(" : ");
		
		return maxWidth;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxEffect.stringWidth(g);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (factorIndex >= 0) {
			CatVariable treat = (CatVariable)getVariable(factorKey);
			tempLabel.label = treat.getLabel(treatIndex).toString() + " : ";
			tempLabel.drawLeft(g, startHoriz + maxWidth, baseLine);
		}
		else if (constLabel != null)
			constLabel.drawLeft(g, startHoriz + maxWidth, baseLine);
	}
	
	protected String getValueString() {
		FactorsModel response = (FactorsModel)getVariable(modelKey);
		
		tempValue.setValue((factorIndex < 0) ? response.getConstant()
																	: response.getMainEffects(factorIndex)[treatIndex]);
		return tempValue.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
