package exerciseNormalProg;

import dataView.*;
import distn.*;

import exerciseNormal.JdistnAreaLookup.*;


public class ContinProbApplet extends NormalProbApplet {
	
	protected void registerParameterTypes() {
		registerParameter("mean2", "const");
		registerParameter("sd2", "const");
		registerParameter("p1", "const");
		super.registerParameterTypes();
	}
	
	protected NumValue getMean1() {
		return super.getMean();
	}
	
	protected NumValue getSD1() {
		return super.getSD();
	}
	
	protected NumValue getMean2() {
		return getNumValueParam("mean2");
	}
	
	protected NumValue getSD2() {
		return getNumValueParam("sd2");
	}
	
	protected double getP1() {
		return getDoubleParam("p1");
	}
	
	protected NumValue getMean() {		//		must be overridden to generate cut-offs
		NumValue mean1 = getMean1();
		NumValue mean2 = getMean2();
		double p1 = getP1();
		int decimals = Math.max(mean1.decimals, mean2.decimals);
		return new NumValue(mean1.toDouble() * p1 + mean2.toDouble() * (1 - p1), decimals);
	}
	
	protected NumValue getSD() {		//		must be overridden to generate cut-offs
		double mean1 = getMean1().toDouble();
		double mean2 = getMean2().toDouble();
		
		NumValue sd1Val = getSD1();
		NumValue sd2Val = getSD2();
		
		double s1 = sd1Val.toDouble();
		double s2 = sd2Val.toDouble();
		double p1 = getP1();
		
		double eX2 = p1 * (s1 * s1 + mean1 * mean1) + (1 - p1) * (s2 * s2 + mean2 * mean2);
		
		double mean = p1 * mean1 + (1 - p1) * mean2;
		
		return new NumValue(Math.sqrt(eX2 - mean * mean),
																					Math.max(sd1Val.decimals, sd2Val.decimals));
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NormalDistnVariable y1Distn = new NormalDistnVariable("Distn1");
		data.addVariable("y1", y1Distn);
		
			NormalDistnVariable y2Distn = new NormalDistnVariable("Distn1");
		data.addVariable("y2", y2Distn);
		
			MixtureDistnVariable mixtureDistn = new MixtureDistnVariable("Mixture", y1Distn, y2Distn);
		data.addVariable("distn", mixtureDistn);
		
		return data;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		normalLookupPanel = new NormalLookupPanel(data, "distn", this, NormalLookupPanel.HIGH_AND_LOW,
																																			NormalLookupPanel.SINGLE_DENSITY);
		registerStatusItem("drag", normalLookupPanel);
		return normalLookupPanel;
	}
	
	protected void setDisplayForQuestion() {
		normalLookupPanel.resetPanel();
		
		data.variableChanged("distn");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable y1Distn = (NormalDistnVariable)data.getVariable("y1");
		double mean1 = getMean1().toDouble();
		double sd1 = getSD1().toDouble();
		y1Distn.setMean(mean1);
		y1Distn.setSD(sd1);
		
		NormalDistnVariable y2Distn = (NormalDistnVariable)data.getVariable("y2");
		double mean2 = getMean2().toDouble();
		double sd2 = getSD2().toDouble();
		y2Distn.setMean(mean2);
		y2Distn.setSD(sd2);
		
		MixtureDistnVariable mixtureDistn = (MixtureDistnVariable)data.getVariable("distn");
		mixtureDistn.setPropn(getP1());
		
		double mean = getMean().toDouble();
		double sd = getSD().toDouble();
		
		mixtureDistn.setMinSelection(mean - sd);
		mixtureDistn.setMaxSelection(mean + sd);
	}
	
	protected String densityString() {
		return "probability density";
	}
}