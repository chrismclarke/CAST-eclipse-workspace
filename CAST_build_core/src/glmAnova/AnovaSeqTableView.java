package glmAnova;

import java.awt.*;

import dataView.*;
import coreGraphics.*;


public class AnovaSeqTableView extends AnovaCombineTableView {
	
	static final private Color kDimVariableColor = new Color(0x444444);
	
	static final private int kArrowGap = 6;
	static final private int kHitSlop = 7;
	
	private String originalNames[];
	private Color originalColors[];
	private SetLastExplanInterface linkedChart, linkedView;
	
	private int maxVarNameWidth;
	
	private boolean doingDrag = false;
	private int dragVert, hitOffset;
	private int lastSeparateX;
	
	public AnovaSeqTableView(DataSet theData, XApplet applet, String[] componentKey, NumValue maxSsq,
									String[] componentName, Color[] componentColor, String[] variableName,
									SetLastExplanInterface linkedChart) {
		super(theData, applet, componentKey, maxSsq, componentName, componentColor, variableName);
		originalNames = (String[])componentName.clone();
		originalColors = (Color[])componentColor.clone();
		lastSeparateX = variableName.length - 1;
		this.linkedChart = linkedChart;
	}
	
	public void setLinkedView(SetLastExplanInterface linkedView) {
		this.linkedView = linkedView;
	}
	
//--------------------------------------------------------------------
	
	protected int getMaxSourceWidth(FontMetrics fm) {
		int sourceWidth = 0;
		for (int i=0 ; i<originalNames.length ; i++)
			if (originalNames[i] != null)
				sourceWidth = Math.max(sourceWidth, fm.stringWidth(originalNames[i]));
		return sourceWidth;
	}
	
	protected void doInitialisation(Graphics g) {
		maxVarNameWidth = 0;
		FontMetrics fm = g.getFontMetrics();
		for (int i=0 ; i<variableName.length ; i++)
			maxVarNameWidth = Math.max(maxVarNameWidth, fm.stringWidth(variableName[i]));
		
		super.doInitialisation(g);
	}
	
	protected int getLeftBorder(Graphics g) {
		return 2 * ModelGraphics.kHandleWidth + 3 * kArrowGap + maxVarNameWidth;
	}
	
	protected void drawLeftBorder(int borderRight, int actualTableWidth, Graphics g) {
		int firstComponentBaseline = getComponentBaseline(0);
		int componentStep = getComponentBaseline(1) - firstComponentBaseline;
		
		g.setColor(Color.red);
		int arrowRight = borderRight - kArrowGap - ModelGraphics.kHandleWidth / 2;
		int arrowLeft = arrowRight - (2 * kArrowGap + ModelGraphics.kHandleWidth + maxVarNameWidth);
		int separatorVert = getZerothSeparatorVert() + (lastSeparateX + 1) * componentStep;
		g.drawLine(arrowLeft, separatorVert, borderRight + actualTableWidth, separatorVert);
		
		LabelValue tempName = new LabelValue("");
		int labelCenter = (arrowLeft + arrowRight) / 2;
		for (int i=0 ; i<=lastSeparateX ; i++) {
			g.setColor(componentColor[i + 1]);
			tempName.label = variableName[i];
			tempName.drawCentred(g, labelCenter, firstComponentBaseline + i * componentStep);
		}
		g.setColor(kDimVariableColor);
		for (int i=lastSeparateX+1 ; i<variableName.length ; i++) {
			tempName.label = variableName[i];
			tempName.drawCentred(g, labelCenter, firstComponentBaseline + i * componentStep);
		}
		
		if (doingDrag) {
			g.setColor(Color.red);
			g.drawLine(arrowLeft, dragVert, arrowRight, dragVert);
			Point p = new Point(arrowRight, dragVert);
			ModelGraphics.drawHandle(g, p, true);
			p.x = arrowLeft;
			ModelGraphics.drawHandle(g, p, true);
		}
		else {
			Point p = new Point(arrowRight, separatorVert);
			ModelGraphics.drawHandle(g, p, false);
			p.x = arrowLeft;
			ModelGraphics.drawHandle(g, p, false);
		}
	}
	
	private int getZerothSeparatorVert() {
		return getComponentBaseline(0) - ascent - kExpResidGap / 2;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int targetVert = getComponentBaseline(lastSeparateX) + descent + kExpResidGap / 2;
		if (Math.abs(y - targetVert) > kHitSlop)
			return null;
		
		return new VertDragPosInfo(y, lastSeparateX, y - targetVert);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doingDrag = true;
		VertDragPosInfo posInfo = (VertDragPosInfo)startInfo;
		hitOffset = posInfo.hitOffset;
		dragVert = posInfo.y  - hitOffset;
		repaint();
		return true;
	}
	
	public void setLastSeparateX(int hitIndex) {
		if (hitIndex < lastSeparateX) {
			for (int i=lastSeparateX-1 ; i>=hitIndex ; i--)
				combineSsqWithNext(i + 2, null, null);
			lastSeparateX = hitIndex;
			if (linkedChart != null)
				linkedChart.setLastExplanatory(lastSeparateX);
			if (linkedView != null)
				linkedView.setLastExplanatory(lastSeparateX);
		}
		else if (hitIndex > lastSeparateX) {
			for (int i=lastSeparateX ; i<hitIndex ; i++)
				splitSsq(i + 2, originalNames[i + 2], originalColors[i + 2], null, null);
			lastSeparateX = hitIndex;
			if (linkedChart !=  null)
				linkedChart.setLastExplanatory(lastSeparateX);
			if (linkedView != null)
				linkedView.setLastExplanatory(lastSeparateX);
		}
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null)
			doingDrag = false;
		else {
			doingDrag = true;
			VertDragPosInfo posInfo = (VertDragPosInfo)toPos;
			int componentStep = getComponentBaseline(1) - getComponentBaseline(0);
			
			int topVert = getZerothSeparatorVert() - componentStep / 2;
			int bottomVert = topVert + (variableName.length + 1) * componentStep;
			dragVert = Math.max(topVert, Math.min(bottomVert, posInfo.y - hitOffset));
			
			int sep0Before = getZerothSeparatorVert() - componentStep / 2;
			int hitIndex = (dragVert - sep0Before) / componentStep;
			hitIndex = Math.max(0, Math.min(variableName.length, hitIndex)) - 1;
			setLastSeparateX(hitIndex);
/*
			if (hitIndex < lastSeparateX) {
				for (int i=lastSeparateX-1 ; i>=hitIndex ; i--)
					combineSsqWithNext(i + 2, null, null);
				lastSeparateX = hitIndex;
				if (linkedChart != null)
					linkedChart.setLastExplanatory(lastSeparateX);
				if (linkedView != null)
					linkedView.setLastExplanatory(lastSeparateX);
			}
			else if (hitIndex > lastSeparateX) {
				for (int i=lastSeparateX ; i<hitIndex ; i++)
					splitSsq(i + 2, originalNames[i + 2], originalColors[i + 2], null, null);
				lastSeparateX = hitIndex;
				if (linkedChart !=  null)
					linkedChart.setLastExplanatory(lastSeparateX);
				if (linkedView != null)
					linkedView.setLastExplanatory(lastSeparateX);
			}
*/
		}
		repaint();
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
}