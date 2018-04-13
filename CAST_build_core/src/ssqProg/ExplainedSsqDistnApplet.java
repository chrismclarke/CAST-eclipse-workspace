package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;

import ssq.*;


public class ExplainedSsqDistnApplet extends CoreSsqDistnApplet {
	
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
		CoreComponentVariable selectedComponent =
							(CoreComponentVariable)data.getVariable(BasicComponentVariable.kComponentKey[1]);
		int df = selectedComponent.getDF();
		
		GammaDistnVariable chiSquared = (GammaDistnVariable)summaryData.getVariable("chiSquared");
		chiSquared.setScale(2.0 * errorSD);
		chiSquared.setShape(df * 0.5);
		
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
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new BorderLayout(20, 0));
		
		bottomPanel.add("Center", ssqPanel(summaryData));
		
			XPanel bottomRightPanel = new XPanel();
			bottomRightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 10));
			bottomRightPanel.add(getTheoryCheck());
			
		bottomPanel.add("East", bottomRightPanel);
		
		return bottomPanel;
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		String regnString = xNumNotCat ? "anova/sigma2.gif" : "anova/gMinus1sigma2.gif";
		
		SsqStackedView theView = new SsqStackedView(summaryData, this,
																			axis, "chiSquared", regnString, kRegnMeanSsqColor);
		theView.setShowDensity(DataPlusDistnInterface.NO_DISTN);
		theView.setActiveNumVariable(BasicComponentVariable.kComponentKey[1]);	//	explained
		
		axis.setForeground(kRegnSsqColor);
		axis.setAxisName("Explained sum of squares");
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return kRegnSsqColor;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		
		thePanel.add(getSsqValueView(summaryData, BasicComponentVariable.kComponentKey[1],
																		"xEquals/explainedSsq.png", kRegnSsqColor));
		thePanel.add(getSampleSizePanel());
		thePanel.add(getSampleButton(translate("Take sample")));
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
}