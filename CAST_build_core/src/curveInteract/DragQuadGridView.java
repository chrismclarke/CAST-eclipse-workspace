package curveInteract;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;


public class DragQuadGridView extends Rotate3DView {
//	static public final String DRAG_QUAD_GRID = "dragQuadGrid";
	
	static final private int NO_DRAG = 0;
	static final private int DRAG_ORIGIN = 1;
	static final private int DRAG_X_MAX = 2;
	static final private int DRAG_Z_MAX = 3;
	static final private int DRAG_X_MIDDLE = 4;
	static final private int DRAG_Z_MIDDLE = 5;
	
	static final public int X_INDEX = 1;
	static final public int Z_INDEX = 2;
	static final public int X2_INDEX = 3;
	static final public int Z2_INDEX = 4;
	
	static final private int kGridLines = 10;
	
	private Color gridColor = Color.black;
	
	protected String modelKey;
	
	private double tempVal[] = new double[2];
	private Point tempPt1, tempPt2, tempPt3, tempPt4, tempPt5;
	
	private int dragType = NO_DRAG;
	private double xMin, zMin, xMax, zMax, xMid, zMid;
	
	private boolean allowXCurvature = true;
	private boolean allowZCurvature = true;
	
	private boolean allowDrag = true;
	
	public DragQuadGridView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
																																						String modelKey) {
		super(theData, applet, xAxis, yAxis, zAxis, null, null, null);
		this.modelKey = modelKey;
		
		xMin = xAxis.getMinOnAxis();
		zMin = zAxis.getMinOnAxis();
		xMax = xAxis.getMaxOnAxis();
		zMax = zAxis.getMaxOnAxis();
		xMid = (xMax + xMin) * 0.5;
		zMid = (zMax + zMin) * 0.5;
	}
	
	public void setAllowXCurvature(boolean allowXCurvature) {
		this.allowXCurvature = allowXCurvature;
		if (!allowXCurvature) {
			ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
			
			double y00 = getPrediction(xMin, zMin, model);
			double yMax0 = getPrediction(xMax, zMax, model);
			
			model.setParameter(X2_INDEX, 0.0);
			
			double xSlope = solveSlope(xMin, xMax, y00, yMax0, 0.0);
			model.setParameter(X_INDEX, xSlope);
			double intercept = solveIntercept(xMin, zMin, y00, model);
			model.setParameter(0, intercept);
		}
	}
	
	public void setAllowZCurvature(boolean allowZCurvature) {
		this.allowZCurvature = allowZCurvature;
		if (!allowZCurvature) {
			ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
			
			double y00 = getPrediction(xMin, zMin, model);
			double y0Max = getPrediction(xMin, zMax, model);
			
			model.setParameter(Z2_INDEX, 0.0);
			
			double zSlope = solveSlope(xMin, xMax, y00, y0Max, 0.0);
			model.setParameter(Z_INDEX, zSlope);
			double intercept = solveIntercept(xMin, zMin, y00, model);
			model.setParameter(0, intercept);
			
			repaint();
		}
	}
	
	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
	}
	
	public void setAllowDrag(boolean allowDrag) {
		this.allowDrag = allowDrag;
	}
	
