package matrix;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class DragOneFactorView extends MarginalDataView {
	
	static final private int kMeanExtra = 10;
	static final private int kHitSlop = 7;
	
	static final private Color kMeanColor = Color.red;
	static final private Color kResidualColor = new Color(0xFFCED3);		//	pink
	static final private Color kRegnLineColor = new Color(0xCCCCCC);
	
	protected String xKey, yKey;
	protected String lsKey;
	protected NumCatAxis xAxis, yAxis;
	private ModelTerm[] xTerms;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	public DragOneFactorView(DataSet theData, XApplet applet,
							NumCatAxis xAxis, NumCatAxis yAxis, String xKey, String yKey,
							String lsKey, ModelTerm[] xTerms) {
		super(theData, applet, new Insets(5, 5, 5, 5), xAxis);
		this.xKey = xKey;
		this.yKey = yKey;
		this.lsKey = lsKey;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.xTerms = xTerms;
	}
	
	protected Point getScreenPoint(double y, int xPos, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		
		int yPos = yAxis.numValToRawPosition(y);
		return translateToScreen(xPos, yPos, thePoint);
	}
	
	private void initialiseJittering(CatVariable xVar) {
		int noOfVals = xVar.noOfValues();
		if (jitter == null || jitter.length != noOfVals)
			jitter = new int[noOfVals];
		int noOfCats = xVar.noOfCategories();
		if (noInXCat == null || noInXCat.length != noOfCats)
			noInXCat = new int[noOfCats];
		else
			for (int i=0 ; i<noOfCats ; i++)
				noInXCat[i] = 0;
		
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xVar.getItemCategory(i);
			jitter[i] = noInXCat[xCat];
			noInXCat[xCat] ++;
		}
		int usedHoriz = noOfVals * 2 + noOfCats * getCrossSize() * 2;
		int availableHoriz = (getSize().width * 3) / 5;
		scalePercent = Math.min(100, (availableHoriz * 100) / usedHoriz);
		
		jitteringInitialised = true;
	}
	
	protected int getXPos(Value x, CatVariable xVar, int index) {
		int xCat = xVar.labelIndex(x);
		int offset = ((2 * jitter[index] - noInXCat[xCat]) * scalePercent) / 100 + 1;
		int xCatPos = (xAxis ==  null) ? getSize().width / 2 : xAxis.catValToPosition(xCat);
		return xCatPos + offset;
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable(xKey);
		
		if (!jitteringInitialised)
			initialiseJittering(xVar);
		
		Point crossPoint = null;
		Point fitPoint = null;
		
		MultipleRegnModel lsFit = (MultipleRegnModel)getVariable(lsKey);
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		int index = 0;
		g.setColor(kResidualColor);
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			crossPoint = getScreenPoint(y, xPos, crossPoint);
			
			if (crossPoint != null) {
				double fit = lsFit.evaluateMean(x);
				int fitPos = yAxis.numValToRawPosition(fit);
				fitPoint  = translateToScreen(xPos, fitPos, fitPoint);
				if (fitPoint != null)
					g.drawLine(fitPoint.x, fitPoint.y, crossPoint.x, crossPoint.y);
			}
			index ++;
		}
		
		drawParameters(g, lsFit, xVar, xAxis, yAxis);
		drawFactorMeans(g, lsFit, xVar, xAxis, yAxis);
		
		ye = yVar.values();
		xe = xVar.values();
		index = 0;
		g.setColor(getForeground());
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			Value x = xe.nextValue();
			int xPos = getXPos(x, xVar, index);
			crossPoint = getScreenPoint(y, xPos, crossPoint);
			
			if (crossPoint != null)
				drawCross(g, crossPoint);
			index ++;
		}
	}
	
	private void drawFactorMeans(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																													NumCatAxis xAxis, NumCatAxis yAxis) {
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			Value xValue = xVar.getLabel(i);
			double yMean = ls.evaluateMean(xValue);
			
			int xCenter = xAxis.catValToPosition(i);
			int maxJitter = noInXCat[i] * scalePercent / 100;
			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			p0 = getScreenPoint(yMean, lowMeanXPos, p0);
			p1 = getScreenPoint(yMean, highMeanXPos, p1);
			
			if (doingDrag && dragCat == i) {
				g.setColor(Color.yellow);
				g.fillRect(p0.x, p0.y - 2, p1.x - p0.x + 1, 5);
			}
			
			g.setColor(kMeanColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			p1.x += 8;
			if (!doingDrag)
				ModelGraphics.drawHandle(g, p1, false);
			else if (dragCat == i)
				ModelGraphics.drawHandle(g, p1, true);
		}
	}
	
	private void drawParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																												NumCatAxis xAxis, NumCatAxis yAxis) {
		if (xTerms[0] instanceof FactorTerm)
			drawSeparateParameters(g, ls, xVar, xAxis, yAxis);
		else if (xTerms[1] instanceof FactorTerm)
			drawBaselineParameters(g, ls, xVar, xAxis, yAxis);
		else {
			ContrastTerm contrast = (ContrastTerm)xTerms[1];
			if (contrast.isSingleIndicator())
				drawTwoGroupParameters(g, ls, xVar, xAxis, yAxis);
			else
				drawLinearParameters(g, ls, xVar, xAxis, yAxis);
		}
	}
	
	private int getLowMeanPos(NumCatAxis xAxis, int i) {
		int xCenter = xAxis.catValToPosition(i);
		int maxJitter = noInXCat[i] * scalePercent / 100;
		return xCenter - maxJitter - kMeanExtra;
	}
	
	private void drawLinearParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																											NumCatAxis xAxis, NumCatAxis yAxis) {
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		
		double mean0 = ls.getParameter(0).toDouble();
//		int pos0 = xAxis.catValToPosition(0);
		
		double slope = ls.getParameter(1).toDouble();
//		int pos1 = xAxis.catValToPosition(1);
		
		int nx = xVar.noOfCategories();
		Point p0 = getScreenPoint(mean0 - slope, xAxis.catValToPosition(-1), null);
																								//	can be less than 0
		Point p1 = getScreenPoint(mean0 + nx * slope, xAxis.catValToPosition(nx), null);
																								//	can be more than nx - 1
		g.setColor(kRegnLineColor);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
		
		p0 = getScreenPoint(mean0, xAxis.catValToPosition(0), p0);
		int maxJitter = noInXCat[1] * scalePercent / 100;
		p1 = getScreenPoint(mean0 + slope, xAxis.catValToPosition(1) + maxJitter + 2, p1);
		
		if (!doingDrag || dragCat == 0) {
			g.setColor(xTerms[0].getTermColor());
			int paramWidth = xTerms[0].maxParamWidth(g);
			int leftX = (p0.x - paramWidth - 2) / 2;
			g.drawLine(leftX, p0.y, p0.x, p0.y);
			g.drawLine(leftX, p0.y, leftX + 4, p0.y - 4);
			g.drawLine(leftX, p0.y, leftX + 4, p0.y + 4);
			xTerms[0].drawParameter(0, leftX - paramWidth - 2, p0.y + baselineOffset, g);
		}
		
		if (!doingDrag || dragCat == 1) {
			g.setColor(kRegnLineColor);
			g.drawLine(p0.x, p0.y, p1.x, p0.y);
			
			g.setColor(xTerms[1].getTermColor());
			if (p1.y > p0.y) {
				g.drawLine(p1.x, p0.y, p1.x, p1.y);
				g.drawLine(p1.x, p1.y, p1.x + 4, p1.y - 4);
				g.drawLine(p1.x, p1.y, p1.x - 4, p1.y - 4);
			}
			else {
				g.drawLine(p1.x, p0.y, p1.x, p1.y);
				g.drawLine(p1.x, p1.y, p1.x + 4, p1.y + 4);
				g.drawLine(p1.x, p1.y, p1.x - 4, p1.y + 4);
			}
			xTerms[1].drawParameter(0, p1.x + 2, (p0.y + p1.y) / 2 + baselineOffset, g);
		}
		
		for (int i=2 ; i<nx ; i++)
			if (!doingDrag || dragCat == i) {
				Point linePoint = getScreenPoint(mean0 + i * slope, xAxis.catValToPosition(i), null);
				g.setColor(kRegnLineColor);
				int lowXPos = getScreenPoint(0.0, getLowMeanPos(xAxis, i), p0).x - 6;
				g.drawLine(lowXPos, linePoint.y, linePoint.x, linePoint.y);
				drawOneLevelOffset(g, ls, xVar, xAxis, yAxis, linePoint, (i - 2),
																											xTerms[2], i, baselineOffset);
			}
	}
	
	private void drawSeparateParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																												NumCatAxis xAxis, NumCatAxis yAxis) {
		Point p0 = null;
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		g.setColor(xTerms[0].getTermColor());
		
		for (int i=0 ; i<xVar.noOfCategories() ; i++) {
			Value xValue = xVar.getLabel(i);
			double yMean = ls.evaluateMean(xValue);
			
			int lowMeanXPos = getLowMeanPos(xAxis, i);
			
			p0 = getScreenPoint(yMean, lowMeanXPos, p0);
			
			if (!doingDrag || dragCat == i) {
				int baseline = p0.y + baselineOffset;
				int horiz = p0.x - xTerms[0].maxParamWidth(g) - 2;
				xTerms[0].drawParameter(i, horiz, baseline, g);
			}
		}
	}
	
	private void drawBaselineParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																											NumCatAxis xAxis, NumCatAxis yAxis) {
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		
		int baselineIndex = ((FactorTerm)xTerms[1]).getBaselineLevel();
		
		Point baselineLeft = drawBaseline(g, baselineIndex, ls, xVar, xAxis,
																																yAxis, baselineOffset);
		
		int drawParamIndex = 0;
		for (int i=0 ; i<xVar.noOfCategories() ; i++)
			if (i != baselineIndex) {
				drawOneLevelOffset(g, ls, xVar, xAxis, yAxis, baselineLeft, drawParamIndex,
																										xTerms[1], i, baselineOffset);
				drawParamIndex ++;
			}
	}
	
	private void drawTwoGroupParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																												NumCatAxis xAxis, NumCatAxis yAxis) {
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		
		Point baselineLeft = drawBaseline(g, 0, ls, xVar, xAxis, yAxis, baselineOffset);
		
		ContrastTerm contrast = (ContrastTerm)xTerms[1];
		int contrastValues[] = contrast.getContrastValues();
		int groupStartIndex = 0;
		for (int i=0 ; i<contrastValues.length ; i++)
			if (contrastValues[i] == 1) {
				groupStartIndex = i;
				break;
			}
				
		Point groupTwoLeft = drawContrastBaseline(g, baselineLeft, ls, xVar, xAxis,
																								yAxis, baselineOffset, groupStartIndex);
		
		int drawParamIndex = 0;
		for (int i=0 ; i<xVar.noOfCategories() ; i++)
			if (i != 0 && i != groupStartIndex) {
				Point p = (i < groupStartIndex) ? baselineLeft : groupTwoLeft;
				drawOneLevelOffset(g, ls, xVar, xAxis, yAxis, p, drawParamIndex,
																										xTerms[2], i, baselineOffset);
				drawParamIndex ++;
			}
	}
	
	private Point drawBaseline(Graphics g, int baselineIndex, MultipleRegnModel ls,
									CatVariable xVar, NumCatAxis xAxis, NumCatAxis yAxis, int baselineOffset) {
		Value baselineValue = xVar.getLabel(baselineIndex);
		double baselineMean = ls.evaluateMean(baselineValue);
		int leftBaselinePos = getLowMeanPos(xAxis, 0) / 2;
		Point baselineLeft = getScreenPoint(baselineMean, leftBaselinePos, null);
		
		g.setColor(xTerms[0].getTermColor());
		g.drawLine(baselineLeft.x, baselineLeft.y, getSize().width - baselineLeft.x, baselineLeft.y);
		if (!doingDrag || dragCat == baselineIndex)
			xTerms[0].drawParameter(0, baselineLeft.x - xTerms[0].maxParamWidth(g) - 2,
																											baselineLeft.y + baselineOffset, g);
		return baselineLeft;
	}
	
	private Point drawContrastBaseline(Graphics g, Point baselineLeft, MultipleRegnModel ls,
									CatVariable xVar, NumCatAxis xAxis, NumCatAxis yAxis, int baselineOffset,
									int groupStartIndex) {
		g.setColor(xTerms[1].getTermColor());
		Value baselineValue = xVar.getLabel(groupStartIndex);
		double baselineMean = ls.evaluateMean(baselineValue);
		int lineExtra = getLowMeanPos(xAxis, 0) / 2;
		int groupLeftBaselinePos = getLowMeanPos(xAxis, groupStartIndex) - lineExtra;
		Point p0 = getScreenPoint(baselineMean, groupLeftBaselinePos, null);
		
		g.setColor(xTerms[1].getTermColor());
		g.drawLine(p0.x, p0.y, getSize().width - lineExtra, p0.y);
		
		if (!doingDrag || dragCat == groupStartIndex) {
			if (p0.y < baselineLeft.y) {
				g.drawLine(p0.x, p0.y, p0.x, baselineLeft.y - 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y + 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y + 4);
			}
			else {
				g.drawLine(p0.x, p0.y, p0.x, baselineLeft.y + 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y - 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y - 4);
			}
			
			int baseline = (p0.y + baselineLeft.y) / 2 + baselineOffset;
			int horiz = p0.x - xTerms[1].maxParamWidth(g) - 2;
			xTerms[1].drawParameter(0, horiz, baseline, g);
		}
		return p0;
	}
	
	private void drawOneLevelOffset(Graphics g, MultipleRegnModel ls, CatVariable xVar,
											NumCatAxis xAxis, NumCatAxis yAxis, Point baselineLeft, int paramindex,
											ModelTerm term, int catIndex, int baselineOffset) {
		g.setColor(term.getTermColor());
		Value xValue = xVar.getLabel(catIndex);
		double yMean = ls.evaluateMean(xValue);
		
		int lowMeanXPos = getLowMeanPos(xAxis, catIndex);
		
		Point p0 = getScreenPoint(yMean, lowMeanXPos - 6, null);
		
		if (!doingDrag || dragCat == catIndex) {
			if (p0.y < baselineLeft.y) {
				g.drawLine(p0.x, p0.y, p0.x, baselineLeft.y - 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y + 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y + 4);
			}
			else {
				g.drawLine(p0.x, p0.y, p0.x, baselineLeft.y + 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y - 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y - 4);
			}
			
			int baseline = (p0.y + baselineLeft.y) / 2 + baselineOffset;
			int horiz = p0.x - term.maxParamWidth(g) - 2;
			term.drawParameter(paramindex, horiz, baseline, g);
		}
	}

