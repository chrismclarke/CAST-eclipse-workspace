package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;


public class ModelGraphics {
	
	static final private int outerArrowX[] = {-3, -3, -6,  0, 6, 3,  3,  6,   0, -6, -3};
	static final private int outerArrowY[] = {-4,  4,  4, 10, 4, 4, -4, -4, -10, -4, -4};
	static final private int innerArrowX[] = {-1, -1, -3, 0, 3, 1,  1,  3,  0, -3, -1};
	static final private int innerArrowY[] = {-5,  5,  5, 8, 5, 5, -5, -5, -8, -5, -5};
	
	static final private int outerPointArrowX[] = {-3, -3,  3,  3,  6,   0, -6, -3};
	static final private int outerPointArrowY[] = {-6, -4, -4, -6, -6, -12, -6, -6};
	static final private int innerPointArrowX[] = {-1, -1,  1,  1,  3,   0, -3, -1};
	static final private int innerPointArrowY[] = {-7, -5, -5, -7, -7, -10, -7, -7};
	
	static final private int kAnchorRadius = 3;
	static final private Color kAnchorColor = new Color(0x6699FF);
	
	static final public int kHandleLength = 21;
	static final public int kHandleWidth = 13;
	
	static public void drawHandle(Graphics g, Point p, boolean bold, Color redColor,
																								Color blackColor, boolean isVertical) {
		int ax[] = new int[outerArrowX.length];
		int ay[] = new int[outerArrowX.length];
		for (int i=0 ; i<ax.length ; i++)
			if (isVertical) {
				ax[i] = outerArrowX[i] + p.x;
				ay[i] = outerArrowY[i] + p.y;
			}
			else {
				ax[i] = outerArrowY[i] + p.x;
				ay[i] = outerArrowX[i] + p.y;
			}
		Color oldColor = g.getColor();
		g.setColor(bold ? blackColor : redColor);
		g.fillPolygon(ax, ay, ax.length);
		g.drawPolygon(ax, ay, ax.length);
		
		for (int i=0 ; i<ax.length ; i++)
			if (isVertical) {
				ax[i] = innerArrowX[i] + p.x;
				ay[i] = innerArrowY[i] + p.y;
			}
			else {
				ax[i] = innerArrowY[i] + p.x;
				ay[i] = innerArrowX[i] + p.y;
			}
		g.setColor(bold ? redColor : Color.white);
		g.fillPolygon(ax, ay, ax.length);
		g.drawPolygon(ax, ay, ax.length);
		
		g.setColor(oldColor);
	}
	
	static public void drawHandle(Graphics g, Point p, boolean bold, Color redColor, Color blackColor) {
		drawHandle(g, p, bold, redColor, blackColor, true);
	}
	
	static public void drawHandle(Graphics g, Point p, boolean bold, boolean isVertical) {
		drawHandle(g,  p, bold, Color.red, Color.black, isVertical);
	}
	
	static public void drawHandle(Graphics g, Point p, boolean bold) {
		drawHandle(g,  p, bold, true);
	}
	
	static public void drawPointHandle(Graphics g, Point p, boolean bold, Color redColor,
																																			Color blackColor) {
		Color oldColor = g.getColor();
		
		int ax[] = new int[outerPointArrowX.length];
		int ay[] = new int[outerPointArrowY.length];
		
		g.setColor(bold ? blackColor : redColor);
		
		for (int k=-1 ; k<=1 ; k+=2) {
			for (int i=0 ; i<ax.length ; i++) {
				ax[i] = p.x + outerPointArrowX[i] * k;
				ay[i] = p.y + outerPointArrowY[i] * k;
			}
			g.fillPolygon(ax, ay, ax.length);
			g.drawPolygon(ax, ay, ax.length);
		}
		
		g.setColor(bold ? redColor : Color.white);
		
		for (int k=-1 ; k<=1 ; k+=2) {
			for (int i=0 ; i<ax.length ; i++) {
				ax[i] = p.x + innerPointArrowX[i] * k;
				ay[i] = p.y + innerPointArrowY[i] * k;
			}
			g.fillPolygon(ax, ay, ax.length);
			g.drawPolygon(ax, ay, ax.length);
		}
		
		g.setColor(oldColor);
	}
	
