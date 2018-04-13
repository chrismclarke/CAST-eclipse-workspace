package time;

import java.awt.*;

import dataView.*;
import axis.*;


public class TimeDragView extends TimeView {
	static final private int kHiliteSize = 6;
	static final private int kMinHitDist = 5;
	
	private int dragIndex = -1;
	private double originalValue;
	private boolean doingDrag = false;
	private int hitOffset;
	
	public TimeDragView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	public void setDragIndex(int newDragIndex) {
		NumVariable yVar = getNumVariable();
		if (newDragIndex >= 0) {
			NumValue theVal = (NumValue)yVar.valueAt(newDragIndex);
			originalValue = theVal.toDouble();
		}
		else {
			NumValue theVal = (NumValue)yVar.valueAt(dragIndex);
			theVal.setValue(originalValue);
			getData().valueChanged(dragIndex);
		}
		dragIndex = newDragIndex;
	}
	
	protected void drawBackground(Graphics g, NumVariable variable) {
		super.drawBackground(g, variable);
		
		if (dragIndex >= 0) {
			NumValue theVal = (NumValue)variable.valueAt(dragIndex);
			Point p = getScreenPoint(dragIndex, theVal.toDouble(), null);
			
			if (doingDrag) {
				g.setColor(Color.orange);
				g.fillRect(p.x - kHiliteSize, p.y - kHiliteSize, 2 * kHiliteSize + 1, 2 * kHiliteSize + 1);
			}
			else {
				g.setColor(Color.yellow);
				g.fillRect(p.x - kHiliteSize, p.y - kHiliteSize, 2 * kHiliteSize + 1, 2 * kHiliteSize + 1);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return dragIndex >= 0;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		NumVariable yVar = getNumVariable();
		NumValue theVal = (NumValue)yVar.valueAt(dragIndex);
		Point p = getScreenPoint(dragIndex, theVal.toDouble(), null);
		
		int dx = (x - p.x);
		int dy = (y - p.y);
		
		if (dx * dx + dy * dy < kMinHitDist * kMinHitDist)
			return new VertDragPosInfo(y, dragIndex, dy);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		return new VertDragPosInfo(hitPos.y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		doingDrag = true;
		VertDragPosInfo startPos = (VertDragPosInfo)startInfo;
		hitOffset = startPos.hitOffset;
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
		int newYPos = dragPos.y + hitOffset;
		
		try {
			double newY = getVertAxis().positionToNumVal(newYPos);
			
			NumVariable yVar = getNumVariable();
			NumValue theVal = (NumValue)yVar.valueAt(dragIndex);
			theVal.setValue(newY);
			
			getData().valueChanged(dragIndex);
			repaint();
		} catch (AxisException e) {
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}

}
	
