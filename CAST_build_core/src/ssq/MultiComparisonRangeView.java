package ssq;

import java.awt.*;

import dataView.*;
import valueList.*;


public class MultiComparisonRangeView extends ValueView {
	
	private String labelString = "Max significant difference at 5% level =";
	
	private NumValue maxValue;
	
	public MultiComparisonRangeView(DataSet theData, XApplet applet, NumValue maxValue) {
		super(theData, applet);
		this.maxValue = maxValue;
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return (labelString == null) ? 0 : g.getFontMetrics().stringWidth(labelString);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		GroupMeansDataSet groupData = (GroupMeansDataSet)getData();
		
		double maxDiff = groupData.getMaxSelection() - groupData.getMinSelection();
		
		NumValue result = new NumValue(maxDiff, maxValue.decimals);
		return result.toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (labelString != null)
			g.drawString(labelString, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
	

//--------------------------------------------------------------------------------
	
	public void setLabel(String labelString) {
		this.labelString = labelString;
	}
}
