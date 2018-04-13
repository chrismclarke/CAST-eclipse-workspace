package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;
import coreGraphics.*;


public class DragFactorMeansView extends CoreOneFactorView {
	
	static final private int kHitSlop = 7;
	
	static final private Color kNoDragMeanColor = Color.lightGray;
	static final private Color kRegnLineColor = new Color(0xCCCCCC);
	
	protected String xNumKey;
	
	private boolean canDrag = true;
	private boolean doingDrag = false;
	private int dragCat;
	private int hitOffset;
	
	public DragFactorMeansView(DataSet theData, XApplet applet, NumCatAxis xAxis, NumCatAxis yAxis,
								String xNumKey, String xKey, String yKey, String modelKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, modelKey);
		this.xNumKey = xNumKey;
	}
	
	public void setCanDrag(boolean canDrag) {
		this.canDrag = canDrag;
	}
	
	protected void drawBackground(Graphics g) {
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		if (model instanceof GroupsModelVariable)
			return;
		
		g.setColor(kRegnLineColor);
		model.drawMean(g, this, xAxis, yAxis);
	}
	
	private boolean[] dragFlags(CoreModelVariable model, int nx) {
		boolean canDragMean[] = new boolean[nx];		//	all false
		if (model instanceof GroupsModelVariable)
			for (int i=0 ; i<canDragMean.length ; i++)
				canDragMean[i] = true;
		else {
			canDragMean[0] = true;
			if (model instanceof LinearModel) {
				canDragMean[canDragMean.length - 1] = true;
				if (model instanceof QuadraticModel)
					canDragMean[(canDragMean.length - 1) / 2] = true;
			}
		}
		return canDragMean;
	}
	
	protected void drawFactorMeans(Graphics g, CoreModelVariable model, CatVariable xVar) {
		int nx = xVar.noOfCategories();
		int xStep = xAxis.catValToPosition(1) - xAxis.catValToPosition(0);
		
		boolean canDragMean[] = dragFlags(model, nx);
		
		Point p0 = null;
		Point p1 = null;
		for (int i=0 ; i<nx ; i++) {
			int xCenter = xAxis.catValToPosition(i);
			int maxJitter = noInXCat[i] * scalePercent / 100;
			int lowMeanXPos = xCenter - maxJitter - kMeanExtra;
			int highMeanXPos = xCenter + maxJitter + kMeanExtra;
			
			double mean = evaluateModelMean(model, i, xVar);
			p0 = getScreenPoint(mean, lowMeanXPos, p0);
			p1 = getScreenPoint(mean, highMeanXPos, p1);
			
			if (doingDrag && dragCat == i) {
				g.setColor(Color.yellow);
				g.fillRect(p0.x, p0.y - 2, p1.x - p0.x + 1, 5);
			}
			
			if (doingDrag && !canDragMean[i])
				g.setColor(kNoDragMeanColor);
			else
				g.setColor(kMeanColor);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
			
			p1.x = Math.min(p1.x + 8, (p1.x + p0.x + xStep - ModelGraphics.kHandleWidth) / 2);
			if (canDrag) {
				if (!doingDrag && canDragMean[i])
					ModelGraphics.drawHandle(g, p1, false);
				else if (doingDrag && dragCat == i)
					ModelGraphics.drawHandle(g, p1, true);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canDrag;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		int cat = xAxis.positionToCatVal(p.x);
		
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		CatVariable xCatVar = (CatVariable)getVariable(xKey);
		int nx = xCatVar.noOfCategories();
		
		boolean canDragMean[] = dragFlags(model, nx);
		
		if ((cat < 0) || (cat >= canDragMean.length) || !canDragMean[cat])
			return null;
		
		double yMean = evaluateModelMean(model, cat, xCatVar);
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
	
	private void setMean(int catIndex, double newMean) {
		CoreModelVariable model = (CoreModelVariable)getVariable(modelKey);
		CatVariable xCatVar = (CatVariable)getVariable(xKey);
		int nx = xCatVar.noOfCategories();
		if (model instanceof GroupsModelVariable) {
			GroupsModelVariable gm = (GroupsModelVariable)model;
			gm.setMean(newMean, catIndex);
		}
		else if (model instanceof QuadraticModel) {
			double x0 = 0.0;
			int midIndex = (nx - 1) / 2;
			double x1 = midIndex;
			double x2 = (nx - 1);
			double y0 = (catIndex == 0) ? newMean : evaluateModelMean(model, 0, xCatVar);
			double y1 = (catIndex == midIndex) ? newMean : evaluateModelMean(model, midIndex, xCatVar);
			double y2 = (catIndex == nx - 1) ? newMean : evaluateModelMean(model, nx - 1, xCatVar);
			
			double b2 = ((y1 - y0) / (x1-x0) - (y2 - y1) / (x2-x1)) / (x0 - x2);
			double b1 = (y1 - y0) / (x1 - x0) - b2 * (x0 + x1);
			double b0 = y0 - x0 * (b1 + x0 * b2);
			
			QuadraticModel qm = (QuadraticModel)model;
			qm.setIntercept(b0);
			qm.setSlope(b1);
			qm.setCurvature(b2);
		}
		else if (model instanceof MeanOnlyModel) {
			MeanOnlyModel mm = (MeanOnlyModel)model;
			mm.setMean(newMean);
		}
		else {
			LinearModel lm = (LinearModel)model;
			if (catIndex == 0) {
				double highY = lm.evaluateMean(nx - 1);
				double slope = (highY - newMean) / (nx - 1);
				lm.setIntercept(newMean);
				lm.setSlope(slope);
			}
			else {
				double slope = (newMean - lm.evaluateMean(0.0)) / (nx - 1);
				lm.setSlope(slope);
			}
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			doingDrag = true;
			VertDragPosInfo posInfo = (VertDragPosInfo)toPos;
			Point p = translateFromScreen(0, posInfo.y, null);
			
			try {
				double newMean = yAxis.positionToNumVal(p.y - hitOffset);
				setMean(dragCat, newMean);
				getData().variableChanged(modelKey);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}
	
