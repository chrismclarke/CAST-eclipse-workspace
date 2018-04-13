package exper;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;


public class AdjustBaseTreatView extends DotPlotView {
//	static public final String ADJUST_BASE_DOTPLOT = "adjustBaseDotPlot";
	
	private static final int kMaxJitter = 30;
	private static final int kMeanExtraPix = 8;
	private static final int kHalfBarWidth = 7;
	private static final int kHalfHiliteHt = 2;
	
	private NumCatAxis treatAxis;
	private CatVariable treatVariable;
	private String modelKey;
	
	private boolean doingDrag = false;
	
	private double catToNum[];			//	used if z-axis is numerical
	private double xMean;
	
	public AdjustBaseTreatView(DataSet theData, XApplet applet, NumCatAxis numAxis,
						NumCatAxis treatAxis, String yKey, String treatKey, String modelKey,
						double[] catToNum, double xMean) {
		super(theData, applet, numAxis, 0.5);
		this.treatAxis = treatAxis;
		this.modelKey = modelKey;
		this.catToNum = catToNum;
		this.xMean = xMean;
		setActiveNumVariable(yKey);
		treatVariable = (CatVariable)getVariable(treatKey);
	}
	
	public AdjustBaseTreatView(DataSet theData, XApplet applet, NumCatAxis numAxis,
										NumCatAxis treatAxis, String yKey, String treatKey, String modelKey) {
		this(theData, applet, numAxis, treatAxis, yKey, treatKey, modelKey, null, 0.0);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		Point newPoint = super.getScreenPoint(index, theVal, thePoint);
		if (newPoint != null) {
			int groupIndex = treatVariable.getItemCategory(index);
			int offset = (catToNum == null) ? treatAxis.catValToPosition(groupIndex)
																			: treatAxis.numValToRawPosition(catToNum[groupIndex]);
			offset -= currentJitter / 2;
			newPoint.x += offset;
		}
		return newPoint;
	}
	
	protected int getMaxJitter() {
		int noOfGroups = treatVariable.noOfCategories();
		return Math.min(kMaxJitter,
							(getSize().width - getViewBorder().left - getViewBorder().right) / noOfGroups / 2);
	}
	
	private void drawBackground(Graphics g) {
		int noOfCategories = treatVariable.noOfCategories();
		int xSpacing = (catToNum == null)
												? (treatAxis.catValToPosition(1) - treatAxis.catValToPosition(0))
												: (treatAxis.numValToRawPosition(catToNum[1]) - treatAxis.numValToRawPosition(catToNum[0]));
		int offset = Math.min(currentJitter / 2 + kMeanExtraPix, xSpacing / 2);
		
		FactorsModel model = (FactorsModel)getVariable("model");
		double base = model.getConstant();
		double effects[] = model.getMainEffects(0);
		boolean linearEffect = (catToNum != null) && effects.length == 1;
		
		if (linearEffect) {
			g.setColor(Color.lightGray);
			double minX = treatAxis.minOnAxis;
			double maxX = treatAxis.maxOnAxis;
			double slop = (maxX - minX) * 0.2;
			minX -= slop;
			maxX += slop;
			
			double minY = base + (minX - xMean) * effects[0];
			double maxY = base + (maxX - xMean) * effects[0];
			int yMinPos = axis.numValToRawPosition(minY);
			int xMinPos = treatAxis.numValToRawPosition(minX);
			Point p0 = translateToScreen(yMinPos, xMinPos, null);
			
			int yMaxPos = axis.numValToRawPosition(maxY);
			int xMaxPos = treatAxis.numValToRawPosition(maxX);
			Point p1 = translateToScreen(yMaxPos, xMaxPos, null);
			g.drawLine(p0.x, p0.y, p1.x, p1.y);
		}
		
		int basePos = axis.numValToRawPosition(base);
		int baseVert = translateToScreen(basePos, 0, null).y;
		
		Point p = null;
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			double effectI = linearEffect ? (catToNum[treat] - xMean) * effects[0] : effects[treat];
			double mean = base + effectI;
			int yPos = axis.numValToRawPosition(mean);
			int xCenter = (catToNum == null) ? treatAxis.catValToPosition(treat)
																			: treatAxis.numValToRawPosition(catToNum[treat]);
			p = translateToScreen(yPos, xCenter, p);
			
			g.setColor(TreatEffectSliderView.getBaseBarColor(treat));
			int top = Math.min(p.y, baseVert);
			int bottom = Math.max(p.y, baseVert);
			g.fillRect(p.x - kHalfBarWidth, top, 2 * kHalfBarWidth, bottom - top);
		}
		
		if (doingDrag) {
			g.setColor(Color.yellow);
			g.fillRect(0, baseVert - kHalfHiliteHt, getSize().width, 2 * kHalfHiliteHt + 1);
		}
		g.setColor(Color.red);
		g.drawLine(0, baseVert, getSize().width, baseVert);
		
		g.setColor(Color.blue);
		for (int treat=0 ; treat<noOfCategories ; treat++) {
			double effectI = linearEffect ? (catToNum[treat] - xMean) * effects[0] : effects[treat];
			double mean = base + effectI;
			int yPos = axis.numValToRawPosition(mean);
			int xCenter = (catToNum == null) ? treatAxis.catValToPosition(treat)
																			: treatAxis.numValToRawPosition(catToNum[treat]);
			p = translateToScreen(yPos, xCenter, p);
			
			g.drawLine(p.x - offset, p.y, p.x + offset, p.y);
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		super.paintView(g);
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return catToNum == null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.x + hitOffset >= axis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + axis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.x);
														//	uses x-coord because translateFromScreen() swaps x and y
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		
		FactorsModel model = (FactorsModel)getVariable("model");
		double base = model.getConstant();
		int basePos = axis.numValToRawPosition(base);
		
		if (Math.abs(basePos - hitPos.x) <= kMinHitDistance)
				return new VertDragPosInfo(hitPos.x, 0, basePos - hitPos.x);
														//	uses x-coord because translateFromScreen() swaps x and y
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
		
		hitOffset = dragPos.hitOffset;
		doingDrag = true;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			return;
		
		FactorsModel response = (FactorsModel)getVariable(modelKey);
		double effects[] = response.getMainEffects(0);
		double oldBase = response.getConstant();
		
		VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
		int newVertPos = dragPos.y + hitOffset;
		try {
			double newBase = axis.positionToNumVal(newVertPos);
			
			response.setConstant(newBase);
			for (int i=0 ; i<effects.length ; i++)
				effects[i] += (oldBase - newBase);
			response.setMainEffect(0, effects);
			
			getData().variableChanged(modelKey);
		} catch (AxisException e) {
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}