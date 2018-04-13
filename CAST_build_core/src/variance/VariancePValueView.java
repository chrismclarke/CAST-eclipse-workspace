package variance;

import java.awt.*;

import dataView.*;
import valueList.*;
import distn.*;


public class VariancePValueView extends ValueView {
	static private final String kZeroString = "0.0000";
	
	private String fKey, fDistnKey;
	private String labelString;
	
	public VariancePValueView(DataSet theData, String fKey, String fDistnKey, XApplet applet) {
		super(theData, applet);
		labelString = applet.translate("p-value") + " =";
		this.fKey = fKey;
		this.fDistnKey = fDistnKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	protected String getValueString() {
		NumVariable fVar = (NumVariable)getVariable(fKey);
		
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex < 0)
			return null;
			
		double f = fVar.doubleValueAt(selectedIndex);
		
		ContinDistnVariable fDistn = (ContinDistnVariable)getVariable(fDistnKey);
		double cumProb = fDistn.getCumulativeProb(f);
		double pValue = 2.0 * Math.min(cumProb, 1.0 - cumProb);
		
		return new NumValue(pValue, 4).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
	

//--------------------------------------------------------------------------------
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}
}
