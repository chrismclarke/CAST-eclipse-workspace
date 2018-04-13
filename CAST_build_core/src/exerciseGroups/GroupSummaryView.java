package exerciseGroups;

import java.awt.*;

import dataView.*;
import valueList.*;


public class GroupSummaryView extends ValueView {
	static public final int NAME_VALUE = 0;
	static public final int COUNT_VALUE = 1;
	static public final int MEAN_VALUE = 2;
	static public final int VARIANCE_VALUE = 3;
	static public final int SD_VALUE = 4;
	
	private String groupKey, yKey;
	private int groupIndex, summaryType;
//	private int decimals;
	private Value maxValue;
	
	public GroupSummaryView(DataSet theData, String groupKey, String yKey, int groupIndex,
																										int summaryType, XApplet applet) {
		super(theData, applet);
		this.groupKey = groupKey;
		this.yKey = yKey;
		this.groupIndex = groupIndex;
		this.summaryType = summaryType;
	}
	
	public GroupSummaryView(DataSet theData, String varKey, int summaryType, XApplet applet) {
		super(theData, applet);
		this.groupKey = null;
		this.yKey = varKey;
		this.groupIndex = -1;
		this.summaryType = summaryType;
	}
	
	public void updateGroupInfo(Value maxValue) {
		this.maxValue = maxValue;
		resetSize();
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		return 0;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	public String getValueString() {
		NumVariable y = (NumVariable)getData().getVariable(yKey);
		CatVariable x = (CatVariable)getData().getVariable(groupKey);
		switch (summaryType) {
			case NAME_VALUE:
				return (x == null) ? y.name : x.getLabel(groupIndex).toString();
			case COUNT_VALUE:
				return String.valueOf((x == null) ? y.noOfValues() : x.getCounts()[groupIndex]);
			case MEAN_VALUE:
			case VARIANCE_VALUE:
			case SD_VALUE:
				double sy = 0.0;
				double syy = 0.0;
				int n = y.noOfValues();
				if (x != null)
					n = Math.min(n, x.noOfValues());
				int nVals = 0;
				for (int i=0; i<n; i++) {
					int group = (x == null) ? -1 : x.getItemCategory(i);
					if (group == groupIndex) {
						double yVal = y.doubleValueAt(i);
						sy += yVal;
						syy += yVal * yVal;
						nVals++;
					}
				}
				double mean = sy / nVals;
				double result = (summaryType == MEAN_VALUE) ? mean : (syy - sy * mean) / (nVals - 1);
				if (summaryType == SD_VALUE)
					result = Math.sqrt(result);
				return (new NumValue(result, ((NumValue)maxValue).decimals)).toString();
		}
		return null;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
