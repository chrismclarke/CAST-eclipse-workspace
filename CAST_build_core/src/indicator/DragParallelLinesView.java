package indicator;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import random.*;
import coreGraphics.*;


public class DragParallelLinesView extends DataView {
	
	static final private int NO_SELECTED_HANDLE = -1;
	
	static final private int kXHitSlop = 30;
	static final private int kEqnOffset = 20;
	static final private int kArrowHead = 5;
	
	static final protected Color kGroupLineColor[] = {new Color(0xBBBBBB), new Color(0xFFAAAA),
																							new Color(0x99CCFF), new Color(0xC1FFC2)};
	static final protected Color kParallelLineColor = new Color(0xCCCC99);
																	//	to draw parallel lines for subclasses with interaction
	
	protected int selectedHandle = NO_SELECTED_HANDLE;
	private int hitHandle = NO_SELECTED_HANDLE;
	private int hitOffset;
	
	protected String[] xDataKey;
	protected String[] xHandleKey;
	protected String yDataKey, yHandleKey, modelKey;
	private String yName, xName;
	
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
	protected boolean showCoeffs;
	
	private int paramDecimals[];
	
	private boolean drawResiduals = false;
	private boolean canDragHandles = true;
	
	protected double xJitter[] = null;
	
	public DragParallelLinesView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String[] xDataKey, String yDataKey, 
						String[] xHandleKey, String yHandleKey, String modelKey, int[] paramDecimals) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		this.xDataKey = xDataKey;
		this.yDataKey = yDataKey;
		this.xHandleKey = xHandleKey;
		this.yHandleKey = yHandleKey;
		this.modelKey = modelKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.paramDecimals = paramDecimals;
	}
	
	public void setDrawResiduals(boolean drawResiduals) {
		this.drawResiduals = drawResiduals;
		repaint();
	}
	
	public void setYXNames(String yName, String xName) {
		this.yName = yName;			//	short  names to draw eqn for group 0
		this.xName = xName;
	}
	
	public void setCanDragHandles(boolean canDragHandles) {
		this.canDragHandles = canDragHandles;
		repaint();
	}
	
	public void setJitter(double maxJitter, long seed) {
		Variable xVar = (Variable)getVariable(xDataKey[0]);
		int nValues = xVar.noOfValues();
		
		RandomRectangular generator = new RandomRectangular(nValues, -maxJitter, maxJitter);
		generator.setSeed(seed);
		xJitter = generator.generate();
	}
	
	public void setShowCoeffs(boolean showCoeffs) {
		this.showCoeffs = showCoeffs;
	}
	
