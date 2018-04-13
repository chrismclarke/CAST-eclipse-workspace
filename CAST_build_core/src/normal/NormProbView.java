package normal;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class NormProbView extends ValueView {
//	static public final String NORM_PROB_VIEW = "normalProbValue";
	
	protected String distnKey;
	protected NumValue biggestX;
	
	private boolean roundX = false;
	
	public NormProbView(DataSet theData, XApplet applet, String distnKey, NumValue biggestX) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.biggestX = biggestX;
	}
	
	public void setRoundX(boolean roundX) {
		this.roundX = roundX;
	}

//--------------------------------------------------------------------------------
	
	public void redrawValue() {
		redrawAll();								//	to ensure that label gets redrawn when selection changes
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth("0.000");
	}
	
	private double roundLimit(double value, int decimals) {
		double temp = value;
		if (decimals >0) {
			for (int i=0 ; i<decimals ; i++)
				temp *= 10.0;
			temp = Math.rint(temp);
			for (int i=0 ; i<decimals ; i++)
				temp /= 10.0;
		}
		return temp;
	}
	
	protected String getValueString() {
		DistnVariable dv = (DistnVariable)getVariable(distnKey);
		
		double low = dv.getMinSelection();
		double high = dv.getMaxSelection();
		if (roundX) {
			if (!Double.isInfinite(low))
				low = roundLimit(low, biggestX.decimals);
			if (!Double.isInfinite(high))
				high = roundLimit(high, biggestX.decimals);
		}
		
		double p = dv.getCumulativeProb(high) - dv.getCumulativeProb(low);
		NumValue propn = new NumValue(p, 3);
		return propn.toString();
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth("P(X \u2264 " + biggestX.toString() + ") = ");
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int theoryLength = getLabelWidth(g);
		
		DistnVariable dv = (DistnVariable)getVariable(distnKey);
		NumValue xValue = new NumValue(dv.getMaxSelection(), biggestX.decimals);
		String labelString = "P(X \u2264 " + xValue.toString() + ") = ";
		int labelLength = g.getFontMetrics().stringWidth(labelString);
		g.drawString(labelString, startHoriz + theoryLength - labelLength, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
