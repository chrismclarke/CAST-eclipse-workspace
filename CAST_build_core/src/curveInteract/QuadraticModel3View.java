package curveInteract;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class QuadraticModel3View extends RotateDotPlaneView {
//	static public final String QUADRATIC_3D_PLOT = "quadratic3D";
	
	static final private Color kPaleRed = new Color(0xFF9999);
	static final private Color kPaleBlue = new Color(0x9999FF);
	
	static final private int kSegments = 20;
	
	private boolean drawResiduals = false;
	
	public QuadraticModel3View(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
	public void setDrawResiduals(boolean drawResiduals) {
		this.drawResiduals = drawResiduals;
	}

//--------------------------------------------------------------------------------
	
	private void drawOnXZPlane(Graphics g, int shadeHandling, boolean fromPlaneTop) {
		g.setColor(shadeHandling == USE_OPAQUE && fromPlaneTop ? kPaleRed : Color.red);
		
		double xMin = zAxis.getMinOnAxis();		//	x and z axes swapped
		double xMax = zAxis.getMaxOnAxis();
		double yMin = yAxis.getMinOnAxis();
		
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<=kSegments ; i++) {
			double x = xMin + i * (xMax - xMin) / kSegments;
			p1 = getScreenPoint(x * x, yMin, x, p1);
			if (p0 != null)
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
		}
	}
	
	private void drawOnFitPlane(Graphics g, MultipleRegnModel model) {
		g.setColor(Color.gray);
		
		double xMin = zAxis.getMinOnAxis();		//	x and z axes swapped
		double xMax = zAxis.getMaxOnAxis();
//		double yMin = yAxis.getMinOnAxis();
		
		double xVals[] = new double[2];
		
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<=kSegments ; i++) {
			double x = xMin + i * (xMax - xMin) / kSegments;
			xVals[0] = x * x;
			xVals[1] = x;
			double fit = model.evaluateMean(xVals);
			p1 = getScreenPoint(x * x, fit, x, p1);
			if (p0 != null)
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			Point pTemp = p0 ; p0 = p1 ; p1 = pTemp;
		}
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		if (fromPlaneTop)
			drawOnXZPlane(g, shadeHandling, fromPlaneTop);
		
		Point crossPos = null;
		Point fitPos = null;
		double xVals[] = new double[explanKey.length];
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			double fit = model.evaluateMean(xVals);
			if (drawResiduals) {
				g.setColor((shadeHandling != USE_OPAQUE) || ((y >= fit) == fromPlaneTop)
																										? Color.blue : kPaleBlue);
				
				fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
				g.drawLine(crossPos.x, crossPos.y, fitPos.x, fitPos.y);
			}
			
			g.setColor((shadeHandling != USE_OPAQUE) || ((y >= fit) == fromPlaneTop)
																										? Color.black : Color.gray);
			drawCross(g, crossPos);
		}
		
		drawOnFitPlane(g, model);
		
		if (!fromPlaneTop)
			drawOnXZPlane(g, shadeHandling, fromPlaneTop);
			
		g.setColor(getForeground());
	}
	
}
	
