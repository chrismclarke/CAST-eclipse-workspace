package multiRegn;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class Dot3ArrowView extends ModelDot3View {
	
	static final private Color kLightGray = new Color(0xDDDDDD);
	
	public Dot3ArrowView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, null, explanKey, yKey);
		setSelectCrosses(true);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable xVar = (NumVariable)getVariable(explanKey[0]);
		NumVariable zVar = (NumVariable)getVariable(explanKey[1]);
		
		int selectedIndex = getSelection().findSingleSetFlag();
		
		if (selectedIndex >= 0) {
			double y = yVar.doubleValueAt(selectedIndex);
			double x = xVar.doubleValueAt(selectedIndex);
			double z = zVar.doubleValueAt(selectedIndex);
			
			double yMin = yAxis.getMinOnAxis();
			double xMin = xAxis.getMinOnAxis();
			double zMin = zAxis.getMinOnAxis();
			
			Point p00 = null;
			Point p01 = null;
			Point p11 = null;
			Point p10 = null;
			g.setColor(kLightGray);
			for (int i=0 ; i<2 ; i++) {
				double xi = (i==0) ? xMin : x;
				p01 = getScreenPoint(xi, yMin, z, p01);
				p11 = getScreenPoint(xi, y, z, p11);
				p10 = getScreenPoint(xi, y, zMin, p10);
				drawLine(g, p01.x, p01.y, p11.x, p11.y, STANDARD, NO_HEAD);
				drawLine(g, p11.x, p11.y, p10.x, p10.y, STANDARD, NO_HEAD);
				if (i == 1) {
					p00 = getScreenPoint(xi, yMin, zMin, p00);
					drawLine(g, p10.x, p10.y, p00.x, p00.y, STANDARD, NO_HEAD);
					drawLine(g, p00.x, p00.y, p01.x, p01.y, STANDARD, NO_HEAD);
				}
			}
			
			for (int j=0 ; j<2 ; j++)
				for (int k=0 ; k<2 ; k++)
					if (j != 0 || k != 0) {
						double yj = (j==0) ? yMin : y;
						double zk = (k==0) ? zMin : z;
						p01 = getScreenPoint(xMin, yj, zk, p01);
						p11 = getScreenPoint(x, yj, zk, p11);
						drawLine(g, p01.x, p01.y, p11.x, p11.y, STANDARD, NO_HEAD);
					}
			
			Point dataPos = getScreenPoint(x, y, z, null);
			
			Point arrowEndPos = getScreenPoint(xMin, y, zMin, null);
			g.setColor(D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND]);
			drawLine(g, dataPos.x, dataPos.y, arrowEndPos.x, arrowEndPos.y, STANDARD, FILLED_HEAD);
			
			arrowEndPos = getScreenPoint(x, yMin, zMin, null);
			g.setColor(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND]);
			drawLine(g, dataPos.x, dataPos.y, arrowEndPos.x, arrowEndPos.y, STANDARD, FILLED_HEAD);
			
			arrowEndPos = getScreenPoint(xMin, yMin, z, null);
			g.setColor(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND]);
			drawLine(g, dataPos.x, dataPos.y, arrowEndPos.x, arrowEndPos.y, STANDARD, FILLED_HEAD);
			
			g.setColor(getForeground());
		}
		
		super.drawData(g, shadeHandling);
	}
}
	
