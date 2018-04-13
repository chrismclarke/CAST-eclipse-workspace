package linMod;

import java.awt.*;
import java.util.*;

import dataView.*;
import random.*;
import graphics3D.*;


public class RotateHistoView extends Rotate3DView {
	
	static final private double kMaxHtFactor = 0.75;
	static final private Color kCrossColor = Color.blue;
	
//	private boolean showingHisto = true;
	private boolean initialised = false;
	
	private String classInfoParam, sortedXParam;
	
	private double class0Start, classWidth;
	private int noOfClasses;
	protected NumValue sortedX[];
	private int xyCount[][];
	private int xCount[];
	private double maxProb;
	
	private double jitterFraction = 0.0;
	private double jittering[] = null;
	
	public RotateHistoView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String classInfoParam, String sortedXParam) {
		super(theData, applet, yAxis, zAxis, xAxis, xKey, yKey, null);
		this.classInfoParam = classInfoParam;
		this.sortedXParam = sortedXParam;
		setCrossSize(LARGE_CROSS);
	}
	
	public void setJitterFraction(double jitterFraction) {
		this.jitterFraction = jitterFraction;
	}
	
	protected boolean initialise() {
		if (!initialised) {
			StringTokenizer theParams = new StringTokenizer(classInfoParam);
			class0Start = Double.parseDouble(theParams.nextToken());
			classWidth = Double.parseDouble(theParams.nextToken());
			noOfClasses = Integer.parseInt(theParams.nextToken());
			
			theParams = new StringTokenizer(sortedXParam);
			sortedX = new NumValue[theParams.countTokens()];
			xyCount = new int[sortedX.length][];
			xCount = new int[sortedX.length];
			for (int i=0 ; i<sortedX.length ; i++) {
				sortedX[i] = (new NumValue(theParams.nextToken()));
				xyCount[i] = new int[noOfClasses];
			}
			
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			ValueEnumeration xe = xVariable.values();
			ValueEnumeration ye = yVariable.values();
			while (xe.hasMoreValues() && ye.hasMoreValues()) {
				double y = ye.nextDouble();
				double x = xe.nextDouble();
				for (int i=0 ; i<sortedX.length ; i++)
					if (x == sortedX[i].toDouble()) {
						xCount[i] ++;
						double lowY = class0Start;
						if (y > lowY) {
							for (int j=0 ; j<noOfClasses ; j++) {
								lowY += classWidth;
								if (y < lowY) {
									xyCount[i][j] ++;
									break;
								}
							}
						}
						break;
					}
			}
			
			maxProb = 0.0;
			for (int i=0 ; i<sortedX.length ; i++) {
				int maxCount = 0;
				for (int j=0 ; j<noOfClasses ; j++)
					if (xyCount[i][j] > maxCount)
						maxCount = xyCount[i][j];
				double maxRowProb = (double)maxCount / xCount[i];
				if (maxRowProb > maxProb)
					maxProb = maxRowProb;
			}
			
			int nValues = xVariable.noOfValues();
			
			RandomRectangular generator = new RandomRectangular(nValues, -0.5, 0.5);
			jittering = generator.generate();
			
			initialised = true;
			return true;
		}
		
		return false;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		initialise();
		
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point crossPos = null;
		int index = 0;
		double xGap = sortedX[1].toDouble() - sortedX[0].toDouble();
		double maxJitter = xGap * jitterFraction;
		g.setColor(kCrossColor);
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double x = xe.nextDouble();
			if (jittering != null)
				x += jittering[index] * maxJitter;
			crossPos = getScreenPoint(ye.nextDouble(), 0.0, x, crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
			index ++;
		}
		
		if (map.getTheta2() < 89.5 || map.getTheta2() > 90.5)
			drawHisto(g);
	}
	
	private void drawAtX(double sortedX, int[] count, int totalCount, Point crossPos,
																int[] xPoint, int[] yPoint, Graphics g) {
		double y = class0Start;
		for (int j=0 ; j<noOfClasses ; j++) {
			double density = ((double)count[j]) / totalCount / maxProb * kMaxHtFactor;
			crossPos = getScreenPoint(y, 0.0, sortedX, crossPos);
			xPoint[0] = crossPos.x;
			yPoint[0] = crossPos.y;
			crossPos = getScreenPoint(y, density, sortedX, crossPos);
			xPoint[1] = crossPos.x;
			yPoint[1] = crossPos.y;
			
			y += classWidth;
			crossPos = getScreenPoint(y, density, sortedX, crossPos);
			xPoint[2] = crossPos.x;
			yPoint[2] = crossPos.y;
			crossPos = getScreenPoint(y, 0.0, sortedX, crossPos);
			xPoint[3] = crossPos.x;
			yPoint[3] = crossPos.y;
			
			g.setColor(Color.lightGray);
			g.fillPolygon(xPoint, yPoint, 4);
			g.setColor(getForeground());
			for (int k=0 ; k<3 ; k++)
				g.drawLine(xPoint[k], yPoint[k], xPoint[k+1], yPoint[k+1]);
		}
		
	}
	
	private void drawHisto(Graphics g) {
		int xPoint[] = new int[4];
		int yPoint[] = new int[4];
		Point crossPos = null;
		
		if (map.xAxisBehind())
			for (int i=0 ; i<sortedX.length ; i++)
				drawAtX(sortedX[i].toDouble(), xyCount[i], xCount[i], crossPos, xPoint, yPoint, g);
		else
			for (int i=sortedX.length-1 ; i>=0 ; i--)
				drawAtX(sortedX[i].toDouble(), xyCount[i], xCount[i], crossPos, xPoint, yPoint, g);
	}
}
	
