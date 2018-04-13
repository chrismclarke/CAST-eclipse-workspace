package twoFactor;

import java.awt.*;

import dataView.*;
import valueList.*;


public class ExplainedSsqView extends ValueView {
	
	protected String model0Key, model1Key, xKey, zKey, yKey;
	protected NumValue biggestSsq;
	
	public ExplainedSsqView(DataSet theData, XApplet applet, String yKey, String model0Key,
																	String model1Key, String xKey, String zKey, NumValue biggestSsq) {
		super(theData, applet);
		this.yKey = yKey;
		this.model0Key = model0Key;
		this.model1Key = model1Key;
		this.xKey = xKey;
		this.zKey = zKey;
		this.biggestSsq = biggestSsq;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return biggestSsq.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		CatVariable zVar = (CatVariable)getVariable(zKey);
		TwoFactorModel model0 = (TwoFactorModel)getVariable(model0Key);
		TwoFactorModel model1 = (TwoFactorModel)getVariable(model1Key);
		
		double rss0 = 0.0;
		double rss1 = 0.0;
		for (int i=0 ; i<yVar.noOfValues() ; i++) {
			int x = xVar.getItemCategory(i);
			int z = zVar.getItemCategory(i);
			double y = yVar.doubleValueAt(i);
			
			double fit0 = model0.evaluateMean(x, z);
			double fit1 = model1.evaluateMean(x, z);
			
			double resid0 = y - fit0;
			double resid1 = y - fit1;
			rss0 += resid0 * resid0;
			rss1 += resid1 * resid1;
		}
		
		return (new NumValue(rss0 - rss1, biggestSsq.decimals)).toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
