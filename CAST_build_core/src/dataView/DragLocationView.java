package dataView;

import java.awt.*;
import java.awt.event.*;

import axis.*;


abstract public class DragLocationView extends DataView implements DragViewInterface {
	protected DragValAxis axis;
	static protected final Color darkGreen = new Color(0x009900);
	
	static private final int kHitSlop = 4;
	private int hitOffset;
	public boolean selectedVal = false;
	
	public DragLocationView(DataSet theData, XApplet applet, DragValAxis theAxis) {
		this(theData, applet, theAxis, new Insets(5, 5, 5, 5));
	}
	
	public DragLocationView(DataSet theData, XApplet applet, DragValAxis theAxis,
																																			Insets border) {
		super(theData, applet, border);
		axis = theAxis;
		if (theAxis != null)
			theAxis.setView(this);
	}
	
	public boolean getDoingDrag() {
		return selectedVal;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (axis != null && axis == theAxis) {
			reinitialiseAfterTransform();
			repaint();
		}
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		try {
			DragValAxis theAxis = (DragValAxis)axis;
			NumValue axisVal = theAxis.getAxisVal();
			if (axisVal == null)
				return null;
			int valPos = theAxis.numValToPosition(axisVal.toDouble());
			
			Point hitPos = translateFromScreen(x, y, null);
			int hitOffset = hitPos.x - valPos;
			if (hitOffset > kHitSlop || hitOffset < -kHitSlop)
				return null;
			else
				return new HorizDragPosInfo(hitPos.x, 0, hitOffset);
		} catch (AxisException e) {
			return null;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset < 0 || hitPos.x - hitOffset >= axis.getAxisLength())
			return null;
		else
			return new HorizDragPosInfo(hitPos.x);
	}
	
	private void redrawAll() {
		repaint();
		if (axis != null)
			axis.repaint();
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			selectedVal = true;
			hitOffset = ((HorizDragPosInfo)startPos).hitOffset;
			redrawAll();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selectedVal = false;
			redrawAll();
		}
		else {
			selectedVal = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			
			int newAxisPos = dragPos.x - hitOffset;
			try {
				DragValAxis theAxis = (DragValAxis)axis;
				theAxis.setAxisValPos(newAxisPos);
				redrawAll();
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectedVal = false;
		redrawAll();
	}
	

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		try {
			DragValAxis theAxis = (DragValAxis)axis;
			int valPos = theAxis.getAxisValPos();
			if (key == KeyEvent.VK_LEFT) {
				theAxis.setAxisValPos(valPos - 1);
				redrawAll();
			}
			else if (key == KeyEvent.VK_RIGHT) {
				theAxis.setAxisValPos(valPos + 1);
				redrawAll();
			}
		} catch (AxisException ex) {
		} catch (ClassCastException ex) {
		}
	
	}
	
}
	
