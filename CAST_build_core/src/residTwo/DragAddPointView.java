package residTwo;

import java.awt.*;

import dataView.*;
import graphics3D.*;

import multiRegn.*;
import residTwoProg.*;


public class DragAddPointView extends RotateModelBandView {
	
//	static final private Color kPaleRed = getShadedColor(Color.red);
//	static final private Color kPaleBlue = getShadedColor(Color.blue);
//	static final private Color kPaleYellow = new Color(0xFFFF99);
	
	private String[] explanKey;
//	private String modelKey;
	private LeverageGraphApplet applet;
	private SummaryDataSet summaryData;
	
//	private double tempVal[];
	
	public DragAddPointView(DataSet theData, XApplet applet, D3Axis xAxis, D3Axis yAxis, D3Axis zAxis,
						String modelKey, String[] explanKey, SummaryDataSet summaryData) {
		super(theData, applet, xAxis, yAxis, zAxis, modelKey);
		this.explanKey = explanKey;
//		this.modelKey = modelKey;
		this.summaryData = summaryData;
		this.applet = (LeverageGraphApplet)applet;
//		tempVal = new double[explanKey.length];
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
	
	public void setLastXZ(XZDragPosInfo toInfo, D3Axis xAxis, D3Axis zAxis) {
		double newX = (toInfo == null) ? Double.NaN : xAxis.positionToNumVal(toInfo.x);
		double newZ = (toInfo == null) ? Double.NaN : zAxis.positionToNumVal(toInfo.z);
		
		NumVariable xVariable = (NumVariable)getVariable(explanKey[0]);
		int lastIndex = xVariable.noOfValues() - 1;
		NumValue xValue = (NumValue)xVariable.valueAt(lastIndex);
		xValue.setValue(newX);
		
		NumVariable zVariable = (NumVariable)getVariable(explanKey[1]);
		NumValue zValue = (NumValue)zVariable.valueAt(lastIndex);
		zValue.setValue(newZ);
		
		if (toInfo == null)
			getData().clearSelection();
		else
			getData().setSelection(lastIndex);
		getData().valueChanged(lastIndex);
		applet.setCoeffDistns(getData(), summaryData);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof XZDragPosInfo) {
			setArrowCursor();
			draggingXZ = true;
			setLastXZ((XZDragPosInfo)startInfo, xAxis, zAxis);
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (draggingXZ) {
			if (toPos == null)
				setLastXZ(null, xAxis, zAxis);
			else
				setLastXZ((XZDragPosInfo)toPos, xAxis, zAxis);
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (draggingXZ) {
			draggingXZ = false;
			setLastXZ(null, xAxis, zAxis);
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
	}
}
	
