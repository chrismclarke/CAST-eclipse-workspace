package regn;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class ResidSsq2View extends ValueImageView {
	
	protected String residKey;
	protected NumValue biggestRss;
	
	public ResidSsq2View(DataSet theData, XApplet applet, String residKey,
														NumValue biggestRss, String labelImageString, int imageAscent) {
		super(theData, applet, labelImageString, imageAscent);
		this.residKey = residKey;
		this.biggestRss = biggestRss;
	}
	
	public ResidSsq2View(DataSet theData, XApplet applet, String residKey,
																																NumValue biggestRss) {
		this(theData, applet, residKey, biggestRss, "xEquals/residSsq.png", 16);
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return biggestRss.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable resid = (NumVariable)getVariable(residKey);
		ValueEnumeration e = resid.values();
		double rss = 0.0;
		while (e.hasMoreValues()) {
			double r = e.nextDouble();
			rss += r * r;
		}
		return (new NumValue(rss, biggestRss.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
