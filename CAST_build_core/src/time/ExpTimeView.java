package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class ExpTimeView extends TimeView {
	
	public ExpTimeView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected void shadeInfluence(Graphics g, StoredFunctVariable smoothed, int selectedIndex) {
		Point thePoint = getScreenBefore(selectedIndex + 1, null);
		int lowPos = thePoint.x;
		
		ExpSmoothVariable expVar = (ExpSmoothVariable)smoothed;
		double smoothConst = expVar.getSmoothConst();
		double p = 1.0;
		
		Color fullColor = g.getColor();
		
		for (int i=selectedIndex ; i>=0 ; i--) {
			int highPos = lowPos;
			thePoint = getScreenBefore(i, thePoint);
			lowPos = thePoint.x;
			
			Color c = dimColor(fullColor, 1 - p);
			g.setColor(c);
			
			g.fillRect(lowPos, 0, (highPos - lowPos), getSize().height);
			
			p *= (1.0 - smoothConst);
		}
	}
}
	
