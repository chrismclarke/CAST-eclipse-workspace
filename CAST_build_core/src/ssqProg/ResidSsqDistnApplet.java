package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import models.*;

import ssq.*;


public class ResidSsqDistnApplet extends CoreSsqDistnApplet {
	
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
		
		BasicComponentVariable residComponent = (BasicComponentVariable)data.
																				getVariable(BasicComponentVariable.kComponentKey[2]);
		int residDF = residComponent.getDF();
		
		GammaDistnVariable chiSquared = (GammaDistnVariable)summaryData.getVariable("chiSquared");
		chiSquared.setScale(2.0 * errorSD);
		chiSquared.setShape(residDF * 0.5);
		
		SsqStackedView chi2DistnView = (SsqStackedView)ssqView;
		if (chi2DistnView != null) {
			chi2DistnView.setChi2Df();
			summaryData.variableChanged("chiSquared");
		}
	}
	
	protected int componentType() {
		return BasicComponentVariable.RESIDUAL;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		return ssqPanel(summaryData);
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		String residString = xNumNotCat ? "anova/nMinus2sigma2.gif" : "anova/nMinusgsigma2.gif";
		
		SsqStackedView theView = new SsqStackedView(summaryData, this,
																		axis, "chiSquared", residString, kResidMeanSsqColor);
		theView.setActiveNumVariable(BasicComponentVariable.kComponentKey[2]);	//	residual
		
		axis.setForeground(kResidSsqColor);
		axis.setAxisName(translate("Residual sum of squares"));
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return kResidSsqColor;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		
		thePanel.add(getSsqValueView(summaryData, BasicComponentVariable.kComponentKey[2],
																		"xEquals/residualSsq.png", kResidSsqColor));
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