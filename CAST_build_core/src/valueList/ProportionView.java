package valueList;

import java.awt.*;

import dataView.*;


public class ProportionView extends ValueView {
	static private final int kDecimals = 4;
	
	protected String variableKey;
	private String labelString;
	private NumValue zeroValue = new NumValue(0.0, kDecimals);
	
	private boolean doHighlight = false;
	private boolean inversePropn = false;
	
	public ProportionView(DataSet theData, String variableKey, XApplet applet) {
		super(theData, applet);
		labelString = applet.translate("proportion") + " =";
		this.variableKey = variableKey;
	}
	
	public void setVariableKey(String variableKey) {
		this.variableKey = variableKey;
		redrawAll();
	}
	
	public void setHighlight(boolean doHighlight) {
		this.doHighlight = doHighlight;
	}
	
	public void setDecimals(int decimals) {
		zeroValue.decimals = decimals;
	}
	
	public void setInversePropn(boolean inversePropn) {
		this.inversePropn = inversePropn;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		if (labelString == null)
			return 0;
		else
			return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return zeroValue.stringWidth(g);
	}
	
	protected String getValueString() {
		CoreVariable variable = getVariable(variableKey);
		double proportion;
		if (variable instanceof Variable) {
			int selectedCount = getSelection().noOfSetFlags();
			proportion = selectedCount / (double)getSelection().getNoOfFlags();
		}
		else {
			DistnVariable dv = (DistnVariable)variable;
			proportion = dv.getCumulativeProb(dv.getMaxSelection()) - dv.getCumulativeProb(dv.getMinSelection());
		}
		if (inversePropn)
			proportion = 1 - proportion;
		NumValue propn = new NumValue(proportion, zeroValue.decimals);
		return propn.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (labelString != null)
			g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return doHighlight;
	}
	

//--------------------------------------------------------------------------------
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}
}
