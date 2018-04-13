package sport;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;

import time.*;


public class WinningTimeView extends TimeView {
	
	static final private int NO_SELECTION = 0;
	static final private int LOW_SELECTION = 1;
	static final private int HIGH_SELECTION = 2;
	
	private boolean drawHandles;
	private int selection = NO_SELECTION;
	private String popnDistnKey, cumProbKey;
	
	public WinningTimeView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis,
									String cumProbKey, String popnDistnKey) {
		super(theData, applet, timeAxis, numAxis);
		drawHandles = (cumProbKey != null) && (popnDistnKey != null);
		if (drawHandles) {
			getViewBorder().left += 10;
			getViewBorder().right += 10;
		}
		this.popnDistnKey = popnDistnKey;
		this.cumProbKey = cumProbKey;
	}
	
	public void paintView(Graphics g) {
		int selectedIndex = getSelection().findSingleSetFlag();
		if (selectedIndex >= 0) {
			g.setColor(Color.yellow);
			
			Point thePoint = getScreenBefore(selectedIndex, null);
			int lowSelectedPos = thePoint.x;
			thePoint = getScreenBefore(selectedIndex + 1, thePoint);
			int highSelectedPos = thePoint.x;
			g.fillRect(lowSelectedPos, 0, (highSelectedPos - lowSelectedPos), getSize().height);
			g.setColor(getForeground());
		}
		
		if (drawHandles) {
			InvNormalQVariable meanExtreme = (InvNormalQVariable)getVariable(cumProbKey);
			
			double lowExpected = meanExtreme.doubleValueAt(0);
			paintArrow(g, lowExpected, 5, selection == LOW_SELECTION);
			
			double highExpected = meanExtreme.doubleValueAt(meanExtreme.noOfValues() - 1);
			paintArrow(g, highExpected, getSize().width - 6, selection == HIGH_SELECTION);
		}
		
		super.paintView(g);
	}
	
	public void paintArrow(Graphics g, double y, int xPos, boolean selected) {
		Point p = getScreenPoint(0, y, null);
		if (p != null) {
			p.x = xPos;
			ModelGraphics.drawHandle(g, p, selected);
		}
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 6;
	
	private int hitOffset;
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (drawHandles) {
			Point hitPos = translateFromScreen(x, y, null);
			InvNormalQVariable meanExtreme = (InvNormalQVariable)getVariable(cumProbKey);
			
			if (hitPos.x < 0) {
				double lowExpected = meanExtreme.doubleValueAt(0);
				try {
					int targetYPos = getVertAxis().numValToPosition(lowExpected);
					if (Math.abs(hitPos.y - targetYPos) < kMinHitDistance)
						return new VertDragPosInfo(hitPos.y, LOW_SELECTION, targetYPos - hitPos.y);
				} catch (AxisException e) {
				}
			}
			else if (hitPos.x > (getTimeAxis().getAxisLength() - 1)) {
				double highExpected = meanExtreme.doubleValueAt(meanExtreme.noOfValues() - 1);
				try {
					int targetYPos = getVertAxis().numValToPosition(highExpected);
					if (Math.abs(hitPos.y - targetYPos) < kMinHitDistance)
						return new VertDragPosInfo(hitPos.y, HIGH_SELECTION, targetYPos - hitPos.y);
				} catch (AxisException e) {
				}
			}
		}
		
		return super.getPosition(x, y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (selection == NO_SELECTION)
			return super.getPosition(x, y);
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= getVertAxis().getAxisLength())
			return new VertDragPosInfo(-hitOffset + getVertAxis().getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null && startPos instanceof VertDragPosInfo) {
			VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
			hitOffset = dragPos.hitOffset;
			selection = dragPos.index;
			repaint();
			return true;
		}
		else
			return super.startDrag(startPos);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (selection == NO_SELECTION)
			super.doDrag(fromPos, toPos);
		else {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newHandleValue = getVertAxis().positionToNumVal(newYPos);
				
				InvNormalQVariable meanExtreme = (InvNormalQVariable)getVariable(cumProbKey);
				NormalDistnVariable popnDistn = (NormalDistnVariable)getVariable(popnDistnKey);
				
				double lowCum = meanExtreme.getCumulative(InvNormalQVariable.LOW_EXTREME);
				double lowStd = NormalTable.quantile(lowCum);
				double lowExpected = (selection == LOW_SELECTION) ? newHandleValue
																				: popnDistn.getQuantile(lowCum);
				double highCum = meanExtreme.getCumulative(InvNormalQVariable.HIGH_EXTREME);
				double highStd = NormalTable.quantile(highCum);
				double highExpected = (selection == HIGH_SELECTION) ? newHandleValue
																				: popnDistn.getQuantile(highCum);
				
				double sd = (highExpected - lowExpected) / (highStd - lowStd);
				double mean = lowExpected - lowStd * sd;
				popnDistn.setSD(sd);
				popnDistn.setMean(mean);
				
				getData().variableChanged(popnDistnKey);
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selection = NO_SELECTION;
		repaint();
	}
}
	
