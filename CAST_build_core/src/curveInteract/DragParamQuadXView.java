package curveInteract;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;

import multiRegn.*;


public class DragParamQuadXView extends DragParam3View {
	
	static final private int DRAG_QUADX = 4;
	
	static final private Color kQuadXColor = ColoredLinearEqnView.kExtraColor;
//	static final private Color kDimQuadXColor = getShadedColor(kQuadXColor);
	
	static final private int kGridLines = 10;
	
	private String quadXModelKey;
	
	private Point tempPt4;
	private double tempVal[] = new double[3];
	
	public DragParamQuadXView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String quadXModelKey, ColoredLinearEqnView equationView) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, equationView);
		this.quadXModelKey = quadXModelKey;
		fillSlopeTriangles = false;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		super.drawData(g, shadeHandling);
		
		MultipleRegnModel quadXModel = (MultipleRegnModel)getVariable(quadXModelKey);
		
		if (arrowsVisible()) {
			boolean viewFromTop = viewingPlaneFromTop();
			
			switch (dragType) {
				case NO_DRAG:
					drawQuadX(g, quadXModel, shadeHandling, viewFromTop, false);
					break;
				case DRAG_QUADX:
					drawQuadX(g, quadXModel, shadeHandling, viewFromTop, true);
					break;
				default:
			}
		}
		drawModelGrid(g, quadXModel, shadeHandling);
	}
	
	protected Point getQuadXFitPoint(double x, double z, MultipleRegnModel quadXModel,
																																		Point p) {
		tempVal[0] = x;
		tempVal[1] = z;
		tempVal[2] = x * x;
		double prediction = quadXModel.evaluateMean(tempVal);
		return getScreenPoint(x, prediction, z, p);
	}
	
	private void drawModelGrid(Graphics g, MultipleRegnModel quadXModel, int shadeHandling) {
		boolean quadXAbove = (quadXModel.getParameter(3).toDouble() >= 0);
		boolean quadXInFront = (shadeHandling == IGNORE_OPAQUE) || (quadXAbove == viewingPlaneFromTop());
		
		g.setColor(quadXInFront ? Color.black : Color.gray);
		
		Point p0 = null;
		Point p1 = null;
		
		double xMax = xAxis.getMaxOnAxis();
		double zMax = zAxis.getMaxOnAxis();
		for (int i=0 ; i<=kGridLines ; i++) {
			double x = xMin + (xMax - xMin) * i / kGridLines;
			p0 = getQuadXFitPoint(x, zMin, quadXModel, p0);
			p1 = getQuadXFitPoint(x, zMax, quadXModel, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		for (int j=0 ; j<=kGridLines ; j++) {
			double z = zMin + (zMax - zMin) * j / kGridLines;
			
			for (int i=0 ; i<kGridLines ; i++) {
				double x = xMin + (xMax - xMin) * i / kGridLines;
				double xPlus = xMin + (xMax - xMin) * (i + 1) / kGridLines;
				p0 = getQuadXFitPoint(x, z, quadXModel, p0);
				p1 = getQuadXFitPoint(xPlus, z, quadXModel, p1);
				
				g.drawLine(p0.x, p0.y, p1.x, p1.y);
			}
		}
	}
	
	private void drawQuadX(Graphics g, MultipleRegnModel quadXModel, int shadeHandling,
																					boolean viewFromTop, boolean highlight) {
		tempVal[0] = tempVal[1] = tempVal[2] = 1.0;
		double p0 = quadXModel.evaluateMean(tempVal);
		
		tempVal[2] = 0.0;
		double p1 = quadXModel.evaluateMean(tempVal);
		
		tempPt1 = getScreenPoint(1.0, p1, 1.0, tempPt1);
		tempPt2 = getScreenPoint(1.0, p0, 1.0, tempPt2);
		
		g.setColor(kQuadXColor);
		
		drawLine(g, tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y, highlight, FILLED_HEAD);
	}
	
	private void drawQuadArrowAt(Graphics g, double x, double z, MultipleRegnModel model) {
		Point p = getQuadXFitPoint(x, z, model, null);
		ModelGraphics.drawHandle(g, p, false);
	}
	
	protected void drawForeground(Graphics g) {
		if (!arrowsVisible())
			return;
		
		MultipleRegnModel quadXModel = (MultipleRegnModel)getVariable(quadXModelKey);
		MultipleRegnModel linModel = (MultipleRegnModel)getVariable(modelKey);
		
		if (dragType == NO_DRAG) {
			drawQuadArrowAt(g, 0.0, 0.0, quadXModel);
			if (xSlopeArrowVisible())
				drawArrowAt(g, 1.0, 0.0, linModel);
			if (zSlopeArrowVisible())
				drawQuadArrowAt(g, 0.0, 1.0, quadXModel);
			drawQuadArrowAt(g, 1.0, 1.0, quadXModel);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected Point[] getDragPoints() {
		MultipleRegnModel quadModel = (MultipleRegnModel)getVariable(quadXModelKey);
		MultipleRegnModel linModel = (MultipleRegnModel)getVariable(modelKey);
		Point p[] = new Point[4];
		tempPt1 = getQuadXFitPoint(0.0, 0.0, quadModel, tempPt1);
		p[0] = tempPt1;
		
		if (xSlopeArrowVisible()) {
			tempPt2 = getFitPoint(1.0, 0.0, linModel, tempPt2);
			p[1] = tempPt2;
		}
		if (zSlopeArrowVisible()) {
			tempPt3 = getQuadXFitPoint(0.0, 1.0, quadModel, tempPt3);
			p[2] = tempPt3;
		}
		tempPt4 = getQuadXFitPoint(1.0, 1.0, quadModel, tempPt4);
		p[3] = tempPt4;
		return p;
	}
	
	protected int[] getDragTypes() {
		int type[] = {DRAG_INTERCEPT, DRAG_X_SLOPE, DRAG_Z_SLOPE, DRAG_QUADX};
		return type;
	}
	
	protected int eqnParamFromDragType(int dragType) {
		if (dragType <= DRAG_X_SLOPE)
			return dragType - 1;
		else if (dragType == DRAG_Z_SLOPE)
			return 2;
		else
			return 3;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragType == DRAG_Z_SLOPE || dragType == DRAG_QUADX) {
			MultipleRegnModel quadXModel = (MultipleRegnModel)getVariable(quadXModelKey);
			
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y - hitOffset;
			Point p = translateToScreen(0, newYPos, null);
			
			try {
				if (dragType == DRAG_Z_SLOPE) {
					double xFract = xAxis.numValToPosition(0.0);
					double zFract = zAxis.numValToPosition(1.0);
					double y1 = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
					
					tempVal[0] = tempVal[1] = tempVal[2] = 0.0;
					double y0 = quadXModel.evaluateMean(tempVal);
					quadXModel.setParameter(2, y1-y0);
				}
				else {
					double xFract = xAxis.numValToPosition(1.0);
					double zFract = zAxis.numValToPosition(1.0);
					double y1 = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
					
					tempVal[0] = 1.0;
					tempVal[1] = 1.0;				//	for fit without interaction
					tempVal[2] = 0.0;
					double y0 = quadXModel.evaluateMean(tempVal);
					quadXModel.setParameter(3, y1 - y0);
				}
			} catch (AxisException e) {
			}
						
			getData().variableChanged(quadXModelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}


}
	
