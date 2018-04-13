package exper2;

import java.awt.*;

import dataView.*;
import models.*;
import imageUtils.*;


public class FactorRssValueView extends ValueImageView {
	
	private String xCatKey, yKey, modelKey;
	private NumValue maxValue;
	
	public FactorRssValueView(DataSet theData, XApplet applet, String xNumKey, String xCatKey,
																								String yKey, String modelKey, NumValue maxValue) {
		super(theData, applet, "xEquals/residualSsq.png", 12);
		this.xCatKey = xCatKey;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.maxValue = maxValue;
		setForeground(Color.blue);
	}
	
	public void setModelKey(String modelKey) {
		this.modelKey = modelKey;
	}

	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	private double getModelMean(CoreModelVariable model, int index, CatVariable xCatVar) {
		if (model instanceof GroupsModelVariable)
			return model.evaluateMean(xCatVar.getLabel(index));
		else if (model instanceof MeanOnlyModel) {
			MeanOnlyModel mm = (MeanOnlyModel)model;
			return mm.evaluateMean(index);
		}
		else {
			LinearModel lm = (LinearModel)model;
			return lm.evaluateMean(index);
		}
	}
	
	protected String getValueString() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xCatVar = (CatVariable)getVariable(xCatKey);
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xCatVar.values();
		double rss = 0.0;
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xCat = xCatVar.labelIndex(x);
			double mean = getModelMean(model, xCat, xCatVar);
			double resid = y - mean;
			
			rss += resid * resid;
		}
		return new NumValue(rss, maxValue.decimals).toString();	
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
