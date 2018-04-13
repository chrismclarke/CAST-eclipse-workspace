package formula;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import valueList.ValueView;


public class ResultValue extends ValueView {
//	static public final String RESULT_VALUE = "resultValue";
	
	private NumValue maxVal;
	protected MainFormulaPanel parent;
	
	protected boolean showingValue = false;
	
	public ResultValue(DataSet theData, NumValue maxVal, FormulaContext context) {
		super(theData, context.getApplet());
		this.maxVal = maxVal;
		setForeground(context.getColor());
		setFont(context.getFont());
		setCenterValue(true);
	}
	
	protected void setParent(MainFormulaPanel parent) {
		this.parent = parent;
	}

	public void clearResult() {
		showingValue = false;
		redrawValue();
	}

	public void displayResult() {
		showingValue = true;
		redrawValue();
	}
	
	public void changeMaxValue(NumValue maxVal) {
		this.maxVal = maxVal;
		initialised = false;
		invalidate();
	}
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxVal.stringWidth(g);
	}
	
	protected String getValueString() {
		if (showingValue)
			return getResult().toString();
		else
			return null;
	}
	
	public NumValue getResult() {
		if (showingValue)
			return new NumValue(parent.evaluateFormula(), maxVal.decimals);
		else
			return null;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
	public void mousePressed(MouseEvent e) {
		requestFocus();
	}
}