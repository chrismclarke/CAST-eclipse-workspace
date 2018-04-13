package graphics3D;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class DragResponseSurfaceView extends ResponseSurfaceView {
//	static public final String DRAG_RESPONSE_SURFACE_PLOT = "dragResponseSurface";
	
	static final private int NO_DRAG = -1;
	static final private int ORIGIN_INDEX = 0;
	static final private int X_MAX_INDEX = 1;
	static final private int Z_MAX_INDEX = 2;
	static final private int X_MIDDLE_INDEX = 3;
	static final private int Z_MIDDLE_INDEX = 4;
	static final private int XZ_MAX_INDEX = 5;
	
	private String xHandleKey, zHandleKey, yHandleKey;
	private String xzHandleKeys[] = new String[2];
	protected double startY;
	
	private double tempExplan[] = new double[2];
	protected Point tempPt = null;
	protected boolean[] tempVisibility = new boolean[6];
	
	protected int dragIndex = NO_DRAG;
	private double xMin, zMin, xMax, zMax, xMid, zMid;
	
	protected double[] constraints = {Double.NaN, Double.NaN, Double.NaN, 0.0, 0.0, 0.0};
	
	public DragResponseSurfaceView(DataSet theData,
						XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, String yKey, String xHandleKey,
						String zHandleKey, String yHandleKey) {
		super(theData, applet, xAxis, yAxis, zAxis,
																			modelKey, explanKey, yKey);
//		this.startY = startY;
		this.xHandleKey = xHandleKey;
		this.zHandleKey = zHandleKey;
		xzHandleKeys[0] = xHandleKey;
		xzHandleKeys[1] = zHandleKey;
		this.yHandleKey = yHandleKey;
		
		drawData = true;
	}
	
	public DragResponseSurfaceView(DataSet theData, XApplet applet,
								D3Axis xAxis, D3Axis yAxis, D3Axis zAxis, String modelKey,
								String[] explanKey, String yKey) {
		this(theData, applet, xAxis, yAxis, zAxis, modelKey, explanKey, yKey,
																							explanKey[0], explanKey[1], yKey);
		
		drawData = false;
	}		//	default uses data values as handles but does not draw crosses for them
	
	
	public void resetModel(double startY) {
		this.startY = startY;
		
		xMin = xAxis.getMinOnAxis();
		zMin = zAxis.getMinOnAxis();
		xMax = xAxis.getMaxOnAxis();
		zMax = zAxis.getMaxOnAxis();
		xMid = (xMax + xMin) * 0.5;
		zMid = (zMax + zMin) * 0.5;
		
		NumVariable yVar = (NumVariable)getVariable(yHandleKey);
		NumVariable xVar = (NumVariable)getVariable(xHandleKey);
		NumVariable zVar = (NumVariable)getVariable(zHandleKey);
		
		setValues(xVar, zVar, yVar, ORIGIN_INDEX, xMin, zMin, startY);
		setValues(xVar, zVar, yVar, X_MAX_INDEX, xMax, zMin, startY);
		setValues(xVar, zVar, yVar, Z_MAX_INDEX, xMin, zMax, startY);
		setValues(xVar, zVar, yVar, X_MIDDLE_INDEX, xMid, zMin, Double.NaN);
		setValues(xVar, zVar, yVar, Z_MIDDLE_INDEX, xMin, zMid, Double.NaN);
		setValues(xVar, zVar, yVar, XZ_MAX_INDEX, xMax, zMax, Double.NaN);
		
		constraints[3] = constraints[4] = constraints[5] = 0.0;
		
		updateModel();
	}
	
	public void setHandlesForModel() {
		xMin = xAxis.getMinOnAxis();
		zMin = zAxis.getMinOnAxis();
		xMax = xAxis.getMaxOnAxis();
		zMax = zAxis.getMaxOnAxis();
		xMid = (xMax + xMin) * 0.5;
		zMid = (zMax + zMin) * 0.5;
		
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		NumVariable yVar = (NumVariable)getVariable(yHandleKey);
		NumVariable xVar = (NumVariable)getVariable(xHandleKey);
		NumVariable zVar = (NumVariable)getVariable(zHandleKey);
		
		setFitValues(xVar, zVar, yVar, ORIGIN_INDEX, model, xMin, zMin);
		setFitValues(xVar, zVar, yVar, X_MAX_INDEX, model, xMax, zMin);
		setFitValues(xVar, zVar, yVar, Z_MAX_INDEX, model, xMin, zMax);
		setFitValues(xVar, zVar, yVar, X_MIDDLE_INDEX, model, xMid, zMin);
		setFitValues(xVar, zVar, yVar, Z_MIDDLE_INDEX, model, xMin, zMid);
		setFitValues(xVar, zVar, yVar, XZ_MAX_INDEX, model, xMax, zMax);
	}
	
	private void setFitValues(NumVariable xVar, NumVariable zVar, NumVariable yVar, int index,
											ResponseSurfaceModel model, double x, double z) {
		double y = Double.NaN;
		if (!Double.isNaN(yVar.doubleValueAt(index))) {
			tempExplan[0] = x;
			tempExplan[1] = z;
			y = model.evaluateMean(tempExplan);
		}
		setValues(xVar, zVar, yVar, index, x, z, y);
	}
	
	protected void setValues(NumVariable xVar, NumVariable zVar, NumVariable yVar,
																							int index, double x, double z, double y) {
		((NumValue)xVar.valueAt(index)).setValue(x);
		((NumValue)zVar.valueAt(index)).setValue(z);
		((NumValue)yVar.valueAt(index)).setValue(y);
	}
	
	protected void updateModel() {
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		model.setXKey(xzHandleKeys);
		model.updateLSParams(yHandleKey, constraints);
		model.setXKey(explanKey);
	}
	
	public void setDataLsModel() {
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		model.setXKey(explanKey);
		model.updateLSParams(yKey, constraints);
		
		NumVariable yVar = (NumVariable)getVariable(yHandleKey);
		NumVariable xVar = (NumVariable)getVariable(xHandleKey);
		NumVariable zVar = (NumVariable)getVariable(zHandleKey);
		for (int i=0 ; i<6 ; i++) {
			NumValue y = (NumValue)yVar.valueAt(i);
			if (!Double.isNaN(y.toDouble())) {
				tempExplan[0] = xVar.doubleValueAt(i);
				tempExplan[1] = zVar.doubleValueAt(i);
				y.setValue(model.evaluateMean(tempExplan));
			}
		}
	}
	
	public void setAllowTerm(int termIndex, boolean allowTerm) {
		constraints[termIndex] = allowTerm ? Double.NaN : 0.0;
		
		NumVariable yVar = (NumVariable)getVariable(yHandleKey);
		ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
		if (allowTerm) {
			NumVariable xVar = (NumVariable)getVariable(xHandleKey);
			NumVariable zVar = (NumVariable)getVariable(zHandleKey);
			tempExplan[0] = xVar.doubleValueAt(termIndex);
			tempExplan[1] = zVar.doubleValueAt(termIndex);
			((NumValue)yVar.valueAt(termIndex)).setValue(model.evaluateMean(tempExplan));
		}
		else {
			((NumValue)yVar.valueAt(termIndex)).setValue(Double.NaN);
			updateModel();
		}
	}
	
//--------------------------------------------------------------
	
	protected boolean[] arrowVisibility() {
		boolean arrowsVisible = map.getTheta2() < 90 || map.getTheta2() > 270;
//		boolean xSlopeArrowVisible = map.getTheta1() % 180 != 90;
//		boolean zSlopeArrowVisible = map.getTheta1() % 180 != 0;
		boolean xSlopeArrowVisible = true;
		boolean zSlopeArrowVisible = true;
		
		tempVisibility[ORIGIN_INDEX] = arrowsVisible;
		tempVisibility[X_MAX_INDEX] = arrowsVisible && xSlopeArrowVisible;
		tempVisibility[Z_MAX_INDEX] = arrowsVisible && zSlopeArrowVisible;
		tempVisibility[X_MIDDLE_INDEX] = arrowsVisible;
		tempVisibility[Z_MIDDLE_INDEX] = arrowsVisible && zSlopeArrowVisible;
		tempVisibility[XZ_MAX_INDEX] = arrowsVisible && xSlopeArrowVisible && zSlopeArrowVisible;
		
		return tempVisibility;
	}
	
	protected Point indexToScreenPoint(int valIndex) {
		NumVariable yVar = (NumVariable)getVariable(yHandleKey);
		NumVariable xVar = (NumVariable)getVariable(xHandleKey);
		NumVariable zVar = (NumVariable)getVariable(zHandleKey);
		
		double y = yVar.doubleValueAt(valIndex);
		if (Double.isNaN(y))
			return null;
		
		double x = xVar.doubleValueAt(valIndex);
		double z = zVar.doubleValueAt(valIndex);
		
		return getScreenPoint(x, y, z, tempPt);
	}
	
	protected void drawArrowAt(Graphics g, int valIndex, boolean highlight) {
		tempPt = indexToScreenPoint(valIndex);
		
		if (tempPt != null)
			ModelGraphics.drawHandle(g, tempPt, highlight);
	}
	
	protected void drawForeground(Graphics g) {
		boolean[] isVisible = arrowVisibility();
		
		for (int i=0 ; i<6 ; i++)
			if (isVisible[i])
				drawArrowAt(g, i, i == dragIndex);
	}

//-----------------------------------------------------------------------------------
	
	static final private int kMinHitDist = 100;
	
	protected int hitOffset;
	
	protected PositionInfo getInitialPosition(int x, int y) {
		boolean[] isVisible = arrowVisibility();
		
		int minHitDist = Integer.MAX_VALUE;
		int minHitIndex = NO_DRAG;
		int minHitOffset = 0;
		
		for (int i=0 ; i<6 ; i++) {
			Point p = indexToScreenPoint(i);
			if (p != null && isVisible[i]) {
				int xDist = x - p.x;
				int yDist = y - p.y;
				int thisHitDist = xDist * xDist + yDist * yDist;
				if (thisHitDist < minHitDist) {
					minHitIndex = i;
					minHitDist = thisHitDist;
					minHitOffset = yDist;
				}
			}
		}
		
		if (minHitDist <= kMinHitDist)
			return new VertDragPosInfo(y, minHitIndex, minHitOffset);
		
		return super.getInitialPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (dragIndex != NO_DRAG)
			return new VertDragPosInfo(y);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof VertDragPosInfo) {
			VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
			setArrowCursor();
			dragIndex = posInfo.index;
			hitOffset = posInfo.hitOffset;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (dragIndex != NO_DRAG) {
			NumVariable xVar = (NumVariable)getVariable(xHandleKey);
			NumVariable zVar = (NumVariable)getVariable(zHandleKey);
			double x = xVar.doubleValueAt(dragIndex);
			double z = zVar.doubleValueAt(dragIndex);
			try {
				double xFract = xAxis.numValToPosition(x);
				double zFract = zAxis.numValToPosition(z);
				
				VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
				int newYPos = dragPos.y - hitOffset;
				Point p = translateToScreen(0, newYPos, null);
				double yNew = yAxis.positionToNumVal(map.mapToD(p, xFract, zFract));
				
				NumVariable yVar = (NumVariable)getVariable(yHandleKey);
				((NumValue)yVar.valueAt(dragIndex)).setValue(yNew);
				
				ResponseSurfaceModel model = (ResponseSurfaceModel)getModel();
				model.setXKey(xzHandleKeys);
				model.updateLSParams(yHandleKey, constraints);
				model.setXKey(explanKey);
				
				getApplet().notifyDataChange(this);
			} catch (AxisException e) {
			}
			getData().variableChanged(modelKey);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragIndex != NO_DRAG) {
			dragIndex = NO_DRAG;
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}

}
	
