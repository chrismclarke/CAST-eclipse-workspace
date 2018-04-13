package exper2;

import java.awt.*;

import dataView.*;
import models.*;
import valueList.ValueView;


public class RawMeanView extends ValueView {
	private Value label;
	protected String xKey, lsKey;
	protected NumValue maxVal, tempVal;
	protected int xIndex;
	
	public RawMeanView(DataSet theData, XApplet applet, String xKey, int xIndex,
																									String lsKey, NumValue maxVal) {
		super(theData, applet);
		this.xKey = xKey;
		this.xIndex = xIndex;
		this.lsKey = lsKey;
		this.maxVal = maxVal;
		tempVal = new NumValue(maxVal);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		label = xVar.getLabel(xIndex);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return label.stringWidth(g);
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		label.drawRight(g, startHoriz, baseLine);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxVal.stringWidth(g);
	}
	
	protected String getValueString() {
		MultipleRegnModel ls = (MultipleRegnModel)getVariable(lsKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		Value treatVal[] = {xVar.getLabel(xIndex)};
		
		double mean = ls.evaluateMean(treatVal);
		tempVal.setValue(mean);
		return tempVal.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
