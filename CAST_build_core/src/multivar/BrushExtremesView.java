package multivar;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class BrushExtremesView extends ScatterView {
	static final private int outerArrowX[] = {-3, -3, -6,  0, 6, 3,  3, -3};
	static final private int outerArrowY[] = { 0,  4,  4, 10, 4, 4,  0,  0};
	static final private int innerArrowX[] = {-1, -1, -3, 0, 3, 1,  1, -1};
	static final private int innerArrowY[] = { 0,  5,  5, 8, 5, 5,  0,  0};
	
	static final private int NO_DRAG = 0;
	static final private int DRAG_TOP = 1;
	static final private int DRAG_BOTTOM = 2;
	
	private int dragType = NO_DRAG;
	private int hitDragType = NO_DRAG;
	
	private int topDragPos;
	private int bottomDragPos;
	
	public BrushExtremesView(DataSet theData, XApplet applet, HorizAxis xAxis,
																								VertAxis yAxis, String xKey, String yKey) {
		super(theData, applet, xAxis, yAxis, xKey, yKey);
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		g.setColor(getForeground());
		
		super.paintView(g);
	}
	
	private void drawArrow(Graphics g, int[] ax, int[] ay, int arrowXStart, int arrowYStart,
										Color outerColor, Color innerColor, int direction) {
		for (int i=0 ; i<ax.length ; i++) {
			ax[i] = outerArrowX[i] + arrowXStart;
			ay[i] = outerArrowY[i] * direction + arrowYStart;
		}
		g.setColor(outerColor);
		g.fillPolygon(ax, ay, ax.length);
		g.drawPolygon(ax, ay, ax.length);
		
		for (int i=0 ; i<ax.length ; i++) {
			ax[i] = innerArrowX[i] + arrowXStart;
			ay[i] = innerArrowY[i] * direction + arrowYStart;
		}
		g.setColor(innerColor);
		g.fillPolygon(ax, ay, ax.length);
		g.drawPolygon(ax, ay, ax.length);
	}
	
	private void drawBackground(Graphics g) {
		int arrowCentre = (getSize().width + getViewBorder().left - getViewBorder().right) / 2;
		int ax[] = new int[outerArrowX.length];
		int ay[] = new int[outerArrowX.length];
		
		if (dragType == DRAG_TOP) {
			int shadeHeight = getSize().height - getViewBorder().bottom - topDragPos;
			g.setColor(Color.yellow);
			g.fillRect(0, 0, getSize().width - 1, shadeHeight);
			g.setColor(Color.gray);
			g.drawLine(0, shadeHeight, getSize().width - 1, shadeHeight);
			
			drawArrow(g, ax, ay, arrowCentre, shadeHeight, Color.black, Color.red, 1);
		}
		else {
			int y = getViewBorder().top - 1;
			g.setColor(Color.gray);
			g.drawLine(0, y, getSize().width - 1, y);
			
			drawArrow(g, ax, ay, arrowCentre, y, Color.red, Color.white, 1);
		}
		
		if (dragType == DRAG_BOTTOM) {
			int shadeHeight = getSize().height - getViewBorder().bottom - bottomDragPos;
			g.setColor(Color.yellow);
			g.fillRect(0, shadeHeight, getSize().width - 1, getSize().height - shadeHeight);
			g.setColor(Color.gray);
			g.drawLine(0, shadeHeight, getSize().width - 1, shadeHeight);
			
			drawArrow(g, ax, ay, arrowCentre, shadeHeight, Color.black, Color.red, -1);
		}
		else {
			int y = getSize().height - getViewBorder().bottom;
			g.setColor(Color.gray);
			g.drawLine(0, y, getSize().width - 1, y);
			
			drawArrow(g, ax, ay, arrowCentre, y, Color.red, Color.white, -1);
		}
	}

//-----------------------------------------------------------------------------------

	static final private int kMinHitDistance = 10;
	
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		if (hitDragType == NO_DRAG)
			return super.getPosition(x, y);
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.y + hitOffset < 0)
			return new VertDragPosInfo(-hitOffset);
		else if (hitPos.y + hitOffset >= yAxis.getAxisLength())
			return new VertDragPosInfo(-hitOffset + yAxis.getAxisLength() - 1);
		else
			return new VertDragPosInfo(hitPos.y);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int xArrowPos = (getSize().width - getViewBorder().left - getViewBorder().right) / 2;
		int yTopArrowPos = getSize().height - getViewBorder().top - getViewBorder().bottom;
		int yBottomArrowPos = 0;
		
		if (Math.abs(xArrowPos - hitPos.x) + Math.abs(yTopArrowPos - hitPos.y) < kMinHitDistance)
			return new VertDragPosInfo(hitPos.y, DRAG_TOP, hitPos.y - yTopArrowPos);
		if (Math.abs(xArrowPos - hitPos.x) + Math.abs(yBottomArrowPos - hitPos.y) < kMinHitDistance)
			return new VertDragPosInfo(hitPos.y, DRAG_BOTTOM, hitPos.y - yBottomArrowPos);
		return super.getInitialPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos instanceof VertDragPosInfo) {
			VertDragPosInfo dragPos = (VertDragPosInfo)startPos;
			hitOffset = dragPos.hitOffset;
			hitDragType = dragPos.index;
			dragType = hitDragType;
			topDragPos = getSize().height - getViewBorder().top - getViewBorder().bottom;
			bottomDragPos = 0;
			if (!getData().clearSelection())
				repaint();
			return true;
		}
		else
			return super.startDrag(startPos);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (hitDragType == NO_DRAG)
			super.doDrag(fromPos, toPos);
		else if (toPos == null) {
			dragType = NO_DRAG;
			repaint();
		}
		else {
			dragType = hitDragType;
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			int newYPos = dragPos.y + hitOffset;
			try {
				double newSelectionExtreme = yAxis.positionToNumVal(newYPos);
				switch (dragType) {
					case DRAG_TOP:
						topDragPos = newYPos;
						getData().setSelection(yKey, newSelectionExtreme, Double.POSITIVE_INFINITY);
						break;
					case DRAG_BOTTOM:
						bottomDragPos = newYPos;
						getData().setSelection(yKey, Double.NEGATIVE_INFINITY, newSelectionExtreme);
						break;
					default:
				}
				repaint();
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragType == NO_DRAG)
			super.endDrag(startPos, endPos);
		else {
			dragType = NO_DRAG;
//			getData().clearSelection();
			repaint();
		}
	}
}
	
