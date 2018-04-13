package valueList;

import java.awt.*;

import dataView.*;


public class ValueCountView extends ProportionView {
	static private final String kMaxCountString = "99999";
	
	public ValueCountView(DataSet theData, XApplet applet) {
		super(theData, null, applet);
		setLabel(applet.translate("Count") + " =");
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(kMaxCountString);
	}
	
	protected String getValueString() {
		Flags selection = getSelection();
		return String.valueOf(selection.getNoOfFlags());
	}
}
