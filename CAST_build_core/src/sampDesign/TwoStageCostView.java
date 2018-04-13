package sampDesign;

import java.awt.*;

import dataView.*;
import valueList.*;


public class TwoStageCostView extends ProportionView {
	static public final String TWO_STAGE_COST_PLOT = "twoStageCostPlot";
	
	private String kLabelString;
	
	private NumValue primaryCost, secondaryCost;
	private NumValue maxCost, tempCost;
	
	public TwoStageCostView(DataSet theData, XApplet applet, String yKey, NumValue primaryCost,
																							NumValue secondaryCost, NumValue maxCost) {
		super(theData, yKey, applet);
		this.primaryCost = primaryCost;
		this.secondaryCost = secondaryCost;
		this.maxCost = maxCost;
		kLabelString = applet.translate("Total cost") + " =";
		
		tempCost = new NumValue(0.0, maxCost.decimals);
		setLabel(kLabelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxCost.stringWidth(g);
	}
	
	protected String getValueString() {
		Sample2StageVariable yVar = (Sample2StageVariable)getData().getVariable(variableKey);
		
		tempCost.setValue(primaryCost.toDouble() * yVar.getPrimarySampleSize()
															+ secondaryCost.toDouble() * yVar.getSampleSize());
		
		return tempCost.toString();
	}
}
	
