package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class Forecast2View extends ExpTimeView {
//	static public final String FORECAST2_PLOT = "forecast2Plot";
	
//	private boolean showErrors = true;
	
	public Forecast2View(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected void shadeInfluence(Graphics g, StoredFunctVariable smoothed, int selectedIndex) {
		int lastDisplayedIndex = smoothed.getMaxInfluence(selectedIndex);
		super.shadeInfluence(g, smoothed, lastDisplayedIndex);
		
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		Point thePoint = null;
		
		StoredFunctVariable smoothedVariable = (StoredFunctVariable)getVariable(getSmoothedKey(0));
		int selectedIndex = getSelection().findSingleSetFlag();
		int lastDisplayedIndex = (selectedIndex < 0) ? -1 :
														smoothedVariable.getMaxInfluence(selectedIndex);
		
		g.setColor(getCrossColor());
		
		int index = 0;
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			
			if (thePoint != null)
				drawSquare(g, thePoint);
			
			if (index == lastDisplayedIndex)
				break;
			index++;
		}
	}
	
	protected void drawSmoothed(Graphics g, int keyIndex) {
		NumVariable smoothedVariable = (NumVariable)getVariable(getSmoothedKey(keyIndex));
		g.setColor(getLineColor(keyIndex));
		
		int selectedIndex = getSelection().findSingleSetFlag();
		
		Point lastPoint = null;
		Point thisPoint = null;
		int index = 0;
		ValueEnumeration e = smoothedVariable.values();
		while (e.hasMoreValues()) {
			thisPoint = getScreenPoint(index, e.nextDouble(), thisPoint);
			if (thisPoint != null && lastPoint != null)
				g.drawLine(lastPoint.x, lastPoint.y, thisPoint.x, thisPoint.y);
			if (selectedIndex == index) {
				if (thisPoint != null) {
					g.setColor(Color.yellow);
					drawCrossBackground(g, thisPoint);
					g.setColor(getSmoothedColor());
					drawCross(g, thisPoint);
				}
				break;
			}
			index++;
			Point temp = lastPoint;
			lastPoint = thisPoint;
			thisPoint = temp;
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		getData().clearSelection();
	}
}
	
