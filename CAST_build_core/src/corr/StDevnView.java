package corr;

import java.awt.*;

import dataView.*;


public class StDevnView extends MeanView {
//	static public final String ST_DEVN = "stDevn";
	
	static final private String kSString = "s";
	
	public StDevnView(DataSet theData, String variableKey,
						int drawFormula, int extraDecimals, XApplet applet) {
		super(theData, variableKey, drawFormula, extraDecimals, applet);
		labelString = applet.translate("st devn");
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		if (drawFormula == DRAW_FORMULA) {
			CoreVariable variable = getVariable(variableKey);
			int width = fm.stringWidth("s" + kEqualsString);
			Font oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
			FontMetrics fms = g.getFontMetrics();
			width += fms.stringWidth(variable.name);
			g.setFont(oldFont);
			return width;
		}
		else if (drawFormula == GENERIC_TEXT_FORMULA)
			return fm.stringWidth(labelString + kEqualsString);
		else
			return fm.stringWidth(kSString + kEqualsString);
	}
	
	protected String getValueString() {
		NumVariable variable = (NumVariable)getVariable(variableKey);
		ValueEnumeration e = variable.values();
		double sx = 0.0;
		double sxx = 0.0;
		int nVals = 0;
		while (e.hasMoreValues()) {
			double val = e.nextDouble();
			sx += val;
			sxx += val * val;
			nVals++;
		}
		double stDevn = Math.sqrt((sxx - sx * sx / nVals) / (nVals - 1));
		int decimals = (maxValue != null) ? maxValue.decimals : variable.getMaxDecimals() + extraDecimals;
		return (new NumValue(stDevn, decimals)).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		switch (drawFormula) {
			case DRAW_FORMULA:
				FontMetrics fm = g.getFontMetrics();
				g.drawString("s", startHoriz, baseLine);
				int horiz = startHoriz + fm.stringWidth("s");
				
				CoreVariable variable = getVariable(variableKey);
				Font oldFont = g.getFont();
				g.setFont(new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() - 2));
				g.drawString(variable.name, horiz, baseLine + 2);
				FontMetrics fms = g.getFontMetrics();
				horiz += fms.stringWidth(variable.name);
				g.setFont(oldFont);
				
				g.drawString(kEqualsString, horiz, baseLine);
				break;
			case GENERIC_TEXT_FORMULA:
				g.drawString(labelString + kEqualsString, startHoriz, baseLine);
				break;
			case CONCISE_FORMULA:
				g.drawString(kSString + kEqualsString, startHoriz, baseLine);
				break;
		}
	}
}