//-------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		jitteringInitialised = false;
		repaint();
	}
	
	public int minDisplayWidth() {
		return 150;
	}

//-----------------------------------------------------------------------------------
	
	private boolean doingDrag = false;
	private int hitOffset, dragCat;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		int cat = xAxis.positionToCatVal(p.x);
		
		CatVariable xVar = (CatVariable)getVariable(xKey);
		Value xValue = xVar.getLabel(cat);
		MultipleRegnModel lsFit = (MultipleRegnModel)getVariable(lsKey);
		double yMean = lsFit.evaluateMean(xValue);
			
		int yMeanPos = yAxis.numValToRawPosition(yMean);
		
		if (Math.abs(yMeanPos - p.y) > kHitSlop)
			return null;
		
		return new VertDragPosInfo(x, cat, p.y - yMeanPos);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doingDrag = true;
		VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
		hitOffset = posInfo.hitOffset;
		dragCat = posInfo.index;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			doingDrag = false;
		else {
			doingDrag = true;
			VertDragPosInfo posInfo = (VertDragPosInfo)toPos;
			Point p = translateFromScreen(0, posInfo.y, null);
			
			try {
				double newMean = yAxis.positionToNumVal(p.y - hitOffset);
				MultipleRegnModel lsFit = (MultipleRegnModel)getVariable(lsKey);
				if (dragCat == 0) {
					double mean0Change = newMean - lsFit.getParameter(0).toDouble();
					lsFit.setParameter(0, newMean);
					for (int i=1 ; i<lsFit.noOfParameters() ; i++)
						lsFit.setParameter(i, lsFit.getParameter(i).toDouble() - mean0Change);
				}
				else
					lsFit.setParameter(dragCat, newMean - lsFit.getParameter(0).toDouble());
				
				getData().variableChanged(lsKey);
			} catch (AxisException e) {
			}
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}
	
