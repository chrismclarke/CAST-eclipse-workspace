package statistic;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;


public class DragCrossView extends DataView implements DragViewInterface {
//	static final public String DRAG_CROSS = "dragCrossView";
	static final public int ABS_DEVN = 0;
	static final public int SQR_DEVN = 1;
	
	static final private int VALUE_DRAG = 0;
	static final private int TARGET_DRAG = 1;
	
	static final private int kMinWidth = 30;
	static final private int kTargetBorder = 3;
	static final private int kArrowHead = 5;
	static final private int kMaxLineSpacing = 15;
	static final private int kSquareBottom = 8;
	
	static final private Color kTargetColor = new Color(0x006600);	//	dark green
	static final private Color kRmseColor = Color.red;
	
	private HorizAxis axis;
	private int devnType;
	private int dragType;
	private double fixedTarget;
	private LabelValue targetLabel;
	private NumValue rmseValue;
	
	private boolean keepTargetToMean = false;
	
	private boolean initialised = false;
	private int targetBaseline, targetArrowStart;
	private int vertPos[];
	
	private boolean popNotSamp = true;
	
	public DragCrossView(DataSet theData, XApplet applet, HorizAxis axis, int devnType,
														double fixedTarget, String targetString, int summaryDecimals) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.axis = axis;
		this.devnType = devnType;
		this.fixedTarget = fixedTarget;
		rmseValue = new NumValue(0.0, summaryDecimals);
		targetLabel = new LabelValue(targetString);
		
