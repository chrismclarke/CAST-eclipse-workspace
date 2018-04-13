package inference;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class FirstValueView extends ValueView {
//	static public final String FIRST_VALUE_VIEW = "firstValue";
	
	private String variableKey;
	private String labelString = "Interval estimate is";
	
	public FirstValueView(DataSet theData, String variableKey, XApplet applet) {
		super(theData, applet);
		this.variableKey = variableKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		CoreVariable variable = getVariable(variableKey);
		if (variable instanceof Variable)
			return ((Variable)variable).getMaxWidth(g);
		else
			return 0;
	}
	
	protected String getValueString() {
		CoreVariable variable = getVariable(variableKey);
		if (!(variable instanceof Variable))
			return null;
		
		Value firstValue = ((Variable)variable).valueAt(0);
		
		if (firstValue != null)
			return firstValue.toString();
		else
			return null;
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
