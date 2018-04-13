package multiRegn;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;


public class DragParam3View extends ModelDot3View {
	
	static final protected int NO_DRAG = 0;
	static final protected int DRAG_INTERCEPT = 1;
	static final protected int DRAG_X_SLOPE = 2;
	static final protected int DRAG_Z_SLOPE = 3;
	
	static final private Color kIntColor = D3Axis.axisColor[D3Axis.Y_AXIS][D3Axis.FOREGROUND];
	static final private Color kDimIntColor = getShadedColor(kIntColor);
//	static final private Color kDim2IntColor = getShadedColor(kDimIntColor);
	
	static final private Color kXSlopeColor = D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.FOREGROUND];
	static final private Color kDimXSlopeColor = getShadedColor(kXSlopeColor);
	static final private Color kDim2XSlopeColor = getShadedColor(kDimXSlopeColor);
	
	static final private Color kZSlopeColor = D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.FOREGROUND];
	static final private Color kDimZSlopeColor = getShadedColor(kZSlopeColor);
	static final private Color kDim2ZSlopeColor = getShadedColor(kDimZSlopeColor);
	
	static final protected Color kLineColor = Color.gray;
	static final protected Color kDimLineColor = getShadedColor(kLineColor);
	
	private ColoredLinearEqnView equationView;
	
	private double tempVal[] = new double[2];
	protected Point tempPt1, tempPt2, tempPt3;
	
	protected int dragType = NO_DRAG;
	protected double xMin, yMin, zMin;
	
	private int xTemp[] = new int[4];
	private int yTemp[] = new int[4];
	
	protected boolean fillSlopeTriangles = true;
	
	public DragParam3View(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, ColoredLinearEqnView equationView) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, null, null);
		this.equationView = equationView;
		xMin = xAxis.getMinOnAxis();
		yMin = yAxis.getMinOnAxis();
		zMin = zAxis.getMinOnAxis();
	}
	
	protected boolean arrowsVisible() {
		return map.getTheta2() < 90 || map.getTheta2() > 270;
	}
	
	protected boolean xSlopeArrowVisible() {
		return map.getTheta1() % 180 != 90;
	}
	
	protected boolean zSlopeArrowVisible() {
		return map.getTheta1() % 180 != 0;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		if (!arrowsVisible())
			return;
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		boolean viewFromTop = viewingPlaneFromTop();
		
		switch (dragType) {
			case NO_DRAG:
				drawIntercept(g, model, shadeHandling, viewFromTop, false);
				drawXSlope(g, model, shadeHandling, viewFromTop, false);
				drawZSlope(g, model, shadeHandling, viewFromTop, false);
				break;
			case DRAG_INTERCEPT:
				drawIntercept(g, model, shadeHandling, viewFromTop, true);
				break;
			case DRAG_X_SLOPE:
				drawXSlope(g, model, shadeHandling, viewFromTop, true);
				break;
			case DRAG_Z_SLOPE:
				drawZSlope(g, model, shadeHandling, viewFromTop, true);
				break;
			default:
		}
	}
	
	private void drawIntercept(Graphics g, MultipleRegnModel model, int shadeHandling,
																boolean viewFromTop, boolean highlight) {
		tempVal[0] = 0.0;
		tempVal[1] = 0.0;
		double prediction = model.evaluateMean(tempVal);
		
		tempVal[0] = xMin;
		tempVal[1] = zMin;
		double predAtOrigin = model.evaluateMean(tempVal);
		
		if (highlight) {
			g.setColor((shadeHandling == IGNORE_OPAQUE) || !viewFromTop
																	? kLineColor : kDimLineColor);
			
			tempPt1 = getScreenPoint(0.0, yMin, 0.0, tempPt1);
			tempPt2 = getScreenPoint(xMin, yMin, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(0.0, yMin, zMin, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			
			tempPt2 = getScreenPoint(0.0, prediction, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
		}
		
		boolean isBold = highlight || (shadeHandling == IGNORE_OPAQUE)
											|| (viewFromTop == (predAtOrigin >= prediction));
		g.setColor(isBold ? kIntColor : kDimIntColor);
		
		tempPt1 = getScreenPoint(0.0, prediction, 0.0, tempPt1);
		tempPt2 = getScreenPoint(xMin, prediction, zMin, tempPt2);
		drawLine(g, tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y, highlight, FILLED_HEAD);
	}
	
	private void drawXSlope(Graphics g, MultipleRegnModel model, int shadeHandling,
															boolean viewFromTop, boolean highlight) {
		if (!xSlopeArrowVisible())
			return;
			
		tempVal[0] = 0.0;
		tempVal[1] = 0.0;
		double p0 = model.evaluateMean(tempVal);
		
		tempVal[0] = 1.0;
		double p1 = model.evaluateMean(tempVal);
		
		if (highlight) {
			g.setColor((shadeHandling == IGNORE_OPAQUE) || !viewFromTop
																	? kLineColor : kDimLineColor);
			
			tempPt1 = getScreenPoint(1.0, yMin, 0.0, tempPt1);
			tempPt2 = getScreenPoint(xMin, yMin, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(1.0, yMin, zMin, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(1.0, p1, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			
			tempPt1 = getScreenPoint(0.0, yMin, 0.0, tempPt1);
			tempPt2 = getScreenPoint(0.0, p0, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(0.0, yMin, zMin, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
		}
		
		boolean triangleFront = (model.getParameter(1).toDouble() <= 0.0) == viewFromTop;
		
		boolean boldTriangle = (shadeHandling == IGNORE_OPAQUE) || triangleFront;
		
		tempPt1 = getScreenPoint(0.0, p0, 0.0, tempPt1);
		xTemp[0] = xTemp[3] = tempPt1.x;
		yTemp[0] = yTemp[3] = tempPt1.y;
		
		tempPt1 = getScreenPoint(1.0, p0, 0.0, tempPt1);
		xTemp[1] = tempPt1.x;
		yTemp[1] = tempPt1.y;
		
		tempPt1 = getScreenPoint(1.0, p1, 0.0, tempPt1);
		xTemp[2] = tempPt1.x;
		yTemp[2] = tempPt1.y;
		
		g.setColor(boldTriangle ? kDimXSlopeColor : kDim2XSlopeColor);
		
		if (fillSlopeTriangles) {
			g.fillPolygon(xTemp, yTemp, 4);
			g.drawPolygon(xTemp, yTemp, 4);		//		so something is drawn if slope = 0
		}
		else
			g.drawLine(xTemp[0], yTemp[0], xTemp[1], yTemp[1]);
		
		g.setColor((highlight || triangleFront) ? kXSlopeColor : kDimXSlopeColor);
		
		drawLine(g, xTemp[1], yTemp[1], xTemp[2], yTemp[2], highlight, FILLED_HEAD);
	}
	
	private void drawZSlope(Graphics g, MultipleRegnModel model, int shadeHandling,
																boolean viewFromTop, boolean highlight) {
		if (!zSlopeArrowVisible())
			return;
			
		tempVal[0] = 0.0;
		tempVal[1] = 0.0;
		double p0 = model.evaluateMean(tempVal);
		
		tempVal[1] = 1.0;
		double p1 = model.evaluateMean(tempVal);
		
		if (highlight) {
			g.setColor((shadeHandling == IGNORE_OPAQUE) || !viewFromTop
																	? kLineColor : kDimLineColor);
			
			tempPt1 = getScreenPoint(0.0, yMin, 1.0, tempPt1);
			tempPt2 = getScreenPoint(0.0, yMin, zMin, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(xMin, yMin, 1.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(0.0, p1, 1.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			
			tempPt1 = getScreenPoint(0.0, yMin, 0.0, tempPt1);
			tempPt2 = getScreenPoint(0.0, p0, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(xMin, yMin, 0.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
		}
		
		boolean triangleFront = (model.getParameter(2).toDouble() <= 0.0) == viewFromTop;
		
		boolean boldTriangle = (shadeHandling == IGNORE_OPAQUE) || triangleFront;
		
		tempPt1 = getScreenPoint(0.0, p0, 0.0, tempPt1);
		xTemp[0] = xTemp[3] = tempPt1.x;
		yTemp[0] = yTemp[3] = tempPt1.y;
		
		tempPt1 = getScreenPoint(0.0, p0, 1.0, tempPt1);
		
		xTemp[1] = tempPt1.x;
		yTemp[1] = tempPt1.y;
		
		tempPt1 = getScreenPoint(0.0, p1, 1.0, tempPt1);
		
		xTemp[2] = tempPt1.x;
		yTemp[2] = tempPt1.y;
		
		g.setColor(boldTriangle ? kDimZSlopeColor : kDim2ZSlopeColor);
		
		if (fillSlopeTriangles) {
			g.fillPolygon(xTemp, yTemp, 4);
			g.drawPolygon(xTemp, yTemp, 4);		//		so something is drawn if slope = 0
		}
		else
			g.drawLine(xTemp[0], yTemp[0], xTemp[1], yTemp[1]);
		
		g.setColor((highlight || triangleFront) ? kZSlopeColor : kDimZSlopeColor);
		
		drawLine(g, xTemp[1], yTemp[1], xTemp[2], yTemp[2], highlight, FILLED_HEAD);
	}
	
	protected Point getFitPoint(double x, double z, MultipleRegnModel model, Point p) {
		tempVal[0] = x;
		tempVal[1] = z;
		double prediction = model.evaluateMean(tempVal);
		return getScreenPoint(x, prediction, z, p);
	}
	
	protected void drawArrowAt(Graphics g, double x, double z, MultipleRegnModel model) {
		tempPt1 = getFitPoint(x, z, model, tempPt1);
		ModelGraphics.drawHandle(g, tempPt1, false);
	}
	
	protected void drawForeground(Graphics g) {
		if (!arrowsVisible())
			return;
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		if (dragType == NO_DRAG) {
			drawArrowAt(g, 0.0, 0.0, model);
			if (xSlopeArrowVisible())
				drawArrowAt(g, 1.0, 0.0, model);
			if (zSlopeArrowVisible())
				drawArrowAt(g, 0.0, 1.0, model);
		}
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinHitDist = 100;
	
	protected int hitOffset;
	
	protected Point[] getDragPoints() {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		Point p[] = new Point[3];
		tempPt1 = getFitPoint(0.0, 0.0, model, tempPt1);
		p[0] = tempPt1;
		
		if (xSlopeArrowVisible()) {
			tempPt2 = getFitPoint(1.0, 0.0, model, tempPt2);
			p[1] = tempPt2;
		}
		if (zSlopeArrowVisible()) {
			tempPt3 = getFitPoint(0.0, 1.0, model, tempPt3);
			p[2] = tempPt3;
		}
		return p;
	}
	
	protected int[] getDragTypes() {
		int type[] = {DRAG_INTERCEPT, DRAG_X_SLOPE, DRAG_Z_SLOPE};
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
	
	protected int eqnParamFromDragType(int dragType) {
		return dragType - 1;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
			setArrowCursor();
			dragType = posInfo.index;
			hitOffset = posInfo.hitOffset;
			if (equationView != null)
				equationView.setSelectedParam(eqnParamFromDragType(dragType));
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragType != NO_DRAG) {
			MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
			
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y - hitOffset;
			Point p = translateToScreen(0, newYPos, null);
			
			try {
				switch (dragType) {
					case DRAG_INTERCEPT:
						double xFract = xAxis.numValToPosition(0.0);
						double zFract = zAxis.numValToPosition(0.0);
						double y = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						model.setParameter(0, y);
						break;
						
					case DRAG_X_SLOPE:
						xFract = xAxis.numValToPosition(1.0);
						zFract = zAxis.numValToPosition(0.0);
						double y1 = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						
						tempVal[0] = tempVal[1] = 0.0;
						double y0 = model.evaluateMean(tempVal);
						model.setParameter(1, y1-y0);
						break;
						
					case DRAG_Z_SLOPE:
						xFract = xAxis.numValToPosition(0.0);
						zFract = zAxis.numValToPosition(1.0);
						y1 = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
						
						tempVal[0] = tempVal[1] = 0.0;
						y0 = model.evaluateMean(tempVal);
						model.setParameter(2, y1-y0);
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
			if (equationView != null)
				equationView.setSelectedParam(-1);
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
