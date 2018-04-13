package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


										//	Assumes that standard view direction is used. The model is set up
										//	so that standard y-x-z and y-x views look down on plane.

public class ConditRegn3DView extends Rotate3DView {
	
	static final private Color kPlaneColor = new Color(0xDDDDDD);
	static final private Color kFixedZColor = new Color(0xC2D7FD);
	static final private Color kPlaneBehindZColor = new Color(0xA4BED8);
	static final private Color kZBehindPlaneColor = new Color(0xBDC4D2);
 	
	static final public Color kBandColor = new Color(0xFF6699);
	static final private Color kPlaneBehindBandColor = new Color(0xB77694);
	static final private Color kBandBehindPlaneColor = new Color(0xF5BBD0);
 	
 	private String planeKey;
 	
 	private boolean sliceZNotX;
 	private double conditValue;
	private boolean showBand = false;
	
	public ConditRegn3DView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String xKey, String yKey, String zKey,
						String planeKey, boolean sliceZNotX, double conditValue) {
		super(theData, applet, xAxis, yAxis, zAxis, xKey, yKey, zKey);
		this.planeKey = planeKey;
		this.sliceZNotX = sliceZNotX;
		this.conditValue = conditValue;
	}
	
	public void setConditValue(double conditValue) {
		this.conditValue = conditValue;
		repaint();
	}
	
	public void setShowBand(boolean showBand) {
		this.showBand = showBand;
		repaint();
	
	}
	
	private Polygon getPartPlanePoly(double conditMin, double conditMax, MultipleRegnModel model,
																											double[] x, double[] z) {
		x[0] = x[3] = x[4] = sliceZNotX ? xAxis.getMinOnAxis() : conditMin;
		x[1] = x[2] = sliceZNotX ? xAxis.getMaxOnAxis() : conditMax;
		z[0] = z[1] = z[4] = sliceZNotX ? conditMin : zAxis.getMinOnAxis();
		z[2] = z[3] = sliceZNotX ? conditMax : zAxis.getMaxOnAxis();
		
		Polygon poly = new Polygon();
		Point p = null;
		double explan[] = new double[2];
		for (int i=0 ; i<5 ; i++) {
			explan[0] = x[i];
			explan[1] = z[i];
			p = getScreenPoint(explan[0], model.evaluateMean(explan), explan[1], p);
			poly.addPoint(p.x, p.y);
		}
		
		return poly; 
	}
	
	private Polygon getVertConditPoly(double conditValue) {
		Polygon poly = new Polygon();
		Point p0 = getScreenPoint(sliceZNotX ? xAxis.getMinOnAxis() : conditValue,
								yAxis.getMinOnAxis(), sliceZNotX ? conditValue : zAxis.getMinOnAxis(), null);
		poly.addPoint(p0.x, p0.y);
		
		Point p1 = null;
		p1 = getScreenPoint(sliceZNotX ? xAxis.getMinOnAxis() : conditValue,
								yAxis.getMaxOnAxis(), sliceZNotX ? conditValue : zAxis.getMinOnAxis(), p1);
		poly.addPoint(p1.x, p1.y);
		p1 = getScreenPoint(sliceZNotX ? xAxis.getMaxOnAxis() : conditValue,
								yAxis.getMaxOnAxis(), sliceZNotX ? conditValue : zAxis.getMaxOnAxis(), p1);
		poly.addPoint(p1.x, p1.y);
		p1 = getScreenPoint(sliceZNotX ? xAxis.getMaxOnAxis() : conditValue,
								yAxis.getMinOnAxis(), sliceZNotX ? conditValue : zAxis.getMaxOnAxis(), p1);
		poly.addPoint(p1.x, p1.y);
		
		poly.addPoint(p0.x, p0.y);
		
		return poly; 
	}
	
	private Polygon getBandPoly(double conditValue) {
		MultipleRegnModel regnPlane = (MultipleRegnModel)getData().getVariable(planeKey);
		double twoSD = 2.0 * regnPlane.evaluateSD().toDouble();
		
		double explan[] = new double[2];
		explan[sliceZNotX ? 1 : 0] = conditValue;
		if (sliceZNotX)
			explan[0] = xAxis.getMinOnAxis();
		else
			explan[1] = zAxis.getMinOnAxis();
		double lowFit = regnPlane.evaluateMean(explan);
		if (sliceZNotX)
			explan[0] = xAxis.getMaxOnAxis();
		else
			explan[1] = zAxis.getMaxOnAxis();
		double highFit = regnPlane.evaluateMean(explan);
		
		Polygon poly = new Polygon();
		double xLow = sliceZNotX ? xAxis.getMinOnAxis() : conditValue;
		double zLow = sliceZNotX ? conditValue : zAxis.getMinOnAxis();
		Point p0 = getScreenPoint(xLow, lowFit - twoSD, zLow, null);
		poly.addPoint(p0.x, p0.y);
		
		Point p1 = getScreenPoint(xLow, lowFit + twoSD, zLow, null);
		poly.addPoint(p1.x, p1.y);
		double xHigh = sliceZNotX ? xAxis.getMaxOnAxis() : conditValue;
		double zHigh = sliceZNotX ? conditValue : zAxis.getMaxOnAxis();
		p1 = getScreenPoint(xHigh, highFit + twoSD, zHigh, p1);
		poly.addPoint(p1.x, p1.y);
		p1 = getScreenPoint(xHigh, highFit - twoSD, zHigh, p1);
		poly.addPoint(p1.x, p1.y);
		
		poly.addPoint(p0.x, p0.y);
		
		return poly; 
	}
	
	protected void drawContents(Graphics g) {
										//	Assumes that standard view direction is used. The model is set up
										//	so that standard y-x-z and y-x views look down on plane.
		MultipleRegnModel regnPlane = (MultipleRegnModel)getData().getVariable(planeKey);
		double[] x = new double[5];
		double[] z = new double[5];
//		double[] y = new double[5];
		
		Polygon backPoly = getPartPlanePoly(sliceZNotX ? zAxis.getMinOnAxis() : xAxis.getMinOnAxis(),
																																			conditValue, regnPlane, x, z);
		Polygon frontPoly = getPartPlanePoly(conditValue, sliceZNotX ? zAxis.getMaxOnAxis() : xAxis.getMaxOnAxis(), regnPlane, x, z);
		Polygon fixedZPoly = getVertConditPoly(conditValue);
		Polygon bandPoly = (showBand) ? getBandPoly(conditValue) : null;
		
		Shape oldClip = g.getClip();
		g.setClip(backPoly);
		g.setColor(kPlaneColor);
		g.fillPolygon(backPoly);
	
		drawAxes(g, BACK_AXIS, D3Axis.SHADED);
		g.setClip(oldClip);
		
		//----
		g.setClip(fixedZPoly);
		g.setColor(kFixedZColor);
		g.fillPolygon(fixedZPoly);
		
		if (showBand) {
			g.setColor(kBandColor);
			g.fillPolygon(bandPoly);
		}
		
		g.setColor(kPlaneBehindZColor);
		g.fillPolygon(backPoly);
		
		if (showBand) {
			g.setClip(bandPoly);
			g.setColor(kPlaneBehindBandColor);
			g.fillPolygon(backPoly);
		}
		
		drawAxes(g, BACK_AXIS, D3Axis.SHADED);
		g.setClip(oldClip);
		
		//----
		g.setClip(frontPoly);
		g.setColor(kPlaneColor);
		g.fillPolygon(frontPoly);
		
		g.setColor(kZBehindPlaneColor);
		g.fillPolygon(fixedZPoly);
		
		if (showBand) {
			g.setColor(kBandBehindPlaneColor);
			g.fillPolygon(bandPoly);
		}
		
		drawAxes(g, BACK_AXIS, D3Axis.SHADED);
		g.setClip(oldClip);
		
		if (yKey != null)
			drawData(g, IGNORE_OPAQUE);
		
		Polygon wholePlanePoly = getPartPlanePoly(sliceZNotX ? zAxis.getMinOnAxis() : xAxis.getMinOnAxis(),
										sliceZNotX ? zAxis.getMaxOnAxis() : xAxis.getMaxOnAxis(), regnPlane, x, z);
		g.setClip(wholePlanePoly);
		if (yKey != null)
			drawData(g, USE_OPAQUE);
		g.setClip(oldClip);
		
		
		//----
		g.setColor(Color.red);
		if (sliceZNotX)
			g.drawLine(backPoly.xpoints[2], backPoly.ypoints[2], backPoly.xpoints[3], backPoly.ypoints[3]);
		else
			g.drawLine(backPoly.xpoints[1], backPoly.ypoints[1], backPoly.xpoints[2], backPoly.ypoints[2]);
		
		g.setColor(getForeground());
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(planeKey);
		boolean fromPlaneTop = true;		//	view is only programmed to work when looking down from top
		
		Point crossPos = null;
		double xVals[] = new double[2];
		
		ValueEnumeration xe = ((NumVariable)getVariable(xKey)).values();
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration ze = ((NumVariable)getVariable(zKey)).values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			xVals[0] = xe.nextDouble();
			xVals[1] = ze.nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			double fit = model.evaluateMean(xVals);
			if (shadeHandling == USE_OPAQUE)
				g.setColor(((y >= fit) == fromPlaneTop) ? Color.black : Color.gray);
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	protected void drawDragCircle(Graphics g) {
										//	Drag rotations not allowed
										//	Cannot drag because shading only works when looking down on plane
	}
	
	protected void setArrowCursor() {
	}
	
	protected boolean canDrag() {
										//	Drag rotations not allowed
		return false;
	}
	
/*
	public boolean mouseMove(Event evt, int x, int y) {
										//	Drag rotations not allowed
		return false;
	}
*/
}
	
