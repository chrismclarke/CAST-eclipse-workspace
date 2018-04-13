package multiRegn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;


public class DragPlaneHandlesView extends ModelDot3View {
	
	static final private Color kPaleRed = getShadedColor(Color.red);
	
	static final public int NO_DRAG = 0;
	static final public int BX_EDGE_DRAG = 1;
	static final public int BZ_EDGE_DRAG = 2;
	static final public int BXZ_SUM_DRAG = 3;
	static final public int BXZ_DIFF_DRAG = 4;
	
	private SummaryDataSet summaryData;
	
	private double xMean, zMean, yMean;
	
	private boolean dragEdgeNotCorner = true;
	
	private int currentDragIndex = NO_DRAG;
	
	public DragPlaneHandlesView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey, SummaryDataSet summaryData) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey);
		this.summaryData = summaryData;
	}
	
	public void setDragEdgeNotCorner(boolean dragEdgeNotCorner) {
		this.dragEdgeNotCorner = dragEdgeNotCorner;
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			ValueEnumeration ye = ((NumVariable)getVariable(yKey)).values();
			ValueEnumeration xe = ((NumVariable)getVariable(explanKey[0])).values();
			ValueEnumeration ze = ((NumVariable)getVariable(explanKey[1])).values();
			double sy = 0.0;
			double sx = 0.0;
			double sz = 0.0;
			int nValues = 0;
			
			while (ye.hasMoreValues()) {
				sy += ye.nextDouble();
				sx += xe.nextDouble();
				sz += ze.nextDouble();
				nValues ++;
			}
			yMean = sy / nValues;
			xMean = sx / nValues;
			zMean = sz / nValues;
			
			return true;
		}
		else
			return false;
	}
	
	private double getBxEdgeTarget(MultipleRegnModel model, double[] x) {
		x[0] = xAxis.getMaxOnAxis();
		x[1] = zMean;
		return model.evaluateMean(x);
	}
	
	private double getBzEdgeTarget(MultipleRegnModel model, double[] x) {
		x[0] = xMean;
		x[1] = zAxis.getMaxOnAxis();
		return model.evaluateMean(x);
	}
	
	private double getBxzSumTarget(MultipleRegnModel model, double[] x) {
		x[0] = xAxis.getMaxOnAxis();
		x[1] = zAxis.getMaxOnAxis();
		return model.evaluateMean(x);
	}
	
	private double getBxzDiffTarget(MultipleRegnModel model, double[] x) {
		x[0] = xAxis.getMinOnAxis();
		x[1] = zAxis.getMaxOnAxis();
		return model.evaluateMean(x);
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
		
		Point p = getScreenPoint(xMean, yMean, zMean, null);
		ModelGraphics.drawAnchor(g, p);
		double x[] = new double[2];
		
		if (dragEdgeNotCorner) {
			p = getScreenPoint(xAxis.getMaxOnAxis(), getBxEdgeTarget(model, x), zMean, p);
			ModelGraphics.drawHandle(g, p, (currentDragIndex == BX_EDGE_DRAG));
			
			p = getScreenPoint(xMean, getBzEdgeTarget(model, x), zAxis.getMaxOnAxis(), p);
			ModelGraphics.drawHandle(g, p, (currentDragIndex == BZ_EDGE_DRAG));
		}
		else {
			p = getScreenPoint(xAxis.getMaxOnAxis(), getBxzSumTarget(model, x), zAxis.getMaxOnAxis(), p);
			ModelGraphics.drawHandle(g, p, (currentDragIndex == BXZ_SUM_DRAG));
			
			p = getScreenPoint(xAxis.getMinOnAxis(), getBxzDiffTarget(model, x), zAxis.getMaxOnAxis(), p);
			ModelGraphics.drawHandle(g, p, (currentDragIndex == BXZ_DIFF_DRAG));
		}
	}
	
	public void dragHandleTo(double y) {
		MultipleRegnModel model = getModel();
		double[] xy = new double[4];
		double[] r = MultipleRegnModel.initSsqMatrix(4);
		double[] x = new double[2];
		
		try {
			xy[0] = 1.0;
			xy[1] = xMean;
			xy[2] = zMean;
			xy[3] = yMean;
			MultipleRegnModel.givenC(r, xy, 1.0);
			
			switch (currentDragIndex) {
				case BX_EDGE_DRAG:
				case BZ_EDGE_DRAG:
					double bxEdgeTarget = (currentDragIndex == BX_EDGE_DRAG) ? y
																								: getBxEdgeTarget(model, x);
					double bzEdgeTarget = (currentDragIndex == BZ_EDGE_DRAG) ? y
																								: getBzEdgeTarget(model, x);
					
					xy[0] = 1.0;
					xy[1] = xAxis.getMaxOnAxis();
					xy[2] = zMean;
					xy[3] = bxEdgeTarget;
					MultipleRegnModel.givenC(r, xy, 1.0);
					
					xy[0] = 1.0;
					xy[1] = xMean;
					xy[2] = zAxis.getMaxOnAxis();
					xy[3] = bzEdgeTarget;
					MultipleRegnModel.givenC(r, xy, 1.0);
					
					break;
				case BXZ_SUM_DRAG:
				case BXZ_DIFF_DRAG:
					double bxzSumTarget = (currentDragIndex == BXZ_SUM_DRAG) ? y
																									: getBxzSumTarget(model, x);
					double bxzDiffTarget = (currentDragIndex == BXZ_DIFF_DRAG) ? y
																									: getBxzDiffTarget(model, x);
					
					xy[0] = 1.0;
					xy[1] = xAxis.getMaxOnAxis();
					xy[2] = zAxis.getMaxOnAxis();
					xy[3] = bxzSumTarget;
					MultipleRegnModel.givenC(r, xy, 1.0);
					
					xy[0] = 1.0;
					xy[1] = xAxis.getMinOnAxis();
					xy[2] = zAxis.getMaxOnAxis();
					xy[3] = bxzDiffTarget;
					MultipleRegnModel.givenC(r, xy, 1.0);
					
					break;
					
				default:
			}
			
			double[] bValue = MultipleRegnModel.bSub(r, 4, null);
			for (int i=0 ; i<3 ; i++)
				model.setParameter(i, bValue[i]);
		} catch (Exception e) {
		}
		getData().variableChanged(modelKey);
		summaryData.setSingleSummaryFromData();
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 20;
	
	private int hitOffset;
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int dragType = NO_DRAG;
		int yDist;
		MultipleRegnModel model = getModel();
		double[] tempX = new double[2];
		
		if (dragEdgeNotCorner) {
			Point p1 = getScreenPoint(xAxis.getMaxOnAxis(), getBxEdgeTarget(model, tempX), zMean, null);
			int p1DistX = x - p1.x;
			int p1DistY = y - p1.y;
			int p1Dist = p1DistX * p1DistX + p1DistY * p1DistY;
			Point p2 = getScreenPoint(xMean, getBzEdgeTarget(model, tempX), zAxis.getMaxOnAxis(), null);
			int p2DistX = x - p2.x;
			int p2DistY = y - p2.y;
			int p2Dist = p2DistX * p2DistX + p2DistY * p2DistY;
			
			if (p1Dist <= p2Dist) {
				if (p1Dist <= kMinHitDistance) {
					dragType = BX_EDGE_DRAG;
					yDist = p1DistY;
				}
				else
					return super.getInitialPosition(x, y);
			}
			else {
				if (p2Dist <= kMinHitDistance) {
					dragType = BZ_EDGE_DRAG;
					yDist = p2DistY;
				}
				else
					return super.getInitialPosition(x, y);
			}
		}
		else {
			Point p1 = getScreenPoint(xAxis.getMaxOnAxis(), getBxzSumTarget(model, tempX), zAxis.getMaxOnAxis(), null);
			int p1DistX = x - p1.x;
			int p1DistY = y - p1.y;
			int p1Dist = p1DistX * p1DistX + p1DistY * p1DistY;
			Point p2 = getScreenPoint(xAxis.getMinOnAxis(), getBxzDiffTarget(model, tempX), zAxis.getMaxOnAxis(), null);
			int p2DistX = x - p2.x;
			int p2DistY = y - p2.y;
			int p2Dist = p2DistX * p2DistX + p2DistY * p2DistY;
			
			if (p1Dist <= p2Dist) {
				if (p1Dist <= kMinHitDistance) {
					dragType = BXZ_SUM_DRAG;
					yDist = p1DistY;
				}
				else
					return super.getInitialPosition(x, y);
			}
			else {
				if (p2Dist <= kMinHitDistance) {
					dragType = BXZ_DIFF_DRAG;
					yDist = p2DistY;
				}
				else
					return super.getInitialPosition(x, y);
			}
		}
		
		return new VertDragPosInfo(y, dragType, yDist);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (currentDragIndex == NO_DRAG)
			return super.getPosition(x, y);
		else
			return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			setArrowCursor();
			hitOffset = ((VertDragPosInfo)startInfo).hitOffset;
			currentDragIndex = ((VertDragPosInfo)startInfo).index;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (currentDragIndex != NO_DRAG) {
			if (toPos != null) {
				VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
				int newYPos = dragPos.y - hitOffset;
				Point p = translateToScreen(0, newYPos, null);
				
				try {
					double xDragVal, zDragVal;
					switch (currentDragIndex) {
						case BX_EDGE_DRAG:
							xDragVal = xAxis.getMaxOnAxis();
							zDragVal = zMean;
							break;
						case BZ_EDGE_DRAG:
							xDragVal = xMean;
							zDragVal = zAxis.getMaxOnAxis();
							break;
						case BXZ_SUM_DRAG:
							xDragVal = xAxis.getMaxOnAxis();
							zDragVal = zAxis.getMaxOnAxis();
							break;
						case BXZ_DIFF_DRAG:
						default:
							xDragVal = xAxis.getMinOnAxis();
							zDragVal = zAxis.getMaxOnAxis();
							break;
					}
					
					double xFract = xAxis.numValToPosition(xDragVal);
					double zFract = zAxis.numValToPosition(zDragVal);
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
		if (currentDragIndex != NO_DRAG) {
			currentDragIndex = NO_DRAG;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
