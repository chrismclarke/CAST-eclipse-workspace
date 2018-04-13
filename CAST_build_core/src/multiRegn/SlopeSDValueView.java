package multiRegn;

import java.awt.*;

import dataView.*;
import valueList.*;
import distn.*;


public class SlopeSDValueView extends ValueView {
//	static final private String SLOPE_SD_VALUEVIEW = "SlopeSDView";
	
	private String distnKey;
	private NumValue maxSD;
	
	private String label;
	private boolean showLabel = true;
	private boolean highlight = true;
	
	public SlopeSDValueView(DataSet theData, XApplet applet, String distnKey, NumValue maxSD) {
		super(theData, applet);
		this.maxSD = maxSD;
		this.distnKey = distnKey;
		label = applet.translate("sd of") + " " + getVariable(distnKey).name + " =";
	}
	
	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}
	
	public void setHighlightValue(boolean highlight) {
		this.highlight = highlight;
	}

	protected int getMaxValueWidth(Graphics g) {
		return maxSD.stringWidth(g);
	}
	
	protected String getValueString() {
		NormalDistnVariable slopeTheory = (NormalDistnVariable)getVariable(distnKey);
		return slopeTheory.getSD().toString();
	}
	
	protected boolean highlightValue() {
		return highlight;
	}
	
	protected int getLabelWidth(Graphics g) {
		return showLabel ? g.getFontMetrics().stringWidth(label) : 0;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (showLabel)
			g.drawString(label, startHoriz, baseLine);
	}
}
