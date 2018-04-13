package corr;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;
import formula.*;


public class MeanView extends ProportionView {
//	static public final String MEAN = "mean";
	
	static public final int DRAW_FORMULA = 0;
	static public final int GENERIC_TEXT_FORMULA = 1;
	static public final int CONCISE_FORMULA = 2;
	
	static final protected String kEqualsString = " =";
//	static final private String kMeanString = "mean";
	static final private String kXbarString = MText.expandText("x#bar#");
	
	protected int drawFormula;
	protected int extraDecimals;
	
//	protected String labelString = kMeanString;
	protected String labelString;
	
	protected NumValue maxValue = null;
	
	public MeanView(DataSet theData, String variableKey, int drawFormula,
																						int extraDecimals, XApplet applet) {
		super(theData, variableKey, applet);
		labelString = applet.translate("mean");
		this.drawFormula = drawFormula;
		this.extraDecimals = extraDecimals;
	}
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}
	
	public void setMaxValue(NumValue maxValue) {
		this.maxValue = maxValue;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		CoreVariable variable = getVariable(variableKey);
		FontMetrics fm = g.getFontMetrics();
		String s = (drawFormula == DRAW_FORMULA) ? variable.name
								: (drawFormula == GENERIC_TEXT_FORMULA) ? labelString
								: kXbarString;
		return fm.stringWidth(s + kEqualsString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		if (maxValue == null) {
			NumVariable variable = (NumVariable)getVariable(variableKey);
			int decimals = variable.getMaxDecimals() + extraDecimals;
			return variable.getMaxAlignedWidth(g, decimals);
		}
		else
			return maxValue.stringWidth(g);
	}
	
	protected int getLabelHeight(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return fm.getAscent() + fm.getDescent() + 2;
	}
	
	protected String getValueString() {
		NumVariable variable = (NumVariable)getVariable(variableKey);
		ValueEnumeration e = variable.values();
		double sx = 0.0;
		int nVals = 0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			sx += val;
			nVals++;
		}
		double mean = sx / nVals;
		int decimals = (maxValue != null) ? maxValue.decimals : variable.getMaxDecimals() + extraDecimals;
		return (new NumValue(mean, decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		switch (drawFormula) {
			case DRAW_FORMULA:
				String variableName = getVariable(variableKey).name;
				g.drawString(variableName + kEqualsString, startHoriz, baseLine);
				
				FontMetrics fm = g.getFontMetrics();
				int barWidth = fm.stringWidth(variableName);
				int barVert = baseLine - fm.getAscent() - 1;
				g.drawLine(startHoriz, barVert, startHoriz + barWidth - 1, barVert);
				break;
			case GENERIC_TEXT_FORMULA:
				g.drawString(labelString + kEqualsString, startHoriz, baseLine);
				break;
			case CONCISE_FORMULA:
				g.drawString(kXbarString + kEqualsString, startHoriz, baseLine);
				break;
		}
	}

//--------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (variableKey.equals(key))
			repaint();
	}
}
