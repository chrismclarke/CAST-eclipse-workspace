package indicator;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class DragLinesView extends DragParallelLinesView {
//	static public final String DRAG_LINES = "dragLines";
	
	static final private NumValue kZero = new NumValue(0.0, 0);
	
	private NumValue horizSlopeValue;
	private double constraints[] = null;
	
	public DragLinesView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String[] xDataKey, String yDataKey, 
						String[] xHandleKey, String yHandleKey, String modelKey, int[] paramDecimals,
						NumValue horizSlopeValue) {
		super(theData, applet, xAxis, yAxis, xDataKey, yDataKey, xHandleKey, yHandleKey, modelKey, paramDecimals);
		this.horizSlopeValue = horizSlopeValue;
	}
	
	public void setConstraints(double[] constraints) {
		this.constraints = constraints;
	}
	
	protected void fillXArray(Value[] x, Value numVal, Value catVal, CatVariable catVar) {
		x[0] = numVal;
		x[1] = catVal;
		int catIndex = catVar.labelIndex(catVal);
		for (int i=2 ; i<x.length ; i++)
			x[i] = (i == catIndex + 1) ? numVal : kZero;
	}
	
	protected void drawParallelLines(Graphics g, MultipleRegnModel model) {
		if (!showCoeffs)
			return;
		
		CatVariable zVar = (CatVariable)getVariable(xDataKey[1]);
		int noOfLines = zVar.noOfCategories();
		
		double lowX = xAxis.minOnAxis;
		double highX = xAxis.maxOnAxis;
		NumValue lowDrawX = new NumValue(lowX - (highX - lowX) * 0.1);
		NumValue highDrawX = new NumValue(highX + (highX - lowX) * 0.1);
		
		Point lowPoint = null;
		Point highPoint = null;
		Value lowXValues[] = new Value[xDataKey.length];
		Value highXValues[] = new Value[xDataKey.length];
		for (int i=2 ; i<xDataKey.length ; i++)
			lowXValues[i] = highXValues[i] = kZero;
		
		g.setColor(kParallelLineColor);
		for (int i=1 ; i<noOfLines ; i++) {
			super.fillXArray(lowXValues, lowDrawX, zVar.getLabel(i), zVar);
			super.fillXArray(highXValues, highDrawX, zVar.getLabel(i), zVar);
			
			lowPoint = getFittedPoint(lowXValues, model, -1, lowPoint);
			highPoint = getFittedPoint(highXValues, model, -1, highPoint);
			
			g.drawLine(lowPoint.x, lowPoint.y, highPoint.x, highPoint.y);
		}
	}
	
	protected void drawGroupParameters(Graphics g, MultipleRegnModel model) {
		CatVariable catVariable = (CatVariable)getVariable(xHandleKey[1]);
		NumVariable yVariable = (NumVariable)getVariable(yHandleKey);
		int nCats = catVariable.noOfCategories();
		for (int i=1 ; i<nCats ; i++) {
			drawGroupOffsetParameter(g, model, i + 1, 0.0);
			if (!Double.isNaN(yVariable.doubleValueAt(nCats + i)))
				drawGroupSlopeParameter(g, model, i, nCats + i);
		}
	}
	
	protected void drawGroupSlopeParameter(Graphics g, MultipleRegnModel model, int groupIndex,
																																				int paramIndex) {
		Font oldFont = null;
		if (selectedHandle == paramIndex) {
			oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize() + 1));
		}
		
		CatVariable zVariable = (CatVariable)getVariable(xHandleKey[1]);
		
		Value x[] = new Value[xDataKey.length];		//	can be more than 2 values for subclass with interactions
		for (int i=0 ; i<x.length ; i++)
			x[i] = kZero;
		
		super.fillXArray(x, horizSlopeValue, zVariable.getLabel(groupIndex), zVariable);
		Point line0Point = getFittedPoint(x, model, -1, null);
		
		fillXArray(x, horizSlopeValue, zVariable.getLabel(groupIndex), zVariable);
		Point line1Point = getFittedPoint(x, model, -1, null);
		
		g.setColor(getParamColor(paramIndex, groupIndex));
		Value label = model.getParameter(paramIndex);
		if (label instanceof NumValue && ((NumValue)label).toDouble() == 0)
			label = kZero;
		else if (horizSlopeValue.toDouble() != 1.0)
			label = new LabelValue(horizSlopeValue.toString() + " * "
																										+ label.toString());
		drawArrow(g, line0Point, line1Point, label);
		
		if (oldFont != null)
			g.setFont(oldFont);
	}
	
	protected double[] getConstraints() {
		return constraints;
	}
}
	