	static public void drawAnchor(Graphics g, Point p, Color c) {
		Color oldColor = g.getColor();
		g.setColor(c);
		g.fillOval(p.x - kAnchorRadius, p.y - kAnchorRadius, 2 * kAnchorRadius, 2 * kAnchorRadius);
		g.setColor(oldColor);
	}
	
	static public void drawAnchor(Graphics g, Point p) {
		drawAnchor(g, p, kAnchorColor);
	}
	
	
//----------------------------------------------------------------------
	
	
	static private PointArray endPoints(NumCatAxis yAxis, NumCatAxis xAxis, double b0, double b1,
																								PointArray p) {
		if (p == null)
			p = new PointArray(4);
		else
			p.reset();
		
		double lowX = xAxis.minOnAxis;
		double highX = xAxis.maxOnAxis;
		double slop = (highX - lowX) * 0.1;
		lowX -= slop;
		highX += slop;
		double lowY = yAxis.minOnAxis;
		double highY = yAxis.maxOnAxis;
		slop = (highY - lowY) * 0.1;
		lowY -= slop;
		highY += slop;
		
		double yLowX = b0 + b1 * lowX;
		double yHighX = b0 + b1 * highX;
		if (yLowX < lowY) {
			p.addPoint(lowX, lowY);
			p.addPoint((lowY - b0) / b1, lowY);
		}
		else if (yLowX > highY) {
			p.addPoint(lowX, highY);
			p.addPoint((highY - b0) / b1, highY);
		}
		else
			p.addPoint(lowX, yLowX);
			
		if (yHighX < lowY) {
			p.addPoint((lowY - b0) / b1, lowY);
			p.addPoint(highX, lowY);
		}
		else if (yHighX > highY) {
			p.addPoint((highY - b0) / b1, highY);
			p.addPoint(highX, highY);
		}
		else
			p.addPoint(highX, yHighX);
		
		return p;
	}
	
	static public void draw2DLine(Graphics g, DataView view, NumCatAxis yAxis,
														NumCatAxis xAxis, double b0, double b1) {
		PointArray p = endPoints(yAxis, xAxis, b0, b1, null);
		
		Point p0 = view.translateToScreen(xAxis.numValToRawPosition(p.x[0]),
														yAxis.numValToRawPosition(p.y[0]), null);
		Point p1 = null;
		for (int i=1 ; i<p.nPoints ; i++) {
			p1 = view.translateToScreen(xAxis.numValToRawPosition(p.x[i]),
														yAxis.numValToRawPosition(p.y[i]), p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			Point temp = p1;
			p1 = p0;
			p0 = temp;
		}
	}
	
	
	static public void draw2DModel(Graphics g, DataView view, NumCatAxis yAxis, NumCatAxis xAxis,
											double b0, double b1, double s0, Color lineColor, Color shadeColor) {
		PointArray pHigh = endPoints(yAxis, xAxis, b0 + 2.0 * s0, b1, null);
		PointArray pLow = endPoints(yAxis, xAxis, b0 - 2.0 * s0, b1, null);
		
		int[] x = new int[pHigh.nPoints + pLow.nPoints + 1];
		int[] y = new int[pHigh.nPoints + pLow.nPoints + 1];
		Point p0 = null;
		
		int pointIndex = 0;
		for (int i=0 ; i<pHigh.nPoints ; i++) {
			p0 = view.translateToScreen(xAxis.numValToRawPosition(pHigh.x[i]),
														yAxis.numValToRawPosition(pHigh.y[i]), p0);
			x[pointIndex] = p0.x;
			y[pointIndex++] = p0.y;
		}
		
		for (int i=pLow.nPoints-1 ; i>=0 ; i--) {
			p0 = view.translateToScreen(xAxis.numValToRawPosition(pLow.x[i]),
														yAxis.numValToRawPosition(pLow.y[i]), p0);
			x[pointIndex] = p0.x;
			y[pointIndex++] = p0.y;
		}
		
		x[pointIndex] = x[0];
		y[pointIndex++] = y[0];
		
		g.setColor(shadeColor);
		g.fillPolygon(x, y, pointIndex);
		g.setColor(view.getForeground());
		g.setColor(lineColor);
		g.drawPolygon(x, y, pointIndex);
		g.setColor(view.getForeground());
	}
}
