package inference;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class ConfidenceLevelView extends ValueView {
//	static public final String CONFIDENCE_LEVEL = "levelValue";
	static private final int decimals = 1;
	static private final String kBigString = "100.0%";
	
	protected String ciKey;
	private String labelString;
	
	public ConfidenceLevelView(DataSet theData, String ciKey, XApplet applet) {
		super(theData, applet);
		this.ciKey = ciKey;
		labelString = applet.translate("Confidence level") + " =";
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kBigString);
	}
	
	protected int getLabelAscent(Graphics g) {
		return g.getFontMetrics().getAscent();
	}
	
	protected int getLabelDescent(Graphics g) {
		return g.getFontMetrics().getDescent();
	}
	
	protected String getValueString() {
		MeanCIVariable variable = (MeanCIVariable)getVariable(ciKey);
		NumValue percent = new NumValue(variable.getConfidenceLevel() * 100.0, decimals);
		return percent.toString() + "%";
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
