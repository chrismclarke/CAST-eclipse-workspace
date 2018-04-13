package randomStat;

import java.awt.*;

import dataView.*;
import valueList.*;
import distn.*;


public class DiscreteProbValueView extends ValueView {
//	static public final String DISCRETE_PROB_VIEW = "discreteProb";
//	static private final int decimals = 4;
	static private final String kZeroString = "0.0000";
	
	private String variableKey;
	private int maxN;
	private int maxLabelWidth;
	
	private boolean printWithHalves = true;
	
	public DiscreteProbValueView(DataSet theData, String variableKey, XApplet applet, int maxN) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.maxN = maxN;
	}
	
	public void setPrintWithHalves(boolean printWithHalves) {
		this.printWithHalves = printWithHalves;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		String maxLabel = getMaxLabelString();
		maxLabelWidth = g.getFontMetrics().stringWidth(maxLabel);
		return maxLabelWidth;
	}
	
	protected String getMaxLabelString() {
		return (printWithHalves ? "P(X < .5) = " : "P(X \u2264 ) = ") + String.valueOf(maxN);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroString);
	}
	
	public double getValue() {
		DistnVariable xVar = (DistnVariable)getVariable(variableKey);
		double dLow = xVar.getMinSelection();
		double dHigh = xVar.getMaxSelection();
		
		double prob;
		
		if (Double.isInfinite(dLow) && Double.isInfinite(dHigh) && dLow < 0 && dHigh < 0)
			return Double.NaN;
		if (Double.isInfinite(dLow)) {
			if (Double.isInfinite(dHigh)) {
				if (dLow < 0.0 && dHigh > 0.0)
					prob = 1.0;
				else
					return 0.0;
			}
			else {
				if (dLow < 0.0)
					prob = xVar.getCumulativeProb(dHigh);
				else
					prob = 0.0;
			}
		}
		else if (Double.isInfinite(dHigh)) {
			if (dHigh < 0.0)
				prob = 0.0;
			else
				prob = 1.0 - xVar.getCumulativeProb(dLow);
		}
		else {
			if (xVar instanceof DiscreteDistnVariable) {
				DiscreteDistnVariable xDVar = (DiscreteDistnVariable)xVar;
				int xLow = (int)Math.round(Math.ceil(dLow));
				int xHigh = (int)Math.round(Math.floor(dHigh));
				prob = 0.0;
				for (int x=xLow ; x<=xHigh ; x++)
					prob += xDVar.getScaledProb(x);
				prob *= xDVar.getProbFactor();
			}
			else										//		also used for normal approximation
				prob = xVar.getCumulativeProb(dHigh) - xVar.getCumulativeProb(dLow);
		}
		return prob;
	}
	
	protected String getValueString() {
		double prob = getValue();
		if (Double.isNaN(prob))
			return null;
		NumValue pVal = new NumValue(prob, 4);
		return pVal.toString();
	}
	
	protected String getLabelString() {
		DistnVariable xVar = (DistnVariable)getVariable(variableKey);
		double dLow = xVar.getMinSelection();
		double dHigh = xVar.getMaxSelection();
		
		String label;
		if (!Double.isInfinite(dLow) && !Double.isInfinite(dHigh)) {
			int xLow = (int)Math.round(Math.ceil(dLow));
			int xHigh = (int)Math.round(Math.floor(dHigh));
			if (xLow == xHigh && xVar instanceof DiscreteDistnVariable)
				label = "P(X = " + String.valueOf(xLow) + ") = ";
			else
				label = "P(" + String.valueOf(xLow - 1) + ".5 < X < " + String.valueOf(xHigh) + ".5) = ";
		}
		else if (Double.isInfinite(dLow) && dLow < 0.0) {
			if (Double.isInfinite(dHigh))
				label = "";
			else {
				int xHigh = (int)Math.round(Math.floor(dHigh));
				label = printWithHalves ? "P(X < " + String.valueOf(xHigh) + ".5) = "
												: "P(X \u2264 " + String.valueOf(xHigh) + ") = ";
			}
		}
		else if (Double.isInfinite(dHigh) && !Double.isInfinite(dLow)) {
			int xLow = (int)Math.round(Math.ceil(dLow));
			label = printWithHalves ? "P(X > " + String.valueOf(xLow - 1) + ".5) = "
											: "P(X \u2264 " + String.valueOf(xLow) + ") = ";
		}
		else
			label = "???";
		return label;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		String label = getLabelString();
		
		int drawStart = startHoriz + maxLabelWidth - g.getFontMetrics().stringWidth(label);
		g.drawString(label, drawStart, baseLine);
	}

//--------------------------------------------------------------------------------
	
	protected boolean highlightValue() {
		return toString() != null;
	}
	
	public void redrawValue() {
		super.redrawValue();
		repaint();
	}
}
