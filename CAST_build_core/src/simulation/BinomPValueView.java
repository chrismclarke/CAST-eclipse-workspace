package simulation;

import java.awt.*;

import dataView.*;
import valueList.*;

import randomStat.*;


public class BinomPValueView extends ValueView {
//	static final public String BINOM_PVALUE_PROB = "binomPValue";
	
	static private final String kZeroString = "0.0000";
	
	private String kPValueString;
	
	private DiscreteProbValueView probView;
	
	public BinomPValueView(DataSet theData, XApplet applet, DiscreteProbValueView probView) {
		super(theData, applet);
		this.probView = probView;
		kPValueString = applet.translate("p-value") + " = ";
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kPValueString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	protected String getValueString() {
		double pValue = Math.min(1.0, 2.0 * probView.getValue());
		
		NumValue pVal = new NumValue(pValue, 4);
		return pVal.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(kPValueString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return true;
	}
}