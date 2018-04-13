package pairBlockProg;

import java.awt.*;

import dataView.*;
import utils.*;

import pairBlock.*;


public class PairingCovarApplet extends RandomisedCovarApplet {
	static final private String PAIRED_TEXT_PARAM = "pairedText";
	
	private XChoice designChoice;
	private int currentDesign = 0;
	
	
	protected XPanel leftControlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			designChoice = new XChoice(this);
			designChoice.addItem(translate("Completely randomised"));
			designChoice.addItem(getParameter(PAIRED_TEXT_PARAM));
			
		thePanel.add(designChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesign) {
				currentDesign = newChoice;
				
				theView.setShowPairBands(newChoice == 1);
				covarView.setShowPairBands(newChoice == 1);
				
				FactorAllocationVariable factorVar = (FactorAllocationVariable)data.getVariable("factor");
				factorVar.setMatchedByCovariate(newChoice == 1);
				
				summaryData.setAccumulate(false);
				summaryData.takeSample();
				summaryData.setAccumulate(accumulateCheck.getState());
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