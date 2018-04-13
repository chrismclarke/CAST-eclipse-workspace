package regnView;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;


public class DragMedianTraceView extends CoreMedianTraceView {
	static final private int NO_SELECTED_BOUNDARY = -1;
	private int selectedBoundary = NO_SELECTED_BOUNDARY;
	
		public DragMedianTraceView(DataSet theData, XApplet applet,
									HorizAxis xAxis, VertAxis yAxis, String xKey, String yKey, 
									double[] initialBoundaries) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, initialBoundaries);
	}
	
	public DragMedianTraceView(DataSet theData, XApplet applet, HorizAxis xAxis,
								VertAxis yAxis, String xKey, String yKey, String initialBoundaries) {
		super(theData, applet, xAxis, yAxis, xKey, yKey, initialBoundaries);
	}
	
	protected int createBoundary(boolean highNotLow) {
		double oldBoundaries[] = boundary;
		boundary = new double[oldBoundaries.length + 1];
		if (highNotLow) {
			for (int i=0 ; i<oldBoundaries.length ; i++)
				boundary[i] = oldBoundaries[i];
			boundary[oldBoundaries.length] = xAxis.maxOnAxis;
			return oldBoundaries.length;
		}
		else {
			for (int i=0 ; i<oldBoundaries.length ; i++)
				boundary[i+1] = oldBoundaries[i];
			boundary[0] = xAxis.minOnAxis;
			return 0;
		}
	}
	
	protected void tidyBoundaries() {	//		combine to delete empty classes
		NumVariable xVar = (NumVariable)getVariable(xKey);
		NumValue x[] = xVar.getSortedData();
		int xIndex = 0;
		int currentLength = boundary.length;
		int boundaryIndex = 0;
		while (boundaryIndex < currentLength) {
			if (xIndex >= x.length || x[xIndex].toDouble() > boundary[boundaryIndex]) {
				currentLength --;
				for (int i=boundaryIndex ; i<currentLength ; i++)
					boundary[i] = boundary[i+1];
			}
			while (xIndex < x.length && x[xIndex].toDouble() <= boundary[boundaryIndex])
				xIndex ++;
			boundaryIndex ++;
		}
		if (xIndex >= x.length)		//	none in final class
			currentLength --;
		
		if (currentLength != boundary.length) {
			double oldBoundaries[] = boundary;
			boundary = new double[currentLength];
			for (int i=0 ; i<boundary.length ; i++)
				boundary[i] = oldBoundaries[i];
		}
	}
	
	protected int changeBoundary(int boundaryIndex, double newBoundary) {		// returns new index
		boundary[boundaryIndex] = newBoundary;
		int newIndex = boundaryIndex;
		while (newIndex > 0 && newBoundary < boundary[newIndex - 1]) {
			boundary[newIndex] = boundary[newIndex - 1];
			boundary[newIndex - 1] = newBoundary;
			newIndex --;
		}
		while (newIndex < boundary.length - 1 && newBoundary >= boundary[newIndex + 1]) {
			boundary[newIndex] = boundary[newIndex + 1];
			boundary[newIndex + 1] = newBoundary;
			newIndex ++;
		}
		return newIndex;
	}
	
	protected boolean canDrawX(double x) {
		return true;
	}
	
	private void drawHandleForNew(Graphics g, int leftEdge) {
		g.setColor(Color.gray);
		g.fillOval(leftEdge, 2, 12, 12);
		g.setColor(Color.white);
		g.drawLine(leftEdge + 2, 2+5, leftEdge + 9, 2+5);
		g.drawLine(leftEdge + 2, 2+6, leftEdge + 9, 2+6);
		g.drawLine(leftEdge + 5, 2+2, leftEdge + 5, 2+9);
		g.drawLine(leftEdge + 6, 2+2, leftEdge + 6, 2+9);
	}
	
	protected void drawBackground(Graphics g) {
		double xMed[] = getXMedians();
		double yMed[] = getYMedians();
		Point p = null;
		
		if (selectedBoundary != NO_SELECTED_BOUNDARY) {
			g.setColor(Color.yellow);
			try {
				int horizPos = xAxis.numValToPosition(boundary[selectedBoundary]);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(p.x - 2, 0, 5, getSize().height);
			} catch (AxisException e) {
			}
		}
		
		if (boundary.length < kMaxBoundaries) {
			drawHandleForNew(g, 2);
			drawHandleForNew(g, getSize().width - 14);
		}
		
		g.setColor(Color.gray);
		for (int i=0 ; i<boundary.length ; i++)
			try {
				int horizPos = xAxis.numValToPosition(boundary[i]);
				p = translateToScreen(horizPos, 0, p);
				g.drawLine(p.x, 0, p.x, getSize().height - 1);
			} catch (AxisException e) {
			}
		
		drawMedianTrace(g, xMed, yMed);
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	private Point crossPos[];
	private static final int kMinPointHitDist = 9;
	private static final int kMinBoundaryHitDist = 4;
	private static final int kMaxBoundaries = 8;
	private int hitOffset;
	
	private PositionInfo getCrossPosition(int x, int y) {
		if (crossPos == null) {
			NumVariable xVariable = (NumVariable)getVariable(xKey);
			NumVariable yVariable = (NumVariable)getVariable(yKey);
			int noOfVals = xVariable.noOfValues();
			crossPos = new Point[noOfVals];
			for (int i=0 ; i<noOfVals ; i++)
				crossPos[i] = getScreenPoint((NumValue)(xVariable.valueAt(i)),
														(NumValue)(yVariable.valueAt(i)), null);
		}
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
			if (crossPos[i] != null) {
				int xDist = crossPos[i].x - x;
				int yDist = crossPos[i].y - y;
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
			}
		if (gotPoint && minDist < kMinPointHitDist)
			return new IndexPosInfo(minIndex);
		else
			return null;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		PositionInfo crossPosInfo = getCrossPosition(x, y);
		if (crossPosInfo != null)
			return crossPosInfo;
		else {
			int axisXPos = translateFromScreen(x, y, null).x;
			for (int i=0 ; i<boundary.length ; i++)
				try {
					int boundaryPos = xAxis.numValToPosition(boundary[i]);
					if (Math.abs(boundaryPos - axisXPos) <= kMinBoundaryHitDist)
						return new HorizDragPosInfo(x, i, axisXPos - boundaryPos);
				} catch (AxisException e) {
					return null;
				}
		}
		if (boundary.length < kMaxBoundaries && y < 14) {
			if (x < 14)
				return new HorizDragPosInfo(x, -1, x - 9);
			else if (x > getSize().width - 14)
				return new HorizDragPosInfo(x, boundary.length, x - getSize().width + 9);
		}
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (selectedBoundary == NO_SELECTED_BOUNDARY)		//	brushing cross
			return getCrossPosition(x, y);
		else if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		else {
			Point hitPos = translateFromScreen(x, y, null);
			if (hitPos.x - hitOffset < 0)
				return new HorizDragPosInfo(hitOffset);
			else if (hitPos.x - hitOffset >= xAxis.getAxisLength())
				return new HorizDragPosInfo(hitOffset + xAxis.getAxisLength() - 1);
			else
				return new HorizDragPosInfo(hitPos.x);
		}
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos instanceof HorizDragPosInfo) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)startPos;
			hitOffset = dragPos.hitOffset;
			selectedBoundary = dragPos.index;
			if (selectedBoundary == -1)
				selectedBoundary = createBoundary(false);
			else if (selectedBoundary == boundary.length)
				selectedBoundary = createBoundary(true);
			
			repaint();
			return true;
		}
		else
			return super.startDrag(startPos);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (selectedBoundary != NO_SELECTED_BOUNDARY) {
			if (toPos == null)
				return;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			int newAxisPos = dragPos.x - hitOffset;
			try {
				double newBoundary = xAxis.positionToNumVal(newAxisPos);
				selectedBoundary = changeBoundary(selectedBoundary, newBoundary);
				repaint();
			} catch (AxisException e) {
			}
		}
		else
			super.doDrag(fromPos, toPos);
	}
	
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (selectedBoundary != NO_SELECTED_BOUNDARY) {
			selectedBoundary = NO_SELECTED_BOUNDARY;
			tidyBoundaries();
			repaint();
		}
		else
			super.endDrag(startPos, endPos);
		crossPos = null;
	}
	
	
	public void mouseExited(MouseEvent e) {
		if (doingDrag)
			mouseReleased(e);
	}
	
/*
	public boolean mouseExit(Event evt, int x, int y) {
		if (!doingDrag)
			return true;
		else
			return mouseUp(evt, -999, -999);		//		We must tidy up unnecessary categories at end
	}
*/
	
}
	
