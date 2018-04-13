package inference;

import java.awt.*;

import dataView.*;
import valueList.*;
import coreSummaries.*;


public class CoverageValueView extends ValueView {
//	static private final int decimals = 4;
	static private final String kZeroString = "0.0000";
	
	private String variableKey;
	
	private NumValue targetValue;
	
	public CoverageValueView(DataSet theData, String variableKey, XApplet applet, NumValue targetValue) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.targetValue = targetValue;
	}
	
	public void setVariableKey(String variableKey) {
		this.variableKey = variableKey;
		redrawValue();
	}
	
	public void setTarget(NumValue targetValue) {
		this.targetValue = targetValue;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(getLabelString());
	}
	
	private String getLabelString() {
		return getApplet().translate("Propn including") + " " + targetValue.toString() + " =";
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	protected String getValueString() {
		IntervalSummaryVariable variable = (IntervalSummaryVariable)getVariable(variableKey);
		int noOfValues = variable.noOfValues();
		if (noOfValues == 0)
			return null;
		
		ValueEnumeration e = variable.values();
		int coverageCount = 0;
		while (e.hasMoreValues()) {
			IntervalValue nextVal = (IntervalValue)e.nextValue();
			boolean includesTarget = nextVal.lowValue.toDouble() <= targetValue.toDouble()
											&& nextVal.highValue.toDouble() >= targetValue.toDouble();
			if (includesTarget)
				coverageCount ++;
		}
		
		NumValue propn = new NumValue(coverageCount / (double)noOfValues, 4);
		return propn.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(getLabelString(), startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
