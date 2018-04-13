package twoGroup;

import java.awt.*;

import dataView.*;
import valueList.*;


public class DistnMeanSdView extends ValueView {
	static final public int MEAN = 0;
	static final public int SD = 1;
	
	private String modelKey;
	private int paramType;
	private String labelString;
	
	private NumValue maxMeanSd;
	
	public DistnMeanSdView(DataSet theData, XApplet applet, String modelKey, int paramType,
																								String labelString, NumValue maxMeanSd) {
		super(theData, applet);
		this.modelKey = modelKey;
		this.paramType = paramType;
		this.labelString = labelString;
		this.maxMeanSd = maxMeanSd;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (labelString == null) ? 0 : g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxMeanSd.stringWidth(g);
	}
	
	protected String getValueString() {
		DistnVariable distnVar = (DistnVariable)getData().getVariable(modelKey);
		NumValue param = (paramType == MEAN) ? distnVar.getMean() : distnVar.getSD();
		return param.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (labelString != null)
			g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
