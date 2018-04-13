package graphics;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import dataView.*;
import axis.*;
import graphics3D.*;


abstract public class Core3DChartView extends Rotate3DView {
	static final private Color kBackColor = new Color(0xEEEEEE);
	static final private Color kBaseColor = new Color(0xDDDDDD);
	static final private Color kBackLineColor = new Color(0xCCCCCC);
	
	static final private int kAxisGap = 4;
//	static final private int kVertLabelGap = 4;
	static final private int kTickLength = 6;
	
	private String yName;
	protected double depth;
	
	public Core3DChartView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis,
																								D3Axis zAxis, String yName, double depth) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.depth = depth;
		this.yName = yName;
	}
	
	public void setDepth(double depth) {
		this.depth = depth;
	}
	
	private void drawBack(Graphics g, double depth, int[] xCoord, int[] yCoord) {
		g.setColor(kBackColor);
		
		double yMin = yAxis.getMinOnAxis();
		double yMax = yAxis.getMaxOnAxis();
		Point p = getScreenPoint(depth, yMin, 0.0, null);
		xCoord[0] = xCoord[4] = p.x;
		yCoord[0] = yCoord[4] = p.y;
		p = getScreenPoint(depth, yMax, 0.0, null);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		p = getScreenPoint(depth, yMax, 1.0, null);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		p = getScreenPoint(depth, yMin, 1.0, null);
		xCoord[3] = p.x;
		yCoord[3] = p.y;
		
		g.setColor(kBackColor);
		g.fillPolygon(xCoord, yCoord, 5);
		
		g.setColor(kBackLineColor);
		Enumeration e = getYAxisLabelEnumeration();
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			double yPropn = theLabel.position;
			double gridY = yPropn * yAxis.getMaxOnAxis();
			p = getScreenPoint(depth, gridY, 0.0, p);
			Point p2 = getScreenPoint(depth, gridY, 1.0, null);
			g.drawLine(p.x, p.y, p2.x, p2.y);
		}
	}
	
	private void drawSide(Graphics g, double zVal, double lowX, double highX,
																														int[] xCoord, int[] yCoord) {
		g.setColor(kBackColor);
		
		double yMin = yAxis.getMinOnAxis();
		double yMax = yAxis.getMaxOnAxis();
		
		Point p = getScreenPoint(lowX, yMin, zVal, null);
		xCoord[0] = xCoord[4] = p.x;
		yCoord[0] = yCoord[4] = p.y;
		p = getScreenPoint(lowX, yMax, zVal, null);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		p = getScreenPoint(highX, yMax, zVal, null);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		p = getScreenPoint(highX, yMin, zVal, null);
		xCoord[3] = p.x;
		yCoord[3] = p.y;
		
		g.setColor(kBackColor);
		g.fillPolygon(xCoord, yCoord, 5);
		
		g.setColor(kBackLineColor);
		Point p2 = null;
		Enumeration e = getYAxisLabelEnumeration();
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			double yPropn = theLabel.position;
			double gridY = yPropn * yAxis.getMaxOnAxis();
			p = getScreenPoint(lowX, gridY, zVal, p);
			p2 = getScreenPoint(highX, gridY, zVal, p2);
			g.drawLine(p.x, p.y, p2.x, p2.y);
		}
	}
	
	abstract protected double getZProportion(int i, int nVals);
																			//	defines spacing of items on z-axis
	
	abstract protected Value[] getZAxisLabels();
	
	private void drawZAxisLabels(Graphics g, double lowX, double highX, boolean zAxisBehind,
																										boolean xAxisBehind, boolean viewFromTop) {
		double yMin = yAxis.getMinOnAxis();
		Point p1 = getScreenPoint(lowX, yMin, 1.0, null);
		Point p2 = getScreenPoint(highX, yMin, 1.0, null);
		
		int dx = p1.x - p2.x;
		int dy = p1.y - p2.y;
		double angle = (dy == 0 && dx == 0) ? 0.5 * Math.PI : Math.atan2(dy, dx);
		int baseline = g.getFontMetrics().getAscent() / 3;
		int direction = viewFromTop ? 1 : -1;
		if (angle > 0.5 * Math.PI && angle <= Math.PI) {
			angle -= Math.PI;
			direction = -direction;
		}
		else if (angle <= 0 && angle > -0.5 * Math.PI)
			direction = -direction;
		else if (angle >= -Math.PI && angle < -0.5 * Math.PI)
			angle += Math.PI;
		
		Value[] labels = getZAxisLabels();
		int nVals = labels.length;
		for (int i=0 ; i<nVals ; i++) {
			double zVal = getZProportion(i, nVals);
			p1 = getScreenPoint(zAxisBehind ? highX : lowX, yMin, zVal, p1);
			p2 = getScreenPoint(zAxisBehind ? lowX : highX, yMin, zVal, p2);
			g.setColor(kBackLineColor);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			
			g.setColor(getForeground());
			Graphics2D g2d = (Graphics2D) g;
			
			AffineTransform oldTransform = g2d.getTransform();
			g2d.translate(p1.x, p1.y);
			g2d.rotate(angle);
			
			g2d.drawLine(0, 0, direction * kTickLength, 0);
			Value tempLabel = labels[i];
			if (direction > 0)
				tempLabel.drawRight(g, 2 * kTickLength, baseline);
			else
				tempLabel.drawLeft(g, -2 * kTickLength, baseline);
			g2d.setTransform(oldTransform);
		}
	}
	
	private void drawBase(Graphics g, double lowX, double highX, boolean zAxisBehind,
											boolean xAxisBehind, boolean viewFromTop, int[] xCoord, int[] yCoord) {
		double yMin = yAxis.getMinOnAxis();
//		double yMax = yAxis.getMaxOnAxis();
		
		Point p = getScreenPoint(lowX, yMin, 0.0, null);
		xCoord[0] = xCoord[4] = p.x;
		yCoord[0] = yCoord[4] = p.y;
		p = getScreenPoint(lowX, yMin, 1.0, p);
		xCoord[1] = p.x;
		yCoord[1] = p.y;
		p = getScreenPoint(highX, yMin, 1.0, p);
		xCoord[2] = p.x;
		yCoord[2] = p.y;
		p = getScreenPoint(highX, yMin, 0.0, p);
		xCoord[3] = p.x;
		yCoord[3] = p.y;
		
		g.setColor(kBaseColor);
		g.fillPolygon(xCoord, yCoord, 5);
		
		drawZAxisLabels(g, lowX, highX, zAxisBehind, xAxisBehind, viewFromTop);
	}
	
	private void drawCorner(Graphics g, double lowX, double highX, boolean zAxisBehind,
																		boolean xAxisBehind, int[] xCoord, int[] yCoord) {
		double yMin = yAxis.getMinOnAxis();
		double yMax = yAxis.getMaxOnAxis();
		
		g.setColor(kBaseColor);
		double xCorner = zAxisBehind ? lowX : highX;
		double zCorner = xAxisBehind ? 0.0 : 1.0;
		Point p1 = getScreenPoint(xCorner, yMin, zCorner, null);
		Point p2 = getScreenPoint(xCorner, yMax, zCorner, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	protected Enumeration getYAxisLabelEnumeration() {
		return yAxis.getLabelEnumeration();
	}
	
	protected String getYAxisName() {
		return yName;
	}
	
	private void drawYAxisValues(Graphics g, double xVal, double zVal, boolean leftNotRight) {
		g.setColor(getForeground());
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		Enumeration e = getYAxisLabelEnumeration();
		Point p = null;
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			NumValue labelValue = (NumValue)theLabel.label;
			double yPropn = theLabel.position;
			double gridY = yPropn * yAxis.getMaxOnAxis();
			p = getScreenPoint(xVal, gridY, zVal, p);
			if (leftNotRight)
				labelValue.drawLeft(g, p.x - kAxisGap, p.y + ascent / 2);
			else
				labelValue.drawRight(g, p.x + kAxisGap, p.y + ascent / 2);
		}
		
		p = getScreenPoint(xVal, yAxis.getMaxOnAxis(), zVal, p);
		String name = getYAxisName();
		int nameWidth = fm.stringWidth(name);
		int nameStart = Math.max(2, Math.min(getSize().width - 2 - nameWidth, p.x - nameWidth / 2));
		g.drawString(name, nameStart, p.y - ascent);
	}
	
	abstract protected void drawInterior(Graphics g, double lowX, double highX,
											boolean zAxisBehind, boolean xAxisBehind, boolean viewFromTop,
											int[] xCoord, int[] yCoord);
	
	protected double getLowX(double depth) {
		return (1.0 - depth) * 0.5;
	}
	
	protected double getHighX(double depth) {
		return 1.0 - getLowX(depth);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		boolean zAxisBehind = getCurrentMap().zAxisBehind();
		boolean xAxisBehind = getCurrentMap().xAxisBehind();
		boolean viewFromTop = getCurrentMap().getTheta2() < 180.0;
		
		double lowX = getLowX(depth);
		double highX = getHighX(depth);
		
		int xCoord[] = new int[5];
		int yCoord[] = new int[5];
		
		drawBack(g, zAxisBehind ? lowX : highX, xCoord, yCoord);
		drawSide(g, xAxisBehind ? 0.0 : 1.0, lowX, highX, xCoord, yCoord);
		if (viewFromTop)
			drawBase(g, lowX, highX, zAxisBehind, xAxisBehind, viewFromTop, xCoord, yCoord);
		drawCorner(g, lowX, highX, zAxisBehind, xAxisBehind, xCoord, yCoord);
		
		drawInterior(g, lowX, highX, zAxisBehind, xAxisBehind, viewFromTop, xCoord, yCoord);
		
		if (!viewFromTop)
			drawBase(g, lowX, highX, zAxisBehind, xAxisBehind, viewFromTop, xCoord, yCoord);
		
		double leftZ = zAxisBehind ? 1.0 : 0.0;
		double rightZ = zAxisBehind ? 0.0 : 1.0;
		double leftX = xAxisBehind ? lowX : highX;
		double rightX = xAxisBehind ? highX : lowX;
		
		drawYAxisValues(g, leftX, leftZ, true);
		drawYAxisValues(g, rightX, rightZ, false);
		
	}
}
	
