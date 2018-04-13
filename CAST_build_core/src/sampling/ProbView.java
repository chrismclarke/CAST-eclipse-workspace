package sampling;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import imageGroups.*;


public class ProbView extends ValueView {
//	static public final String PROB_VIEW = "probValue";
	
	static final private int kProbDecimals = 3;
	static final private String kZeroProbString = "0.000";
	
	private String variableKey;
//	private int decimals;
	
	public ProbView(DataSet theData, XApplet applet, String variableKey) {
		super(theData, applet);
		this.variableKey = variableKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return MeanSDImages.kParamWidth;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kZeroProbString);
	}
	
	protected int getLabelAscent(Graphics g) {
		return MeanSDImages.kParamAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return MeanSDImages.kParamDescent;
	}
	
	protected String getValueString() {
		CoreVariable y = getVariable(variableKey);
		if (y == null)
			return "???";
		
		double prob;
		if (y instanceof CatVariable) {
			CatVariable v = (CatVariable)y;
			int count[] = v.getCounts();
			int total = 0;
			for (int i=0 ; i<count.length ; i++)
				total += count[i];
			if (total == 0)
				return "";
			prob = count[0] / (double)total;
		}
		else {
			CatDistnVariable v = (CatDistnVariable)y;
			double p[] = v.getProbs();
			prob = p[0];
		}
		return (new NumValue(prob, kProbDecimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		CoreVariable y = getVariable(variableKey);
		Image label = (y == null || y instanceof CatDistnVariable) ? MeanSDImages.popnProp : MeanSDImages.sampProp;
		g.drawImage(label, startHoriz, baseLine - MeanSDImages.kParamAscent, this);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