//--------------------------------------------------------------
	
	protected boolean arrowsVisible() {
		return allowDrag && (map.getTheta2() < 90 || map.getTheta2() > 270);
	}
	
	protected boolean xSlopeArrowVisible() {
		return true;
//		return map.getTheta1() % 180 != 90;
	}
	
	protected boolean zSlopeArrowVisible() {
		return true;
//		return map.getTheta1() % 180 != 0;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
		
		drawModelGrid(g, model, shadeHandling);
	}
	
	private double getPrediction(double x, double z, ResponseSurfaceModel model) {
		tempVal[0] = x;
		tempVal[1] = z;
		return model.evaluateMean(tempVal);
	}
	
	protected Point getFitPoint(double x, double z, ResponseSurfaceModel model,
																																		Point p) {
		double prediction = getPrediction(x, z, model);
		return getScreenPoint(x, prediction, z, p);
	}
	
	protected void drawModelGrid(Graphics g, ResponseSurfaceModel model, int shadeHandling) {
		g.setColor(gridColor);
		
		Point p0 = null;
		Point p1 = null;
		
		for (int i=0 ; i<=kGridLines ; i++) {
			double x = xMin + (xMax - xMin) * i / kGridLines;
			
			if (model.getParameter(Z2_INDEX).toDouble() == 0.0) {
				p0 = getFitPoint(x, zMin, model, p0);
				p1 = getFitPoint(x, zMax, model, p1);
				
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			else
				for (int j=0 ; j<kGridLines ; j++) {
					double z = zMin + (zMax - zMin) * j / kGridLines;
					double zPlus = zMin + (zMax - zMin) * (j + 1) / kGridLines;
					p0 = getFitPoint(x, z, model, p0);
					p1 = getFitPoint(x, zPlus, model, p1);
					
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
				}
		}
		
		for (int j=0 ; j<=kGridLines ; j++) {
			double z = zMin + (zMax - zMin) * j / kGridLines;
			
			if (model.getParameter(X2_INDEX).toDouble() == 0.0) {
				p0 = getFitPoint(xMin, z, model, p0);
				p1 = getFitPoint(xMax, z, model, p1);
				
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
			else
				for (int i=0 ; i<kGridLines ; i++) {
					double x = xMin + (xMax - xMin) * i / kGridLines;
					double xPlus = xMin + (xMax - xMin) * (i + 1) / kGridLines;
					p0 = getFitPoint(x, z, model, p0);
					p1 = getFitPoint(xPlus, z, model, p1);
					
					g.drawLine(p0.x, p0.y, p1.x, p1.y);
				}
		}
	}
	
	protected void drawArrowAt(Graphics g, double x, double z, ResponseSurfaceModel model,
																													boolean highlight) {
		tempPt1 = getFitPoint(x, z, model, tempPt1);
		ModelGraphics.drawHandle(g, tempPt1, highlight);
	}
	
	protected void drawForeground(Graphics g) {
		if (!arrowsVisible())
			return;
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
		
		drawArrowAt(g, xMin, zMin, model, dragType == DRAG_ORIGIN);
		if (xSlopeArrowVisible()) {
			if (allowXCurvature)
				drawArrowAt(g, xMid, zMin, model, dragType == DRAG_X_MIDDLE);
			drawArrowAt(g, xMax, zMin, model, dragType == DRAG_X_MAX);
		}
		if (zSlopeArrowVisible()) {
			if (allowZCurvature)
				drawArrowAt(g, xMin, zMid, model, dragType == DRAG_Z_MIDDLE);
			drawArrowAt(g, xMin, zMax, model, dragType == DRAG_Z_MAX);
		}
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinHitDist = 100;
	
	protected int hitOffset;
	
	protected Point[] getDragPoints() {
		ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
		Point p[] = new Point[5];
		tempPt1 = getFitPoint(xMin, zMin, model, tempPt1);
		p[0] = tempPt1;
		
		if (xSlopeArrowVisible()) {
			tempPt2 = getFitPoint(xMax, zMin, model, tempPt2);
			p[1] = tempPt2;
		}
		if (zSlopeArrowVisible()) {
			tempPt3 = getFitPoint(xMin, zMax, model, tempPt3);
			p[2] = tempPt3;
		}
		if (xSlopeArrowVisible() && allowXCurvature) {
			tempPt4 = getFitPoint(xMid, zMin, model, tempPt4);
			p[3] = tempPt4;
		}
		if (zSlopeArrowVisible() && allowZCurvature) {
			tempPt5 = getFitPoint(xMin, zMid, model, tempPt5);
			p[4] = tempPt5;
		}
		return p;
	}
	
	protected int[] getDragTypes() {
		int type[] = {DRAG_ORIGIN, DRAG_X_MAX, DRAG_Z_MAX, DRAG_X_MIDDLE, DRAG_Z_MIDDLE};
		return type;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (arrowsVisible()) {
			int minHitDist = Integer.MAX_VALUE;
			int minHitType = 0;
			int minHitOffset = 0;
			
			Point pts[] = getDragPoints();
			int types[] = getDragTypes();
			
			for (int i=0 ; i<types.length ; i++)
				if (pts[i] != null) {
					int xDist = x - pts[i].x;
					int yDist = y - pts[i].y;
					int thisHitDist = xDist * xDist + yDist * yDist;
					if (thisHitDist < minHitDist) {
						minHitType = types[i];
						minHitDist = thisHitDist;
						minHitOffset = yDist;
					}
				}
			
			if (minHitDist <= kMinHitDist)
				return new VertDragPosInfo(y, minHitType, minHitOffset);
		}
		return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (dragType != NO_DRAG)
			return new VertDragPosInfo(y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
			setArrowCursor();
			dragType = posInfo.index;
			hitOffset = posInfo.hitOffset;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	private double solveCurvature(double x0, double x2, double y0, double y1, double y2) {
		return 2.0 * (y2 - 2.0 * y1 + y0) / ((x2 - x0) * (x2 - x0));
	}
	
	private double solveSlope(double x0, double x2, double y0, double y2, double curvature) {
		return (y2 - y0) / (x2 - x0) - curvature * (x0 + x2);
	}
	
	private double solveIntercept(double x0, double z0, double y0, ResponseSurfaceModel model) {
		tempVal[0] = x0;
		tempVal[1] = z0;
		return model.getParameter(0).toDouble() + y0 - model.evaluateMean(tempVal);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragType != NO_DRAG) {
			ResponseSurfaceModel model = (ResponseSurfaceModel)getVariable(modelKey);
			
			double y00 = getPrediction(xMin, zMin, model);
			double yMid0 = getPrediction(xMid, zMin, model);
			double yMax0 = getPrediction(xMax, zMin, model);
			double y0Mid = getPrediction(xMin, zMid, model);
			double y0Max = getPrediction(xMin, zMax, model);
			
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y - hitOffset;
			Point p = translateToScreen(0, newYPos, null);
			
			try {
				switch (dragType) {
					case DRAG_ORIGIN:
						double xFract = xAxis.numValToPosition(xMin);
						double zFract = zAxis.numValToPosition(zMin);
						double yNew = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						
						double xCurvature = 0.0;
						if (allowXCurvature) {
							xCurvature = solveCurvature(xMin, xMax, yNew, yMid0, yMax0);
							model.setParameter(X2_INDEX, xCurvature);
						}
						double xSlope = solveSlope(xMin, xMax, yNew, yMax0, xCurvature);
						model.setParameter(X_INDEX, xSlope);
						
						double zCurvature = 0.0;
						if (allowZCurvature) {
							zCurvature = solveCurvature(zMin, zMax, yNew, y0Mid, y0Max);
							model.setParameter(Z2_INDEX, zCurvature);
						}
						double zSlope = solveSlope(zMin, zMax, yNew, y0Max, zCurvature);
						model.setParameter(Z_INDEX, zSlope);
						
						double intercept = solveIntercept(xMin, zMin, yNew, model);
						model.setParameter(0, intercept);
						
						break;
						
					case DRAG_X_MAX:
					case DRAG_X_MIDDLE:
						boolean maxNotMid = (dragType == DRAG_X_MAX);
						xFract = xAxis.numValToPosition(maxNotMid ? xMax : xMid);
						zFract = zAxis.numValToPosition(zMin);
						yNew = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						
						xCurvature = 0.0;
						if (allowXCurvature) {
							xCurvature = solveCurvature(xMin, xMax, y00, maxNotMid ? yMid0 : yNew,
																														maxNotMid ? yNew : yMax0);
							model.setParameter(X2_INDEX, xCurvature);
						}
						
						xSlope = solveSlope(xMin, xMax, y00, maxNotMid ? yNew : yMax0, xCurvature);
						model.setParameter(X_INDEX, xSlope);
						intercept = solveIntercept(xMin, zMin, y00, model);
						model.setParameter(0, intercept);
						break;
						
					case DRAG_Z_MAX:
					case DRAG_Z_MIDDLE:
						maxNotMid = (dragType == DRAG_Z_MAX);
						xFract = xAxis.numValToPosition(xMin);
						zFract = zAxis.numValToPosition(maxNotMid ? zMax : zMid);
						yNew = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						
						zCurvature = 0.0;
						if (allowZCurvature) {
							zCurvature = solveCurvature(zMin, zMax, y00, maxNotMid ? y0Mid : yNew,
																														maxNotMid ? yNew : y0Max);
							model.setParameter(Z2_INDEX, zCurvature);
						}
						
						zSlope = solveSlope(zMin, zMax, y00, maxNotMid ? yNew : y0Max, zCurvature);
						model.setParameter(Z_INDEX, zSlope);
						intercept = solveIntercept(xMin, zMin, y00, model);
						model.setParameter(0, intercept);
						break;
						
					default:
				}
			} catch (AxisException e) {
			}
			getData().variableChanged(modelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragType != NO_DRAG) {
			dragType = NO_DRAG;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
