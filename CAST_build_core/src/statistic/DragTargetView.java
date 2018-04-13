package statistic;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DragTargetView extends DataView implements DragViewInterface {
	static final private int kMinWidth = 30;
//	static final private int kTargetBorder = 3;
//	static final private int kArrowHead = 5;
//	static final private int kMaxLineSpacing = 15;
	static final private int kSquareBottom = 8;
	static final private int kNoOfShades = 50;
	
	static final private Color kTargetColor = new Color(0x006600);	//	dark green
	
	private DragValAxis axis;
	
	private Color fillColor = Color.red;
	private Color fillShade[] = new Color[kNoOfShades + 1];
	
	public DragTargetView(DataSet theData, XApplet applet, DragValAxis axis) {
		super(theData, applet, new Insets(5, 5, 5, 5));
																//		5 pixels round for crosses to overlap into
		this.axis = axis;
		AccurateDistn2Artist.setShades(fillShade, fillColor);
	}
	
	public double getTarget() {
		return axis.getAxisVal().toDouble();
	}
	
	public int minDisplayWidth() {
		return kMinWidth;
	}
	
	public boolean getDoingDrag() {
		return doingDrag;
	}
	
	private void drawTarget(Graphics g, int targetHoriz) {
		if (doingDrag) {
			g.setColor(Color.yellow);
			g.fillRect(targetHoriz - 2, 0, 5, getSize().height);
		}
		g.setColor(kTargetColor);
		g.drawLine(targetHoriz, 0, targetHoriz, getSize().height - 1);
		
		g.setColor(getForeground());
	}
	
	private Point drawSqrBackground(Graphics g, double value, int shading,
																int targetHoriz, Point p) {
		g.setColor(new Color(shading, shading, 255));
		int horizPos = axis.numValToRawPosition(value);
		p = translateToScreen(horizPos, kSquareBottom, p);
		int vertPos = targetHoriz - p.x;
		
		if (vertPos > 0)
			g.fillRect(p.x, p.y - vertPos, vertPos, vertPos);
		else
			g.fillRect(targetHoriz, p.y + vertPos, -vertPos, -vertPos);
		
		g.setColor(Color.blue);
		if (vertPos > 0)
			g.drawRect(p.x, p.y - vertPos, vertPos, vertPos);
		else
			g.drawRect(targetHoriz, p.y + vertPos, -vertPos, -vertPos);
		return p;
	}
	
	private int colorShade(int order, int max) {
		return (max - order) * 200 / max;
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		int nValues = variable.noOfValues();
		Point thePoint = null;
		
		double target = getTarget();
		int targetHoriz = axis.numValToRawPosition(target);
		thePoint = translateToScreen(targetHoriz, 0, thePoint);
		targetHoriz = thePoint.x;
		
		NumValue[] sortedData = variable.getSortedData();
//		int[] sortedIndex = variable.getSortedIndex();
		int lowCount = 0;
		double sx = 0.0;
		for (int i=0 ; i<sortedData.length ; i++) {
			double x = sortedData[i].toDouble();
			if (x <= target)
				lowCount++;
			sx += x;
		}
		for (int i=0 ; i<lowCount ; i++)
			drawSqrBackground(g, sortedData[i].toDouble(), colorShade(i, lowCount),
																										targetHoriz, thePoint);
		for (int i=0 ; i<nValues - lowCount ; i++)
			drawSqrBackground(g, sortedData[nValues - i - 1].toDouble(), colorShade(i, nValues - lowCount),
																										targetHoriz, thePoint);
		
		drawTarget(g, targetHoriz);
		
		g.setColor(getForeground());
		ValueEnumeration e = variable.values();
		while (e.hasMoreValues()) {
			double nextDouble = e.nextDouble();
			int horizPos = axis.numValToRawPosition(nextDouble);
			thePoint = translateToScreen(horizPos, 0, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
		}
		
		int n = sortedData.length;
		double meanX = sx / n;
		drawSsqGraph(g, meanX, targetHoriz);
	}
	
	private void setFillShade(double p, Graphics g) {
		int shadeInt = (int)Math.round(kNoOfShades * p);
		g.setColor(fillShade[shadeInt]);
	}
	
	private void drawSsqGraph(Graphics g, double meanX, int targetHoriz) {
		double minX = axis.minOnAxis;
		double maxX = axis.maxOnAxis;
		double maxDiff = Math.max(meanX - minX, maxX - meanX);
		
		int noOfClasses = axis.getAxisLength();
		int pixHt = getSize().height - getViewBorder().top - getViewBorder().bottom;
		double baselinePix = pixHt * 0.3;
		double graphHt = pixHt - baselinePix;
//		int horizOffset = getViewBorder().left;
		
		g.setColor(Color.red);
		Point p = new Point(0,0);
		for (int i=0 ; i<noOfClasses ; i++)
			try {
				double x = axis.positionToNumVal(i);
				double z = (x - meanX) / maxDiff;
				double htPix = baselinePix + z * z * graphHt;
				double htFloor = Math.floor(htPix);
				int ht = (int)Math.round(htFloor);
				p = translateToScreen(i, ht, p);
				
				if (p.x != targetHoriz) {
					double shadePropn = htPix - htFloor;
					setFillShade(shadePropn, g);
					g.fillRect(p.x, p.y - 1, 1, 1);
					
					setFillShade(1.0 - shadePropn, g);
					g.fillRect(p.x, p.y, 1, 1);
					
					g.setColor(Color.white);
					g.fillRect(p.x, p.y - 4, 1, 3);
					g.fillRect(p.x, p.y + 1, 1, 3);
				}
			} catch (AxisException e) {
			}
	}

//-----------------------------------------------------------------------------------
	
	private static final int kMinHitDist = 25;
	
	private boolean doingDrag = false;
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
		
		try {
			int valPos = axis.numValToPosition(getTarget());
			
			minDist = valPos - hitPos.x;
			gotPoint = true;
		} catch (AxisException e) {			//		gotPoint is still false;
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
		axis.repaint();
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null && startPos instanceof HorizDragPosInfo) {
			HorizDragPosInfo posInfo = (HorizDragPosInfo)startPos;
			doingDrag = true;
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
				axis.setAxisValPos(newAxisPos);
			} catch (AxisException e) {
				doingDrag = false;
				repaint();
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
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
			int valPos = axis.getAxisValPos();
			if (key == KeyEvent.VK_LEFT) {
				axis.setAxisValPos(valPos - 1);
				redrawAll();
			}
			else if (key == KeyEvent.VK_RIGHT) {
				axis.setAxisValPos(valPos + 1);
				redrawAll();
			}
		} catch (AxisException ex) {
		}
	}
	
}
	
