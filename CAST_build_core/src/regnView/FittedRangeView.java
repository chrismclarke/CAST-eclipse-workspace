package regnView;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;

import regn.*;


public class FittedRangeView extends DragLocationView {
//	static public final String FITTED_RANGE_PLOT = "fittedRangePlot";
	static final public Color errorColor = new Color(0x009900);		//	dark green
	
	private String xKey, yKey, lineKey;
	private VertAxis yAxis;
	private boolean showErrors = false;
	
	private PredictionView linkedPrediction = null;
	private PlusMinusView linkedPredictionSD = null;
	
	private Color lineColor = Color.lightGray;
	
	public FittedRangeView(DataSet theData, XApplet applet, DragValAxis xAxis,
												VertAxis yAxis, String xKey, String yKey, String lineKey) {
		super(theData, applet, xAxis);
		this.xKey = xKey;
		this.yKey = yKey;
		this.lineKey = lineKey;
		this.yAxis = yAxis;
	}
	
	public void setLinkedPrediction(PredictionView linkedPrediction, PlusMinusView linkedPredictionSD) {
		this.linkedPrediction = linkedPrediction;
		this.linkedPredictionSD = linkedPredictionSD;
	}
	
	public void setErrorDisplay(boolean showErrors) {
		this.showErrors = showErrors;
	}
	
	public void setLineColor(Color newColor) {
		lineColor = newColor;
	}
	
	protected Point getScreenPoint(NumValue xVal, NumValue yVal, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = axis.numValToPosition(xVal.toDouble());
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		NumVariable xVariable = (NumVariable)getVariable(xKey);
		NumVariable yVariable = (NumVariable)getVariable(yKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point thePoint = null;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			NumValue xVal = (NumValue)xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			thePoint = getScreenPoint(xVal, yVal, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
		
		if (yAxis instanceof PredictionAxis) {
			((PredictionAxis)yAxis).checkPrediction();
			yAxis.repaint();
		}
		
		if (linkedPrediction != null)
			linkedPrediction.redrawAll();
		
		if (linkedPredictionSD != null)
			linkedPredictionSD.redrawAll();
	}
	
	private void drawBackground(Graphics g) {
		LinearModel model = (LinearModel)getVariable(lineKey);
		DragValAxis xAxis = (DragValAxis)axis;
		
		g.setColor(lineColor);
		model.drawMean(g, this, xAxis, yAxis);
		
		double constValue = xAxis.getAxisVal().toDouble();
		
		try {
			int xPos = xAxis.numValToPosition(constValue);
			double prediction = model.evaluateMean(constValue);
			int yPos = yAxis.numValToPosition(prediction);
			Point linePt = translateToScreen(xPos, yPos, null);
			
			if (selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(linePt.x - 2, 0, 5, getSize().height);
			}
			
			if (showErrors) {
				double predict2SD = 2.0 * model.evaluateSD(xAxis.getAxisVal()).toDouble();
				int meanPlusPos = yAxis.numValToRawPosition(prediction + predict2SD);
				int meanMinusPos = yAxis.numValToRawPosition(prediction - predict2SD);
				Point topPt = translateToScreen(xPos, meanPlusPos, null);
				Point bottomPt = translateToScreen(xPos, meanMinusPos, null);
				
				g.setColor(errorColor);
				g.fillRect(topPt.x - 3, topPt.y, 7, bottomPt.y - topPt.y + 1);
			}
			
			g.setColor(Color.red);
			g.drawLine(linePt.x, 0, linePt.x, getSize().height - 1);
			
			g.setColor(Color.blue);
			g.drawLine(linePt.x, linePt.y, 0, linePt.y);
			g.drawLine(0, linePt.y, 3, linePt.y + 3);
			g.drawLine(0, linePt.y, 3, linePt.y - 3);
		} catch (AxisException e) {
		}
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
}
	
