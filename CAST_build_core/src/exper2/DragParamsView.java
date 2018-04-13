package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;
import formula.*;


public class DragParamsView extends CoreOneFactorView {
	
	static final private int kHitSlop = 7;
	
	static final private Color kBaselineColor = new Color(0x0066FF);
	static final private Color kLevelOffsetColor = new Color(0xFF6600);
	
	private String baselineParam, offsetParam;
	
	private int baselineIndex = -1;		//	no baseline
	
	public DragParamsView(DataSet theData, XApplet applet, NumCatAxis xAxis,
											NumCatAxis yAxis, String xKey, String yKey, String modelKey,
											String baselineParam, String offsetParam) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.baselineParam = baselineParam;
		this.offsetParam = offsetParam;
	}
	
	public void setBaselineIndex(int baselineIndex) {
		this.baselineIndex = baselineIndex;
		repaint();
	}
	
	protected void drawOneMean(int levelIndex, Point p0, Point p1, Graphics g) {
		if (doingDrag && dragCat == levelIndex) {
			g.setColor(Color.yellow);
			g.fillRect(p0.x, p0.y - 2, p1.x - p0.x + 1, 5);
		}
		super.drawOneMean(levelIndex, p0, p1, g);
		
		p1.x += 8;
		if (!doingDrag)
			ModelGraphics.drawHandle(g, p1, false);
		else if (dragCat == levelIndex)
			ModelGraphics.drawHandle(g, p1, true);
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
		drawParameters(g, (MultipleRegnModel)model, xVar, xAxis, yAxis);
		super.drawFactorMeans(g, model, xVar);
	}
	
	private void drawParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																												NumCatAxis xAxis, NumCatAxis yAxis) {
		if (baselineIndex < 0)
			drawSeparateParameters(g, ls, xVar, xAxis, yAxis);
		else
			drawBaselineParameters(g, ls, xVar, xAxis, yAxis);
	}
	
	private int getLowMeanPos(NumCatAxis xAxis, int i) {
		int xCenter = xAxis.catValToPosition(i);
		int maxJitter = noInXCat[i] * scalePercent / 100;
		return xCenter - maxJitter - kMeanExtra;
	}
	
	private void drawSeparateParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																												NumCatAxis xAxis, NumCatAxis yAxis) {
		Point p0 = null;
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		g.setColor(kLevelOffsetColor);
		
		for (int i=0 ; i<xVar.noOfCategories() ; i++)
			if (!doingDrag || dragCat == i) {
				Value xValue = xVar.getLabel(i);
				double yMean = ls.evaluateMean(xValue);
				
				int lowMeanXPos = getLowMeanPos(xAxis, i);
				
				p0 = getScreenPoint(yMean, lowMeanXPos, p0);
				
				int baseline = p0.y + baselineOffset;
				LabelValue paramLabel = new LabelValue(offsetParam + getSubscript(i));
				paramLabel.drawLeft(g, p0.x - 2, baseline);
			}
	}
	
	private String getSubscript(int i) {
		return MText.expandText("#sub" + (i + 1) +"#");
	}
	
	private void drawBaselineParameters(Graphics g, MultipleRegnModel ls, CatVariable xVar,
																											NumCatAxis xAxis, NumCatAxis yAxis) {
		FontMetrics fm = g.getFontMetrics();
		int baselineOffset = (fm.getAscent() - fm.getDescent()) / 2;
		
		int baselineVert = drawBaseline(g, ls, xVar, xAxis, yAxis, baselineOffset);
		
		for (int i=0 ; i<xVar.noOfCategories() ; i++)
			if (i != baselineIndex)
				drawOneLevelOffset(g, ls, xVar, xAxis, yAxis, baselineVert, i, baselineOffset);
	}
	
	private int drawBaseline(Graphics g, MultipleRegnModel ls,
									CatVariable xVar, NumCatAxis xAxis, NumCatAxis yAxis, int baselineOffset) {
		Value baselineValue = xVar.getLabel(baselineIndex);
		double baselineMean = ls.evaluateMean(baselineValue);
		int leftBaselinePos = getLowMeanPos(xAxis, 0) / 2;
		Point baselineLeft = getScreenPoint(baselineMean, leftBaselinePos, null);
		
		g.setColor(kBaselineColor);
		g.drawLine(baselineLeft.x, baselineLeft.y, getSize().width - baselineLeft.x, baselineLeft.y);
		if (!doingDrag || dragCat == baselineIndex) {
			int baseline = baselineLeft.y + baselineOffset;
			LabelValue paramLabel = new LabelValue(baselineParam);
			paramLabel.drawLeft(g, baselineLeft.x - 2, baseline);
		}
		return baselineLeft.y;
	}
	
	private void drawOneLevelOffset(Graphics g, MultipleRegnModel ls, CatVariable xVar,
											NumCatAxis xAxis, NumCatAxis yAxis, int baselineVert, int levelIndex,
											int baselineOffset) {
		g.setColor(kLevelOffsetColor);
		Value xValue = xVar.getLabel(levelIndex);
		double yMean = ls.evaluateMean(xValue);
		
		int lowMeanXPos = getLowMeanPos(xAxis, levelIndex);
		
		Point p0 = getScreenPoint(yMean, lowMeanXPos - 6, null);
		
		if (!doingDrag || dragCat == levelIndex) {
			if (p0.y < baselineVert) {
				g.drawLine(p0.x, p0.y, p0.x, baselineVert - 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y + 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y + 4);
			}
			else {
				g.drawLine(p0.x, p0.y, p0.x, baselineVert + 1);
				g.drawLine(p0.x, p0.y, p0.x + 4, p0.y - 4);
				g.drawLine(p0.x, p0.y, p0.x - 4, p0.y - 4);
			}
			
			int baseline = (p0.y + baselineVert) / 2 + baselineOffset;
			LabelValue paramLabel = new LabelValue(offsetParam + getSubscript(levelIndex));
			paramLabel.drawLeft(g, p0.x - 2, baseline);
		}
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
		MultipleRegnModel lsFit = (MultipleRegnModel)getVariable(modelKey);
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
				MultipleRegnModel lsFit = (MultipleRegnModel)getVariable(modelKey);
				if (dragCat == 0) {
					double mean0Change = newMean - lsFit.getParameter(0).toDouble();
					lsFit.setParameter(0, newMean);
					for (int i=1 ; i<lsFit.noOfParameters() ; i++)
						lsFit.setParameter(i, lsFit.getParameter(i).toDouble() - mean0Change);
				}
				else
					lsFit.setParameter(dragCat, newMean - lsFit.getParameter(0).toDouble());
				
				getData().variableChanged(modelKey);
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
	
