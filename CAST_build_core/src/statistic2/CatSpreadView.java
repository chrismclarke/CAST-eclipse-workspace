package statistic2;

import dataView.*;
import coreGraphics.*;

import boxPlot.*;


public class CatSpreadView extends CatCenterView {
	
	private SpreadCalculator spreadCalc = new SpreadCalculator(SpreadCalculator.RANGE);
	
	public CatSpreadView(DataSet theData, XApplet applet, GroupedBoxView dotView, int decimals) {
		super(theData, applet, dotView, decimals);
	}
	
	protected double evaluateStat(NumVariable variable, BoxInfo theBoxInfo) {
		return spreadCalc.evaluateStat(variable, theBoxInfo);
	}
	
	protected SpreadLimits findSpreadLimits(NumVariable variable, BoxInfo theBoxInfo) {
		return spreadCalc.findSpreadLimits(variable, theBoxInfo);
	}
	
	public void setSpreadStat(int newStat) {
		spreadCalc.setStat(newStat);
		setCenterStat(newStat);
	}
	
	public int getSpreadStat() {
		return spreadCalc.getStat();
	}
}
