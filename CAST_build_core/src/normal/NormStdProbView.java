package normal;

import java.awt.*;

import dataView.*;
import sampling.*;


public class NormStdProbView extends NormProbView {
//	static public final String NORM_STDPROB_VIEW = "normalStdProb";
	
	private StandardisingAxis axis;
	
	public NormStdProbView(DataSet theData, XApplet applet, String distnKey, StandardisingAxis axis, NumValue biggestX) {
		super(theData, applet, distnKey, biggestX);
		this.axis = axis;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth("P(X \u2264 " + biggestX.toString()
																						+ ") = P(Z \u2264 -3.00) = ");
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int theoryLength = getLabelWidth(g);
		
		DistnVariable dv = (DistnVariable)getVariable(distnKey);
		NumValue zValue = new NumValue(Math.max(dv.getMaxSelection(), -9.0), 2);
		
		double x = axis.minScaled + (zValue.toDouble() - axis.minOnAxis)
								* (axis.maxScaled - axis.minScaled) / (axis.maxOnAxis - axis.minOnAxis);
		
		NumValue xValue = new NumValue(x, biggestX.decimals);
		String labelString = "P(X \u2264 " + xValue.toString() + ") = P(Z \u2264 "
																				+ zValue.toString() + ") = ";
		int labelLength = g.getFontMetrics().stringWidth(labelString);
		g.drawString(labelString, startHoriz + theoryLength - labelLength, baseLine);
	}
}
