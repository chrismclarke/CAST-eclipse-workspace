package multiRegn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;


public class Model3DragView extends ModelDot3View {
	
	static final private Color kPaleRed = getShadedColor(Color.red);
	
	static final public int B0_DRAG = 0;
	static final public int BX_DRAG = 1;
	static final public int BZ_DRAG = 2;
	static final public int BW_DRAG = 3;
	
	private double[] xAnchor = new double[3];
	private double[] zAnchor = new double[3];
	
	private int currentDragIndex = B0_DRAG;
	boolean draggingAnchor = false;
	
	public Model3DragView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
	}
	
	public void setDragIndex(int newDragIndex) {
		if (currentDragIndex != newDragIndex) {
			currentDragIndex = newDragIndex;
			
			MultipleRegnModel model = getModel();
			double[] fixedB = new double[explanKey.length + 1];
			for (int i=0 ; i<newDragIndex ; i++)
				fixedB[i] = Double.NaN;
//			double startB = (newDragIndex == 0)
//								? (0.3 * yAxis.getMinOnAxis() + 0.7 * yAxis.getMaxOnAxis())
//								: model.getParameter(newDragIndex).toDouble();
			double startB = model.getParameter(newDragIndex).toDouble();
			fixedB[newDragIndex] = startB;
			for (int i=newDragIndex+1 ; i<explanKey.length + 1 ; i++)
				fixedB[i] = 0.0;
			
			model.updateLSParams(yKey, fixedB);
			repaint();
		}
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			ValueEnumeration xe = ((NumVariable)getVariable(explanKey[0])).values();
			ValueEnumeration ze = ((NumVariable)getVariable(explanKey[1])).values();
			double sx = 0.0;
			double sz = 0.0;
			double sxx = 0.0;
			double sxz = 0.0;
			int nValues = 0;
			
			while (xe.hasMoreValues()) {
				double x = xe.nextDouble();
				double z = ze.nextDouble();
				sx += x;
				sz += z;
				sxx += x * x;
				sxz += x * z;
				nValues ++;
			}
			double xBar = sx / nValues;
			double zBar = sz / nValues;
			xAnchor[0] = xBar;
			zAnchor[0] = zBar;
			sxx -= sx * xBar;
			sxz -= sz * xBar;
			
			double xMax = xAxis.getMaxOnAxis();
			
			xAnchor[1] = xMax;
			zAnchor[1] = zBar + sxz / sxx * (xMax - xBar);
			
			xAnchor[2] = xBar;
			zAnchor[2] = zAxis.getMaxOnAxis();
			
			return true;
		}
		else
			return false;
	}
	
	protected Polygon drawShadeRegion(Graphics g) {
		if (currentDragIndex > BZ_DRAG)
			return null;
		return super.drawShadeRegion(g);
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		MultipleRegnModel model = getModel();
		
		g.setColor(Color.red);
		boolean fromPlaneTop = viewingPlaneFromTop();
		
		ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
		ValueEnumeration xe[] = new ValueEnumeration[explanKey.length];
		for (int i=0 ; i<explanKey.length ; i++)
			xe[i] = ((NumVariable)getVariable(explanKey[i])).values();
		
		Point crossPos = null;
		double xVals[] = new double[explanKey.length];
		
		Point fitPos = null;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			for (int i=0 ; i<explanKey.length ; i++)
				xVals[i] = xe[i].nextDouble();
			crossPos = getScreenPoint(xVals[0], y, xVals[1], crossPos);
			double fit = model.evaluateMean(xVals);
			fitPos = getScreenPoint(xVals[0], fit, xVals[1], fitPos);
			if (shadeHandling == USE_OPAQUE)
				g.setColor(((y >= fit) == fromPlaneTop) ? Color.red : kPaleRed);
			g.drawLine(fitPos.x, fitPos.y, crossPos.x, crossPos.y);
		}
		
		super.drawData(g, shadeHandling);
		
		if (currentDragIndex <= BZ_DRAG) {
			Point p = null;
			double[] x = new double[2];
			for (int i=0 ; i<=currentDragIndex ; i++) {
				x[0] = xAnchor[i];
				x[1] = zAnchor[i];
				double y = model.evaluateMean(x);
				p = getScreenPoint(xAnchor[i], y, zAnchor[i], p);
				if (i == currentDragIndex)
					ModelGraphics.drawHandle(g, p, draggingAnchor);
				else
					ModelGraphics.drawAnchor(g, p);
			}
		}
	}
	
	public void dragHandleTo(double y) {
		MultipleRegnModel model = getModel();
		double[] xy = new double[4];
		double[] r = MultipleRegnModel.initSsqMatrix(4);
		double[] x = new double[2];
		
		try {
			for (int i=0 ; i<=currentDragIndex ; i++) {
				xy[0] = 1.0;
				xy[1] = xAnchor[i];
				xy[2] = zAnchor[i];
				if (i == currentDragIndex)
					xy[3] = y;
				else {
					x[0] = xy[1];
					x[1] = xy[2];
					xy[3] = model.evaluateMean(x);
				}
				MultipleRegnModel.givenC(r, xy, 1.0);
			}
			for (int i=currentDragIndex+1 ; i<3 ; i++) {
				xy[0] = xy[1] = xy[2] = 0.0;
				xy[i] = 1.0;
				xy[3] = 0.0;
				MultipleRegnModel.givenC(r, xy, 1.0);
			}
			
			double[] bValue = MultipleRegnModel.bSub(r, 4, null);
			for (int i=0 ; i<3 ; i++)
				model.setParameter(i, bValue[i]);
			for (int i=3 ; i<explanKey.length+1 ; i++)
				model.setParameter(i, 0.0);
							//		If there are more than 2 explan variables, this assumes that
							//		their parameters should be set to zero.
		} catch (Exception e) {
		}
		getData().variableChanged(modelKey);
//		repaint();
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 20;
	
	private int hitOffset;
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (currentDragIndex > BZ_DRAG)
			return super.getInitialPosition(x, y);
		
		MultipleRegnModel model = getModel();
		double[] explan = new double[2];
		explan[0] = xAnchor[currentDragIndex];
		explan[1] = zAnchor[currentDragIndex];
		double yVal = model.evaluateMean(explan);
		Point p = getScreenPoint(explan[0], yVal, explan[1], null);
		
		int xDist = x - p.x;
		int yDist = y - p.y;
		if (xDist * xDist + yDist * yDist <= kMinHitDistance)
			return new VertDragPosInfo(y, currentDragIndex, yDist);
		else
			return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (draggingAnchor)
			return new VertDragPosInfo(y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			setArrowCursor();
			hitOffset = ((VertDragPosInfo)startInfo).hitOffset;
			draggingAnchor = true;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingAnchor) {
			if (toPos != null) {
				VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
				int newYPos = dragPos.y - hitOffset;
				Point p = translateToScreen(0, newYPos, null);
				
				try {
					double xFract = xAxis.numValToPosition(xAnchor[currentDragIndex]);
					double zFract = zAxis.numValToPosition(zAnchor[currentDragIndex]);
					double y = Math.min(1.0, Math.max(0.0, map.mapToD(p, xFract, zFract)));
					
					double yMin = yAxis.getMinOnAxis();
					double yMax = yAxis.getMaxOnAxis();
					double yVal = yMin + y * (yMax - yMin);
					
					dragHandleTo(yVal);
				} catch (AxisException e) {
				}
			}
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingAnchor) {
			draggingAnchor = false;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
