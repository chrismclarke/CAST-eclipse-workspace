package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class DragParamResidView extends DragParam3View {
	
	static final protected Color kPaleRed = getShadedColor(Color.red);
	
	private String yKey, xKey, zKey;
	
	public DragParamResidView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String yKey, String xKey, String zKey, ColoredLinearEqnView equationView) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, equationView);
		this.yKey = yKey;
		this.xKey = xKey;
		this.zKey = zKey;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		super.drawData(g, shadeHandling);
		
		MultipleRegnModel model = getModel();
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe = ((NumVariable)getVariable(xKey)).values();
		ValueEnumeration ze = ((NumVariable)getVariable(zKey)).values();
		
		Point crossPos = null;
		Point fitPos = null;
		double xVals[] = new double[2];
		
		g.setColor(Color.red);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			xVals[0] = xe.nextDouble();
			xVals[1] = ze.nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			double fit = model.evaluateMean(xVals);
			fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
			boolean thisSideOfPlane = (y >= fit) == fromPlaneTop;
			drawResidual(g, shadeHandling, thisSideOfPlane, crossPos, fitPos);
		}
		
		g.setColor(Color.black);
		
		ye = ((NumVariable)getVariable(yKey)).values();
		xe = ((NumVariable)getVariable(xKey)).values();
		ze = ((NumVariable)getVariable(zKey)).values();
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
	}
	
	protected void drawResidual(Graphics g, int shadeHandling, boolean thisSideOfPlane,
												Point crossPos, Point fitPos) {
		if (shadeHandling == USE_OPAQUE)
			g.setColor(thisSideOfPlane ? Color.red : kPaleRed);
		g.drawLine(fitPos.x, fitPos.y, crossPos.x, crossPos.y);
	}
}
	
