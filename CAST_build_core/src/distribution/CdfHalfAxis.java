package distribution;

import java.awt.*;

import dataView.*;
import axis.*;


public class CdfHalfAxis extends VertAxis {
	
	public CdfHalfAxis(XApplet applet) {			//	shows labels for CDF on top half; axis must be cleared on bottom half
		super(applet);
		labels.removeAllElements();
		addAxisLabel(new NumValue(0, 0), 0.5);
		addAxisLabel(new NumValue(1, 0), 1.0);
	}
	
	public void corePaint(Graphics g) {
		super.corePaint(g);
		g.setColor(getBackground());
		int lineXPos = axisWidth - 1;
		int axisStartPos = highBorderUsed;
		int axisEndPos = axisStartPos + axisLength - 1;
		try {
			int axisPos = axisPosition(0.5);
			int halfVertPos = axisEndPos - axisPos;
			g.drawLine(lineXPos, getSize().height, lineXPos, halfVertPos + 1);
		} catch (AxisException e) {
		}
	}
}