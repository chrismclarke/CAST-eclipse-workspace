package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class ForecastView extends TimeView {
//	static public final String FORECAST_PLOT = "forecastPlot";
	
	private boolean showErrors = true;
	
	public ForecastView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		Point thePoint = null;
		Point smoothPoint = null;
		
		NumVariable smoothedVariable = (NumVariable)getVariable(getSmoothedKey(0));
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			boolean nextSel = fe.nextFlag();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			double smoothedVal = smoothedVariable.doubleValueAt(index);
			smoothPoint = getScreenPoint(index, smoothedVal, smoothPoint);
			
			if (thePoint != null && smoothPoint != null) {
				g.setColor(getErrorColor());
				int top = Math.min(thePoint.y, smoothPoint.y);
				int bottom = Math.max(thePoint.y, smoothPoint.y);
				if (showErrors)
					g.drawLine(thePoint.x, top, thePoint.x, bottom);
			}
			if (nextSel && smoothPoint != null) {
				g.setColor(getSmoothedColor());
				drawBlob(g, smoothPoint);
			}
			
			if (thePoint != null) {
				g.setColor(getCrossColor());
				if (nextSel)
					drawBlob(g, thePoint);
				else
					drawSquare(g, thePoint);
			}
			index++;
		}
	}
}
	
