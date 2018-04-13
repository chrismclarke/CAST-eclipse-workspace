package statistic2;

import java.awt.*;

import dataView.*;
import coreGraphics.*;

import boxPlot.*;


public class CatCenterView extends CatLabelsView {
	
	private CenterCalculator centerCalc = new CenterCalculator(CenterCalculator.MEDIAN);
	
	private int decimals;
	private GroupedBoxView dotView;
	
	public CatCenterView(DataSet theData, XApplet applet, GroupedBoxView dotView, int decimals) {
		super(theData, applet);
		this.decimals = decimals;
		this.dotView = dotView;
	}
	
	protected double evaluateStat(NumVariable variable, BoxInfo theBoxInfo) {
		return centerCalc.evaluateStat(variable, theBoxInfo);
	}
	
	protected Value[] generateValues() {
		CatVariable catVar = getCatVariable();
		int noOfCats = catVar.noOfCategories();
		NumValue result[] = new NumValue[noOfCats];
		for (int i=0 ; i<noOfCats ; i++)
			result[i] = new NumValue(evaluateStat(dotView.getGroupVariable(i),
																				dotView.getBoxInfo(i)), decimals);
		return result;
	}
	
	public void setCenterStat(int newStat) {
		centerCalc.setStat(newStat);
		clearValues();
		repaint();
		dotView.repaint();
	}
	
	public int getCenterStat() {
		return centerCalc.getStat();
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		clearValues();
		repaint();
	}
}
