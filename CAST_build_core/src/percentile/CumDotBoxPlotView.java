package percentile;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import exercise2.*;

//import boxPlot.*;


public class CumDotBoxPlotView extends DotPlotView implements PropnConstants {
//	static public final String CUM_DOT_BOX_PLOT = "cumDotBox";
	
	static final private int kDotPlotWidth = 30;
	static final private int kMinBoxPlotWidth = 60;
	static final private int kDotPlotBottomMargin = 5;
	static final private int kDotPlotTopMargin = 10;
	
	static final private Color kBottomBackground = new Color(0xCCCCFF);
	static final private Color kHiliteBottomBackground = new Color(0xCCCC66);
	static final private Color kHiliteTopBackground = Color.yellow;
	static final private Color kBoxFillColor = new Color(0xDDDDDD);
	
	static final protected int kHitSlop = 4;
	
	protected DataSet refData;
	private String refKey;
	private int inequality;
	private boolean lowInequality;
	protected boolean allowDrag = true;
	
	protected BoxInfo boxInfo;
	
	protected int hitOffset;
	protected boolean doingDrag = false;
	
	public CumDotBoxPlotView(DataSet theData, XApplet applet,
								NumCatAxis theAxis, DataSet refData, String refKey, int inequality) {
																							//	inequality as defined in PropnConstants
		super(theData, applet, theAxis, 1.0);
		setViewBorder(new Insets(5, 5, kDotPlotWidth + kDotPlotBottomMargin + kDotPlotTopMargin + 5, 5));
		
		setMinDisplayWidth(kMinBoxPlotWidth);
		this.refData = refData;
		this.refKey = refKey;
		changeInequality(inequality);
		
		boxInfo = new BoxInfo();
		boxInfo.setFillColor(kBoxFillColor);
	}
	
	public void changeInequality(int inequality) {
															//	inequality as defined in PropnConstants
		this.inequality = inequality;
		lowInequality = (inequality == LESS_THAN || inequality == LESS_EQUAL);
		repaint();
	}
	
	public void setAllowDrag(boolean allowDrag) {
		this.allowDrag = allowDrag;
		repaint();
	}
	
	protected Color getTopHiliteBackground() {
		return kHiliteTopBackground;
	}
	
	protected Color getBottomHiliteBackground() {
		return kHiliteBottomBackground;
	}
	
	protected NumVariable getReferenceVariable() {
		if (refData == null)
			return null;
		else
			return (NumVariable)refData.getVariable(refKey);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (isBadValue(theVal))
			return null;
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos = -kDotPlotWidth - kDotPlotTopMargin;			//	draw in margin
			if (currentJitter > 0 && jittering != null && index < jittering.length)
				vertPos += (currentJitter * jittering[index]) >> 14;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected void drawTopPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		boxInfo.initialiseBox(yVar.getSortedData(), true, axis);
		boxInfo.vertMidLine = getDisplayWidth() / 2;
		boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
		
		g.setColor(getForeground());
		boxInfo.drawBoxPlot(g, this, yVar.getSortedData(), axis);
	}
	
	protected void drawBottomPanel(Graphics g, NumVariable yVar, double ref, int refHorizPos) {
		checkJittering();
		Point thePoint = null;
		
		ValueEnumeration e = yVar.values();
		int index = 0;
		g.setColor(getForeground());
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = allowDrag && !Double.isNaN(ref)
															&& PropnRangeView.doComparison(nextVal.toDouble(), ref, inequality);
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null)
				drawMark(g, thePoint, nextSel ? 5 : 0);
			index++;
		}
	}
	
	protected double getXReferenceValue() {
		NumVariable refVar = getReferenceVariable();
		if (refVar == null)
			return Double.NaN;
		else
			return refVar.doubleValueAt(0);
	}
	
	protected void drawDragLine(Graphics g, int refHoriz, int topBorder) {
		if (doingDrag) {
			g.setColor(getForeground());
			g.drawLine(refHoriz, topBorder, refHoriz, getSize().height);
			g.setColor(Color.red);
			g.drawLine(refHoriz - 1, topBorder, refHoriz - 1, getSize().height);
			g.drawLine(refHoriz + 1, topBorder, refHoriz + 1, getSize().height);
		}
		else {
			g.setColor(Color.red);
			g.drawLine(refHoriz, topBorder, refHoriz, getSize().height);
		}
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = getNumVariable();
		
		double ref = getXReferenceValue();
		int refHorizPos = 0;
		int boxShadeVert = getSize().height - getViewBorder().bottom;
		int topBorder = getViewBorder().top;
		if (!Double.isNaN(ref)) {
			refHorizPos = axis.numValToRawPosition(ref);
			Point thePoint = translateToScreen(refHorizPos, 0, null);
			int refHoriz = thePoint.x;
			
			if (allowDrag) {
				g.setColor(lowInequality ? getBottomHiliteBackground() : kBottomBackground);
				g.fillRect(0, boxShadeVert, refHoriz, getSize().height - boxShadeVert);
				
				g.setColor(lowInequality ? kBottomBackground : getBottomHiliteBackground());
				g.fillRect(refHoriz, boxShadeVert, getSize().width - refHoriz, getSize().height - boxShadeVert);
				
				g.setColor(lowInequality ? getTopHiliteBackground() : Color.white);
				g.fillRect(0, topBorder, refHoriz, boxShadeVert - topBorder);
				
				g.setColor(lowInequality ? Color.white : getTopHiliteBackground());
				g.fillRect(refHoriz, topBorder, getSize().width - refHoriz, boxShadeVert - topBorder);
				
				drawDragLine(g, refHoriz, topBorder);
			}
		}
		
		if (Double.isNaN(ref) || !allowDrag) {
			g.setColor(kBottomBackground);
			g.fillRect(0, boxShadeVert, getSize().width, getSize().height - boxShadeVert);
			g.setColor(Color.white);
			g.fillRect(0, topBorder, getSize().width, boxShadeVert - topBorder);
		}
		
		drawTopPanel(g, yVar, ref, refHorizPos);
		
		drawBottomPanel(g, yVar, ref, refHorizPos);
	}
	
	protected int getMaxJitter() {
		return kDotPlotWidth;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return allowDrag;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		double ref = getXReferenceValue();
		if (Double.isNaN(ref))
			return null;
		int refHorizPos = axis.numValToRawPosition(ref);
		int refHoriz = translateToScreen(refHorizPos, 0, null).x;
		
		if (Math.abs(x - refHoriz) > kHitSlop)
			return null;
		else
			return new HorizDragPosInfo(x, 0, x - refHoriz);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset < 0 || hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		hitOffset = dragPos.hitOffset;
		doingDrag = true;
		repaint();
		
		return true;
	}
	
	private double round(double x, int decimals) {
		double factor = Math.pow(10.0, decimals);
		return Math.rint(x * factor) / factor;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			if (fromPos == null)
				doingDrag = true;
			NumVariable refVar = getReferenceVariable();
			NumValue refValue = (NumValue)(refVar.valueAt(0));
//			double oldRef = refValue.toDouble();
			double newRef = 0.0;
			HorizDragPosInfo newPos = (HorizDragPosInfo)toPos;
			try {
				newRef = axis.positionToNumVal(newPos.x - hitOffset - getViewBorder().left);
			} catch (AxisException ex) {
				return;
			}
			refValue.setValue(round(newRef, refValue.decimals));
			
			refData.valueChanged(0);
			getData().variableChanged(getData().getDefaultNumVariableKey());
			
			if (getApplet() instanceof ExerciseApplet)
				((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
	
}
	
