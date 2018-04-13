package graphics;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class BarChartRotateView extends Core3DChartView {
	
	private String yKey;
	private Color barColor, barColor2, barTopColor;
	private String labelKey;
	
	public BarChartRotateView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String labelKey, String yKey, Color barColor, String yName, double depth) {
		super(theData, applet, xAxis, yAxis, zAxis, yName, depth);
		this.yKey = yKey;
		this.barColor = barColor;
		if (barColor != null) {		//	can be null for BivarBarChartView super-class
			barTopColor = dimColor(barColor, 0.5);
			barColor2 = dimColor(barColor, 0.2);
		}
		this.labelKey = labelKey;
	}
	
	protected void drawOneBar(Graphics g, double x, double y, double z, double barDiameter, Color barColor,
											Color barColor2, Color barTopColor, boolean zAxisBehind, boolean xAxisBehind,
											boolean viewFromTop, int[] xCoord, int[] yCoord) {
		double xBarRadius = barDiameter / 2;
		double zBarRadius = xBarRadius * (zAxis.getMaxOnAxis() - zAxis.getMinOnAxis())
																								/ (xAxis.getMaxOnAxis() - xAxis.getMinOnAxis());
		double frontX = zAxisBehind ? x + xBarRadius : x - xBarRadius;
		double backX = zAxisBehind ? x - xBarRadius : x + xBarRadius;
		double frontZ = xAxisBehind ? z + zBarRadius : z - zBarRadius;
		double backZ = xAxisBehind ? z - zBarRadius : z + zBarRadius;
		
		
		Point p0Low = getScreenPoint(frontX, 0.0, frontZ, null);
		Point p1Low = getScreenPoint(frontX, 0.0, backZ, null);
		Point p0High = getScreenPoint(frontX, y, frontZ, null);
		Point p1High = getScreenPoint(frontX, y, backZ, null);
		
		xCoord[0] = xCoord[4] = p0Low.x;
		xCoord[1] = p0High.x;
		xCoord[2] = p1High.x;
		xCoord[3] = p1Low.x;
		yCoord[0] = yCoord[4] = p0Low.y;
		yCoord[1] = p0High.y;
		yCoord[2] = p1High.y;
		yCoord[3] = p1Low.y;
		g.setColor(barColor2);
		g.fillPolygon(xCoord, yCoord, 5);
		
		p1Low = getScreenPoint(backX, 0.0, frontZ, p1Low);
		p1High = getScreenPoint(backX, y, frontZ, p1High);
		xCoord[2] = p1High.x;
		xCoord[3] = p1Low.x;
		yCoord[2] = p1High.y;
		yCoord[3] = p1Low.y;
		
		g.setColor(barColor);
		g.fillPolygon(xCoord, yCoord, 5);
		
		if (viewFromTop) {
			p0Low = getScreenPoint(frontX, y, backZ, p0Low);
			p1Low = getScreenPoint(backX, y, backZ, p1Low);
			xCoord[0] = xCoord[4] = p0Low.x;
			xCoord[3] = p1Low.x;
			yCoord[0] = yCoord[4] = p0Low.y;
			yCoord[3] = p1Low.y;
			
			g.setColor(barTopColor);
			g.fillPolygon(xCoord, yCoord, 5);
		}
	}
	
	protected double getZProportion(int i, int nVals) {
		return (i + 0.5) / nVals;
	}
	
	protected Value[] getZAxisLabels() {
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		int nVals = labelVar.noOfValues();
		Value labelValues[] = new Value[nVals];
		for (int i=0 ; i<nVals ; i++)
			labelValues[i] = labelVar.valueAt(i);
		return labelValues;
	}
	
	private double getBarDiameter(double depth, int nVals) {
		return Math.min(depth * 0.5, depth / nVals);
	}
	
	protected void drawBarChartRow(Graphics g, double x, NumVariable yVar, double scaling,
												Color barColor, Color barColor2, Color barTopColor, boolean zAxisBehind,
												boolean xAxisBehind, boolean viewFromTop, int[] xCoord, int[] yCoord) {
		int nVals = yVar.noOfValues();
		double barDiameter = getBarDiameter(depth, nVals);
		if (xAxisBehind) {
			for (int i=0 ; i<nVals ; i++) {
				double z = getZProportion(i, nVals);
				double y = yVar.doubleValueAt(i) * scaling;
				drawOneBar(g, x, y, z, barDiameter, barColor, barColor2, barTopColor, zAxisBehind,
																										xAxisBehind, viewFromTop, xCoord, yCoord);
			}
		}
		else {
			for (int i=nVals-1 ; i>=0 ; i--) {
				double z = getZProportion(i, nVals);
				double y = yVar.doubleValueAt(i) * scaling;
				drawOneBar(g, x, y, z, barDiameter, barColor, barColor2, barTopColor, zAxisBehind,
																										xAxisBehind, viewFromTop, xCoord, yCoord);
			}
		}
	}
	
	protected void drawInterior(Graphics g, double lowX, double highX, boolean zAxisBehind,
												boolean xAxisBehind, boolean viewFromTop, int[] xCoord, int[] yCoord) {
		double x = (lowX + highX) * 0.5;
		NumVariable yVar = (NumVariable)getVariable(yKey);
		drawBarChartRow(g, x, yVar, 1.0, barColor, barColor2, barTopColor, zAxisBehind, xAxisBehind,
																																	viewFromTop, xCoord, yCoord);
	}
	
	protected double getLowX(double depth) {
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		int nVals = labelVar.noOfValues();
		return Math.max(0.0, 0.5 - 1.5 * getBarDiameter(depth, nVals));
	}
	
	protected double getHighX(double depth) {
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		int nVals = labelVar.noOfValues();
		return Math.min(1.0, 0.5 + 1.5 * getBarDiameter(depth, nVals));
	}
}
	