		dragType = (axis instanceof DragValAxis) ? TARGET_DRAG : VALUE_DRAG;
	}
	
	public void setKeepTargetToMean(boolean keepTargetToMean) {
		this.keepTargetToMean = keepTargetToMean;
		if (keepTargetToMean)
			setTargetToMean();
	}
	
	public void setDevnType(int devnType) {
		this.devnType = devnType;
		repaint();
	}
	
	public void setPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
	}
	
	public double getTarget() {
		return (dragType == VALUE_DRAG) ? fixedTarget
															: ((DragValAxis)axis).getAxisVal().toDouble();
	}
	
	public int minDisplayWidth() {
		return kMinWidth;
	}
	
	public boolean getDoingDrag() {
		return (dragType == TARGET_DRAG) && doingDrag;
	}
	
	protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		
		NumVariable variable = getNumVariable();
		NumValue[] sortedData = variable.getSortedData();
		int[] sortedIndex = variable.getSortedIndex();
		
		int lowCount = 0;
		for (int i=0 ; i<sortedData.length ; i++)
			if (sortedData[i].toDouble() <= fixedTarget)
				lowCount++;
		
		vertPos = new int[sortedData.length];
		for (int i=0 ; i<lowCount ; i++)
			vertPos[sortedIndex[lowCount - i - 1]] = 2 * (i + 1);
		for (int i=0 ; i<sortedData.length-lowCount ; i++)
			vertPos[sortedIndex[lowCount + i]] = 2 * i + 1;
		
		FontMetrics fm = g.getFontMetrics();
		targetBaseline = kTargetBorder + fm.getAscent();
		targetArrowStart = targetBaseline + fm.getDescent() + kTargetBorder;
		
		initialised = true;
		return true;
	}
	
	private void drawTarget(Graphics g, int targetHoriz) {
		if (dragType == VALUE_DRAG) {
			g.setColor(kTargetColor);
			targetLabel.drawCentred(g, targetHoriz, targetBaseline);
			g.drawLine(targetHoriz, targetArrowStart, targetHoriz, getSize().height - 1);
			g.drawLine(targetHoriz, getSize().height - 1, targetHoriz - kArrowHead, getSize().height - 1 - kArrowHead);
			g.drawLine(targetHoriz, getSize().height - 1, targetHoriz + kArrowHead, getSize().height - 1 - kArrowHead);
		}
		else {
			if (doingDrag) {
				g.setColor(Color.yellow);
				g.fillRect(targetHoriz - 2, 0, 5, getSize().height);
			}
			g.setColor(kTargetColor);
			g.drawLine(targetHoriz, 0, targetHoriz, getSize().height - 1);
		}
		g.setColor(getForeground());
	}
	
	private Point drawAbsBackground(Graphics g, double value, int vertPos,
																int targetHoriz, boolean selected, Point p) {
		int horizPos = axis.numValToRawPosition(value);
		p = translateToScreen(horizPos, vertPos, p);
		if (selected) {
			g.setColor(Color.yellow);
			g.fillRect(p.x - 1, p.y, 3, getSize().height - p.y);
		}
		g.setColor(Color.lightGray);
		g.drawLine(p.x, p.y, p.x, getSize().height);
		g.setColor(Color.blue);
		g.drawLine(p.x, p.y, targetHoriz, p.y);
		g.drawLine(p.x, p.y + 1, targetHoriz, p.y + 1);
		return p;
	}
	
	private Point drawSqrBackground(Graphics g, double value, int shading,
																int targetHoriz, boolean selected, Point p) {
		g.setColor(new Color(shading, shading, 255));
		int horizPos = axis.numValToRawPosition(value);
		p = translateToScreen(horizPos, kSquareBottom, p);
		int vertPos = targetHoriz - p.x;
		
		if (vertPos > 0)
			g.fillRect(p.x, p.y - vertPos, vertPos, vertPos);
		else
			g.fillRect(targetHoriz, p.y + vertPos, -vertPos, -vertPos);
		
		if (selected) {
			g.setColor(Color.yellow);
			g.fillRect(p.x - 1, 0, 3, getSize().height);
		}
		
		g.setColor(Color.blue);
		if (vertPos > 0)
			g.drawRect(p.x, p.y - vertPos, vertPos, vertPos);
		else
			g.drawRect(targetHoriz, p.y + vertPos, -vertPos, -vertPos);
		return p;
	}
	
	private Point drawSdSqr(Graphics g, double rmse, double target, int targetHoriz, Point p) {
		g.setColor(kRmseColor);
		boolean drawOnLeft = targetHoriz > (getSize().width - getViewBorder().left - getViewBorder().right) / 2;
		
		
		int rmsePos = axis.numValToRawPosition(target - (drawOnLeft ? 1.0 : -1.0) * rmse);
		p = translateToScreen(rmsePos, kSquareBottom, p);
		int dist = targetHoriz - p.x;
		
		int left = (dist > 0) ? p.x : targetHoriz;
		if (dist < 0)
			dist = -dist;
		
		g.drawRect(left, p.y - dist, dist, dist);
		g.drawRect(left + 1, p.y - dist + 1, dist - 2, dist - 2);
		
		int arrowVert = p.y - dist - 6;
		g.drawLine(left, arrowVert, left + dist, arrowVert);
		if (dist > 6) {
			g.drawLine(left + 1, arrowVert - 1, left + dist - 1, arrowVert - 1);
			g.drawLine(left + 1, arrowVert + 1, left + dist - 1, arrowVert + 1);
		}
		if (dist > 2) {
			g.drawLine(left + 1, arrowVert - 1, left + 1, arrowVert + 1);
			g.drawLine(left + dist - 1, arrowVert - 1, left + dist - 1, arrowVert + 1);
		}
		if (dist > 8) {
			g.drawLine(left + 2, arrowVert - 2, left + 2, arrowVert + 2);
			g.drawLine(left + dist - 2, arrowVert - 2, left + dist - 2, arrowVert + 2);
		}
		if (dist > 12) {
			g.drawLine(left + 3, arrowVert - 3, left + 3, arrowVert + 3);
			g.drawLine(left + dist - 3, arrowVert - 3, left + dist - 3, arrowVert + 3);
		}
		if (dist > 16) {
			g.drawLine(left + 4, arrowVert - 4, left + 4, arrowVert + 4);
			g.drawLine(left + dist - 4, arrowVert - 4, left + dist - 4, arrowVert + 4);
		}
		
		rmseValue.setValue(rmse);
		int valueWidth = rmseValue.stringWidth(g);
		
		if (dist > valueWidth + 12)
			rmseValue.drawCentred(g, left + dist / 2, arrowVert - 4);
		else if (left == targetHoriz)
			rmseValue.drawRight(g, left + 5, arrowVert - 6);
		else
			rmseValue.drawLeft(g, targetHoriz - 5, arrowVert - 6);
		return p;
	}
	
	private int colorShade(int order, int max) {
		return (max - order) * 200 / max;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		NumVariable variable = getNumVariable();
		int nValues = variable.noOfValues();
		Point thePoint = null;
		
		double target = getTarget();
		int targetHoriz = axis.numValToRawPosition(target);
		thePoint = translateToScreen(targetHoriz, 0, thePoint);
		targetHoriz = thePoint.x;
		
		int lineSpacing = Math.min(kMaxLineSpacing,
												(getSize().height - targetArrowStart) / (2 * nValues));
		
		if (devnType == ABS_DEVN) {
			ValueEnumeration e = variable.values();
			int index = 0;
			while (e.hasMoreValues()) {
				double nextDouble = e.nextDouble();
				boolean nextSel = (index == selectedIndex);
				drawAbsBackground(g, nextDouble, vertPos[index] * lineSpacing, targetHoriz, nextSel, thePoint);
				index ++;
			}
			drawTarget(g, targetHoriz);
		}
		else {
			NumValue[] sortedData = variable.getSortedData();
			int[] sortedIndex = variable.getSortedIndex();
			int lowCount = 0;
			double see = 0.0;
			for (int i=0 ; i<sortedData.length ; i++) {
				double e = (sortedData[i].toDouble() - target);
				see += e * e;
				if (e <= 0)
					lowCount++;
			}
			double rmse = Math.sqrt(see
									/ (popNotSamp ? sortedData.length : (sortedData.length - 1)));
			for (int i=0 ; i<lowCount ; i++)
				drawSqrBackground(g, sortedData[i].toDouble(), colorShade(i, lowCount),
															targetHoriz, (sortedIndex[i] == selectedIndex), thePoint);
			for (int i=0 ; i<nValues - lowCount ; i++)
				drawSqrBackground(g, sortedData[nValues - i - 1].toDouble(), colorShade(i, nValues - lowCount),
											targetHoriz, (sortedIndex[nValues - i - 1] == selectedIndex), thePoint);
			
			drawTarget(g, targetHoriz);
			drawSdSqr(g, rmse, target, targetHoriz, thePoint);
		}
		
		
		g.setColor(getForeground());
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextDouble = e.nextDouble();
			int horizPos = axis.numValToRawPosition(nextDouble);
			thePoint = translateToScreen(horizPos, 0, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
	}
	
	private void setTargetToMean() {
		NumVariable variable = getNumVariable();
		int nValues = variable.noOfValues();
		
		ValueEnumeration e = variable.values();
		double sx = 0.0;
		while (e.hasMoreValues())
			sx += e.nextDouble();
		
		fixedTarget = sx / nValues;
	}

//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 25;
	
	private boolean doingDrag = false;
	private int selectedIndex = -1;
	private int hitOffset;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		
		if (dragType == VALUE_DRAG) {
			NumVariable variable = getNumVariable();
//			int noOfVals = variable.noOfValues();
//			Point p = null;
			
			ValueEnumeration e = variable.values();
			int index = 0;
			while (e.hasMoreValues()) {
				double nextDouble = e.nextDouble();
				int horizPos = axis.numValToRawPosition(nextDouble);
				int xDist = horizPos - hitPos.x;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = index;
					minDist = xDist;
				}
				else if (xDist * xDist < minDist * minDist) {
					minIndex = index;
					minDist = xDist;
				}
				index ++;
			}
		}
		else {
			try {
				DragValAxis theAxis = (DragValAxis)axis;
				int valPos = theAxis.numValToPosition(theAxis.getAxisVal().toDouble());
				
				minDist = valPos - hitPos.x;
				gotPoint = true;
			} catch (AxisException e) {			//		gotPoint is still false;
			} catch (ClassCastException e) {
			}
		}
		
		if (gotPoint && minDist * minDist < kMinHitDist)
			return new HorizDragPosInfo(x, minIndex, -minDist);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x - hitOffset >= 0 && hitPos.x - hitOffset < axis.getAxisLength())
			return new HorizDragPosInfo(hitPos.x);
		else
			return null;
	}
	
	private void redrawAll() {
		repaint();
		if (dragType == TARGET_DRAG)
			axis.repaint();
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null && startPos instanceof HorizDragPosInfo) {
			HorizDragPosInfo posInfo = (HorizDragPosInfo)startPos;
			doingDrag = true;
			selectedIndex = posInfo.index;
			hitOffset = posInfo.hitOffset;
			redrawAll();
			return true;
		}
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else {
			doingDrag = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			
			int newAxisPos = dragPos.x - hitOffset;
			try {
				if (dragType == VALUE_DRAG) {
					double newVal = axis.positionToNumVal(newAxisPos);
					NumVariable y = getData().getNumVariable();
					((NumValue)y.valueAt(selectedIndex)).setValue(newVal);
					y.clearSortedValues();
					if (keepTargetToMean)
						setTargetToMean();
					getData().variableChanged(getActiveNumKey());
				}
				else {
					DragValAxis theAxis = (DragValAxis)axis;
					theAxis.setAxisValPos(newAxisPos);
//					redrawAll();
				}
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		selectedIndex = -1;
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
	
