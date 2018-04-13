package multiRegn;

import java.awt.*;

import dataView.*;
import models.*;
import graphics3D.*;


public class PredictDrag3View extends ModelDot3View {
	
	static final private Color kPaleRed = getShadedColor(Color.red);
	static final private Color kPaleBlue = getShadedColor(Color.blue);
	
	static final private Color kPaleYellow = new Color(0xFFFF99);
//	static final private Color kPointHighlight = new Color(0xFF9900);
	
	private double tempVal[];
	
	public PredictDrag3View(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, null);
		tempVal = new double[explanKey.length];
	}
	
	protected void drawData(Graphics g, int shadeHandling) {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			
			for (int i=0 ; i<explanKey.length ; i++) {
				NumVariable xVariable = (NumVariable)getVariable(explanKey[i]);
				tempVal[i] = xVariable.doubleValueAt(selectedIndex);
			}
			
			MultipleRegnModel theModel = (MultipleRegnModel)getVariable(modelKey);
			double prediction = theModel.evaluateMean(tempVal);
			
			double xFract = xAxis.numValToPosition(tempVal[0]);
			double yFract = yAxis.numValToPosition(prediction);
			double zFract = zAxis.numValToPosition(tempVal[1]);
			
			tempVal[0] = xAxis.getMinOnAxis();
			tempVal[1] = zAxis.getMinOnAxis();
			double predictionAtOrigin = theModel.evaluateMean(tempVal);
			boolean arrowAbovePlane = prediction >= predictionAtOrigin;
			boolean fromPlaneTop = viewingPlaneFromTop();
			
			Point fitPoint = translateToScreen(map.mapH3DGraph(0.0, xFract, zFract),
											map.mapV3DGraph(0.0, xFract, zFract), null);
											
//			g.setColor(kPointHighlight);
//			g.fillOval(fitPoint.x - 2, fitPoint.y - 2, 5, 5);
			
			g.setColor((fromPlaneTop && shadeHandling == USE_OPAQUE) ? kPaleBlue : Color.blue);
			
			//		lines on x-z plane
			Point otherPoint = translateToScreen(map.mapH3DGraph(0.0, 0.0, zFract),
											map.mapV3DGraph(0.0, 0.0, zFract), null);
			drawLine(g, otherPoint.x, otherPoint.y, fitPoint.x, fitPoint.y, draggingXZ, NO_HEAD);
			
			otherPoint = translateToScreen(map.mapH3DGraph(0.0, xFract, 0.0),
											map.mapV3DGraph(0.0, xFract, 0.0), otherPoint);
			drawLine(g, otherPoint.x, otherPoint.y, fitPoint.x, fitPoint.y, draggingXZ, NO_HEAD);
			
			//		line from x-z plane to regn plane
			otherPoint = translateToScreen(map.mapH3DGraph(yFract, xFract, zFract),
											map.mapV3DGraph(yFract, xFract, zFract), otherPoint);
			drawLine(g, fitPoint.x, fitPoint.y, otherPoint.x, otherPoint.y, draggingXZ, LINE_HEAD);
			
			//		line from regn plane to y-axis
			g.setColor((fromPlaneTop == arrowAbovePlane || shadeHandling != USE_OPAQUE)
																							? Color.red : kPaleRed);
			fitPoint = translateToScreen(map.mapH3DGraph(yFract, 0.0, 0.0),
											map.mapV3DGraph(yFract, 0.0, 0.0), fitPoint);
			drawLine(g, otherPoint.x, otherPoint.y, fitPoint.x, fitPoint.y, draggingXZ, FILLED_HEAD);
		}
	}
	
	private void drawBase(Graphics g) {
		int x[] = new int[5];
		int y[] = new int[5];
		Point p0 = translateToScreen(map.mapH3DGraph(0.0, 0.0, 0.0),
															map.mapV3DGraph(0.0, 0.0, 0.0), null);
		x[0] = x[4] = p0.x;
		y[0] = y[4] = p0.y;
		p0 = translateToScreen(map.mapH3DGraph(0.0, 1.0, 0.0),
															map.mapV3DGraph(0.0, 1.0, 0.0), p0);
		x[1] = p0.x;
		y[1] = p0.y;
		p0 = translateToScreen(map.mapH3DGraph(0.0, 1.0, 1.0),
															map.mapV3DGraph(0.0, 1.0, 1.0), p0);
		x[2] = p0.x;
		y[2] = p0.y;
		p0 = translateToScreen(map.mapH3DGraph(0.0, 0.0, 1.0),
															map.mapV3DGraph(0.0, 0.0, 1.0), p0);
		x[3] = p0.x;
		y[3] = p0.y;
		
		g.setColor(kPaleYellow);
		g.fillPolygon(x, y, 5);
	}
	
	protected void drawAxes(Graphics g, boolean backNotFront, int colourType) {
		if (colourType == D3Axis.BACKGROUND && map.getTheta2() < 180) {
			drawBase(g);
			super.drawAxes(g, backNotFront, colourType);
		}
		else if (colourType == D3Axis.FOREGROUND && map.getTheta2() > 180) {
			drawBase(g);
			super.drawAxes(g, backNotFront, colourType);
		}
		else
			super.drawAxes(g, backNotFront, colourType);
	}

//-----------------------------------------------------------------------------------
	
	private Point hitPos = new Point(0,0);
	private double hitFracts[] = new double[2];
	private boolean draggingXZ = false;
	
	private double constrain01(double fract) {
		return Math.max(0.0, Math.min(1.0, fract));
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		PositionInfo result = super.getInitialPosition(x, y);
		if (result == null) {
			hitPos = translateFromScreen(x, y, hitPos);
			hitFracts = map.mapToYX(hitPos);
			
			result = new XZDragPosInfo(constrain01(hitFracts[0]), constrain01(hitFracts[1]));
		}
		return result;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (draggingXZ) {
			hitPos = translateFromScreen(x, y, hitPos);
			hitFracts = map.mapToYX(hitPos);
			
			return new XZDragPosInfo(constrain01(hitFracts[0]), constrain01(hitFracts[1]));
		}
		else
			return super.getPosition(x, y);
	}
	
	private void setSelectedXZ(XZDragPosInfo toInfo) {
		double newX = xAxis.positionToNumVal(toInfo.x);
		double newZ = zAxis.positionToNumVal(toInfo.z);
		
		NumVariable xVariable = (NumVariable)getVariable(explanKey[0]);
		NumValue xValue = (NumValue)xVariable.valueAt(0);
		xValue.setValue(newX);
		
		NumVariable zVariable = (NumVariable)getVariable(explanKey[1]);
		NumValue zValue = (NumValue)zVariable.valueAt(0);
		zValue.setValue(newZ);
		
		getData().setSelection(0);
		getData().valueChanged(0);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof XZDragPosInfo) {
			setArrowCursor();
			draggingXZ = true;
			setSelectedXZ((XZDragPosInfo)startInfo);
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingXZ) {
			if (toPos == null)
				getData().clearSelection();
			else
				setSelectedXZ((XZDragPosInfo)toPos);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingXZ) {
			draggingXZ = false;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
