package loess;

import java.awt.*;

import dataView.*;
import valueList.*;


public class SelectionValueView extends ValueView {
//	static public final String SELECTION_VALUE = "selectionValue";
	static final private String kEqualsString = " = ";
	
	private String selectedXKey;
	private NumValue maxXValue;
	
	public SelectionValueView(DataSet theData, String selectedXKey, NumValue maxXValue, XApplet applet) {
		super(theData, applet);
		this.selectedXKey = selectedXKey;
		this.maxXValue = maxXValue;
	}

//--------------------------------------------------------------------------------
	
	public NumValue getMaxValue() {
		return maxXValue;
	}
	
	protected int getLabelWidth(Graphics g) {
		CoreVariable xVar = getVariable(selectedXKey);
		return g.getFontMetrics().stringWidth(xVar.name + kEqualsString);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseline) {
		CoreVariable xVar = getVariable(selectedXKey);
		g.drawString(xVar.name + kEqualsString, startHoriz, baseline);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxXValue.stringWidth(g);
	}
	
	protected String getValueString() {
		double value = getValue();
		if (Double.isNaN(value))
			return null;
		else
			return new NumValue(value, maxXValue.decimals).toString();
	}
	
	public double getValue() {
		SelectionVariable selectedXVar = (SelectionVariable)getVariable(selectedXKey);
		return selectedXVar.getMinSelection();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
