package graphics;

import java.awt.*;

import dataView.*;
import graphics3D.*;


public class RotatePieView extends Rotate3DView {
	
	static final private double kTwoPi = 2.0 * Math.PI;
	
//	static protected Color darkerColor(Color c, double propn) {
//		int m1 = (int)Math.round(propn * 256);
//		int m2 = 256 - m1;
//		return new Color(c.getRed() * m2 / 256, c.getGreen() * m2 / 256,
//																										c.getBlue() * m2 / 256);
//	}
	
	private String yKey;
	private Color catColors[];
	private Color darkCatColors[];
	
	private double thickness;
	
	public RotatePieView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String yKey, Color[] catColors, double thickness) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.yKey = yKey;
		this.catColors = catColors;
		this.thickness = thickness;
		darkCatColors = new Color[catColors.length];
		for (int i=0 ; i<catColors.length ; i++)
			darkCatColors[i] = darkenColor(catColors[i], 0.5);
	}
	
	public void setThickness(double thickness) {
		this.thickness = thickness;
	}
	
	private void draw2DPie(Graphics g, double propns[], double y) {
		RotateMap map = getCurrentMap();
		
		double sin = map.getSinTheta1();
		double cos = map.getCosTheta1();
		boolean fromTop = map.getTheta2() < 180.0;
		
		Point p0 = getScreenPoint(cos - sin, y, sin + cos, null);
		Point p1 = getScreenPoint(sin - cos, y, -sin - cos, null);
		
		int xMin = Math.min(p0.x, p1.x);
		int xMax = Math.max(p0.x, p1.x);
		int yMin = Math.min(p0.y, p1.y);
		int yMax = Math.max(p0.y, p1.y);
		
		double endCum = map.getTheta1() / 360.0;
		for (int i=0 ; i<propns.length ; i++) {
			if (endCum < 0.0)
				endCum += 1.0;
			double startCum = endCum;
			endCum -= propns[i];
			
			int startDegrees = (int)Math.round(360 * startCum);
			int endDegrees = (int)Math.round(360 * endCum);
			g.setColor(catColors[i]);
			if (fromTop)
				g.fillArc(xMin, yMin, xMax - xMin, yMax - yMin, startDegrees, (endDegrees - startDegrees));
			else
				g.fillArc(xMin, yMin, xMax - xMin, yMax - yMin, 360 - startDegrees, (startDegrees - endDegrees));
		}
		
		g.setColor(getForeground());
		endCum = 0.0;
		for (int i=0 ; i<propns.length ; i++) {
			endCum += propns[i];
			
			double endAngle = kTwoPi * endCum;
			double endX = Math.cos(endAngle);
			double endZ = Math.sin(endAngle);
			p0 = getScreenPoint(0.0, y, 0.0, p0);
			p1 = getScreenPoint(endX, y, endZ, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		g.drawOval(xMin, yMin, xMax - xMin, yMax - yMin);
	}
	
	private void fillBetweenPies(Graphics g, double propns[], double yLow, double yHigh) {
		RotateMap map = getCurrentMap();
		
		int xCoord[] = new int[5];
		int yCoord[] = new int[5];
		Point p[] = new Point[4];
		
		double theta = map.getTheta1();
		double visibleStartAngle = theta * kTwoPi / 360.0;
		double thetaPlus180 = theta + 180.0;
		double visibleEndAngle = thetaPlus180 * kTwoPi / 360.0;
		
		double endCum = 0.0;
		for (int i=0 ; i<propns.length ; i++) {
//		for (int i=0 ; i<1 ; i++) {
			if (endCum < 0.0)
				endCum += 1.0;
			double startAngle = kTwoPi * endCum;
			
			endCum += propns[i];
			double endAngle = kTwoPi * endCum;
			
			double drawStartAngle = startAngle;
			double drawEndAngle = endAngle;
			if (drawEndAngle < visibleStartAngle) {
				drawStartAngle += kTwoPi;
				drawEndAngle += kTwoPi;
			}
			drawStartAngle = Math.max(drawStartAngle, visibleStartAngle);
			drawEndAngle = Math.min(drawEndAngle, visibleEndAngle);
			
//			System.out.println("visibleStartAngle = " + visibleStartAngle + ", visibleEndAngle = " + visibleEndAngle
//								+ ", startAngle = " + startAngle + ", endAngle = " + endAngle 
//								+ ", drawStartAngle = " + drawStartAngle + ", drawEndAngle = " + drawEndAngle); 
			
			if (drawStartAngle < drawEndAngle) {
				double startX = Math.cos(drawStartAngle);
				double startZ = Math.sin(drawStartAngle);
				double endX = Math.cos(drawEndAngle);
				double endZ = Math.sin(drawEndAngle);
				
				p[0] = getScreenPoint(startX, yLow, startZ, p[0]);
				p[1] = getScreenPoint(startX, yHigh, startZ, p[1]);
				p[2] = getScreenPoint(endX, yHigh, endZ, p[2]);
				p[3] = getScreenPoint(endX, yLow, endZ, p[3]);
				
				for (int j=0 ; j<4 ; j++) {
					xCoord[j] = p[j].x;
					yCoord[j] = p[j].y;
				}
				xCoord[4] = xCoord[0];
				yCoord[4] = yCoord[0];
				
				g.setColor(catColors[i]);
//				g.setColor(Color.lightGray);
				g.fillPolygon(xCoord, yCoord, 5);
				g.drawLine(xCoord[1], yCoord[1], xCoord[2], yCoord[2]);
				g.drawLine(xCoord[3], yCoord[3], xCoord[4], yCoord[4]);
				
				g.setColor(getForeground());
				g.drawLine(xCoord[0], yCoord[0], xCoord[1], yCoord[1]);
				g.drawLine(xCoord[2], yCoord[2], xCoord[3], yCoord[3]);
			}
		}
		
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		CatVariable yVar = (CatVariable)getVariable(yKey);
		int counts[] = yVar.getCounts();
		double propns[] = new double[counts.length];
		double total = yVar.noOfValues();
		for (int i=0 ; i<counts.length ; i++)
			propns[i] = counts[i] / total;
		
		if (getCurrentMap().getTheta2() < 180) {		//	viewing from top
			draw2DPie(g, propns, 0.0);
			fillBetweenPies(g, propns, 0.0, thickness);
			draw2DPie(g, propns, thickness);
		}
		else {		//	viewing from underneath
			draw2DPie(g, propns, thickness);
			fillBetweenPies(g, propns, 0.0, thickness);
			draw2DPie(g, propns, 0.0);
		}
	}
}
	
