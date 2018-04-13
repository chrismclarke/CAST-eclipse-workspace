package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class Model3XView extends ModelDot3View {
	
	static final public int DRAW_PLANE = 0;
	static final public int NO_DRAW_PLANE = 1;
	
	static final private Color kSelectedDistnColor = Color.blue;
	static final private Color kDimSelDistnColor = getShadedColor(kSelectedDistnColor);
	
	static final private Color kTwoSDPlaneColor = new Color(0x999999);
	static final private Color kDimTwoSDPlaneColor = getShadedColor(kTwoSDPlaneColor);
	
	static final private Color kNormalDistnColor = Color.red;
	static final private Color kDimmedDistnColor = getShadedColor(kNormalDistnColor);	//	orange
	
	static final private int kDistnWidth = 20;
	
	private int drawPlaneType;
	
	public Model3XView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey, int drawPlaneType) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		this.drawPlaneType = drawPlaneType;
		setSelectCrosses(true);
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		if (drawPlaneType == NO_DRAW_PLANE)
			return null;
		
		return super.drawShadeRegion(g);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		double twoSD = 2.0 * model.evaluateSD().toDouble();
		
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		FlagEnumeration fe = getSelection().getEnumeration();
			
		Point lowPos = null;
		Point highPos = null;
		double xVals[] = new double[explanKey.length];
		
		boolean verticalView = map.getTheta2() == 90 || map.getTheta2() == 270;
		
		if (drawPlaneType == DRAW_PLANE && shadeHandling == USE_OPAQUE) {
			boolean fromPlaneTop = viewingPlaneFromTop();
			Point fitPos = null;
			
			Color minusColor = fromPlaneTop ? kDimTwoSDPlaneColor : kTwoSDPlaneColor;
			Color plusColor = fromPlaneTop ? kTwoSDPlaneColor : kDimTwoSDPlaneColor;
			drawTwoDSPlanes(g, model, twoSD, plusColor, minusColor);
			
			g.setColor(kNormalDistnColor);
			while (xe[0].hasMoreValues()) {
				for (int i=0 ; i<explanKey.length ; i++)
					xVals[i] = xe[i].nextDouble();
				double fit = model.evaluateMean(xVals);
				fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
				
				boolean selected = fe.nextFlag();
				if (selected) {
					double threeSD = 3.0 * model.evaluateSD().toDouble();
					lowPos = getScreenPoint(xVals[0], fit - threeSD, xVals[1], lowPos);
					highPos = getScreenPoint(xVals[0], fit + threeSD, xVals[1], highPos);
					Color topColor = fromPlaneTop ? kDimSelDistnColor : kSelectedDistnColor;
					Color bottomColor = fromPlaneTop ? kSelectedDistnColor : kDimSelDistnColor;
					drawDistn(g, lowPos, highPos, verticalView, topColor, bottomColor);
				}
				
				if (verticalView) {
					if (!selected)
						drawCross(g, fitPos);
				}
				else {
					lowPos = getScreenPoint(xVals[0], fit - twoSD, xVals[1], lowPos);
					highPos = getScreenPoint(xVals[0], fit + twoSD, xVals[1], highPos);
					g.setColor(fromPlaneTop ? kDimmedDistnColor : kNormalDistnColor);
					g.drawLine(lowPos.x, lowPos.y, fitPos.x, fitPos.y);
					g.setColor(fromPlaneTop ? kNormalDistnColor : kDimmedDistnColor);
					g.drawLine(fitPos.x, fitPos.y, highPos.x, highPos.y);
				}
			}
		}
		else {
			drawTwoDSPlanes(g, model, twoSD, kTwoSDPlaneColor, kTwoSDPlaneColor);
			
			g.setColor(kNormalDistnColor);
			while (xe[0].hasMoreValues()) {
				for (int i=0 ; i<explanKey.length ; i++)
					xVals[i] = xe[i].nextDouble();
				double fit = model.evaluateMean(xVals);
				
				boolean selected = fe.nextFlag();
				if (selected) {
					double threeSD = 3.0 * model.evaluateSD().toDouble();
					lowPos = getScreenPoint(xVals[0], fit - threeSD, xVals[1], lowPos);
					highPos = getScreenPoint(xVals[0], fit + threeSD, xVals[1], highPos);
					drawDistn(g, lowPos, highPos, verticalView, kSelectedDistnColor, kSelectedDistnColor);
				}
				lowPos = getScreenPoint(xVals[0], fit - twoSD, xVals[1], lowPos);
				if (verticalView) {
					if (!selected)
						drawCross(g, lowPos);
				}
				else {
					highPos = getScreenPoint(xVals[0], fit + twoSD, xVals[1], highPos);
					g.drawLine(lowPos.x, lowPos.y, highPos.x, highPos.y);
				}
			}
			g.setColor(getForeground());
		}
		
		super.drawData(g, shadeHandling);
	}
	
	private void drawTwoDSPlanes(Graphics g, MultipleRegnModel model, double twoSD,
												Color plusColor, Color minusColor) {
		double b0 = model.getParameter(0).toDouble();
		double bx = model.getParameter(1).toDouble();
		double bz = model.getParameter(2).toDouble();
		
		Polygon p = ModelGraphics3D.getFull3DPlane(map, this, yAxis, xAxis, zAxis, b0 - twoSD,
																		bx, bz);
		g.setColor(minusColor);
		g.drawPolygon(p);
		p = ModelGraphics3D.getFull3DPlane(map, this, yAxis, xAxis, zAxis, b0 + twoSD,
																		bx, bz);
		g.setColor(plusColor);
		g.drawPolygon(p);
	}
	
	private void drawDistn(Graphics g, Point lowPos, Point highPos, boolean verticalView,
																Color topColor, Color bottomColor) {
		if (verticalView) {
			g.setColor(kSelectedDistnColor);
			drawCross(g, lowPos);
		}
		else {
			int highY = Math.max(highPos.y, lowPos.y);
			int lowY = Math.min(highPos.y, lowPos.y);
			int distnHt = highY - lowY;
			if (distnHt == 0) {
				g.setColor(kSelectedDistnColor);
				g.drawLine(lowPos.x, lowY, lowPos.x + kDistnWidth, lowY);
			}
			else {
				double mid = (lowY + highY) * 0.5;
				double sd = distnHt / 6.0;
				g.setColor(topColor);
				for (int pos=highY ; pos>=mid ;  pos--) {
					double z = (pos - mid) / sd;
					double density = Math.exp(-0.5 * z * z);
					int width = (int)Math.round(density * kDistnWidth);
					g.drawLine(lowPos.x, pos, lowPos.x + width, pos);
				}
				g.setColor(bottomColor);
				for (int pos=lowY ; pos<=mid ;  pos++) {
					double z = (pos - mid) / sd;
					double density = Math.exp(-0.5 * z * z);
					int width = (int)Math.round(density * kDistnWidth);
					g.drawLine(lowPos.x, pos, lowPos.x + width, pos);
				}
			}
		}
		g.setColor(kNormalDistnColor);
	}

//-----------------------------------------------------------------------------------

	static final private int kMinMeanHitDistance = 50;
	
	protected void findScreenPositions(Point[] crossPos) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		int noOfVals = ((NumVariable)getVariable(explanKey[0])).noOfValues();
		double xVals[] = new double[explanKey.length];
		for (int i=0 ; i<noOfVals ; i++) {
			for (int j=0 ; j<explanKey.length ; j++)
				xVals[j] = xe[j].nextDouble();
			double fit = model.evaluateMean(xVals);
			crossPos[i] = getScreenPoint(xVals[0], fit, xVals[1], crossPos[i]);
		}
	}
	
	protected int distance(int x, int y, Point crossPos) {
		int xDist = crossPos.x - x;
		int yDist = (crossPos.y - y) / 2;
		return xDist * xDist + yDist * yDist;
	}
	
	protected int minHitDistance() {
		return kMinMeanHitDistance;
	}
}
	
