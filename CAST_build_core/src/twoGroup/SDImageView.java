package twoGroup;

import java.awt.*;

import dataView.*;
import distn.*;
import imageUtils.*;


public class SDImageView extends ValueImageView {
	
	private String normalKey;
	private NumValue maxSD;
	private boolean sdNotMean = true;
	
	public SDImageView(DataSet theData, XApplet applet, String imageName, int imageAscent,
																									String normalKey, NumValue maxSD) {
		super(theData, applet, imageName, imageAscent);
		this.normalKey = normalKey;
		this.maxSD = maxSD;
	}
	
	public void setSDNotMean(boolean sdNotMean) {
		this.sdNotMean = sdNotMean;
	}

//--------------------------------------------------------------------------------
	
	protected int getMaxValueWidth(Graphics g) {
		return maxSD.stringWidth(g);
	}
	
	protected String getValueString() {
		NormalDistnVariable dist = (NormalDistnVariable)getVariable(normalKey);
		maxSD.setValue(sdNotMean ? dist.getSD().toDouble() : dist.getMean().toDouble());
		return maxSD.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
	
}
