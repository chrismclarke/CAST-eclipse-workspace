package bivarCat;

import java.awt.*;

import dataView.*;
import axis.*;

import cat.CountPropnAxis;


public class BarTimeView extends Bar2WayView {
//	static public final String BAR_TIME_PLOT = "barTimePlot";
	
	private int barWidth;
	
	public BarTimeView(DataSet theData, XApplet applet, CountPropnAxis vertAxis, Axis horizAxis,
														String xKey, String yKey, int barWidth) {
		super(theData, applet, vertAxis, horizAxis, xKey, yKey);
		this.barWidth = barWidth;
	}
	
	protected int getBarWidth() {
		return barWidth;
	}
	
	protected int [] getMainOffsets(int noOfTimes) {
		int [] result = new int[noOfTimes];
		try {
			if (horizAxis instanceof IndexTimeAxis) {
				IndexTimeAxis timeAxis = (IndexTimeAxis)horizAxis;
				for (int i=0 ; i<noOfTimes ; i++)
					result[i] = timeAxis.timePosition(i);
			}
			else {
				HorizAxis numAxis = (HorizAxis)horizAxis;
				NumVariable y = (NumVariable)getVariable(yKey);
				ValueEnumeration e = y.values();
				for (int i=0 ; i<noOfTimes ; i++)
					result[i] = numAxis.numValToPosition(((NumValue)e.nextGroup().val).toDouble());
			}
		} catch (AxisException e) {
		}
		return result;
	}
	
	protected Color [] getInnerColors(int noOfYCats) {
		return null;
	}
}
	
