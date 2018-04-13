package statistic;

import java.awt.*;

import dataView.*;
import axis.*;


public class BalanceView extends DragLocationView {
	
	private int balanceHeight;
	
	public BalanceView(DataSet theData, XApplet applet, DragValAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	private boolean initialised = false;
	
	private void paintBalance(Graphics g, Point pivot) {
		int x[] = {0, 2, 4, -4, -2};
		int y[] = {0, 2, 15, 15, 2};
		
		if (selectedVal) {
			g.setColor(Color.yellow);
			g.fillRect(pivot.x - 2, pivot.y + y[2], 5, getSize().height - pivot.y - y[2]);
		}
		g.setColor(Color.red);
		g.drawLine(pivot.x, pivot.y + y[2], pivot.x, getSize().height - 1);
		
		for (int i=0 ; i<5 ; i++) {
			x[i] += pivot.x;
			y[i] += pivot.y;
		}
		g.fillPolygon(x, y, 5);
		
		g.fillRect(getViewBorder().left - 3, getSize().height - getViewBorder().bottom, 5, getViewBorder().bottom);
		g.fillRect(getSize().width - getViewBorder().right - 3, getSize().height - getViewBorder().bottom, 5, getViewBorder().bottom);
	}
	
	private void paintBeam(Graphics g, int pivotPos, Point factors) {
		int v1 = balanceHeight + pivotPos * factors.x / factors.y;
		int h2 = axis.getAxisLength();
		int v2 = balanceHeight + (pivotPos - h2) * factors.x / factors.y;
		Point p1 = translateToScreen(0, v1, null);
		Point p2 = translateToScreen(h2, v2, null);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	private Point getScreenPoint(double value, int pivotPos, Point factors, Point thePoint) {
		try {
			int horizPos = axis.numValToPosition(value);
			int vertPos = balanceHeight + getCrossSize() + (pivotPos - horizPos) * factors.x / factors.y;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private double mean(NumVariable variable) {
		double sum = 0.0;
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			sum += nextVal;
		}
		return sum / variable.noOfValues();
	}
	
	private Point getFactors(int pivotPos, int balancePos) {
		int denom = Math.max(pivotPos, axis.getAxisLength() - pivotPos);
		int numer = balancePos - pivotPos;
		
		if (numer != 0) {
			int minPos = 0;
			if (pivotPos != minPos)
				numer = Math.max(Math.min(numer,
									(getSize().height - getViewBorder().top - getViewBorder().bottom - balanceHeight)
																								* denom / (pivotPos - minPos)),
									- balanceHeight * denom / (pivotPos - minPos));
			
			int maxPos = axis.getAxisLength() - 1;
			if (pivotPos != maxPos)
				numer = Math.min(Math.max(numer,
									(getSize().height - getViewBorder().top - getViewBorder().bottom - balanceHeight)
																								* denom / (pivotPos - maxPos)),
								- balanceHeight * denom / (pivotPos - maxPos));
		}
		return new Point(numer, denom);
	}
	
	public void paintView(Graphics g) {
		if (!initialised) {
			requestFocus();
			initialised = true;
		}
		balanceHeight = (getSize().height - getViewBorder().top - getViewBorder().bottom) / 2;
		
		NumVariable variable = getNumVariable();
		DragValAxis theAxis = (DragValAxis)axis;
		NumValue constValue = theAxis.getAxisVal();
		
		try {
			int pivotPos = axis.numValToPosition(constValue.toDouble());
			Point pivot = translateToScreen(pivotPos, balanceHeight, null);
			int balancePos = axis.numValToPosition(mean(variable));
			Point factors = getFactors(pivotPos, balancePos);
			
			paintBalance(g, pivot);
			paintBeam(g, pivotPos, factors);
			
			Point thePoint = null;
			
			NumValue xSorted[] = variable.getSortedData();
			double xPrev = Double.NaN;
			int xRepeat = 0;
			
			for (int i=0 ; i<xSorted.length ; i++) {
				double nextX = xSorted[i].toDouble();
				thePoint = getScreenPoint(nextX, pivotPos, factors, thePoint);
				if (nextX == xPrev)
					xRepeat ++;
				else {
					xRepeat = 0;
					xPrev = nextX;
				}
				thePoint.y -= xRepeat * (getCrossSize() * 2 + 1);
				if (thePoint != null) {
					if (nextX < constValue.toDouble())
						g.setColor(Color.blue);
					else
						g.setColor(darkGreen);		
					drawBlob(g, thePoint);
				}
				
			}
		} catch (AxisException e) {
		}
	}
}
	