//------------------------------------------------------
	
	protected Point getScreenPoint(Value xVal, NumValue yVal, int index, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			double x = ((NumValue)xVal).toDouble();
			int horizPos = xAxis.numValToPosition(xJitter == null ? x
																										: x + xJitter[index]);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected Point getFittedPoint(Value[] x, MultipleRegnModel model, int index, Point thePoint) {
		double y = model.evaluateMean(x);
		int vertPos = yAxis.numValToRawPosition(y);
		double x0 = ((NumValue)x[0]).toDouble();
		int horizPos = xAxis.numValToRawPosition(xJitter == null || index < 0 ? x0
																														: x0 + xJitter[index]);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void fillXArray(Value[] x, Value numVal, Value catVal, CatVariable catVar) {
		x[0] = numVal;
		x[1] = catVal;		//	subclass adds interactions
	}
	
	private void drawResiduals(Graphics g, MultipleRegnModel model) {
		Variable xVariable = (Variable)getVariable(xDataKey[0]);		// num or cat
		CatVariable zVariable = (CatVariable)getVariable(xDataKey[1]);		// cat
		NumVariable yVariable = (NumVariable)getVariable(yDataKey);
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ze = zVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point dataPoint = null;
		Point fittedPoint = null;
		Value x[] = new Value[xDataKey.length];			//	can be more than 2 values for subclass with interactions
		int index = 0;
		while (xe.hasMoreValues() && ze.hasMoreValues() && ye.hasMoreValues()) {
			fillXArray(x, xe.nextValue(), ze.nextValue(), zVariable);
			NumValue yVal = (NumValue)ye.nextValue();
			
			int zCat = zVariable.labelIndex(x[1]);
			g.setColor(kGroupLineColor[zCat % kGroupLineColor.length]);
			dataPoint = getScreenPoint(x[0], yVal, index, dataPoint);
			fittedPoint = getFittedPoint(x, model, index, fittedPoint);
			
			g.drawLine(dataPoint.x, dataPoint.y, dataPoint.x, fittedPoint.y);
			index ++;
		}
	}
	
	
	private Point[] getHandles(MultipleRegnModel model) {
		Variable xVariable = (Variable)getVariable(xHandleKey[0]);
		NumVariable yVariable = (NumVariable)getVariable(yHandleKey);
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ye = yVariable.values();
		Point handlePoint[] = new Point[xVariable.noOfValues()];
		int handleIndex = 0;
		while (xe.hasMoreValues() && ye.hasMoreValues()) {
			Value x = xe.nextValue();
			NumValue y = (NumValue)ye.nextValue();
			if (!Double.isNaN(y.toDouble()))
				handlePoint[handleIndex] = getScreenPoint(x, y, -1, null);
			handleIndex ++;
		}
		return handlePoint;
	}
	
	protected void drawParallelLines(Graphics g, MultipleRegnModel model) {
											//	dim parallel lines for subclass with interaction
	}
	
	protected void drawLines(Graphics g, MultipleRegnModel model) {
		CatVariable zVar = (CatVariable)getVariable(xDataKey[1]);
		int noOfLines = zVar.noOfCategories();
		
		double lowX = xAxis.minOnAxis;
		double highX = xAxis.maxOnAxis;
		NumValue lowDrawX = new NumValue(lowX - (highX - lowX) * 0.1);
		NumValue highDrawX = new NumValue(highX + (highX - lowX) * 0.1);
		
		Point lowPoint = null;
		Point highPoint = null;
		Value lowXValues[] = new Value[xDataKey.length];		//	can be more than 2 values for subclass with interactions
		Value highXValues[] = new Value[xDataKey.length];
		
		for (int i=0 ; i<noOfLines ; i++) {
			fillXArray(lowXValues, lowDrawX, zVar.getLabel(i), zVar);
			fillXArray(highXValues, highDrawX, zVar.getLabel(i), zVar);
			
			lowPoint = getFittedPoint(lowXValues, model, -1, lowPoint);
			highPoint = getFittedPoint(highXValues, model, -1, highPoint);
			
			g.setColor(kGroupLineColor[i % kGroupLineColor.length]);
			g.drawLine(lowPoint.x, lowPoint.y, highPoint.x, highPoint.y);
		}
	}
	
	
	private void drawData(Graphics g) {
		g.setColor(getForeground());
		Variable xVariable = (Variable)getVariable(xDataKey[0]);
		CatVariable zVariable = (CatVariable)getVariable(xDataKey[1]);
		NumVariable yVariable = (NumVariable)getVariable(yDataKey);
		Point thePoint = null;
		
		ValueEnumeration xe = xVariable.values();
		ValueEnumeration ze = zVariable.values();
		ValueEnumeration ye = yVariable.values();
		int index = 0;
		while (xe.hasMoreValues() && ze.hasMoreValues() && ye.hasMoreValues()) {
			Value xVal = xe.nextValue();
			NumValue yVal = (NumValue)ye.nextValue();
			Value zVal = ze.nextValue();
			int zCat = zVariable.labelIndex(zVal);
			thePoint = getScreenPoint(xVal, yVal, index, thePoint);
			if (thePoint != null)
				drawMark(g, thePoint, zCat);
			index ++;
		}
	}
	
	
	private void drawParameters(Graphics g, MultipleRegnModel model) {
		if (xName == null || yName == null || !showCoeffs)
			return;
		
		drawBaseParameters(g, model);
		drawGroupParameters(g, model);
		
		g.setColor(getForeground());
	}
	
	protected void drawGroupParameters(Graphics g, MultipleRegnModel model) {
		double xOffset = (xAxis.maxOnAxis - xAxis.minOnAxis) * 0.04;
		for (int i=2 ; i<model.noOfParameters() ; i++)
			drawGroupOffsetParameter(g, model, i, xOffset);
	}
	
	protected void drawBaseParameters(Graphics g, MultipleRegnModel model) {
		Font oldFont = null;
		if (selectedHandle == 0 || selectedHandle == 1) {
			oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize() + 1));
		}
		
		Point handles[] = getHandles(model);
		Point mid0Point = new Point((handles[0].x + handles[1].x) / 2, (handles[0].y + handles[1].y) / 2);
		
		double sumDiff = 0.0;
		for (int i=2 ; i<model.noOfParameters() ; i++)
			sumDiff += model.getParameter(i).toDouble();
		boolean drawAbove = sumDiff <= 0;
		
		FontMetrics fm  = g.getFontMetrics();
		int baseline = drawAbove ? (mid0Point.y - fm.getDescent() - kEqnOffset)
																					: (mid0Point.y + fm.getAscent() + kEqnOffset);
		
		String eqnString = yName + " = " + model.getParameter(0).toString();
		eqnString += " + (" + model.getParameter(1).toString() + ") " + xName;
		int eqnWidth = fm.stringWidth(eqnString);
		
		boolean drawLeft = (model.getParameter(1).toDouble() > 0) == drawAbove;
		int eqnHoriz = mid0Point.x;
		if (drawLeft) {
			eqnHoriz -= eqnWidth;
			eqnHoriz = Math.max(eqnHoriz, 2);
		}
		else
			eqnHoriz = Math.min(eqnHoriz, getSize().width - eqnWidth - 2);
		
		g.setColor(selectedHandle == 0 || selectedHandle == 1 ? Color.black : kGroupLineColor[0]);
		g.drawString(eqnString, eqnHoriz, baseline);
		
		if (oldFont != null)
			g.setFont(oldFont);
	}
	
	protected void drawArrow(Graphics g, Point startPt, Point endPt, Value paramValue) {
		g.drawLine(startPt.x, startPt.y, endPt.x, endPt.y);
		
		int arrowHeadY = endPt.y > startPt.y ? -kArrowHead : kArrowHead;
		g.drawLine(endPt.x, endPt.y, endPt.x - kArrowHead, endPt.y + arrowHeadY);
		g.drawLine(endPt.x, endPt.y, endPt.x + kArrowHead, endPt.y + arrowHeadY);
		
		FontMetrics fm = g.getFontMetrics();
		int middle = (startPt.y + endPt.y) / 2;
		int baseline = middle + (fm.getAscent() - fm.getDescent()) / 2;
		baseline = Math.min(baseline, getSize().height - 2);
		
		int paramWidth = paramValue.stringWidth(g);
		int gap = (Math.abs(middle - endPt.y) < ModelGraphics.kHandleLength / 2) ? 9 : 4;
		if (startPt.x + paramWidth + gap > getSize().width)
			paramValue.drawLeft(g, startPt.x - gap, baseline);
		else
			paramValue.drawRight(g, startPt.x + gap, baseline);
	}
	
	protected Color getParamColor(int paramIndex, int groupIndex) {
		return selectedHandle == paramIndex ? getCrossColor(groupIndex)
															: kGroupLineColor[groupIndex % kGroupLineColor.length];
	}
	
	protected void drawGroupOffsetParameter(Graphics g, MultipleRegnModel model, int paramIndex,
																																			double xOffset) {
		Font oldFont = null;
		if (selectedHandle == paramIndex) {
			oldFont = g.getFont();
			g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize() + 1));
		}
		
		NumVariable xVariable = (NumVariable)getVariable(xHandleKey[0]);
		CatVariable zVariable = (CatVariable)getVariable(xHandleKey[1]);
		
		NumValue xVal = new NumValue((NumValue)xVariable.valueAt(paramIndex));
		xVal.setValue(xVal.toDouble() + xOffset);
		
		Value x[] = new Value[xDataKey.length];		//	can be more than 2 values for subclass with interactions
		
		fillXArray(x, xVal, zVariable.getLabel(0), zVariable);
		Point line0Point = getFittedPoint(x, model, -1, null);
		
		fillXArray(x, xVal, zVariable.getLabel(paramIndex - 1), zVariable);
		Point line1Point = getFittedPoint(x, model, -1, null);
		
		g.setColor(getParamColor(paramIndex, paramIndex - 1));
		drawArrow(g, line0Point, line1Point, model.getParameter(paramIndex));
		
		if (oldFont != null)
			g.setFont(oldFont);
	}
	
	protected void drawHandles(Graphics g, Point[] handlePoints) {
		for (int i=0 ; i<handlePoints.length ; i++)
			if (handlePoints[i] != null)
				if (selectedHandle >= 0 && selectedHandle != i)
					ModelGraphics.drawAnchor(g, handlePoints[i], Color.black);
				else
					ModelGraphics.drawHandle(g, handlePoints[i], selectedHandle == i);
	}
	
	public void paintView(Graphics g) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		
		if (drawResiduals && xDataKey != null && yDataKey != null)
			drawResiduals(g, model);
		
		drawParameters(g, model);
		
		if (canDragHandles) {
			Point[] handlePoints = getHandles(model);
			drawHandles(g, handlePoints);
			drawParallelLines(g, model);
		}
		
		drawLines(g, model);
		
		if (xDataKey != null && yDataKey != null)
			drawData(g);
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canDragHandles;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kXHitSlop || y < 0 || x >= getSize().width + kXHitSlop || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= yAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + yAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		Point[] handles = getHandles(model);
		Point hitPos = translateFromScreen(x, y, null);
		for (int i=0 ; i<handles.length ; i++)
			if (handles[i] != null && Math.abs(handles[i].x - x) + Math.abs(handles[i].y - y) < kMinHitDistance)
				return new VertDragPosInfo(hitPos.y, i, y - handles[i].y);
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
//		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		hitOffset = ((VertDragPosInfo)startPos).hitOffset;
		hitHandle = ((VertDragPosInfo)startPos).index;
		selectedHandle = hitHandle;
		repaint();
		return true;
	}
	
	protected double[] getConstraints() {
		return null;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedHandle = NO_SELECTED_HANDLE;
			repaint();
		}
		else {
			selectedHandle = hitHandle;
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newHandleValue = yAxis.positionToNumVal(newYPos);
				MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
				model.setXKey(xHandleKey);
				NumVariable yHandleVar = (NumVariable)getVariable(yHandleKey);
				((NumValue)yHandleVar.valueAt(hitHandle)).setValue(newHandleValue);
				model.setLSParams(yHandleKey, getConstraints(), paramDecimals, 0);
				getData().variableChanged(modelKey);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedHandle = NO_SELECTED_HANDLE;
		repaint();
	}
}
	
