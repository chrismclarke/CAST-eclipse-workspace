package curveInteract;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;
import graphics3D.*;

import multiRegn.*;


public class DragParamInteractView extends DragParam3View {
//	static public final String DRAG_PARAM_INTERACT = "dragParamInteract";
	
	static final private int DRAG_INTERACT = 4;
	
	static final private Color kInteractColor = ColoredLinearEqnView.kExtraColor;
	static final private Color kDimInteractColor = getShadedColor(kInteractColor);
//	static final private Color kDim2InteractColor = getShadedColor(kDimInteractColor);
	
	static final private int kGridLines = 10;
	
	private String interactModelKey;
	
	private Point tempPt4;
	private double tempVal[] = new double[3];
	
	public DragParamInteractView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String interactModelKey, ColoredLinearEqnView equationView) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, equationView);
		this.interactModelKey = interactModelKey;
		fillSlopeTriangles = false;
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		super.drawData(g, shadeHandling);
		
		MultipleRegnModel interactModel = (MultipleRegnModel)getVariable(interactModelKey);
		
		if (arrowsVisible()) {
//			MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
			
			boolean viewFromTop = viewingPlaneFromTop();
			
			switch (dragType) {
				case NO_DRAG:
					drawInteract(g, interactModel, shadeHandling, viewFromTop, false);
					break;
				case DRAG_INTERACT:
					drawInteract(g, interactModel, shadeHandling, viewFromTop, true);
					break;
				default:
			}
		}
		drawModelGrid(g, interactModel, shadeHandling);
	}
	
	protected Point getInteractFitPoint(double x, double z, MultipleRegnModel interactModel,
																																		Point p) {
		tempVal[0] = x;
		tempVal[1] = z;
		tempVal[2] = x * z;
		double prediction = interactModel.evaluateMean(tempVal);
		return getScreenPoint(x, prediction, z, p);
	}
	
	private void drawModelGrid(Graphics g, MultipleRegnModel interactModel, int shadeHandling) {
		boolean interactAbove = (interactModel.getParameter(3).toDouble() >= 0);
		boolean interactInFront = (shadeHandling == IGNORE_OPAQUE) || (interactAbove == viewingPlaneFromTop());
		
		g.setColor(interactInFront ? Color.black : Color.gray);
		
		Point p0 = null;
		Point p1 = null;
		
		double xMax = xAxis.getMaxOnAxis();
		double zMax = zAxis.getMaxOnAxis();
		for (int i=0 ; i<=kGridLines ; i++) {
			double x = xMin + (xMax - xMin) * i / kGridLines;
			p0 = getInteractFitPoint(x, zMin, interactModel, p0);
			p1 = getInteractFitPoint(x, zMax, interactModel, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		for (int i=0 ; i<=kGridLines ; i++) {
			double z = zMin + (zMax - zMin) * i / kGridLines;
			p0 = getInteractFitPoint(xMin, z, interactModel, p0);
			p1 = getInteractFitPoint(xMax, z, interactModel, p1);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
	}
	
	private void drawInteract(Graphics g, MultipleRegnModel interactModel,
																int shadeHandling, boolean viewFromTop, boolean highlight) {
		tempVal[0] = tempVal[1] = tempVal[2] = 1.0;
		double p0 = interactModel.evaluateMean(tempVal);
		
		tempVal[2] = 0.0;
		double p1 = interactModel.evaluateMean(tempVal);
		
		if (highlight) {
			g.setColor((shadeHandling == IGNORE_OPAQUE) || !viewFromTop
																	? kLineColor : kDimLineColor);
			
			tempPt1 = getScreenPoint(1.0, yMin, 1.0, tempPt1);
			tempPt2 = getScreenPoint(xMin, yMin, 1.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(1.0, yMin, zMin, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
			tempPt2 = getScreenPoint(1.0, p1, 1.0, tempPt2);
			g.drawLine(tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y);
		}
		
		boolean triangleFront = (interactModel.getParameter(1).toDouble() <= 0.0) == viewFromTop;
		
//		boolean boldTriangle = (shadeHandling == IGNORE_OPAQUE) || triangleFront;
		
		tempPt1 = getScreenPoint(1.0, p1, 1.0, tempPt1);
		tempPt2 = getScreenPoint(1.0, p0, 1.0, tempPt2);
		
		g.setColor((highlight || triangleFront) ? kInteractColor : kDimInteractColor);
		
		drawLine(g, tempPt1.x, tempPt1.y, tempPt2.x, tempPt2.y, highlight, FILLED_HEAD);
	}
	
	protected void drawArrowAt(Graphics g, double x, double z, MultipleRegnModel model) {
		Point p = getInteractFitPoint(x, z, model, null);
		ModelGraphics.drawHandle(g, p, false);
	}
	
	protected void drawForeground(Graphics g) {
		if (!arrowsVisible())
			return;
		
		MultipleRegnModel interactModel = (MultipleRegnModel)getVariable(interactModelKey);
		
		if (dragType == NO_DRAG) {
			drawArrowAt(g, 0.0, 0.0, interactModel);
			if (xSlopeArrowVisible())
				drawArrowAt(g, 1.0, 0.0, interactModel);
			if (zSlopeArrowVisible())
				drawArrowAt(g, 0.0, 1.0, interactModel);
			drawArrowAt(g, 1.0, 1.0, interactModel);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected Point[] getDragPoints() {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(interactModelKey);
		Point p[] = new Point[4];
		tempPt1 = getInteractFitPoint(0.0, 0.0, model, tempPt1);
		p[0] = tempPt1;
		
		if (xSlopeArrowVisible()) {
			tempPt2 = getInteractFitPoint(1.0, 0.0, model, tempPt2);
			p[1] = tempPt2;
		}
		if (zSlopeArrowVisible()) {
			tempPt3 = getInteractFitPoint(0.0, 1.0, model, tempPt3);
			p[2] = tempPt3;
		}
		tempPt4 = getInteractFitPoint(1.0, 1.0, model, tempPt4);
		p[3] = tempPt4;
		return p;
	}
	
	protected int[] getDragTypes() {
		int type[] = {DRAG_INTERCEPT, DRAG_X_SLOPE, DRAG_Z_SLOPE, DRAG_INTERACT};
		return type;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragType == DRAG_INTERACT) {
			MultipleRegnModel interactModel = (MultipleRegnModel)getVariable(interactModelKey);
			
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y - hitOffset;
			Point p = translateToScreen(0, newYPos, null);
			
			try {
				double xFract = xAxis.numValToPosition(1.0);
				double zFract = zAxis.numValToPosition(1.0);
				double y1 = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
				
				tempVal[0] = tempVal[1] = 1.0;
				tempVal[2] = 0.0;				//	for fit without interaction
				double y0 = interactModel.evaluateMean(tempVal);
				interactModel.setParameter(3, y1 - y0);
			} catch (AxisException e) {
			}
						
			getData().variableChanged(interactModelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}


}
	
