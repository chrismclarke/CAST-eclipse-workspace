package twoFactor;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class RotateFactorModelView extends Rotate3DView {
	
	static final private Color kVeryPaleBlue = new Color(0xCCEEFF);
//	static final private Color kPaleBlue = new Color(0x99CCFF);
	static final private Color kMeanColor = new Color(0x660000);
	static final private Color kResidualColor = Color.red;
	
	private String xKey, yDataKey;
	protected Normal3DArtist normalArtist;
	
	private boolean showSDBand = false;
	
	private boolean jitteringInitialised = false;
	private double jitterPropn;
	private double pJitter[] = null;
	
	public RotateFactorModelView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String yKey,
						String xKey, String dataKey, double jitterPropn) {
		super(theData, applet, yAxis, zAxis, xAxis, xKey, yKey, null);
		
		this.xKey = xKey;
		this.yDataKey = dataKey;
		this.jitterPropn = jitterPropn;
		
		normalArtist = new Normal3DArtist(this, yKey, xKey, theData, yAxis, xAxis, true);
	}
	
	public void setFixedMinSD(double minFixedSD) {
		normalArtist.setFixedMinSD(minFixedSD);
	}
	
	protected Point getScreenPoint(double y, int x, int noOfXCats, int dataIndex, Point thePoint) {
		if (Double.isNaN(x) || Double.isNaN(y))
			return null;
		
		double xFract = xAxis.numValToPosition(y);
		double yFract = 0.0;
		double zFract = zAxis.catValToPosition(x, noOfXCats);
		if (pJitter != null)
			zFract += (1.0 / noOfXCats) * pJitter[dataIndex] * jitterPropn;
		return translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), thePoint);
	}
	
	public void setShowSDBand(boolean showSDBand) {
		this.showSDBand = showSDBand;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		boolean fromAbove = map.getTheta2() < 180;
		
		if (fromAbove) {
			if (showSDBand)
				drawSDBand(g);
			if (map.getTheta2() < 89.5 || map.getTheta2() > 90.5)
				drawPDFs(g);
		}
		else {
			if (map.getTheta2() < 89.5 || map.getTheta2() > 90.5)
				drawPDFs(g);
			if (showSDBand)
				drawSDBand(g);
		}
		
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
			
			drawOneBand(g, mean, sd, 2.0, xFract, xOffset, xCoord, yCoord, crossPos, kVeryPaleBlue);
			
			double meanFract = xAxis.numValToPosition(mean);
			crossPos = translateToScreen(map.mapH3DGraph(0.0, meanFract, xFract - xOffset),
											map.mapV3DGraph(0.0, meanFract, xFract - xOffset), crossPos);
			int lowMeanX = crossPos.x;
			int lowMeanY = crossPos.y;
			crossPos = translateToScreen(map.mapH3DGraph(0.0, meanFract, xFract + xOffset),
											map.mapV3DGraph(0.0, meanFract, xFract + xOffset), crossPos);
			int highMeanX = crossPos.x;
			int highMeanY = crossPos.y;
			g.setColor(kMeanColor);
			g.drawLine(lowMeanX, lowMeanY, highMeanX, highMeanY);
		}
		g.setColor(getForeground());
	}
	
	private void drawOnePDF(Graphics g, GroupsModelVariable model, CatVariable xVar, int i) {
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
	
	private void initialiseJittering(CatVariable xCatVar) {
		if (jitteringInitialised)
			return;
		jitteringInitialised = true;
		
		int noOfVals = xCatVar.noOfValues();
		int jitter[] = new int[noOfVals];
		int noOfCats = xCatVar.noOfCategories();
		int noInXCat[] = new int[noOfCats];
		
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xCatVar.getItemCategory(i);
			jitter[i] = noInXCat[xCat];
			noInXCat[xCat] ++;
		}
		
		int maxInCat = 0;
		for (int i=0 ; i<noOfCats ; i++)
			maxInCat = Math.max(maxInCat, noInXCat[i]);
		double pScale = 1.0 / maxInCat;
		
		pJitter = new double[noOfVals];
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xCatVar.getItemCategory(i);
			pJitter[i] = (jitter[i] - 0.5 * noInXCat[xCat]) * pScale;
		}
	}
	
	private void drawSample(Graphics g) {
		NumVariable yVariable = (NumVariable)getVariable(yDataKey);
		GroupsModelVariable model = (GroupsModelVariable)getVariable(yKey);
		CatVariable xVariable = (CatVariable)getVariable(xKey);
		int noOfXCats = xVariable.noOfCategories();
		
		initialiseJittering(xVariable);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point crossPos = null;
		Point meanPos = null;
		int i=0;
		g.setColor(kResidualColor);
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			double mean = model.evaluateMean(x);
			crossPos = getScreenPoint(y, xVariable.labelIndex(x), noOfXCats, i, crossPos);
			meanPos = getScreenPoint(mean, xVariable.labelIndex(x), noOfXCats, i, meanPos);
			if (crossPos != null && meanPos != null)
				g.drawLine(crossPos.x, crossPos.y, meanPos.x, meanPos.y);
			i ++;
		}
		
		xe = xVariable.values();
		ye = yVariable.values();
		i=0;
		g.setColor(getForeground());
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			crossPos = getScreenPoint(y, xVariable.labelIndex(x), noOfXCats, i, crossPos);
			if (crossPos != null)
				drawCross(g, crossPos);
			i ++;
		}
		
		g.setColor(kMeanColor);
		Point meanPos2 = null;
		for (i=0 ; i<noOfXCats ; i++) {
			Value x = xVariable.getLabel(i);
			double mean = model.evaluateMean(x);
		
			double xFract = xAxis.numValToPosition(mean);
			double yFract = 0.0;
			double zFract = zAxis.catValToPosition(i, noOfXCats);
			double zOffset = 0.7 / noOfXCats * jitterPropn;
			meanPos = translateToScreen(map.mapH3DGraph(yFract, xFract, zFract - zOffset),
												map.mapV3DGraph(yFract, xFract, zFract - zOffset), meanPos);
			meanPos2 = translateToScreen(map.mapH3DGraph(yFract, xFract, zFract + zOffset),
												map.mapV3DGraph(yFract, xFract, zFract + zOffset), meanPos2);
			g.drawLine(meanPos.x, meanPos.y, meanPos2.x, meanPos2.y);
		}
	}
	
}
