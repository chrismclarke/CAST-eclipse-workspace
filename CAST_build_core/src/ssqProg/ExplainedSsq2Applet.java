package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import models.*;

import ssq.*;


public class ExplainedSsq2Applet extends CoreSsqDistnApplet {
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData = super.getSummaryData(sourceData);
			
			GammaDistnVariable chiSquared = new GammaDistnVariable("chiSqr");
			
		summaryData.addVariable("chiSquared", chiSquared);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable("model");
		double errorSD = model.evaluateSD().toDouble();
		
		BasicComponentVariable explComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[1]);
		int explDF = explComponent.getDF();
		
		GammaDistnVariable chiSquared = (GammaDistnVariable)summaryData.getVariable("chiSquared");
		chiSquared.setScale(2.0 * errorSD);
		chiSquared.setShape(explDF * 0.5);
		
		SsqStackedView chi2DistnView = (SsqStackedView)ssqView;
		if (chi2DistnView != null) {
			chi2DistnView.setChi2Df();
			summaryData.variableChanged("chiSquared");
		}
	}
	
	protected int componentType() {
		return BasicComponentVariable.EXPLAINED;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		return ssqPanel(summaryData);
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		String explString = xNumNotCat ? "anova/sigma2.gif" : "anova/gMinus1sigma2.gif";
		
		SsqStackedView theView = new SsqStackedView(summaryData, this,
													axis, "chiSquared", explString, kRegnMeanSsqColor);
		theView.setActiveNumVariable(BasicComponentVariable.kComponentKey[1]);	//	explained
		
		axis.setForeground(kRegnSsqColor);
		axis.setAxisName(translate("Explained sum of squares"));
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return kRegnSsqColor;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		
		thePanel.add(getSsqValueView(summaryData, BasicComponentVariable.kComponentKey[1],
																		"xEquals/explainedSsq.png", kResidSsqColor));
		thePanel.add(getSampleSizePanel());
		thePanel.add(getEffectSizeSlider(data));
		thePanel.add(getSampleButton(translate("Take sample")));
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
}