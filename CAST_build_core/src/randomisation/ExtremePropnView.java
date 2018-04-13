package randomisation;

import java.awt.*;

import dataView.*;
import valueList.*;


public class ExtremePropnView extends ValueView {
//	static final public String EXTREME_PROPN_VIEW = "extremePropn";
	
	static final private NumValue kZero = new NumValue(0.0, 3);
	
	private String labelString;
	
	private String variableKey;
	private double lowLimit, highLimit;
	
	private boolean doHighlight = false;
	
	public ExtremePropnView(DataSet theData, XApplet applet, String variableKey, double lowLimit, double highLimit) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.lowLimit = lowLimit;
		this.highLimit = highLimit;
	}
	
	public ExtremePropnView(DataSet theData, XApplet applet, String variableKey, double absLimit) {
		this(theData, applet, variableKey, -absLimit, absLimit);
	}
	
	public void setHighlight(boolean doHighlight) {
		this.doHighlight = doHighlight;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		if (labelString == null)
			labelString = getApplet().translate("propn as extreme") + " =";
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return kZero.stringWidth(g);
	}
	
	protected String getValueString() {
		int total = 0;
		int extreme = 0;
		
		NumVariable variable = (NumVariable)getVariable(variableKey);
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double y = e.nextDouble();
			if (y <= lowLimit || y >= highLimit)
				extreme ++;
			total ++;
		}
		
		NumValue propn = new NumValue(extreme / (double)total, kZero.decimals);
		return propn.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (labelString == null)
			labelString = getApplet().translate("propn as extreme") + " =";
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return doHighlight;
	}
	

//--------------------------------------------------------------------------------
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}
}
