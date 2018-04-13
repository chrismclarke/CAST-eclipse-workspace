package linMod;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class YSDView extends ValueView {
//	static public final String Y_SD_VIEW = "ySDValue";
	
	private String distnKey;
	private NumValue maxSD;
	
	private boolean estimated = false;
	
	public YSDView(DataSet theData, XApplet applet, String distnKey, NumValue maxSD) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.maxSD = maxSD;
		RegnImages.loadRegn(applet);
	}
	
	public void setEstimated(boolean estimated) {
		this.estimated = estimated;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return estimated ? RegnImages.kMuYParamWidth : RegnImages.kYParamWidth;
	}
	
	protected int getLabelAscent(Graphics g) {
		return estimated ? RegnImages.kMuYParamAscent : RegnImages.kYParamAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return estimated ? RegnImages.kMuYParamDescent : RegnImages.kYParamDescent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int imageAscent = estimated ? RegnImages.kMuYParamAscent : RegnImages.kYParamAscent;
		g.drawImage(estimated ? RegnImages.sigmaYHat : RegnImages.ySD, startHoriz, baseLine - imageAscent, this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxSD.stringWidth(g);
	}
	
	protected String getValueString() {
		CoreVariable distn = getVariable(distnKey);
		NumValue sd = null;
		if (distn instanceof CoreModelVariable) {
			CoreModelVariable m = (CoreModelVariable)distn;
			sd =  m.evaluateSD();
		}
		else {
			DistnVariable d = (DistnVariable)distn;
			sd = d.getSD();
		}
		return sd.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
