package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class TimeARView extends TimeView {

	static final public Color kPredictColor = new Color(0x006600);
	static final public Color kSelectBackgroundColor = new Color(0xFFFF99);
	
	public TimeARView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected void drawBackground(Graphics g, NumVariable variable) {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex > 0) {
			g.setColor(kSelectBackgroundColor);
			try {
				int t0Pos = getTimeAxis().timePositionBefore(selectedIndex - 1);
				int t1Pos = getTimeAxis().timePositionBefore(selectedIndex + 1);
				Point thePoint = translateToScreen(t0Pos, 0, null);
				g.fillRect(thePoint.x, 0, t1Pos - t0Pos, getSize().height);
			} catch (AxisException ex) {
			}
			
			NumVariable smoothedVariable = (NumVariable)getVariable(smoothedKey[0]);
			double smoothedVal = smoothedVariable.doubleValueAt(selectedIndex);
			Point smoothPoint = getScreenPoint(selectedIndex, smoothedVal, null);
			
			double laggedVal = variable.doubleValueAt(selectedIndex - 1);
			Point laggedPoint = getScreenPoint(selectedIndex - 1, laggedVal, null);
			
			g.setColor(kPredictColor);
			if (laggedPoint != null && smoothPoint != null)
				g.drawLine(laggedPoint.x, laggedPoint.y, smoothPoint.x, smoothPoint.y);
		}
	}
}
	
