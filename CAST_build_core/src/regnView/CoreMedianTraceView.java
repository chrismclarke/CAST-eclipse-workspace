package regnView;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


abstract public class CoreMedianTraceView extends DataView {
	protected String xKey, yKey;
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
	protected double boundary[] = null;
	
	public CoreMedianTraceView(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, 
									double[] initialBoundaries) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.xKey = xKey;
		this.yKey = yKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		boundary = initialBoundaries;
	}
	
	static private double[] parseDoubles(String s) {
		StringTokenizer st = new StringTokenizer(s);
		double array[] = new double[st.countTokens()];
		for (int i=0 ; i<array.length ; i++)
			array[i] = Double.parseDouble(st.nextToken());
		return array;
	}
	
	public CoreMedianTraceView(DataSet theData, XApplet applet, HorizAxis xAxis, VertAxis yAxis,
										String xKey, String yKey, String initialBoundaries) {
		this(theData, applet, xAxis, yAxis, xKey, yKey, parseDoubles(initialBoundaries));
	}
	
	protected double[] getXMedians() {
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumValue x[] = xVar.getSortedData();
		int count[] = new int[boundary.length + 1];
		int group = 0;
		for (int i=0 ; i<x.length ; i++) {
			while (group < boundary.length && x[i].toDouble() > boundary[group])
				group++;
			count[group] ++;
		}
		int cumulative = 0;
		double median[] = new double[boundary.length + 1];
		for (int i=0 ; i<count.length ; i++) {
			if (count[i] == 0)
				median[i] = Double.NaN;
			else if ((count[i] & 0x1) == 0)		//	even
				median[i] = (x[cumulative + count[i] / 2 - 1].toDouble() + x[cumulative + count[i] / 2].toDouble()) * 0.5;
			else											//	odd
				median[i] = x[cumulative + count[i] / 2].toDouble();
			cumulative += count[i];
		}
		return median;
	}
	
	protected double[] getYMedians() {
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumVariable yGroup[] = new NumVariable[boundary.length + 1];
		for (int i=0 ; i<yGroup.length ; i++)
			yGroup[i] = new NumVariable("");
			
		ValueEnumeration xe = xVar.values();
		ValueEnumeration ye = yVar.values();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			Value y = ye.nextValue();
			int group = 0;
			for ( ; group < boundary.length ; group++) {
				if (x <= boundary[group])
					break;
			}
			yGroup[group].addValue(y);
		}
		
		double median[] = new double[boundary.length + 1];
		for (int i=0 ; i<yGroup.length ; i++) {
			if (yGroup[i].noOfValues() == 0)
				median[i] = Double.NaN;
			else {
				NumValue sorted[] = yGroup[i].getSortedData();
				if ((sorted.length & 0x1) == 0)		//even
					median[i] = (sorted[sorted.length / 2 - 1].toDouble() + sorted[sorted.length / 2].toDouble()) * 0.5;
				else
					median[i] = sorted[sorted.length / 2].toDouble();
			}
		}
		return median;
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	abstract protected boolean canDrawX(double x);
	
	public void paintView(Graphics g) {
		g.setColor(Color.yellow);
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
//		int index = 0;
		Point thePoint = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				thePoint = getScreenPoint(xVal, yVal, thePoint);
				if (thePoint != null)
					drawCrossBackground(g, thePoint);
			}
//			index++;
		}
		
		drawBackground(g);
		
		g.setColor(getForeground());
		xe = xVariable.values();
		ye = yVariable.values();
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			if (canDrawX(xVal.toDouble())) {
				thePoint = getScreenPoint(xVal, yVal, thePoint);
				if (thePoint != null)
					drawCross(g, thePoint);
			}
		}
	}
	
	abstract protected void drawBackground(Graphics g);
	
	protected void drawMedianTrace(Graphics g, double[] xMed, double[] yMed) {
		g.setColor(Color.blue);
		Point previous = null;
		Point next = null;
		for (int i=0 ; i<xMed.length ; i++)
			if (!Double.isNaN(xMed[i]) && !Double.isNaN(yMed[i]))
				try {
					int vertPos = yAxis.numValToPosition(yMed[i]);
					int horizPos = xAxis.numValToPosition(xMed[i]);
					next = translateToScreen(horizPos, vertPos, next);
					if (previous != null)
						g.drawLine(previous.x, previous.y, next.x, next.y);
					Point temp = previous;
					previous = next;
					next = temp;
				} catch (AxisException e) {
				}
				
		g.setColor(Color.red);
		int oldCrossSize = getCrossSize();
		setCrossSize(LARGE_CROSS);
		for (int i=0 ; i<xMed.length ; i++)
			if (!Double.isNaN(xMed[i]) && !Double.isNaN(yMed[i]))
				try {
					int vertPos = yAxis.numValToPosition(yMed[i]);
					int horizPos = xAxis.numValToPosition(xMed[i]);
					next = translateToScreen(horizPos, vertPos, next);
					drawBlob(g, next);
				} catch (AxisException e) {
				}
		setCrossSize(oldCrossSize);
	}

}
	
