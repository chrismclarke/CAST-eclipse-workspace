package regn;

import java.awt.*;

import dataView.*;
import valueList.*;
import models.*;


public class ModelSDView extends ValueView {
//	static public final String MODEL_SD = "modelSD";
	
	protected String modelKey;
//	private int decimals;
	private String labelString;
	
	private NumValue maxMeanRss;
	
	public ModelSDView(DataSet theData, XApplet applet, String modelKey, NumValue maxMeanRss,
																																	String labelString) {
		super(theData, applet);
		this.modelKey = modelKey;
		this.maxMeanRss = maxMeanRss;
		this.labelString = labelString;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxMeanRss.stringWidth(g);
	}
	
	protected String getValueString() {
		LinearModel lsModel = (LinearModel)getData().getVariable(modelKey);
		return lsModel.evaluateSD().toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
