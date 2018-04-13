package graphics;

import java.awt.*;
import java.util.*;

import dataView.*;
import graphics3D.*;


public class BivarBarChartView extends BarChartRotateView {
	
	static final public int kEndFrame = 40;
	
	private String yKeys[];
	private Color barColor[], barColor2[], barTopColor[];
	
	private boolean showProportions = false;
	private String propnAxisName;
	private D3Axis propnAxis;
	
	public BivarBarChartView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String labelKey, String yKeys[], Color barColor[], String yName,
						double depth, String propnAxisInfo, String propnAxisName) {
		super(theData, applet, xAxis, yAxis, zAxis, labelKey, null, null, yName, depth);
		this.yKeys = yKeys;
		this.barColor = barColor;
		barColor2 = new Color[barColor.length];
		barTopColor = new Color[barColor.length];
		for (int i=0 ; i<barColor.length ; i++) {
			barColor2[i] = dimColor(barColor[i], 0.2);
			barTopColor[i] = dimColor(barColor[i], 0.5);
		}
		this.propnAxisName = propnAxisName;
		propnAxis = new D3Axis(propnAxisName, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, applet);
		propnAxis.setNumScale(propnAxisInfo);
	}
	
	public void setShowProportions(boolean showProportions) {
		this.showProportions = showProportions;
		if (showProportions)
			animateFrames(0, kEndFrame, 16, null);
		else
			animateFrames(kEndFrame, -kEndFrame, 16, null);
	}
	
	private double getYScaling(NumVariable yVar) {
		double p = getCurrentFrame() / (double)kEndFrame;
		double scaling = 1.0;
		if (p > 0.0) {
			double sumY = 0.0;
			for (int j=0 ; j<yVar.noOfValues() ; j++)
				sumY += yVar.doubleValueAt(j);
			scaling = yAxis.getMaxOnAxis() / propnAxis.getMaxOnAxis() / sumY;
			
			scaling = (1 - p) + p * scaling;
		}
		return scaling;
	}
	
	protected void drawInterior(Graphics g, double lowX, double highX, boolean zAxisBehind,
												boolean xAxisBehind, boolean viewFromTop, int[] xCoord, int[] yCoord) {
		if (zAxisBehind) {
			for (int i=0 ; i<yKeys.length ; i++) {
				double x = lowX + (i + 0.5) * (highX - lowX) / yKeys.length;
				NumVariable yVar = (NumVariable)getVariable(yKeys[i]);
				double scaling = getYScaling(yVar);
				drawBarChartRow(g, x, yVar, scaling, barColor[i], barColor2[i], barTopColor[i], zAxisBehind,
																												xAxisBehind, viewFromTop, xCoord, yCoord);
			}
		}
		else {
			for (int i=yKeys.length-1 ; i>=0 ; i--) {
				double x = lowX + (i + 0.5) * (highX - lowX) / yKeys.length;
				NumVariable yVar = (NumVariable)getVariable(yKeys[i]);
				double scaling = getYScaling(yVar);
				drawBarChartRow(g, x, yVar, scaling, barColor[i], barColor2[i], barTopColor[i], zAxisBehind,
																												xAxisBehind, viewFromTop, xCoord, yCoord);
			}
		}
	}
	
	protected double getLowX(double depth) {
		return 0.0;
	}
	
	protected double getHighX(double depth) {
		return 1.0;
	}
	
	protected Enumeration getYAxisLabelEnumeration() {
		return showProportions ? propnAxis.getLabelEnumeration() : yAxis.getLabelEnumeration();
	}
	
	protected String getYAxisName() {
		return showProportions ? propnAxisName : super.getYAxisName();
	}
}
	
