package loess;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class JoinedScatterView extends ScatterView {
//	static public final String JOINED_SCATTER_PLOT = "joinedScatterPlot";
	
	public JoinedScatterView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
									String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	protected int groupIndex(int itemIndex) {
		return 3;
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		NumVariable xVar = (NumVariable)getVariable(xKey);
		g.setColor(getForeground());
		ValueEnumeration e = xVar.values();
//		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		Point oldPoint = null;
		Point newPoint = null;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			newPoint = getScreenPoint(index, nextVal, newPoint);
			if (newPoint != null && oldPoint != null)
				g.drawLine(oldPoint.x, oldPoint.y, newPoint.x, newPoint.y);
			Point temp = oldPoint;
			oldPoint = newPoint;
			newPoint = temp;
			index++;
		}
	}
}
	
