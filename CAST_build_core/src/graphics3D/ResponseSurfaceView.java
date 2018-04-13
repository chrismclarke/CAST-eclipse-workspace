package graphics3D;

import java.awt.*;

import dataView.*;
import models.*;


public class ResponseSurfaceView extends SurfaceView {

	static final private double kEpsilon = 0.1;	// offset for working out whether above surface
	
	static final private Color kUnderSurfaceCrossColor = new Color(0xCCCCCC);
	static final private Color kUnderSurfaceResidColor = new Color(0xFF6699);
	
	private boolean neverDimCross = false;
		
	private boolean drawResids = false;
	private boolean squaredResids = false;
	
	public ResponseSurfaceView(DataSet theData,
						XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
	public void setNeverDimCross(boolean neverDimCross) {
		this.neverDimCross = neverDimCross;
	}
	
	public void setDrawResids(boolean drawResids) {
		this.drawResids = drawResids;
	}
	
	public void setSquaredResids(boolean squaredResids) {
		this.squaredResids = squaredResids;
	}

//-------------------------------------------------------------------

	
	protected boolean viewFromTop(double x, double z, MultipleRegnModel model) {
		if (model == null)
			return true;
		
		double minX = xAxis.getMinOnAxis();
		double maxX = xAxis.getMaxOnAxis();
		double xChange = (maxX - minX) * kEpsilon * map.getSinTheta1();
		
		double minZ = zAxis.getMinOnAxis();
		double maxZ = zAxis.getMaxOnAxis();
		double zChange = - (maxZ - minZ) * kEpsilon * map.getCosTheta1();
		
		double nearX = x - xChange;
		double farX = x + xChange;
		double nearZ = z - zChange;
		double farZ = z + zChange;
		
		double xVals[] = new double[2];
		xVals[0] = nearX;
		xVals[1] = nearZ;
		Point nearPos = getModelPoint(xVals, model);
		
		xVals[0] = farX;
		xVals[1] = farZ;
		Point farPos = getModelPoint(xVals, model);
		
		return farPos.y < nearPos.y;
	}
	
	protected NumVariable getYDataVar() {
		return (NumVariable)getVariable(yKey);
	}
	
	protected NumVariable getXDataVar() {
		return (NumVariable)getVariable(explanKey[0]);
	}
	
	protected NumVariable getZDataVar() {
		return (NumVariable)getVariable(explanKey[1]);
	}

	protected void drawResids(Graphics g, int shadeHandling) {
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		
		Point crossPos = null;
		Point fitPos = null;
		double xVals[] = new double[2];
		
		ValueEnumeration ye = getYDataVar().values();
		ValueEnumeration xe = getXDataVar().values();
		ValueEnumeration ze = getZDataVar().values();
		
		g.setColor(Color.red);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			xVals[0] = xe.nextDouble();
			xVals[1] = ze.nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			
			double fit = model.evaluateMean(xVals);
			fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
			if (shadeHandling == USE_OPAQUE && drawType == SURFACE)
				g.setColor(((y >= fit) == viewFromTop(xVals[0], xVals[1], model))
																						? Color.red : kUnderSurfaceResidColor);
			
			if (squaredResids) {
				if (fitPos.y >= crossPos.y)
					g.fillRect(crossPos.x, crossPos.y, fitPos.y - crossPos.y, fitPos.y - crossPos.y);
				else
					g.fillRect(fitPos.x, fitPos.y, crossPos.y - fitPos.y, crossPos.y - fitPos.y);
			}
			else
				g.drawLine(crossPos.x, crossPos.y, fitPos.x, fitPos.y);
		}
	}

	protected void drawData(Graphics g, int shadeHandling) {
		if (!drawData)
			return;
		
		if (drawResids)
			drawResids(g, shadeHandling);
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		
		Point crossPos = null;
		double xVals[] = new double[2];
		
		ValueEnumeration ye = getYDataVar().values();
		ValueEnumeration xe = getXDataVar().values();
		ValueEnumeration ze = getZDataVar().values();
		
		g.setColor(Color.black);
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			xVals[0] = xe.nextDouble();
			xVals[1] = ze.nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			if (model != null && !neverDimCross) {
				double fit = model.evaluateMean(xVals);
				if (shadeHandling == USE_OPAQUE && drawType == SURFACE)
					g.setColor(((y >= fit) == viewFromTop(xVals[0], xVals[1], model))
																							? Color.black : kUnderSurfaceCrossColor);
			}
			drawCross(g, crossPos);
		}
		g.setColor(getForeground());
	}

}
	
