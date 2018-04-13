package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class TimeSeasonView extends TimeView {
	
	private boolean selection[];
	
	public TimeSeasonView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
		selection = new boolean[getNumVariable().noOfValues()];
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		Point thePoint = null;
		Point smoothPoint = null;
		
		int smoothedIndex = (noOfSmoothedLines == 1) ? 0 : 1;
		NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[smoothedIndex]);
		
		ValueEnumeration e = variable.values();
		ValueEnumeration se = smoothedVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			double nextSmoothed = se.nextDouble();
			boolean nextSel = fe.nextFlag();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (nextSel) {
				smoothPoint = getScreenPoint(index, nextSmoothed, smoothPoint);
				if (thePoint != null && smoothPoint != null) {
					g.setColor(getErrorColor());
					int top = Math.min(thePoint.y, smoothPoint.y);
					int bottom = Math.max(thePoint.y, smoothPoint.y);
					g.drawLine(thePoint.x, top, thePoint.x, bottom - 1);
				}
				if (smoothPoint != null) {
					g.setColor(getSmoothedColor());
					drawBlob(g, smoothPoint);
				}
			}
			g.setColor(getForeground());
			if (thePoint != null)
				drawSquare(g, thePoint);
			index++;
		}
	}
	
	protected int firstSmoothedForShading() {
					//		allows TimeView to exclude "y" and still shade at ends
		return (noOfSmoothedLines == 1) ? 0 : 1;
	}
	
	protected String getMainSmoothedKey() {
		return (noOfSmoothedLines == 1) ? smoothedKey[0] : smoothedKey[1];
	}

//-----------------------------------------------------------------------------------
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			int noOfSeasons = ((SeasonTimeAxis)getTimeAxis()).getNoOfSeasons();
			int hitCycle = ((IndexPosInfo)startInfo).itemIndex % noOfSeasons;
			
			int nVals = selection.length;
			for (int i=0 ; i<nVals ; i++)
				selection[i] = (i % noOfSeasons) == hitCycle;
			getData().setSelection(selection);
		}
		return true;
	}
}
	
