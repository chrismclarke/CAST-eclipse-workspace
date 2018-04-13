package normal;

import java.awt.*;
import javax.swing.*;

import dataView.*;
import valueList.ProportionView;


public class NormalCalcView extends ProportionView {
	static public final int BELOW = 0;
	static public final int ABOVE = 1;
	static public final int BETWEEN = 2;
	
	static final private String s1 = "P(X < ";
	static final private String s2 = ")  =  P(Z < ";
	static final private String s3 = ")  = ";
	
	static final private String t1 = "P(X > ";
	static final private String t2 = ")  =  P(Z > ";
	static final private String t3 = ")  = ";
	
	static final private String r1 = "P(";
	static final private String r2 = " < X < ";
	static final private String r3 = ")  =  P(";
	static final private String r4 = " < Z > ";
	static final private String r5 = ")  = ";
	
	static final private String maxXString = "1234.567";
	static final private String maxZString = "-10.000";
	
	private JTextField meanField, sdField, x1Field, x2Field;
	private int intervalType;
	
	private int maxLabelWidth;
	
	public NormalCalcView(DataSet theData, String variableKey,
										XApplet applet, JTextField meanField, JTextField sdField,
										JTextField x1Field, JTextField x2Field, int intervalType) {
		super(theData, variableKey, applet);
		this.meanField = meanField;
		this.sdField = sdField;
		this.x1Field = x1Field;
		this.x2Field = x2Field;
		this.intervalType = intervalType;
	}
	
	protected int getLabelWidth(Graphics g) {
		if (intervalType == BETWEEN)
			maxLabelWidth = g.getFontMetrics().stringWidth(r1 + maxXString + r2 + maxXString
																		+ r3 + maxZString + r4 + maxZString + r5);
		else
			maxLabelWidth = g.getFontMetrics().stringWidth(s1 + maxXString + s2 + maxZString + s3);
		return maxLabelWidth;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		double x1Value = Double.parseDouble(x1Field.getText());
		double meanValue = Double.parseDouble(meanField.getText());
		double sdValue = Double.parseDouble(sdField.getText());
		
		NumValue z1Value = new NumValue((x1Value - meanValue) / sdValue, 3);
		String labelString;
		switch (intervalType) {
			case BELOW:
				labelString = s1 + x1Field.getText() + s2 + z1Value.toString() + s3;
				break;
			case ABOVE:
				labelString = t1 + x1Field.getText() + t2 + z1Value.toString() + t3;
				break;
			default:
				double x2Value = Double.parseDouble(x2Field.getText());
				NumValue z2Value = new NumValue((x2Value - meanValue) / sdValue, 3);
				labelString = r1 + x1Field.getText() + r2 + x2Field.getText()
										+ r3 + z1Value.toString() + r4 + z2Value.toString() + r5;
				break;
		}
		int labelWidth = g.getFontMetrics().stringWidth(labelString);
		g.drawString(labelString, maxLabelWidth - labelWidth, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}

//--------------------------------------------------------------------------------
	
	public void redrawValue() {
		redrawAll();								//	to ensure that label gets redrawn when selection changes
	}

//--------------------------------------------------------------------------------
}
