package simulation;

import dataView.*;
import valueList.*;


public class ClearOneValueView extends OneValueView {
//	static public final String CLEAR_ONE_VALUE = "clearOneValue";
	
	private boolean clear = false;
	
	public ClearOneValueView(DataSet theData, String variableKey, XApplet applet, Value maxValue) {
		super(theData, variableKey, applet, maxValue);
	}
	
	public ClearOneValueView(DataSet theData, String variableKey, XApplet applet) {
		this(theData, variableKey, applet, null);
	}
	
	public void setClear(boolean clear) {
		if (this.clear != clear) {
			this.clear = clear;
			repaint();
		}
	}
	
	protected Value getSelectedValue(String key) {
		if (clear)
			return null;
		else
			return super.getSelectedValue(key);
	}
}
