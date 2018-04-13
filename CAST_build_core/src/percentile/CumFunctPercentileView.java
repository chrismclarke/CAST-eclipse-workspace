package percentile;

import java.awt.*;

import dataView.*;
import axis.*;


public class CumFunctPercentileView extends CumFunctDotPlotView {
//	static public final String CUM_FUNCT_PERCENTILE_PLOT = "cumFunctPercentile";
	
	static final private Color kGreen = new Color(0x009900);
	
	static final private double kEps = 0.00001;
	
	static final private double kDisplayProbs[] = {0.0, 0.25, 0.5, 0.75, 1.0};
	static final private Color kOuterFillColor = Color.white;
	static final private Color kInnerFillColor = Color.lightGray;
	
	private double displayProbs[] = (double[])kDisplayProbs.clone();
	
	private int selectedIndex = -1;
	
	public CumFunctPercentileView(DataSet theData, XApplet applet,NumCatAxis theAxis,
												DataSet refData, String refKey, int inequality,VertAxis cumProbAxis) {
																							//	inequality as defined in PropnRangeView
		super(theData, applet, theAxis, refData, refKey, inequality, cumProbAxis);
		setAllowDrag(false);	//	to prevent shading on left of graph
	}
	
	private void drawLineToGraph(Graphics g, double propn, double x, boolean selected) {
		int propnPos = cumProbAxis.numValToRawPosition(propn);
		int xPos = axis.numValToRawPosition(x);
		Point p = translateToScreen(xPos, propnPos, null);
		g.setColor(Color.red);
		g.drawLine(0, p.y, p.x - 1, p.y);
		if (selected) {
			g.drawLine(0, p.y - 1, p.x - 1, p.y - 1);
			g.drawLine(0, p.y + 1, p.x - 1, p.y + 1);
			for (int i=1 ; i<5 ; i++)
				g.drawLine(p.x - 1 - i, p.y - i, p.x - 1 - i, p.y + i);
		}
		
		g.setColor(kGreen);
		g.drawLine(p.x, p.y, p.x, getSize().height - getViewBorder().bottom);
	}
	
	protected void drawTopPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		NumValue sortedData[] = getNumVariable().getSortedData();
		
		PercentileInfo boxInfo = new PercentileInfo(displayProbs);
		boxInfo.initialisePercentiles(sortedData, axis, PercentileInfo.STEP);
		
		for (int i=0 ; i<displayProbs.length ; i++)
			drawLineToGraph(g, displayProbs[i], boxInfo.percentile[i], selectedIndex == i);
		
		super.drawTopPanel(g, yVar, ref, refHorizPos);
	}
	
	private void drawArrowToBox(Graphics g, int boxTop, int boxPos) {
		Point p = translateToScreen(boxPos, boxTop, null);
		p.y -= 2;
		g.drawLine(p.x, getSize().height - getViewBorder().bottom, p.x, p.y);
		for (int i=1 ; i<5 ; i++)
			g.drawLine(p.x - i, p.y - i, p.x + i, p.y - i);
	}
	
	protected void drawBottomPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		NumValue sortedData[] = getNumVariable().getSortedData();
		
		boolean isBoxplotPercentiles = true;
		for (int i=0 ; i<5 ; i++)
			isBoxplotPercentiles = isBoxplotPercentiles && (displayProbs[i] == kDisplayProbs[i]);
		
		if (isBoxplotPercentiles) {
			boxInfo.initialiseBox(sortedData, true, axis);
			boxInfo.vertMidLine = -getViewBorder().bottom / 2;
			boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
			
			g.setColor(getForeground());
			boxInfo.drawBoxPlot(g, this, yVar.getSortedData(), axis);
		
			g.setColor(kGreen);
			drawArrowToBox(g, boxInfo.boxBottom + boxInfo.getBoxHeight(), axis.numValToRawPosition(sortedData[0].toDouble()));
			drawArrowToBox(g, boxInfo.boxBottom + boxInfo.getBoxHeight(), axis.numValToRawPosition(sortedData[sortedData.length - 1].toDouble()));
			for (int i=1 ; i<displayProbs.length-1 ; i++)
				drawArrowToBox(g, boxInfo.boxBottom + boxInfo.getBoxHeight(), boxInfo.boxPos[i]);
		}
		else {
			PercentileInfo rectInfo = new PercentileInfo(displayProbs);
			rectInfo.initialisePercentiles(sortedData, axis, PercentileInfo.STEP);
			rectInfo.vertMidLine = -getViewBorder().bottom / 2;
			rectInfo.boxBottom = rectInfo.vertMidLine - PercentileInfo.kBoxHeight / 2;
			rectInfo.setFillColor(kOuterFillColor, 0);
			rectInfo.setFillColor(kInnerFillColor, 1);
			rectInfo.setFillColor(kInnerFillColor, 2);
			rectInfo.setFillColor(kOuterFillColor, 3);
			
			g.setColor(getForeground());
			rectInfo.drawPercentilePlot(g, this, yVar.getSortedData(), axis);
		
			g.setColor(kGreen);
			for (int i=0 ; i<displayProbs.length ; i++)
				drawArrowToBox(g, rectInfo.boxBottom + PercentileInfo.kBoxHeight, rectInfo.percentilePos[i]);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected int getMinMouseMove() {
		return 1;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int hitIndex = -1;
		int minHitDist = Integer.MAX_VALUE;
		for (int i=1 ; i<displayProbs.length-1 ; i++) {
			double propn = displayProbs[i];
			int propnVertPos = cumProbAxis.numValToRawPosition(propn);
			int propnVert = translateToScreen(0, propnVertPos, null).y;
			int hitDist = y - propnVert;
			if (Math.abs(hitDist) < Math.abs(minHitDist)) {
				minHitDist = hitDist;
				hitIndex = i;
			}
		}
		
		if (Math.abs(minHitDist) > kHitSlop)
			return null;
		else
			return new VertDragPosInfo(y, hitIndex, minHitDist);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0 || hitPos.y + hitOffset >= cumProbAxis.getAxisLength())
			return null;
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		selectedIndex = dragPos.index;
		repaint();
		((DragMultiVertAxis)cumProbAxis).setDragValue(new NumValue(displayProbs[selectedIndex] * 100, 0));
		((DragMultiVertAxis)cumProbAxis).setAlternateLabels(2);
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			if (fromPos == null)
				doingDrag = true;
			double oldPropn = displayProbs[selectedIndex];
			
			double newPropn = 0.0;
			VertDragPosInfo newPos = (VertDragPosInfo)toPos;
			int newPercent = 0;
			
			try {
				newPropn = cumProbAxis.positionToNumVal(newPos.y + hitOffset);
				newPercent = (int)Math.round(newPropn * 100);
				newPropn = newPercent / 100.0;
			} catch (AxisException ex) {
				return;
			}
			
			if (newPropn - displayProbs[selectedIndex - 1] < 0.05 - kEps
									|| displayProbs[selectedIndex + 1] - newPropn < 0.05 - kEps)
				return;
			
			if (newPropn != oldPropn) {
				displayProbs[selectedIndex] = newPropn;
				repaint();
				((DragMultiVertAxis)cumProbAxis).setDragValue(new NumValue(newPercent, 0));
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedIndex = -1;
		repaint();
		((DragMultiVertAxis)cumProbAxis).setAlternateLabels(1);
	}
	
	
}
	
