package regn;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class ErrorSdEstView extends ValueImageView {
	
	protected String residKey;
	protected NumValue biggestErrorSD;
	
	public ErrorSdEstView(DataSet theData, XApplet applet, String residKey,
																															NumValue biggestErrorSD) {
		super(theData, applet, "xEquals/errorSdEst.png", 26);
		this.residKey = residKey;
		this.biggestErrorSD = biggestErrorSD;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return biggestErrorSD.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable resid = (NumVariable)getVariable(residKey);
		ValueEnumeration e = resid.values();
		double rss = 0.0;
		while (e.hasMoreValues()) {
			double r = e.nextDouble();
			rss += r * r;
		}
		int n = resid.noOfValues();
		return (new NumValue(Math.sqrt(rss / (n-2)), biggestErrorSD.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return true;
	}
}
