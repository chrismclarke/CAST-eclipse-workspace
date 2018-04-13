package sport;

import java.awt.*;

import dataView.*;
import valueList.*;


public class HandicapValueView extends ValueView {
//	static public final String HANDICAP_VALUE = "handicapValue";
	
	protected String variableKey;
	
	private NumValue maxValue;
	
	public HandicapValueView(DataSet theData, String variableKey, XApplet applet) {
		super(theData, applet);
		this.variableKey = variableKey;
		HandicapNormalVariable handicappedDistn
													= (HandicapNormalVariable)getVariable(variableKey);
		maxValue = handicappedDistn.getMaxHandicap();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		HandicapNormalVariable handicappedDistn
													= (HandicapNormalVariable)getVariable(variableKey);
		
		NumValue handicap = new NumValue(handicappedDistn.getHandicap(), maxValue.decimals);
		return handicap.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
