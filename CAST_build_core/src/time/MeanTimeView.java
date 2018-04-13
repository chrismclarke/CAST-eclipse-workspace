package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class MeanTimeView extends TimeView {
	
	public MeanTimeView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected void shadeInfluence(Graphics g, StoredFunctVariable smoothed, int selectedIndex) {
		MeanMedianVariable meanVar = (MeanMedianVariable)smoothed;
		int halfRunLength = (meanVar.getMeanRun() - 1) / 2;
		
		Point thePoint = getScreenBefore(selectedIndex - halfRunLength, null);
		int lowPos = thePoint.x;
		thePoint = getScreenBefore(selectedIndex + halfRunLength + 1, thePoint);
		int highPos = thePoint.x;
		g.fillRect(lowPos, 0, (highPos - lowPos), getSize().height);
		
		if (meanVar.getMeanRun() % 2 == 0) {
			g.setColor(dimColor(g.getColor(), 0.5));
			
			thePoint = getScreenBefore(selectedIndex - halfRunLength - 1, null);
			int lowPosMinus = thePoint.x;
			g.fillRect(lowPosMinus, 0, (lowPos - lowPosMinus), getSize().height);
			
			thePoint = getScreenBefore(selectedIndex + halfRunLength + 2, null);
			int highPosPlus = thePoint.x;
			g.fillRect(highPos, 0, (highPosPlus - highPos), getSize().height);
		}
	}
}
	
