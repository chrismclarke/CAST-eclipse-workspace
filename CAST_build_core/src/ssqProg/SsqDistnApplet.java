package ssqProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreGraphics.*;
import models.*;

import ssq.*;


public class SsqDistnApplet extends CoreSsqDistnApplet {
	private HorizAxis ssqAxis;
	
	private XChoice componentChoice;
	private int currentComponentIndex = 0;
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		AnovaSummaryData summaryData = super.getSummaryData(sourceData);
			
			GammaDistnVariable chiSquared = new GammaDistnVariable("chiSqr1");
			
		summaryData.addVariable("chiSquared", chiSquared);
		
			adjustDF(summaryData);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private void adjustDF(AnovaSummaryData summaryData) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable("model");
		double errorSD = model.evaluateSD().toDouble();
		CoreComponentVariable selectedComponent =
																		(CoreComponentVariable)data.getVariable(getComponentKey());
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
	
	private String getComponentKey() {
		return (currentComponentIndex == 0) ? BasicComponentVariable.kComponentKey[0]		// total
																				: BasicComponentVariable.kComponentKey[2];		// residual
	}
	
	protected int componentType() {
		return (currentComponentIndex == 0) ? BasicComponentVariable.TOTAL
																				: BasicComponentVariable.RESIDUAL;
	}
	
	protected XPanel ssqAndDistnPanel(AnovaSummaryData summaryData) {
		XPanel bottomPanel = new XPanel();
		bottomPanel.setLayout(new BorderLayout(20, 0));
		
		bottomPanel.add("Center", ssqPanel(summaryData));
		
			XPanel bottomRightPanel = new XPanel();
			bottomRightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 10));
			bottomRightPanel.add(getTheoryCheck());
		
				componentChoice = new XChoice(this);
				componentChoice.addItem("Total ssq");
				componentChoice.addItem("Residual ssq");
				currentComponentIndex = 0;
				
			bottomRightPanel.add(componentChoice);
			
		bottomPanel.add("East", bottomRightPanel);
		
		return bottomPanel;
	}
	
	protected DataView getSsqDotView(SummaryDataSet summaryData, HorizAxis axis) {
		String meanStrings[] = new String[2];
		meanStrings[0] = "anova/nMinus1sigma2";
		meanStrings[1] = xNumNotCat ? "anova/nMinus2sigma2.gif" : "anova/nMinusgsigma2.gif";
		
		SsqStackedView theView = new SsqStackedView(summaryData, this,
																axis, "chiSquared", meanStrings, kTotalMeanSsqColor);
		theView.setShowDensity(DataPlusDistnInterface.NO_DISTN);
		theView.setActiveNumVariable(BasicComponentVariable.kComponentKey[0]);	//	total
		ssqAxis = axis;
		
		adjustComponentDisplay(theView);
		
		return theView;
	}
	
	protected Color getSsqColor() {
		return Color.black;
	}
	
	protected XPanel controlPanel(CoreModelDataSet data, AnovaSummaryData summaryData) {
		XPanel thePanel = super.controlPanel(data, summaryData);
		
		thePanel.add(getSsqValueView(summaryData, BasicComponentVariable.kComponentKey[2],
																		"xEquals/residualSsq.png", kResidSsqColor));
		thePanel.add(getSsqValueView(summaryData, BasicComponentVariable.kComponentKey[0],
																		"xEquals/totalSsq.png", kTotalSsqColor));
		thePanel.add(getSampleSizePanel());
		thePanel.add(getSampleButton(translate("Take sample")));
		return thePanel;
	}
	
	protected void changeSampleSize(int newSizeIndex) {
		super.changeSampleSize(newSizeIndex);
		
		adjustDF(summaryData);
	}
	
	private void adjustComponentDisplay(SsqStackedView theView) {
		Color newComponentColor = (currentComponentIndex == 0) ? kTotalSsqColor : kResidSsqColor;
		Color newComponentMeanColor = (currentComponentIndex == 0) ? kTotalMeanSsqColor
																									: kResidMeanSsqColor;
		
		theView.setSsqType(getComponentKey(), currentComponentIndex,
																										newComponentColor, newComponentMeanColor);
		
		summaryData.variableChanged("chiSquared");
		
		ssqAxis.setForeground(newComponentColor);
		ssqAxis.setAxisName((currentComponentIndex == 0) ? "Total sum of squares" : "Residual sum of squares");
		ssqAxis.repaint();
		
		int currentComponentType = (currentComponentIndex == 0) ? BasicComponentVariable.TOTAL
																										: BasicComponentVariable.RESIDUAL;
		changeComponentDataDisplay(currentComponentType);
	}

	
	private boolean localAction(Object target) {
		if (target == componentChoice) {
			int newChoiceIndex = componentChoice.getSelectedIndex();
			if (newChoiceIndex != currentComponentIndex) {
				currentComponentIndex = newChoiceIndex;
				adjustDF(summaryData);
				adjustComponentDisplay((SsqStackedView)ssqView);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}