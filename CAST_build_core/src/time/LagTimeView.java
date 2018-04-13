package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class LagTimeView extends TimeView {
	static final public Color kLagColor = new Color(0x009900);
	
	private int lag;
	
	public LagTimeView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis, int lag) {
		super(theData, applet, timeAxis, numAxis);
		this.lag = lag;
	}
	
	public void setLag(int lag) {
		this.lag = lag;
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		int selectedIndex = getSelection().findSingleSetFlag();
		
		if (lag > 0 && noOfSmoothedLines == 1 && selectedIndex >= 0 && selectedIndex >= lag) {
			NumValue theVal = (NumValue)variable.valueAt(selectedIndex - lag);
			Point thePoint = getScreenPoint(selectedIndex - lag, theVal.toDouble(), null);
			
			NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[0]);
			double smoothedVal = ((NumValue)smoothedVariable.valueAt(selectedIndex - lag)).toDouble();
			Point smoothPoint = getScreenPoint(selectedIndex - lag, smoothedVal, null);
			if (thePoint != null && smoothPoint != null) {
				g.setColor(kLagColor);
				int top = Math.min(thePoint.y, smoothPoint.y);
				int bottom = Math.max(thePoint.y, smoothPoint.y);
				g.drawLine(thePoint.x, top, thePoint.x, bottom - 1);
			}
			if (smoothPoint != null) {
				g.setColor(kLagColor);
				drawBlob(g, smoothPoint);
			}
		}
		super.drawDotPlot(g, variable);
	}
	
}
	
