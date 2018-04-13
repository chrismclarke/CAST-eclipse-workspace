package exerciseTime;

import java.awt.*;

import dataView.*;
import valueList.*;


public class SmoothListColumn extends ListColumn {
	
	private int missingIndex = -1;
	private Value missingText = null;
	
	public SmoothListColumn(DataSet data, String varKey, int displayType) {
		super(data, varKey, displayType);
	}
	
	public void setMissing(int missingIndex, Value missingText) {
		this.missingIndex = missingIndex;
		this.missingText = missingText;
	}
	
	public boolean isMissing(int rowIndex) {
		return rowIndex == missingIndex;
	}
	
	public boolean isUnknown(int rowIndex) {
		CoreVariable v = data.getVariable(varKey);
		return v instanceof NumVariable && Double.isNaN(((NumVariable)v).doubleValueAt(rowIndex));
	}
	
	protected void drawValue(Graphics g, int index, int baseline, int columnLeft) {
		if (index == missingIndex) {
			int valueRight = columnLeft + columnWidth - columnOffset;
			missingText.drawLeft(g, valueRight, baseline);
		}
		else
			super.drawValue(g, index, baseline, columnLeft);
	}
}