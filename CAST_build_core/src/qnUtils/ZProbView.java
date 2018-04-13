package qnUtils;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;


public class ZProbView extends ProportionView {
//	static public final String Z_VIEW = "zValue";
	
	static public final int BELOW = 0;
	static public final int ABOVE = 1;
	static public final int BETWEEN = 2;
	
	private String s1;
	static final private String s2 = ")  = ";
	
	private String t1;
	static final private String t2 = ")  = ";
	
	static final private String r1 = "P(";
	private String r2;
	static final private String r3 = s2;
	
	static final private String maxZString = "-10.000";
	
	private int intervalType;
	
	private int maxLabelWidth;
	
	public ZProbView(DataSet theData, String variableKey, XApplet applet, int intervalType) {
		super(theData, variableKey, applet);
		this.intervalType = intervalType;
		
		String variableName = theData.getVariable(variableKey).name;
		s1 = "P(" + variableName + " < ";	
		t1 = "P(" + variableName + " > ";
		r2 = " < " + variableName + " < ";
	}
	
	protected int getLabelWidth(Graphics g) {
		if (intervalType == BETWEEN)
			maxLabelWidth = g.getFontMetrics().stringWidth(r1 + maxZString + r2 + maxZString + r3);
		else
			maxLabelWidth = g.getFontMetrics().stringWidth(s1 + maxZString + s2);
		return maxLabelWidth;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		DistnVariable y = (DistnVariable)getVariable(variableKey);
		
		String labelString;
		switch (intervalType) {
			case BELOW:
				NumValue zMaxValue = new NumValue(y.getMaxSelection(), 3);
				labelString = s1 + zMaxValue.toString() + s2;
				break;
			case ABOVE:
				NumValue zMinValue = new NumValue(y.getMinSelection(), 3);
				labelString = t1 + zMinValue.toString() + t2;
				break;
			default:
				zMinValue = new NumValue(y.getMinSelection(), 3);
				zMaxValue = new NumValue(y.getMaxSelection(), 3);
				labelString = r1 + zMinValue.toString() + r2 + zMaxValue.toString() + r3;
				break;
		}
		int labelWidth = g.getFontMetrics().stringWidth(labelString);
		g.drawString(labelString, maxLabelWidth - labelWidth, baseLine);
	}

//--------------------------------------------------------------------------------
	
	protected boolean highlightValue() {
		return true;
	}
	
	public void redrawValue() {
		super.redrawValue();
		repaint();
	}
}
