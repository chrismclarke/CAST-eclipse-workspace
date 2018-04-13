package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;


public class PredictGroupDotView extends CoreGroupDotView {
	static final private Color kDarkGreen = new Color(0x009900);
	
	public PredictGroupDotView(DataSet theData, XApplet applet, String yKey, NumCatAxis numAxis,
																								NumCatAxis groupAxis, int sDecimals) {
		super(theData, applet, yKey, numAxis, groupAxis, sDecimals);
	}
	
	protected void fiddleColor(Graphics g, int index) {
		int colorIndex = groupingVariable.getItemCategory(index);
		Color c = (colorIndex == 0) ? Color.black : (colorIndex == 1) ? Color.blue : kDarkGreen;
		g.setColor(c);
	}
	
	protected int groupIndex(int itemIndex) {
		return 20 - groupingVariable.getItemCategory(itemIndex) * 4;
											//	black but different shapes (colours changed later by fiddleColor())
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = drawingGroups ? groupingVariable.getItemCategory(index)
																					: groupingVariable.noOfCategories() - 1;
			int offset = groupAxis.catValToPosition(groupIndex) - currentJitter / 2 - 10;
			newPoint.y -= offset;
		}
		return newPoint;
	}
	
	protected int getNoOfAxisCats() {
		return groupingVariable.noOfCategories();
	}
	
	protected int getNoOfRealCats() {
		return groupingVariable.noOfCategories() - 1;
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getPosition(int x, int y) {
		int yOnScreen = translateFromScreen(x, y, null).y;
		
		int nCats = groupingVariable.noOfCategories();
		
		int minDist = Integer.MAX_VALUE;
		int nearestIndex = 0;
		
		for (int i=0 ; i<nCats ; i++) {
			int catPos = groupAxis.catValToPosition(i);
			int dist = Math.abs(catPos - yOnScreen);
			if (dist < minDist) {
				minDist = dist;
				nearestIndex = i;
			}
		}
		
		int yOffset = groupAxis.catValToPosition(nearestIndex)
																								- groupAxis.catValToPosition(nCats - 1);
		
		return super.getPosition(x, y + yOffset);
	}
}