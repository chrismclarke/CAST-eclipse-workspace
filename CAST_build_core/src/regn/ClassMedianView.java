package regn;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class ClassMedianView extends ValueView {
//	static public final String CLASS_MEDIAN_VIEW = "classMedian";
	private String medianString;
	
	private NumValue value = null;
	private String varKey;
	
	public ClassMedianView(DataSet theData, XApplet applet, String varKey) {
		super(theData, applet);
		this.varKey = varKey;
		medianString = applet.translate("Median") + " ";
	}
	
	public void setValue(NumValue newValue) {
		value = newValue;
		redrawAll();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		CoreVariable variable = getVariable(varKey);
		return g.getFontMetrics().stringWidth(medianString + variable.name + " =");
	}
	
	protected int getMaxValueWidth(Graphics g) {
		NumVariable variable = (NumVariable)getVariable(varKey);
		return variable.getMaxWidth(g);
	}
	
	protected int getLabelAscent(Graphics g) {
		return g.getFontMetrics().getAscent();
	}
	
	protected int getLabelDescent(Graphics g) {
		return g.getFontMetrics().getDescent();
	}
	
	protected String getValueString() {
		return (value == null) ? null : value.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		CoreVariable variable = getVariable(varKey);
		g.drawString(medianString + variable.name + " =", startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return value != null;
	}
}
