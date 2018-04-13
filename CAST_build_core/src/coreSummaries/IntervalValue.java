package coreSummaries;

import java.awt.*;

import dataView.*;



public class IntervalValue extends Value {
	static final private String kToString = " to ";
	
	private double mean, sd;
	public NumValue lowValue, highValue;
	
	public IntervalValue(double low, double high, int theDecimals) {
		lowValue = new NumValue(low, theDecimals);
		highValue = new NumValue(high, theDecimals);
	}
	
	public IntervalValue(double mean, double sd, double tValue, int theDecimals) {
		this.mean = mean;
		this.sd = sd;
		lowValue = new NumValue(mean - tValue * sd, theDecimals);
		highValue = new NumValue(mean + tValue * sd, theDecimals);
	}

	public String toString() {
		return lowValue.toString() + kToString + highValue.toString();
	}
	
	public void drawAtPoint(Graphics g, int x, int y) {
		lowValue.drawLeft(g, x, y);
		g.drawString(kToString + highValue.toString(), x, y);
	}

//--------------------------------------------------------------------------
	
	public void drawAtPoint(Graphics g, int drawDecs, int x, int y) {
		lowValue.drawLeft(g, drawDecs, x, y);
		g.drawString(kToString + highValue.toString(drawDecs), x, y);
	}
	
	public void drawRight(Graphics g, int drawDecs, int x, int y) {
		g.drawString(toString(drawDecs), x, y);
	}
	
	public void drawCentred(Graphics g, int drawDecs, int x, int y) {
		String wholeValue = toString(drawDecs);
		
		FontMetrics fMetrics = g.getFontMetrics();
		int wholeLength = fMetrics.stringWidth(wholeValue);
		
		g.drawString(wholeValue, x - wholeLength / 2, y);
	}
	
	public void drawLeft(Graphics g, int drawDecs, int x, int y) {
		String wholeValue = toString(drawDecs);
		
		FontMetrics fMetrics = g.getFontMetrics();
		int wholeLength = fMetrics.stringWidth(wholeValue);
		
		g.drawString(wholeValue, x - wholeLength, y);
	}
	
	public int stringWidth(Graphics g, int drawDecs) {
		return g.getFontMetrics().stringWidth(toString(drawDecs));
	}

	public String toString(int drawDecs) {
		return lowValue.toString(drawDecs) + kToString + highValue.toString(drawDecs);
	}
	
	public void setDecimals(int decimals) {
		lowValue.decimals = decimals;
		highValue.decimals = decimals;
	}
	
	public void setT(double tValue) {
		lowValue.setValue(mean - tValue * sd);
		highValue.setValue(mean + tValue * sd);
	}
}