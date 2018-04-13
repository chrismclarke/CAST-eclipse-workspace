package valueList;

import java.awt.*;

import dataView.*;


public class OneValueView extends ValueView {
	
	protected String variableKey;
	private boolean nameDraw = true;
	private String label = null;
	
	private Value maxValue;
	
	private boolean highlightSelection = false;
	
	public OneValueView(DataSet theData, String variableKey, XApplet applet, Value maxValue) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.maxValue = maxValue;
	}
	
	public OneValueView(DataSet theData, String variableKey, XApplet applet) {
		this(theData, variableKey, applet, null);
	}
	
	public void setVariableKey(String variableKey) {
		this.variableKey = variableKey;
		redrawAll();
	}
	
	public void setNameDraw(boolean nameDraw) {
		this.nameDraw = nameDraw;
	}
	
	public void setLabel(String label) {
		this.label = label;
		repaint();
	}
	
	public void setHighlightSelection(boolean highlightSelection) {
		this.highlightSelection = highlightSelection;
	}
	
	public void reset(Value maxValue) {
		this.maxValue = maxValue;
		initialised = false;
		invalidate();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		if (nameDraw) {
			String nameString = (label == null) ? getVariable(variableKey).name : label;
			return g.getFontMetrics().stringWidth(nameString);
		}
		else
			return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (maxValue != null)
			return maxValue.stringWidth(g);
		else {
			CoreVariable variable = getVariable(variableKey);
			if (variable instanceof Variable)
				return ((Variable)variable).getMaxWidth(g);
			else
				return 0;
		}
	}
	
	protected Value getSelectedValue(String key) {
		CoreVariable variable = getVariable(key);
		if (!(variable instanceof Variable))
			return null;
		
		int selectedIndex = getSelection().findSingleSetFlag();
//		System.out.println("Getting selected value for " + variable.name + ": selectedIndex = " + selectedIndex);
		if (selectedIndex < 0)
			return null;
		else
			return ((Variable)variable).valueAt(selectedIndex);
	}
	
	private Value getSelectedValue() {
		return getSelectedValue(variableKey);
	}
	
	public double getValue() {
		Value selectedValue = getSelectedValue();
		if (selectedValue == null || !(selectedValue instanceof NumValue))
			return Double.NaN;
		else
			return ((NumValue)selectedValue).toDouble();
	}
	
	protected String getValueString() {
		Value selectedValue = getSelectedValue();
		if (selectedValue != null)
			return selectedValue.toString();
		else
			return null;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (nameDraw) {
			String nameString = (label == null) ? getVariable(variableKey).name : label;
			g.drawString(nameString, startHoriz, baseLine);
		}
	}
	
	protected boolean highlightValue() {
		if (!highlightSelection)
			return false;
		int selectedIndex = getSelection().findSingleSetFlag();
		return selectedIndex >= 0;
	}
	

//--------------------------------------------------------------------------------
	
	protected String getInvertedValueString(Variable variable, Flags selection,
																							int index) {
		if (selection.valueAt(index)) {
			Value val = variable.valueAt(index);
			return (val == null) ? null : val.toString();
		}
		else
			return null;
	}
	
}
