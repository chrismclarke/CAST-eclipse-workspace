package boxPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class BoxDotHiliteView extends BoxAndDotView {
//	static public final String BOX_DOT_HILITE_PLOT = "boxDotHilitePlot";
	
	static final private Color kHiliteColor = new Color(0xFFFFCC);
	
	private int hitAreaHilite = NONE;
	private int dragAreaHilite = NONE;
	
	public BoxDotHiliteView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	private int minHilite() {
		return Math.min(hitAreaHilite, dragAreaHilite);
	}
	
	private int maxHilite() {
		if (hitAreaHilite == NONE || dragAreaHilite == NONE)
			return NONE;
		return Math.max(hitAreaHilite, dragAreaHilite);
	}
	
	protected void shadeBackground(Graphics g) {
		if (hitAreaHilite != NONE && dragAreaHilite != NONE) {
			Color oldColor = g.getColor();
			g.setColor(kHiliteColor);
			
			int maxLowBorder = Math.max(getViewBorder().left, getViewBorder().bottom);
			Point p1 = translateToScreen(boxInfo.boxPos[minHilite()], -maxLowBorder, null);
			int maxWidth = Math.max(getSize().width, getSize().height);
			Point p2 = translateToScreen(boxInfo.boxPos[maxHilite() + 1], maxWidth, null);
			
			int left = Math.min(p1.x, p2.x);
			int width = Math.abs(p1.x - p2.x);
			int top = Math.min(p1.y, p2.y);
			int height = Math.abs(p1.y - p2.y);
			g.fillRect(left, top, width, height);
			
//				Point p = translateToScreen(boxInfo.boxPos[areaHighlight], 0, null);
//				g.fillRect(p.x, 0, boxInfo.boxPos[areaHighlight + 1] - boxInfo.boxPos[areaHighlight],
//																											getSize().height);
			g.setColor(oldColor);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal = 0.0;
		try {
			hitVal = axis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			if (e.axisProblem == AxisException.TOO_LOW_ERROR)
				hitVal = axis.minOnAxis;
			else if (e.axisProblem == AxisException.TOO_HIGH_ERROR)
				hitVal = axis.maxOnAxis;
		}
		if (hitVal < boxInfo.boxVal[LOW_EXT])
			return null;
		else if (hitVal < boxInfo.boxVal[LOW_QUART])
			return new IndexPosInfo(LOW_OUTER);
		else if (hitVal < boxInfo.boxVal[MEDIAN])
			return new IndexPosInfo(LOW_INNER);
		else if (hitVal < boxInfo.boxVal[HIGH_QUART])
			return new IndexPosInfo(HIGH_INNER);
		else if (hitVal < boxInfo.boxVal[HIGH_EXT])
			return new IndexPosInfo(HIGH_OUTER);
		else
			return null;
	}
	
	private boolean[] getHitIndices(int lowHighlight, int highHighlight) {
		double lowVal, highVal;
		switch (lowHighlight) {
			case LOW_OUTER:
				lowVal = boxInfo.boxVal[LOW_EXT];
				break;
			case LOW_INNER:
				lowVal = boxInfo.boxVal[LOW_QUART];
				break;
			case HIGH_INNER:
				lowVal = boxInfo.boxVal[MEDIAN];
				break;
			default:
				lowVal = boxInfo.boxVal[HIGH_QUART];
				break;
		}
		switch (highHighlight) {
			case LOW_OUTER:
				highVal = boxInfo.boxVal[LOW_QUART];
				break;
			case LOW_INNER:
				highVal = boxInfo.boxVal[MEDIAN];
				break;
			case HIGH_INNER:
				highVal = boxInfo.boxVal[HIGH_QUART];
				break;
			default:
				highVal = boxInfo.boxVal[HIGH_EXT];
				break;
		}
		
		NumVariable variable = getNumVariable();
		boolean selection[] = new boolean[variable.noOfValues()];
		ValueEnumeration e = variable.values();
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			selection[index] = (nextVal >= lowVal && nextVal <= highVal);
			index++;
		}
		return selection;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			hitAreaHilite = dragAreaHilite = NONE;
			getData().clearSelection();
		}
		else {
			hitAreaHilite = dragAreaHilite = ((IndexPosInfo)startInfo).itemIndex;
			boolean newSelection[] = getHitIndices(hitAreaHilite, hitAreaHilite);
			getData().setSelection(newSelection);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (hitAreaHilite == BoxInfo.NONE || toPos == null)
			return;
		
		dragAreaHilite = ((IndexPosInfo)toPos).itemIndex;
		boolean newSelection[] = getHitIndices(minHilite(), maxHilite());
		getData().setSelection(newSelection);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
//		startDrag(null);
	}
	
}
	
