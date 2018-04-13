package propnVenn;

import java.awt.*;

import dataView.*;
import valueList.*;

import contin.*;


public class ConditionalProbView extends ValueView {
//	static public final String CONDITIONAL_VIEW = "conditionalValue";
	static private final int decimals = 4;
	static private final NumValue kZero = new NumValue(0.0, decimals);
	
	protected String xKey, yKey;
//	private String labelString;
	
	public ConditionalProbView(DataSet theData, XApplet applet, String xKey, String yKey) {
		super(theData, applet);
//		labelString = applet.translate("proportion") + " =";
		this.xKey = xKey;
		this.yKey = yKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return kZero.stringWidth(g);
	}
	
	protected String getValueString() {
		ContinResponseVariable yVar = (ContinResponseVariable)getVariable(yKey);
		CatDistnVariable xVar = (CatDistnVariable)getVariable(xKey);
		
		double pYGivenX[][] = yVar.getConditionalProbs();
		double pX[] = xVar.getProbs();
		
		double py0 = 0.0;
		for (int i=0 ; i< pX.length ; i++)
			py0 += pX[i] * pYGivenX[i][0];
		
		double pX0GivenY0 = pX[0] * pYGivenX[0][0] / py0;
		
		NumValue propValue = new NumValue(pX0GivenY0, decimals);
		return propValue.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
