package sampling;

import java.awt.*;

import dataView.*;
import valueList.*;


public class CatProportionView extends ValueView {
//	static public final String CAT_PROPORTION_VIEW = "catProportionValue";
	
	static final public int PROPORTION = 0;
	static final public int PERCENTAGE = 1;
	
	protected String catKey;
	private int catIndex;
	
	private int displayType;
	private int propnDecimals;
	
	private String labelString;
	
	public CatProportionView(DataSet theData, XApplet applet, String catKey, int catIndex, int displayType,
																																	int propnDecimals) {
		super(theData, applet);
		labelString = applet.translate("proportion") + " =";
		this.catKey = catKey;
		this.displayType = displayType;
		this.propnDecimals = propnDecimals;
		this.catIndex = catIndex;
		if (displayType == PERCENTAGE)
			setUnitsString("%");
	}
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		NumValue maxVal = new NumValue(1.0, propnDecimals);
		if (displayType == PERCENTAGE) {
			maxVal.setValue(maxVal.toDouble() * 100);
			maxVal.decimals -= 2;
		}
		return maxVal.stringWidth(g);
	}
	
	protected String getValueString() {
		CatVariable catVar = (CatVariable)getVariable(catKey);
		int[] counts = catVar.getCounts();
		
		double proportion = counts[catIndex] / (double)catVar.noOfValues();
		
		NumValue value = new NumValue(proportion, propnDecimals);
		if (displayType == PERCENTAGE) {
			value.setValue(value.toDouble() * 100);
			value.decimals -= 2;
		}
		
		return value.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
