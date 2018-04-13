package loess;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class DurbWatsonValueView extends ValueImageView {
	
	private String residKey;
	
	public DurbWatsonValueView(DataSet theData, XApplet applet, String residKey) {
		super(theData, applet, "xEquals/durbWatson.png", 35);
		
		this.residKey = residKey;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return (new NumValue(99.0, DurbinWatsonValueVariable.kDecimals)).stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable resid = (NumVariable)getVariable(residKey);
		return DurbinWatsonValueVariable.durbWatsonFromResid(resid).toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
