package pairBlockProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;
import multiRegn.*;
import pairBlock.*;


public class BlockTreatComponent2Applet extends ResidComponent2Applet {
	
	private XChoice componentChoice;
	private int currentComponent = 0;
	
	
	public void setupApplet() {
		super.setupApplet();
		
		add("East", keyPanel(data));
	}
	
	protected BlockTreatComponent2View getDataView(MultiRegnDataSet data, HorizAxis yAxis,
																														MultiVertAxis treatBlockAxis) {
		BlockTreatComponent2View theView = new BlockTreatComponent2View(data, this, yAxis,
																										treatBlockAxis, "y", "x", "z");
		theView.setShowResidNotExplained(false);
		theView.setTransitionType(BlockTreatComponent2View.BLOCK_TO_TREAT);
		return theView;
	}
	
	private XPanel keyPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		thePanel.add(new CatVariableKey(data, this, "z"));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(30, 8, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
			componentChoice = new XChoice(this);
			componentChoice.addItem(translate("Treatment sum of squares"));
			componentChoice.addItem(translate("Block sum of squares"));
		
		thePanel.add(componentChoice);
			
		thePanel.add(ssqEquationPanel(data, 2));
		
		return thePanel;
	}
	
	protected void checkControlEnabling(BlockTreatComponent2View c, int transitionType,
																																	int transitionStage) {
		if (transitionStage == BlockTreatComponent2View.MIDDLE)
			componentChoice.disable();
		else
			componentChoice.enable();
	}

	
	private boolean localAction(Object target) {
		if (target == componentChoice) {
			int newDisplay = componentChoice.getSelectedIndex();
			if (newDisplay != currentComponent) {
				currentComponent = newDisplay;
				
//				BlockTreatComponent2View c = componentPlot;
				componentPlot.animateTransition(BlockTreatComponent2View.BLOCK_TO_TREAT);
				
				ssqEquation.highlightComponent(newDisplay == 0 ? 2 : 1);
			
				checkEnabling();
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