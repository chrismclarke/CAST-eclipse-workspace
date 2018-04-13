package sport;

import java.awt.*;

import dataView.*;
import valueList.*;


public class TeamAValueView extends ValueView {
//	static public final String TEAM_A_VALUE = "teamAValue";
	
	static final public int WIN_PROB = 0;
	static final public int DRAW_PROB = 1;
	static final public int LOSE_PROB = 2;
	
	protected String variableKey;
	private String label;
	private int probType;
	
	private NumValue maxValue;
	private double[] probs = new double[2];
	
	public TeamAValueView(DataSet theData, String variableKey, XApplet applet, String label,
																															int probType, int decimals) {
		super(theData, applet);
		this.variableKey = variableKey;
		this.label = label;
		this.probType = probType;
		maxValue = new NumValue(1.0, decimals);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(label);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)getVariable(variableKey);
		
		resultsVar.setResultProbs(0, 1, probs);
		
		double result = (probType == WIN_PROB) ? probs[0]
								: (probType == DRAW_PROB) ? probs[1]
								: (1.0 - probs[0] - probs[1]);
		return (new NumValue(result, maxValue.decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(label, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
