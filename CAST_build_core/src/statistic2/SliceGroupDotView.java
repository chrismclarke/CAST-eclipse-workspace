package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;


public class SliceGroupDotView extends CoreGroupDotView {
//	static public final String SLICE_GROUP_DOT = "sliceGroupDot";
	
	private int displayCat = 0;
	
	public SliceGroupDotView(DataSet theData, XApplet applet, String yKey, NumCatAxis numAxis,
																						NumCatAxis groupAxis, int sDecimals) {
		super(theData, applet, yKey, numAxis, groupAxis, sDecimals);
	}
	
	public void setDisplayCat(int displayCat) {
		this.displayCat = displayCat;
		repaint();
	}
	
	protected void drawMeanSD(Graphics g, int n, double sy, double syy, int catIndex) {
		if (catIndex == displayCat)
			catIndex = 0;
		else if (catIndex == groupingVariable.noOfCategories())
			catIndex = 1;
		else
			return;
		
		super.drawMeanSD(g, n, sy, syy, catIndex);
	}

//-----------------------------------------------------------------------------------
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int drawGroupIndex = 1;		//	all groups
			if (drawingGroups)
				if (groupingVariable.getItemCategory(index) == displayCat)
					drawGroupIndex = 0;
				else
					return null;
			
			int offset = groupAxis.catValToPosition(drawGroupIndex) - currentJitter / 2 - 10;
			newPoint.y -= offset;
		}
		return newPoint;
	}
	
	protected int getNoOfAxisCats() {
		return 2;
	}
	
	protected int getNoOfRealCats() {
		return groupingVariable.noOfCategories();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false; 
	}
}