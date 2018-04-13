package linMod;

import java.awt.*;

import dataView.*;
import valueList.ValueView;


public class SlopeDistnView extends ValueView {
//	static public final String SLOPE_DISTN_VIEW = "slopeDistnValue";
	
	static final public int SLOPE_MEAN = 0;
	static final public int SLOPE_SD = 1;
	static final public int COUNT = 2;
	static final public int SLOPE = 3;
	
	private String distnKey;
	private NumValue maxVal;
	private int valueType;
	
	public SlopeDistnView(DataSet theData, XApplet applet, String distnKey, NumValue maxVal, int valueType) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.maxVal = maxVal;
		this.valueType = valueType;
		RegnImages.loadRegn(applet);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return RegnImages.kSlopeMeanHatWidth;
	}
	
	protected int getLabelAscent(Graphics g) {
		return RegnImages.kSlopeMeanHatAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return RegnImages.kSlopeMeanHatDescent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		Image theImage = (valueType == SLOPE_MEAN) ? RegnImages.slopeMeanHat
							 : (valueType == SLOPE_SD) ? RegnImages.slopeSDHat
							 : (valueType == SLOPE) ? RegnImages.b1
							 : RegnImages.regnN;
		g.drawImage(theImage, startHoriz, baseLine - RegnImages.kSlopeMeanHatAscent, this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxVal.stringWidth(g);
	}
	
	protected String getValueString() {
		SlopeDistnVariable slopeDistn = (SlopeDistnVariable)getVariable(distnKey);
		NumValue val = (valueType == SLOPE_MEAN || valueType == SLOPE) ? slopeDistn.getMean()
						 : (valueType == SLOPE_SD) ? slopeDistn.getSD()
						 : new NumValue(slopeDistn.getN(), 0);
		return val.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
