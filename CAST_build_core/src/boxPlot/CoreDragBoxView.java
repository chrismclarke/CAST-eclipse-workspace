package boxPlot;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


abstract public class CoreDragBoxView extends BoxAndDotView {
//	static public final String CORE_DRAG_BOX_PLOT = "coreDragBoxPlot";
	
	static private final int kQuartileHitSlop = 4;
	
	private int selectedQuartile = NO_SELECTED_QUART;
	private int hitOffset;
	
	public CoreDragBoxView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis);
	}
	
	abstract protected int getDragTop();
	
	protected void initialiseBox(NumValue sortedVal[], BoxInfo boxInfo) {
		double axisRange = axis.maxOnAxis - axis.minOnAxis;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			boxInfo.boxPos[i] = getAxisPos(axis.minOnAxis + axisRange * (2 * i + 1) * 0.1);
	}
	
	protected void shadeBackground(Graphics g) {
		Color oldColor = g.getColor();
		g.setColor(Color.yellow);
		Point p = null;
		if (selectedQuartile != NO_SELECTED_QUART) {
			p = translateToScreen(boxInfo.boxPos[selectedQuartile], 0, p);
			g.fillRect(p.x - 2, getSize().height - getDragTop(), 5, getDragTop());
		}
		
		g.setColor(Color.lightGray);
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			p = translateToScreen(boxInfo.boxPos[i], 0, p);
			g.drawLine(p.x, getSize().height - getDragTop(), p.x, getSize().height);
		}
		
		g.setColor(oldColor);
	}
	
	protected void notifyStartDrag() {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y >= getDragTop())
			return null;
		int hitIndex = NO_SELECTED_QUART;
		int hitOffset = kQuartileHitSlop;
		int absOffset = kQuartileHitSlop;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++) {
			int thisHitOffset = hitPos.x - boxInfo.boxPos[i];
			int thisAbsOffset = Math.abs(thisHitOffset);
			if (thisAbsOffset < absOffset) {
				hitIndex = i;
				hitOffset = thisHitOffset;
				absOffset = thisAbsOffset;
			}
		}
		if (hitIndex == NO_SELECTED_QUART)
			return null;
		else
			return new HorizDragPosInfo(hitPos.x, hitIndex, hitOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y >= getDragTop() || hitPos.x - hitOffset < 0
														|| hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null) {
			if (selectedQuartile != NO_SELECTED_QUART) {
				selectedQuartile = NO_SELECTED_QUART;
				repaint();
			}
			return false;
		}
		else {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
			hitOffset = dragPos.hitOffset;
			selectedQuartile = dragPos.index;
			repaint();
			notifyStartDrag();
			return true;
		}
	}
	
	private void setSelectedQuartile(int newPos) {
		if (newPos < 0 || newPos >= axis.getAxisLength())
			return;
		int newRank = 0;
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			if (i != selectedQuartile && newPos > boxInfo.boxPos[i])
				newRank++;
		if (newRank > selectedQuartile) {
			for (int i=selectedQuartile ; i<newRank ; i++)
				boxInfo.boxPos[i] = boxInfo.boxPos[i+1];
			selectedQuartile = newRank;
		}
		else if (newRank < selectedQuartile) {
			for (int i=selectedQuartile ; i>newRank ; i--)
				boxInfo.boxPos[i] = boxInfo.boxPos[i-1];
			selectedQuartile = newRank;
		}
		
		boxInfo.boxPos[selectedQuartile] = newPos;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			setSelectedQuartile(dragPos.x - hitOffset);
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
	

//-----------------------------------------------------------------------------------
	
	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_LEFT && selectedQuartile != NO_SELECTED_QUART) {
			setSelectedQuartile(boxInfo.boxPos[selectedQuartile] - 1);
			repaint();
		}
		else if (key == KeyEvent.VK_RIGHT && selectedQuartile != NO_SELECTED_QUART) {
			setSelectedQuartile(boxInfo.boxPos[selectedQuartile] + 1);
			repaint();
		}
	}
	
	public void focusLost(FocusEvent e) {
		if (selectedQuartile != NO_SELECTED_QUART) {
			selectedQuartile = NO_SELECTED_QUART;
			repaint();
		}
	}

}
	
