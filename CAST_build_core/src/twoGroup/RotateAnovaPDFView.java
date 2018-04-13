package twoGroup;

import java.awt.*;

import dataView.*;
import random.*;
import models.*;
import graphics3D.*;


public class RotateAnovaPDFView extends Rotate3DView {
	
	static final private Color kVeryPaleBlue = new Color(0xCCEEFF);
	static final private Color kPaleBlue = new Color(0x99CCFF);
	static final private Color kMidBlue = new Color(0x3399FF);
	
	static final private double kZ50 = 0.6745;
	
	private String xKey, yDataKey;
	protected Normal3DArtist normalArtist;
	
	private boolean showData = false;
	private boolean showSDBand = false;
	
	private double jitterPropn;
	protected double jittering[] = null;
	
	private boolean show50PercentBand = false;
	
	public RotateAnovaPDFView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String yKey,
						String xKey, String dataKey, double jitterPropn) {
		super(theData, applet, yAxis, zAxis, xAxis, xKey, yKey, null);
		
		normalArtist = new Normal3DArtist(this, yKey, xKey, theData, yAxis, xAxis, true);
		this.xKey = xKey;
		this.yDataKey = dataKey;
		this.jitterPropn = jitterPropn;
	}
	
	public void setFixedMinSD(double minFixedSD) {
		normalArtist.setFixedMinSD(minFixedSD);
	}
	
	public void setShow50PercentBand(boolean show50PercentBand) {
		this.show50PercentBand = show50PercentBand;
	}
	
	public void setDensityColors(Color middleColor, Color tailColor) {
		normalArtist.setFillColor(middleColor);
		normalArtist.setHighlightColor(tailColor);
	}
	
	protected Point getScreenPoint(double y, int x, int noOfXCats, int dataIndex, Point thePoint) {
		if (Double.isNaN(x) || Double.isNaN(y))
			return null;
		
		double xFract = xAxis.numValToPosition(y);
		double yFract = 0.0;
		double zFract = zAxis.catValToPosition(x, noOfXCats);
		if (jittering != null)
			zFract += (1.0 / noOfXCats) * jittering[dataIndex] * jitterPropn;
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	public void setShowData(boolean showData) {
		this.showData = showData;
	}
	
	public void setShowSDBand(boolean showSDBand) {
		this.showSDBand = showSDBand;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (showSDBand)
			drawSDBand(g);
		
		if (map.getTheta2() < 89.5 || map.getTheta2() > 90.5)
			drawPDFs(g);
		
		if (showData)
			drawSample(g);
	}
	
	private void drawOneBand(Graphics g, double mean, double sd, double z, double xFract,
							double xOffset, int[] xCoord, int[] yCoord, Point crossPos, Color fillColor) {
		double highYFract = xAxis.numValToPosition(mean + z * sd);
		double lowYFract = xAxis.numValToPosition(mean - z * sd);
		crossPos = translateToScreen(map.mapH3DGraph(0.0, lowYFract, xFract - xOffset),
										map.mapV3DGraph(0.0, lowYFract, xFract - xOffset), crossPos);
		xCoord[0] = crossPos.x;
		yCoord[0] = crossPos.y;
		crossPos = translateToScreen(map.mapH3DGraph(0.0, lowYFract, xFract + xOffset),
										map.mapV3DGraph(0.0, lowYFract, xFract + xOffset), crossPos);
		xCoord[1] = crossPos.x;
		yCoord[1] = crossPos.y;
		crossPos = translateToScreen(map.mapH3DGraph(0.0, highYFract, xFract + xOffset),
										map.mapV3DGraph(0.0, highYFract, xFract + xOffset), crossPos);
		xCoord[2] = crossPos.x;
		yCoord[2] = crossPos.y;
		crossPos = translateToScreen(map.mapH3DGraph(0.0, highYFract, xFract - xOffset),
										map.mapV3DGraph(0.0, highYFract, xFract - xOffset), crossPos);
		xCoord[3] = crossPos.x;
		yCoord[3] = crossPos.y;
		
		g.setColor(fillColor);
		g.fillPolygon(xCoord, yCoord, 4);
		g.drawPolygon(xCoord, yCoord, 4);
	}
	
	private void drawSDBand(Graphics g) {
		CatVariable xVar = (CatVariable)getVariable(xKey);
		GroupsModelVariable model = (GroupsModelVariable)getVariable(yKey);
		int xCoord[] = new int[4];
		int yCoord[] = new int[4];
		Point crossPos = new Point(0,0);
		int noOfXCats = xVar.noOfCategories();
		double xOffset = (0.6 / noOfXCats) * jitterPropn;	//	20% more than to cover cross centres
		
		for (int i=0 ; i<noOfXCats ; i++) {
			double xFract = zAxis.catValToPosition(i, noOfXCats);
			double mean = model.getMean(i).toDouble();
			double sd = model.getSD(i).toDouble();
			
			if (show50PercentBand) {
				drawOneBand(g, mean, sd, 2.0, xFract, xOffset, xCoord, yCoord, crossPos, kVeryPaleBlue);
				drawOneBand(g, mean, sd, kZ50, xFract, xOffset, xCoord, yCoord, crossPos, kPaleBlue);
			}
			else
				drawOneBand(g, mean, sd, 2.0, xFract, xOffset, xCoord, yCoord, crossPos, kPaleBlue);
			
			double meanFract = xAxis.numValToPosition(mean);
			crossPos = translateToScreen(map.mapH3DGraph(0.0, meanFract, xFract - xOffset),
											map.mapV3DGraph(0.0, meanFract, xFract - xOffset), crossPos);
			int lowMeanX = crossPos.x;
			int lowMeanY = crossPos.y;
			crossPos = translateToScreen(map.mapH3DGraph(0.0, meanFract, xFract + xOffset),
											map.mapV3DGraph(0.0, meanFract, xFract + xOffset), crossPos);
			int highMeanX = crossPos.x;
			int highMeanY = crossPos.y;
			g.setColor(kMidBlue);
			g.drawLine(lowMeanX, lowMeanY, highMeanX, highMeanY);
		}
		g.setColor(getForeground());
	}
	
	private void drawSample(Graphics g) {
		checkJittering();
		
		NumVariable yVariable = (NumVariable)getVariable(yDataKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int noOfXCats = xVariable.noOfCategories();
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point crossPos = null;
		int i=0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			crossPos = getScreenPoint(ye.nextDouble(), xVariable.labelIndex(xe.nextValue()),
																								noOfXCats, i, crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
			i ++;
		}
	}
	
	private void drawOnePDF(Graphics g, GroupsModelVariable model, CatVariable xVar, int i) {
		if (showSDBand && show50PercentBand) {
			double mean = model.getMean(i).toDouble();
			double sd = model.getSD(i).toDouble();
			double highYFract = mean + kZ50 * sd;
			double lowYFract = mean - kZ50 * sd;
			normalArtist.paintDistn(g, xVar.getLabel(i), lowYFract, highYFract);
		}
		else
			normalArtist.paintDistn(g, xVar.getLabel(i));
	}
	
	private void drawPDFs(Graphics g) {
		CatVariable xVar = (CatVariable)getVariable(xKey);
		GroupsModelVariable model = (GroupsModelVariable)getVariable(yKey);
		
		if (map.xAxisBehind())
			for (int i=0 ; i<xVar.noOfCategories() ; i++)
				drawOnePDF(g, model, xVar, i);
		else
			for (int i=xVar.noOfCategories()-1 ; i>=0 ; i--)
				drawOnePDF(g, model, xVar, i);
	}
	
	private void checkJittering() {
		int dataLength = ((Variable)getVariable(yDataKey)).noOfValues();
		if (jitterPropn > 0.0 && (jittering == null || jittering.length != dataLength)) {
			RandomRectangular generator = new RandomRectangular(dataLength, -0.5, 0.5);
			jittering = generator.generate();
		}
	}
	
	public void resetJittering() {
		jittering = null;
	}
}
