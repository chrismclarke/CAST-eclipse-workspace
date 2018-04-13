package exper;

import java.awt.*;

import dataView.*;
import valueList.*;


public class DiffMeanValueView extends ValueView {
//	static public final String DIFF_MEAN_VIEW = "diffMeanValue";
	
	private String groupKey, yKey;
	
	private String label;
	private NumValue maxDiff;
	
	public DiffMeanValueView(DataSet theData, XApplet applet,
											String groupKey, String yKey, NumValue maxDiff, String label) {
		super(theData, applet);
		this.groupKey = groupKey;
		this.yKey = yKey;
		this.maxDiff = maxDiff;
		this.label = label;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(label);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxDiff.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable groupVariable = (CatVariable)getVariable(groupKey);
		
		int noOfCategories = groupVariable.noOfCategories();
		int n[] = new int[noOfCategories];
		double sy[] = new double[noOfCategories];
		ValueEnumeration ye = yVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			int group = groupVariable.getItemCategory(index);
			n[group] ++;
			sy[group] += ye.nextDouble();
			index ++;
		}
		
		if (Double.isNaN(sy[0]) || Double.isNaN(sy[1]))
			return null;
		
		NumValue diffValue = new NumValue(sy[1] / n[1] - sy[0] / n[0], maxDiff.decimals);
		return diffValue.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseline) {
		g.drawString(label, startHoriz, baseline);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
