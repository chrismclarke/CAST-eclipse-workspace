package graphics;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class RibbonChartView extends Core3DChartView {
	
	private String yKeys[];
	private Color ribbonColor[];
	private Color ribbonFillColor[];
	private String labelKey;
	
	public RibbonChartView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String labelKey, String[] yKeys, Color[] ribbonColor, String yName, double depth) {
		super(theData, applet, xAxis, yAxis, zAxis, yName, depth);
		this.yKeys = yKeys;
		this.ribbonColor = ribbonColor;
		ribbonFillColor = new Color[ribbonColor.length];
		for (int i=0 ; i<ribbonColor.length ; i++)
			ribbonFillColor[i] = dimColor(ribbonColor[i], 0.5);
		this.labelKey = labelKey;
	}
	
	private void drawRibbon(Graphics g, String yKey, double ribbonLow, double ribbonWidth,
											Color cOutline, Color cFill, boolean xAxisBehind, int[] xCoord, int[] yCoord) {
//		RotateMap map = getCurrentMap();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		int nVals = yVar.noOfValues();
		
		Point pLow = null;
		Point pHigh = null;
		
		int startIndex = xAxisBehind ? 0 : (nVals - 1);
		int endIndex = xAxisBehind ? nVals  : -1;
		int step = xAxisBehind ? 1 : -1;
		
		for (int i=startIndex ; i!=endIndex ; i+=step) {
			double y = yVar.doubleValueAt(i);
			double z = i / (double)(nVals - 1);
			pLow = getScreenPoint(ribbonLow, y, z, pLow);
			pHigh = getScreenPoint(ribbonLow + ribbonWidth, y, z, pHigh);
			if ((i - startIndex) % 2 == 0) {
				xCoord[0] = xCoord[4] = pLow.x;
				xCoord[1] = pHigh.x;
				yCoord[0] = yCoord[4] = pLow.y;
				yCoord[1] = pHigh.y;
			}
			else {
				xCoord[2] = pHigh.x;
				xCoord[3] = pLow.x;
				yCoord[2] = pHigh.y;
				yCoord[3] = pLow.y;
			}
			
			if (i != startIndex) {
				g.setColor(cFill);
				g.fillPolygon(xCoord, yCoord, 5);
			}
			g.setColor(cOutline);
			g.drawLine(pLow.x, pLow.y, pHigh.x, pHigh.y);
			if (i != startIndex) {
				g.drawLine(xCoord[1], yCoord[1], xCoord[2], yCoord[2]);
				g.drawLine(xCoord[3], yCoord[3], xCoord[4], yCoord[4]);
			}
		}
	}
	
	protected double getZProportion(int i, int nVals) {
		return i / (double)(nVals - 1);
	}
	
	protected Value[] getZAxisLabels() {
		LabelVariable labelVar = (LabelVariable)getVariable(labelKey);
		int nVals = labelVar.noOfValues();
		Value labelValues[] = new Value[nVals];
		for (int i=0 ; i<nVals ; i++)
			labelValues[i] = labelVar.valueAt(i);
		return labelValues;
	}
	
	protected void drawInterior(Graphics g, double lowX, double highX,
											boolean zAxisBehind, boolean xAxisBehind, boolean viewFromTop,
											int[] xCoord, int[] yCoord) {
		double ribbonWidth = 0.5 * depth / yKeys.length;
		if (zAxisBehind) {
			for (int i=0 ; i<yKeys.length ; i++) {
				double ribbonStart = lowX + (2 * i + 0.5) * ribbonWidth;
				drawRibbon(g, yKeys[i], ribbonStart, ribbonWidth, ribbonColor[i], ribbonFillColor[i],
																																xAxisBehind, xCoord, yCoord);
			}
		}
		else {
			for (int i=yKeys.length-1 ; i>=0 ; i--) {
				double ribbonStart = lowX + (2 * i + 0.5) * ribbonWidth;
				drawRibbon(g, yKeys[i], ribbonStart, ribbonWidth, ribbonColor[i], ribbonFillColor[i],
																																xAxisBehind, xCoord, yCoord);
			}
		}
	}
}
	
