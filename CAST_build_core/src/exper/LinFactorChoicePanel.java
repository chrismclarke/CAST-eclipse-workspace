package exper;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;


public class LinFactorChoicePanel extends XPanel {
	private DataSet data;
	private String modelKey;
	private double[] zCatToNum;
	private double meanZ;
	private int factorIndex, nFactors;
	
	private XChoice linFactorChoice;
	private int currentChoice;
	
	public LinFactorChoicePanel(DataSet data, String modelKey, int factorIndex, int nFactors,
																				double[] zCatToNum, double meanZ, XApplet applet) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			linFactorChoice = new XChoice(applet);
			linFactorChoice.addItem("Categorical factor");
			linFactorChoice.addItem("Linear effect");
			linFactorChoice.select(0);
			currentChoice = 0;
		add(linFactorChoice);
		
		this.data = data;
		this.zCatToNum = zCatToNum;
		this.meanZ = meanZ;
		this.modelKey = modelKey;
		this.factorIndex = factorIndex;
		this.nFactors = nFactors;
	}
	
	private void setFactorEffects() {
		double newEffect[] = new double[zCatToNum.length];
		FactorsModel responseVar = (FactorsModel)data.getVariable(modelKey);
		double slope = responseVar.getMainEffects(factorIndex)[0];
		for (int i=0 ; i<zCatToNum.length ; i++)
			newEffect[i] = slope * (zCatToNum[i] - meanZ);
		responseVar.setCatToNum(factorIndex, nFactors, null, meanZ);
		responseVar.setMainEffect(factorIndex, newEffect);
		data.variableChanged(modelKey);
	}
	
	private void setLinearEffects() {
		FactorsModel responseVar = (FactorsModel)data.getVariable(modelKey);
		double oldEffect[] = responseVar.getMainEffects(factorIndex);
		
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double sxy = 0.0;
		double n = zCatToNum.length;
		for (int i=0 ; i<n ; i++) {
			double x = (zCatToNum[i] - meanZ);
			double y = oldEffect[i];
			sx += x;
			sy += y;
			sxx += x * x;
			sxy += x * y;
		}
		double slope = (sxy - sx * sy / n) / (sxx - sx * sx / n);
		double intercept = (sy - sx * slope) / n;
		
		responseVar.setCatToNum(factorIndex, nFactors, zCatToNum, meanZ);
		double newEffect[] = {slope};
		responseVar.setMainEffect(factorIndex, newEffect);
		responseVar.setConstant(responseVar.getConstant() + intercept);
		data.variableChanged(modelKey);
	}
	
	private boolean localAction(Object target) {
		if (target == linFactorChoice) {
			if (linFactorChoice.getSelectedIndex() != currentChoice) {
				currentChoice = linFactorChoice.getSelectedIndex();
				if (currentChoice == 0)
					setFactorEffects();
				else
					setLinearEffects();
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