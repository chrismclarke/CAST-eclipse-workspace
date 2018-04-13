package indicator;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreVariables.*;
import coreGraphics.*;


public class DragCatCatLinesView extends DragParallelLinesView {
	
	static final private Color kMainEffectArrowColor = Color.red;
	static final private Color kInteractionArrowColor = new Color(0x009900);
	
	private CatVariable xDataVar;
	private CatCatInteractionVariable xzInteractionDataVar;
	
	private int nx, nz;
	
	private double constraints[] = null;
	
	public DragCatCatLinesView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String[] xDataKey, String yDataKey, 
						String[] xHandleKey, String yHandleKey, String modelKey, int[] paramDecimals) {
		super(theData, applet, xAxis, yAxis, xDataKey, yDataKey, xHandleKey, yHandleKey, modelKey, paramDecimals);
		xDataVar = (CatVariable)theData.getVariable(xDataKey[0]);
		nx = xDataVar.noOfCategories();
		CatVariable zVar = (CatVariable)getVariable(xDataKey[1]);
		nz = zVar.noOfCategories();
		xzInteractionDataVar = (CatCatInteractionVariable)theData.getVariable(xDataKey[2]);
	}
	
	public void setConstraints(double[] constraints) {
		this.constraints = constraints;
	}
	
	protected Point getScreenPoint(Value xVal, NumValue yVal, int index, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			int horizPos = xAxis.catValToPosition(xDataVar.labelIndex(xVal));
			if (index >= 0 && xJitter != null) {
				int jitter = (int)Math.round(xJitter[index] * xAxis.axisLength / xDataVar.noOfCategories());
				horizPos += jitter;
			}
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected Point getFittedPoint(Value[] x, MultipleRegnModel model, int index, Point thePoint) {
		double y = model.evaluateMean(x);
		int vertPos = yAxis.numValToRawPosition(y);
		int horizPos = xAxis.catValToPosition(xDataVar.labelIndex(x[0]));
		if (xJitter != null && index >= 0) {
			int jitter = (int)Math.round(xJitter[index] * xAxis.axisLength / xDataVar.noOfCategories());
			horizPos += jitter;
		}
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void fillXArray(Value[] x, Value xVal, Value zVal, CatVariable zVar) {
		x[0] = xVal;
		x[1] = zVal;
		int xCat = xDataVar.labelIndex(xVal);
		int zCat = zVar.labelIndex(zVal);
		x[2] = xzInteractionDataVar.getLabel(xCat, zCat);
	}
	
/*
	private int getSelectedX() {
		if (selectedHandle < 0)
			return -1;
		
		if (selectedHandle < nx)
			return selectedHandle;
		else if (selectedHandle < nx + nz - 1)
			return 0;
		else
			return (selectedHandle - nx - nz + 1) % (nz - 1) + 1;
	}
*/
	
	private int getSelectedZ() {
		if (selectedHandle < 0)
			return -1;
		
		if (selectedHandle < nx)
			return 0;
		else if (selectedHandle < nx + nz - 1)
			return selectedHandle - nx + 1;
		else
			return (selectedHandle - nx - nz + 1) / (nz - 1) + 1;
	}
	
	protected void drawParallelLines(Graphics g, MultipleRegnModel model) {
		if (!showCoeffs)
			return;
		
		CatVariable zVar = (CatVariable)getVariable(xDataKey[1]);
		
		if (selectedHandle < nx)
			return;
		
//		int noOfLines = zVar.noOfCategories();
		int selectedZIndex = getSelectedZ();
		
		Point lowPoint = null;
		Point highPoint = null;
		Value xValues[] = new Value[xDataKey.length];
		xValues[2] = xzInteractionDataVar.getLabel(0);		//	no interaction
		
		g.setColor(kParallelLineColor);
			for (int j=0 ; j<xDataVar.noOfCategories() ; j++) {
				super.fillXArray(xValues, xDataVar.getLabel(j), zVar.getLabel(selectedZIndex), zVar);
																										//	does not set interaction value
				Point tempPt = lowPoint;
				lowPoint = highPoint;
				highPoint = getFittedPoint(xValues, model, -1, tempPt);
				
				if (j > 0)
					g.drawLine(lowPoint.x, lowPoint.y, highPoint.x, highPoint.y);
			}
	}
	
	protected void drawLines(Graphics g, MultipleRegnModel model) {
		CatVariable zVar = (CatVariable)getVariable(xDataKey[1]);
		int noOfLines = zVar.noOfCategories();
		
		Point lowPoint = null;
		Point highPoint = null;
		Value xValues[] = new Value[xDataKey.length];
		
		for (int i=0 ; i<noOfLines ; i++) {
			int zCat = getSelectedZ();
			if (zCat > 0 && i != zCat && i != 0)
				continue;
			
			g.setColor(kGroupLineColor[i % kGroupLineColor.length]);
			for (int j=0 ; j<xDataVar.noOfCategories() ; j++) {
				fillXArray(xValues, xDataVar.getLabel(j), zVar.getLabel(i), zVar);
				
				Point tempPt = lowPoint;
				lowPoint = highPoint;
				highPoint = getFittedPoint(xValues, model, -1, tempPt);
				
				if (j > 0)
					g.drawLine(lowPoint.x, lowPoint.y, highPoint.x, highPoint.y);
			}
		}
	}
	
	private int interactionParamIndex(int xCat, int zCat, int nx, int nz) {
		return nx + nz - 1 + (xCat - 1) * (nx - 1) + (zCat - 1);
	}
	
	protected void drawGroupParameters(Graphics g, MultipleRegnModel model) {
		int zCat = getSelectedZ();
		CatVariable zVar = (CatVariable)getVariable(xHandleKey[1]);
		if (selectedHandle >= nx + nz - 1) {
//			int xCat = getSelectedX();
				
			Font oldFont = null;
			oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize() + 1));
				
			Value x[] = new Value[xDataKey.length];
			
			for (int i=1 ; i<nx ; i++) {
				fillXArray(x, xDataVar.getLabel(i), zVar.getLabel(zCat), zVar);
				Point line1Point = getFittedPoint(x, model, -1, null);
				
				x[2] = xzInteractionDataVar.getLabel(0);
				Point line0Point = getFittedPoint(x, model, -1, null);
				
				g.setColor(Color.black);
				int paramIndex = interactionParamIndex(i, zCat, nx, nz);
				drawArrow(g, line0Point, line1Point, model.getParameter(paramIndex));
			}
			
			g.setFont(oldFont);
		}
	}
	
	protected void drawBaseParameters(Graphics g, MultipleRegnModel model) {
	}
	
	protected double[] getConstraints() {
		return constraints;
	}
	
	protected void drawHandles(Graphics g, Point[] handlePoints) {
		for (int i=0 ; i<handlePoints.length ; i++)
			if (handlePoints[i] != null)
				if (selectedHandle >= 0 && selectedHandle != i)
					ModelGraphics.drawAnchor(g, handlePoints[i], Color.black);
				else {
					Color mainArrowColor = (i < nx + nz - 1) ? kMainEffectArrowColor : kInteractionArrowColor;
					Color boldArrowColor = Color.black;
					ModelGraphics.drawHandle(g, handlePoints[i], selectedHandle == i,
																												mainArrowColor, boldArrowColor);
				}
	}
}
	
