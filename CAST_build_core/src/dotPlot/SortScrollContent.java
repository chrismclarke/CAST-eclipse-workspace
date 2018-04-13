package dotPlot;

import java.awt.*;

import dataView.*;
import valueList.*;

public class SortScrollContent extends ScrollValueContent {
//	static public final String SORT_SCROLL_LIST = "sortScrollList";
	
	static public final int kSortedIndex = 100;
	static final private Color kLowColor = new Color(0x0000FF);		//		blue
	static final private Color kHighColor = new Color(0xFF0000);		//		red
	
	private boolean colourRows = true;
	
	public SortScrollContent(DataSet theData, XApplet applet, ScrollValueList listPanel) {
		super(theData, applet, listPanel);
	}
	
	public void setColourRows(boolean colourRows) {
		this.colourRows = colourRows;
	}
	
	protected void drawValues(Graphics g, Flags selection, int[] sortLookup) {
//		int baseline0 = kTopBottomBorder + baselineOffset - topIndex * scrollAmount;
		int baseline0 = kTopBottomBorder - topIndex * lineHt / kStepsPerValue + baselineOffset;
		int noOfValues = selection.getNoOfFlags();
		int currentFrame = getCurrentFrame();
		
		int inverseSortLookup[] = new int[sortLookup.length];
		if (smallFirst)
			for (int i=0 ; i<sortLookup.length ; i++)
				inverseSortLookup[sortLookup[i]] = i;
		else
			for (int i=0 ; i<sortLookup.length ; i++)
				inverseSortLookup[sortLookup[noOfValues - i - 1]] = i;
		
		for (int rawIndex=0 ; rawIndex<noOfValues ; rawIndex++) {
			int rawPos = baseline0 + rawIndex * lineHt;
			int sortedIndex = inverseSortLookup[rawIndex];
			int sortedPos = baseline0 + lineHt * sortedIndex;
			
			int valBaseline = (rawPos * (kSortedIndex - currentFrame) + sortedPos * currentFrame) / kSortedIndex;

			Color textColor = getForeground();
			if (colourRows && currentFrame > 0) {
				int red = (kLowColor.getRed() * sortedIndex + kHighColor.getRed() * (noOfValues - sortedIndex - 1)) / (noOfValues - 1);
				int blue = (kLowColor.getBlue() * sortedIndex + kHighColor.getBlue() * (noOfValues - sortedIndex - 1)) / (noOfValues - 1);
				int green = (kLowColor.getGreen() * sortedIndex + kHighColor.getGreen() * (noOfValues - sortedIndex - 1)) / (noOfValues - 1);
				
				red = (red * currentFrame) / kSortedIndex;
				blue = (blue * currentFrame) / kSortedIndex;
				green = (green * currentFrame) / kSortedIndex;
				
				textColor = new Color(red, green, blue);
			}
			
			drawDataRow(g, selection.valueAt(rawIndex), rawIndex, valBaseline, textColor);
		}
		g.setColor(Color.black);
	}

//-----------------------------------------------------------------------------------

	protected boolean canDrag() {
		return getCurrentFrame() == kSortedIndex;
	}

}
