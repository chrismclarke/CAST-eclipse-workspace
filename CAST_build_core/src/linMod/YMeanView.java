package linMod;

import java.awt.*;

import dataView.*;
import valueList.ValueView;
import models.*;


public class YMeanView extends ValueView {
//	static public final String Y_MEAN_VIEW = "yMeanValue";
	
	private String distnKey;
	private NumValue maxMean;
	
	public YMeanView(DataSet theData, XApplet applet, String distnKey, NumValue maxMean) {
		super(theData, applet);
		this.distnKey = distnKey;
		this.maxMean = maxMean;
		RegnImages.loadRegn(applet);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return RegnImages.kYParamWidth;
	}
	
	protected int getLabelAscent(Graphics g) {
		return RegnImages.kYParamAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return RegnImages.kYParamDescent;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int imageAscent = RegnImages.kYParamAscent;
		g.drawImage(RegnImages.yMean, startHoriz, baseLine - imageAscent, this);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxMean.stringWidth(g);
	}
	
	protected String getValueString() {
		CoreVariable distn = getVariable(distnKey);
		NumValue mean = null;
		if (distn instanceof MultipleRegnModel) {
			MultipleRegnModel m = (MultipleRegnModel)distn;
			mean =  m.getParameter(0);
		}
		else if (distn instanceof PolynomialModel) {
			PolynomialModel m = (PolynomialModel)distn;
			mean =  m.getParameter(0);
		}
		else if (distn instanceof LinearModel) {
			LinearModel m = (LinearModel)distn;
			mean =  m.getIntercept();
		}
		else {
			DistnVariable d = (DistnVariable)distn;
			mean = d.getMean();
		}
		return mean.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
