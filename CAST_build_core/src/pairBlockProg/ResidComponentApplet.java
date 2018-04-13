package pairBlockProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import multiRegn.*;
import ssq.*;
import pairBlock.*;

public class ResidComponentApplet extends BlockTreatComponentApplet {
	
	static final private int kAllComponents[] = {1, 2, 3};
	static final private int kTreatResidComponents[] = {2, 3};
	static final private int kResidComponent[] = {3};
	
	private XCheckbox blockEffectCheck, treatEffectCheck;
	
	protected TwoTreatDataSet readData() {
		TwoTreatDataSet data = super.readData();
		
		NumVariable rawY = (NumVariable)data.getVariable("y");
		RemoveBlockVariable adjY = new RemoveBlockVariable(rawY.name, data, "y",
																																				"z", "x", -1);
		data.addVariable("adjustedY", adjY);
		return data;
	}
	
	protected BlockTreatComponentView getComponentView(MultiRegnDataSet data, HorizAxis yAxis,
																											VertAxis treatAxis) {
		return new BlockTreatComponentView(data, this, yAxis, treatAxis, "adjustedY", "x", "z",
											BlockTreatComponentView.TOTAL_COMPONENT_DISPLAY, BlockTreatComponentView.OVERALL_MEAN_ONLY);
	}
	
	protected XPanel controlPanel(MultiRegnDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
			
			XPanel checkPanel = new InsetPanel(30, 0, 0, 0);
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
			
				blockEffectCheck = new XCheckbox("Remove block effect", this);
			checkPanel.add(blockEffectCheck);
		
				treatEffectCheck = new XCheckbox("Remove treatment effect", this);
				treatEffectCheck.disable();
			checkPanel.add(treatEffectCheck);
		
		thePanel.add(checkPanel);
		
			AnovaImages.loadBlockImages(this);
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			ssqEquation = new ComponentEqnPanel(summaryData, SeqXZComponentVariable.kZXComponentKey, 
							maxSsq, AnovaImages.blockSsqs, SeqXZComponentVariable.kComponentColor,
							AnovaImages.kBlockSsqWidth, AnovaImages.kBlockSsqHeight, bigContext);
			ssqEquation.highlightComponent(ComponentEqnPanel.combineComponents(kAllComponents));
		thePanel.add(ssqEquation);
		
		return thePanel;
	}
	
	public void finishedAnimation(boolean animateToRemove, boolean removeBlocksNotTreats) {
		if (!animateToRemove && removeBlocksNotTreats)
			blockEffectCheck.enable();
		else if (animateToRemove && removeBlocksNotTreats
																				|| !animateToRemove && !removeBlocksNotTreats) {
			blockEffectCheck.enable();
			treatEffectCheck.enable();
		}
		else
			treatEffectCheck.enable();
		
		componentPlot.changeComponentDisplay(BlockTreatComponentView.TOTAL_COMPONENT_DISPLAY,
																			BlockTreatComponentView.OVERALL_MEAN_ONLY);
	}

	
	private boolean localAction(Object target) {
		if (target == blockEffectCheck) {
			int newComponents[] = blockEffectCheck.getState() ? kTreatResidComponents : kAllComponents;
			ssqEquation.highlightComponent(ComponentEqnPanel.combineComponents(newComponents));
			
			blockEffectCheck.disable();
			treatEffectCheck.disable();
		
			componentPlot.changeComponentDisplay(BlockTreatComponentView.BLOCK_COMPONENT_DISPLAY,
																											BlockTreatComponentView.BLOCK_MEAN);
			componentPlot.animateRemoveBlock(blockEffectCheck.getState());
			return true;
		}
		else if (target == treatEffectCheck) {
			int newComponents[] = treatEffectCheck.getState() ? kResidComponent : kTreatResidComponents;
			ssqEquation.highlightComponent(ComponentEqnPanel.combineComponents(newComponents));
			
			blockEffectCheck.disable();
			treatEffectCheck.disable();
			componentPlot.changeComponentDisplay(BlockTreatComponentView.TREAT_COMPONENT_DISPLAY,
																											BlockTreatComponentView.TREAT_MEAN);
			componentPlot.animateRemoveTreat(treatEffectCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}