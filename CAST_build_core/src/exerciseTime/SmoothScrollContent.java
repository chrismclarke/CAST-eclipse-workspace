package exerciseTime;

import java.awt.*;

import dataView.*;
import valueList.*;


public class SmoothScrollContent extends ScrollValueContent {
	
	static final private Color kGreyColor = new Color(0xDDDDDD);
	
	public SmoothScrollContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void addVariableToList(String newKey, int displayType) {
		varColumns.addElement(new SmoothListColumn(getData(), newKey, displayType));
	}
	
	public void setMissing(int[] missingIndex, Value[] missingText) {
		for (int i=0 ; i<varColumns.size() ; i++) {
			SmoothListColumn yColumn = (SmoothListColumn)varColumns.elementAt(i);
			yColumn.setMissing(missingIndex[i], missingText[i]);
		}
	}
	
	protected Color getBackgroundColor(int rowIndex, int colIndex, boolean selectedRow) {
		SmoothListColumn yColumn = (SmoothListColumn)varColumns.elementAt(colIndex);
		
		if (yColumn.isUnknown(rowIndex))
			return kGreyColor;
		
		if (yColumn.isMissing(rowIndex))
			return Color.yellow;
		return null;
	}
}
