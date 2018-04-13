package survey;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class PopSampProportionView extends ValueView {
	static final public int POPN = 0;
	static final public int SAMPLE = 1;
	static final public int ERROR = 2;
	
	static private final int decimals = 4;
	static private final String kZeroString = "-0.0000";
	
	private String kPopulationString, kSampleString, kErrorString;
	
	protected int popSampError;
	
	private boolean doHighlight = false;
	
	public PopSampProportionView(DataSet theData, XApplet applet, int popSampError) {
		super(theData, applet);
		
		kPopulationString = applet.translate("Popn");
		kSampleString = applet.translate("Sample");
		kErrorString = applet.translate("Error");
		
		this.popSampError = popSampError;
	}
	
	public void setHighlight(boolean doHighlight) {
		this.doHighlight = doHighlight;
		repaint();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return Math.max(fm.stringWidth(kPopulationString), Math.max(fm.stringWidth(kSampleString), fm.stringWidth(kErrorString)));
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	protected NumValue getPopnSummary() {
		CatVariable v = getCatVariable();
		Value successVal = v.getLabel(0);
		
		int n = 0;
		int x = 0;
		
		ValueEnumeration e = v.values();
		
		while (e.hasMoreValues()) {
			Value nextVal = e.nextValue();
			if (nextVal == successVal)
				x ++;
			n ++;
		}
		
		return new NumValue(x / (double)n, decimals);
	}
	
	protected NumValue getSampleSummary() {
		CatVariable v = getCatVariable();
		Value successVal = v.getLabel(0);
		
		int n = 0;
		int x = 0;
		
		FlagEnumeration fe = getSelection().getEnumeration();
		ValueEnumeration e = v.values();
		
		while (e.hasMoreValues()) {
			Value nextVal = e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				if (nextVal == successVal)
					x ++;
				n ++;
			}
		}
		if (n == 0)
			return null;
		else
			return new NumValue(x / (double)n, decimals);
	}
	
	protected String getValueString() {
		if (!isEnabled())
			return "";
		
		switch (popSampError) {
			case POPN:
				return getPopnSummary().toString();
			case SAMPLE:
				return getSampleSummary().toString();
			case ERROR:
				NumValue error = getSampleSummary();
				if (error != null) {
					error.setValue(error.toDouble() - getPopnSummary().toDouble());
					return error.toString();
				}
		}
		return "";		//	should never happen
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		String label = (popSampError == POPN) ? kPopulationString
											: (popSampError == SAMPLE) ? kSampleString
											: kErrorString;
		g.drawString(label, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return doHighlight && isEnabled() && (popSampError != POPN);
	}
}
