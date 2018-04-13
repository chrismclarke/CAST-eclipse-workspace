package survey;

import java.awt.*;

import dataView.*;


public class PopSampMeanView extends PopSampProportionView {
	
	private NumValue maxMean;
	private String freqKey = null;
	
	public PopSampMeanView(DataSet theData, XApplet applet, int popSampError,
																														NumValue maxMean) {
		super(theData, applet, popSampError);
		this.maxMean = maxMean;
	}
	
	public PopSampMeanView(DataSet theData, XApplet applet, int popSampError,
																	NumValue maxMean, String yKey, String freqKey) {
		this(theData, applet, popSampError, maxMean);
		setActiveNumVariable(yKey);
		this.freqKey = freqKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return maxMean.stringWidth(g);
	}
	
	protected NumValue getPopnSummary() {
		int n = 0;
		double sy = 0.0;
		
		NumVariable v = getNumVariable();
		ValueEnumeration e = v.values();
		
		while (e.hasMoreValues()) {
			sy += e.nextDouble();
			n ++;
		}
		return new NumValue(sy / n, maxMean.decimals);
	}
	
	protected NumValue getSampleSummary() {
		int n = 0;
		double sy = 0.0;
		
		NumVariable v = getNumVariable();
		ValueEnumeration e = v.values();
		
		if (freqKey == null) {
			FlagEnumeration fe = getSelection().getEnumeration();
			while (e.hasMoreValues()) {
				double y = e.nextDouble();
				boolean nextSel = fe.nextFlag();
				if (nextSel) {
					sy += y;
					n ++;
				}
			}
		}
		else {
			FreqVariable fVar = (FreqVariable)getVariable(freqKey);
			ValueEnumeration fe = fVar.values();
			while (e.hasMoreValues()) {
				double y = e.nextDouble();
				FreqValue f = (FreqValue)fe.nextValue();
				sy += y * f.intValue;
				n += f.intValue;
			}
		}
		
		if (n == 0)
			return null;
		else
			return new NumValue(sy / n, maxMean.decimals);
	}
}
