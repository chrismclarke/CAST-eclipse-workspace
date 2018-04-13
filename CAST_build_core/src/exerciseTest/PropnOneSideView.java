package exerciseTest;

import java.awt.*;

import dataView.*;
import formula.*;
import valueList.*;


public class PropnOneSideView extends ValueView {
	static public final int LESS_EQUAL = 0;
	static public final int GREATER_EQUAL = 1;
	
	static private final int kDecimals = 4;
	
	private String yKey;
	private int side;
	private NumValue cutoff;
	private boolean discrete;
	
	private NumValue zeroValue = new NumValue(0.0, kDecimals);
	
	private String variableName = "X";
	
	public PropnOneSideView(DataSet theData, String yKey, int side, NumValue cutoff, XApplet applet) {
		super(theData, applet);
		this.yKey = yKey;
		this.side = side;
		this.cutoff = cutoff;
	}
	
	public void setDecimals(int decimals) {
		zeroValue.decimals = decimals;
	}
	
	public void setCutoff(NumValue cutoff) {
		this.cutoff = cutoff;
		initialised = false;
		invalidate();
	}
	
	public void setDiscrete(boolean discrete) {
		this.discrete = discrete;
	}
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

//--------------------------------------------------------------------------------
	
	private String labelString() {
		if (cutoff == null)
			return "P =";
		
		if (discrete)
			if (side == LESS_EQUAL) {
				int intCutoff = (int)Math.round(Math.floor(cutoff.toDouble()));
				return MText.expandText("P(" + variableName + " #le# " + intCutoff + ") =");
			}
			else {
				int intCutoff = (int)Math.round(Math.ceil(cutoff.toDouble()));
				return MText.expandText("P(" + variableName + " #ge# " + intCutoff + ") =");
			}
		else
			return MText.expandText("P(" + variableName + " " + (side == LESS_EQUAL ? "#le# " : "#ge# ") + cutoff + ") =");
	}
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString());
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return zeroValue.stringWidth(g);
	}
	
	protected String getValueString() {
		if (cutoff == null)
			return "";
		
		int n = 0;
		int x = 0;
		double cutoffY = cutoff.toDouble();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (side == LESS_EQUAL && y <= cutoffY || side == GREATER_EQUAL && y >= cutoffY)
				x ++;
			n ++;
		}
		
		NumValue propn = new NumValue(x / (double)n, zeroValue.decimals);
		return propn.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString(), startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
}
