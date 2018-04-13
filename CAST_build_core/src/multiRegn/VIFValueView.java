package multiRegn;

import java.awt.*;

import dataView.*;
import valueList.*;
import distn.*;


public class VIFValueView extends ValueView {
//	static final private String VIF_VALUEVIEW = "VIFView";
	
	static final private String kLabelString = "VIF =";
	
	private String distnKey;
	private NumValue minSDValue, maxVIF;
	
	private boolean showLabel = true;
	private boolean highlight = true;
	
	public VIFValueView(DataSet theData, XApplet applet,
											String distnKey, NumValue minSDValue, NumValue maxVIF) {
		super(theData, applet);
		this.minSDValue = minSDValue;
		this.maxVIF = maxVIF;
		this.distnKey = distnKey;
	}
	
	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}
	
	public void setHighlightValue(boolean highlight) {
		this.highlight = highlight;
	}

	protected int getMaxValueWidth(Graphics g) {
		return maxVIF.stringWidth(g);
	}
	
	protected String getValueString() {
		NormalDistnVariable slopeTheory = (NormalDistnVariable)getVariable(distnKey);
		double actualSD = slopeTheory.getSD().toDouble();
		double sdif = actualSD  / minSDValue.toDouble();
		double vif = sdif * sdif;
		return new NumValue(vif, maxVIF.decimals).toString();
	}
	
	protected boolean highlightValue() {
		return highlight;
	}
	
	protected int getLabelWidth(Graphics g) {
		return showLabel ? g.getFontMetrics().stringWidth(kLabelString) : 0;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (showLabel)
			g.drawString(kLabelString, startHoriz, baseLine);
	}
}
