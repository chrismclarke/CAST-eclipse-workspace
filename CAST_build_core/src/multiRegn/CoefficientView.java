package multiRegn;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class CoefficientView extends ValueView {
	private String leftString;
	private String distnKey;
	private NumValue maxVal;
	private int paramIndex;
	
	private double coeffOffset = Double.NaN;
	
	public CoefficientView(DataSet theData, XApplet applet, String distnKey, NumValue maxVal,
																						int paramIndex, String leftString, String rightString) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.maxVal = maxVal;
		this.paramIndex = paramIndex;
		this.leftString = leftString;
		if (rightString != null)
			setUnitsString(rightString);
	}
	
	public void setCoeffOffset(double coeffOffset) {
		this.coeffOffset = coeffOffset;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(leftString);
	}
	
	protected int getLabelAscent(Graphics g) {
		return g.getFontMetrics().getAscent();
	}
	
	protected int getLabelDescent(Graphics g) {
		return g.getFontMetrics().getDescent();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(leftString, startHoriz, baseLine);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxVal.stringWidth(g);
	}
	
	protected String getValueString() {
		MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(distnKey);
		NumValue param = lsModel.getParameter(paramIndex);
		if (!Double.isNaN(coeffOffset)) {
			param = new NumValue(param);
			param.setValue(param.toDouble() + coeffOffset);
		}
		return param.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
