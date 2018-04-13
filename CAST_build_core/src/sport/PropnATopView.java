package sport;

import java.awt.*;

import dataView.*;
import valueList.*;


public class PropnATopView extends ValueView {
	
	protected String variableKey;
	private String label;
//	private int probType;
	
	private NumValue maxValue = new NumValue(1.0, 4);
	
	public PropnATopView(DataSet theData, String variableKey, XApplet applet, String label) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.label = label;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(label);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		TeamARankVariable rankVar = (TeamARankVariable)getVariable(variableKey);
		
		int winCount = 0;
		int totalCount = 0;
		ValueEnumeration e = rankVar.values();
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			if (nextVal.toDouble() < 1.5)
				winCount ++;
			totalCount ++;
		}
		if (totalCount == 0)
			return null;
		else
			return (new NumValue(((double)winCount) / totalCount, maxValue.decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(label, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
