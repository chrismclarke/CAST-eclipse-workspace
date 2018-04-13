package indicator;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class SimpleParallelLinesView extends DataView {
	
//	static final private int kXHitSlop = 30;
	static final private int kArrowHead = 5;
	
	static final protected Color kGroupLineColor[] = {new Color(0xBBBBBB), new Color(0xFFAAAA),
																							new Color(0x99CCFF), new Color(0xC1FFC2)};
	static final private Color kPropnColor = new Color(0xF2F2F8);
	
	protected String[] xKeys;
	protected String yDataKey, modelKey;
	
	protected HorizAxis xAxis;
	protected VertAxis yAxis;
	
//	private int paramDecimals[];
	
	public SimpleParallelLinesView(DataSet theData, XApplet applet,
						HorizAxis xAxis, VertAxis yAxis, String[] xKeys, String yDataKey, 
						String modelKey, int[] paramDecimals) {
		super(theData, applet, new Insets(0, 10, 0, 10));
		this.xKeys = xKeys;
		this.yDataKey = yDataKey;
		this.modelKey = modelKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
//		this.paramDecimals = paramDecimals;
	}
	
	public void setDataAndModel(String yDataKey, String[] xKeys, String modelKey) {
		this.yDataKey = yDataKey;
		this.xKeys = xKeys;
		this.modelKey = modelKey;
	}
	
//------------------------------------------------------
	
	private Point getScreenPoint(Value xVal, NumValue yVal, int index, Point thePoint) {
		try {
			int vertPos = yAxis.numValToPosition(yVal.toDouble());
			double x = ((NumValue)xVal).toDouble();
			int horizPos = xAxis.numValToPosition(x);
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	private Point getFittedPoint(Value[] x, MultipleRegnModel model, int index, Point thePoint) {
		double y = model.evaluateMean(x);
		int vertPos = yAxis.numValToRawPosition(y);
		double x0 = ((NumValue)x[0]).toDouble();
		int horizPos = xAxis.numValToRawPosition(x0);
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	private void fillXArray(Value[] x, Value numVal, Value catVal, CatVariable catVar) {
		x[0] = numVal;
		x[1] = catVal;		//	subclass adds interactions
	}
	
	private void drawLines(Graphics g, MultipleRegnModel model) {
		CatVariable zVar = (CatVariable)getVariable(xKeys[1]);
		int noOfLines = zVar.noOfCategories();
		
		double lowX = xAxis.minOnAxis;
		double highX = xAxis.maxOnAxis;
		NumValue lowDrawX = new NumValue(lowX - (highX - lowX) * 0.1);
		NumValue highDrawX = new NumValue(highX + (highX - lowX) * 0.1);
		
		Point lowPoint = null;
		Point highPoint = null;
		Value lowXValues[] = new Value[xKeys.length];		//	can be more than 2 values for subclass with interactions
		Value highXValues[] = new Value[xKeys.length];
		
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
		Variable xVariable = (Variable)getVariable(xKeys[0]);
		CatVariable zVariable = (CatVariable)getVariable(xKeys[1]);
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
	
	private void drawGroupOffsets(Graphics g, MultipleRegnModel model) {
		double xOffset = (xAxis.maxOnAxis - xAxis.minOnAxis) * 0.04;
		for (int i=2 ; i<model.noOfParameters() ; i++)
			drawGroupOffsetParameter(g, model, i, xAxis.maxOnAxis - i * xOffset);
	}
	
	private void drawArrow(Graphics g, Point startPt, Point endPt, Value paramValue) {
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
	
	private void drawGroupOffsetParameter(Graphics g, MultipleRegnModel model, int paramIndex,
																																			double xDrawValue) {
		CatVariable zVariable = (CatVariable)getVariable(xKeys[1]);
		
		NumValue xVal = new NumValue(xDrawValue);
		
		Value x[] = new Value[xKeys.length];
		
		fillXArray(x, xVal, zVariable.getLabel(0), zVariable);
		Point line0Point = getFittedPoint(x, model, -1, null);
		
		fillXArray(x, xVal, zVariable.getLabel(paramIndex - 1), zVariable);
		Point line1Point = getFittedPoint(x, model, -1, null);
		
		g.setColor(getCrossColor(paramIndex - 1));
		drawArrow(g, line0Point, line1Point, model.getParameter(paramIndex));
	}
	
	private void drawBackgroundPropns(Graphics g) {
		double cutoff = (xAxis.maxOnAxis + xAxis.minOnAxis) / 2;
		int n[][] = new int[2][2];
		
		NumVariable covar = (NumVariable)getVariable(xKeys[0]);
		CatVariable treat = (CatVariable)getVariable(xKeys[1]);
		for (int i=0 ; i<covar.noOfValues() ; i++) {
			int treatIndex = treat.getItemCategory(i);
			int covarIndex = (covar.doubleValueAt(i) < cutoff) ? 0 : 1;
			n[covarIndex][treatIndex] ++;
		}
		
		Font oldFont = g.getFont();
		Font f = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize());
		g.setFont(f);
		while (g.getFontMetrics().getAscent() < getSize().height / 4)
			g.setFont(f = new Font(f.getName(), f.getStyle(), f.getSize() * 4 / 3));
		
		g.setColor(kPropnColor);
		int topBaseline = getSize().height / 2 - 7;
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int bottomBaseline = topBaseline + 14 + ascent;
		int stagger = ascent / 8;
		for (int i=0 ; i<2 ; i++) {
			int center = (i == 0) ? (getSize().width / 4) : (getSize().width * 3 / 4);
			
			int total = n[i][0] + n[i][1];
			String totalString = String.valueOf(total);
			String treatString = String.valueOf(n[i][0]);
			int totalWidth = fm.stringWidth(totalString);
			int treatWidth = fm.stringWidth(treatString);
			int lineLeft = center - totalWidth / 2;
			int lineRight = lineLeft + totalWidth;
			for (int j=0 ; j<6 ; j++)
				g.drawLine(lineLeft, getSize().height / 2 + stagger + j, lineRight, getSize().height / 2 + j);
//			g.fillRect(center - totalWidth / 2, getSize().height / 2 - 3, totalWidth, 6);
			g.drawString(treatString, center - treatWidth / 2, topBaseline);
			g.drawString(totalString, center - totalWidth / 2, bottomBaseline);
		}
		
		g.setFont(oldFont);
	}
	
	public void paintView(Graphics g) {
		if (xKeys != null && yDataKey != null)
			drawBackgroundPropns(g);
		
		MultipleRegnModel model = (MultipleRegnModel)getVariable(modelKey);
		drawGroupOffsets(g, model);
		drawLines(g, model);
		
		if (xKeys != null && yDataKey != null)
			drawData(g);
	}

//-----------------------------------------------------------------------------------

//	static final private int kMinHitDistance = 10;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
