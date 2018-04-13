package twoGroup;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class DragMeanSdView extends StackedPlusNormalView {
	static final private int NO_DRAG = 0;
	static final private int MEAN_DRAG = 1;
	static final private int SD_DRAG = 2;
	
	static final private int kTopBorder = 20;
	static final private int kHitSlop = 5;
	
	static final private Color kLightGray = new Color(0x999999);
	static final private Color kLabelColor = new Color(0xAAAAFF);
	
	private boolean canDragMean, canDragSd;
	private double roundFactor;
	
	private DragMeanSdView linkedView;
	private boolean equalMeans = false;
	private boolean equalSds = false;
	
	private int dragType = NO_DRAG;
	private int lastDragType = NO_DRAG;
	private int hitOffset;
	
	private double mean, sd2;
	
	public DragMeanSdView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey,
										String normalKey, boolean canDragMean, boolean canDragSd, int meanSdDecimals) {
		super(theData, applet, theAxis, normalKey);
		setActiveNumVariable(yKey);
		this.canDragMean = canDragMean;
		this.canDragSd = canDragSd;
		
		roundFactor = 1.0;
		while (meanSdDecimals > 0) {
			roundFactor *= 10;
			meanSdDecimals --;
		}
		while (meanSdDecimals < 0) {
			roundFactor /= 10;
			meanSdDecimals ++;
		}
		
		Font f = applet.getBigBoldFont();
		int biggerSize = (int)Math.round(2.0 * f.getSize());
		Font biggerFont = new Font(f.getName(), f.getStyle(), biggerSize);
		setFont(biggerFont);
		setDistnLabel(new LabelValue(getVariable(yKey).name), kLabelColor);
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(normalKey);
		mean = distnVar.getMean().toDouble();
		sd2 = distnVar.getSD().toDouble() * 2;
	}
	
	public void setLinkedView(DragMeanSdView linkedView) {
		this.linkedView = linkedView;
	}
	
	public void setEqualMeans(boolean equalMeans) {
		this.equalMeans = equalMeans;
		if (equalMeans && linkedView != null)
			mean = Math.min(mean, linkedView.mean);
	}
	
	public void setEqualSds(boolean equalSds) {
		this.equalSds = equalSds;
		if (equalSds && linkedView != null)
			sd2 = Math.min(sd2, linkedView.sd2);
	}
	
	protected void paintBackground(Graphics g) {
		super.paintBackground(g);
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(normalKey);
		double mean = distnVar.getMean().toDouble();
		int centerPos = axis.numValToRawPosition(mean);
		int centerX = translateToScreen(centerPos, 0, null).x;
		
		double sd = distnVar.getSD().toDouble();
		int scalePos = axis.numValToRawPosition(mean + 2 * sd);
		int scaleX = translateToScreen(scalePos, 0, null).x;
			
		if (canDragMean && dragType != SD_DRAG) {
			if (dragType == NO_DRAG) {
				g.setColor(Color.red);
				g.drawLine(centerX, 0, centerX, getSize().height);
			}
			else {				//	MEAN_DRAG
				g.setColor(Color.yellow);
				g.drawLine(centerX - 1, 0, centerX - 1, getSize().height);
				g.drawLine(centerX + 1, 0, centerX + 1, getSize().height);
				g.setColor(Color.black);
				g.drawLine(centerX, 0, centerX, getSize().height);
			}
		}
		else {
			g.setColor(kLightGray);
			g.drawLine(centerX, 0, centerX, kTopBorder);
		}
		
		int arrowCenter = kTopBorder / 2;
		g.setColor(kLightGray);
		g.drawLine(centerX + 1, arrowCenter, scaleX, arrowCenter);
		if (canDragSd && dragType != MEAN_DRAG) {
			if (dragType == NO_DRAG) {
				g.setColor(Color.red);
				g.drawLine(scaleX, 0, scaleX, kTopBorder);
			}
			else {				//	SD_DRAG
				g.setColor(Color.yellow);
				g.drawLine(scaleX - 1, 0, scaleX - 1, kTopBorder);
				g.drawLine(scaleX + 1, 0, scaleX + 1, kTopBorder);
				g.setColor(Color.black);
				g.drawLine(scaleX, 0, scaleX, kTopBorder);
			}
		}
		else {
			g.setColor(kLightGray);
			g.drawLine(scaleX, 0, scaleX, kTopBorder);
		}
		
		g.setColor(getForeground());
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		
		ContinDistnVariable distnVar = (ContinDistnVariable)getVariable(normalKey);
		double mean = distnVar.getMean().toDouble();
		int centerPos = axis.numValToRawPosition(mean);
		
		double sd = distnVar.getSD().toDouble();
		int scalePos = axis.numValToRawPosition(mean + 2 * sd);
		
		int hitOffset = hitPos.x - scalePos;
		if (Math.abs(hitOffset) <= kHitSlop)
			return new HorizDragPosInfo(hitPos.x, SD_DRAG, hitOffset);
		else {
			hitOffset = hitPos.x - centerPos;
			if (Math.abs(hitOffset) <= kHitSlop)
				return new HorizDragPosInfo(hitPos.x, MEAN_DRAG, hitOffset);
			else
				return super.getInitialPosition(x, y);
		}
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		if (dragType != NO_DRAG)
			return new HorizDragPosInfo(hitPos.x);
		else
			return super.getPosition(x, y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null && startInfo instanceof HorizDragPosInfo) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
			hitOffset = dragPos.hitOffset;
			dragType = dragPos.index;
			repaint();
			return true;
		}
		else
			return super.startDrag(startInfo);
	}
	
	private double round(double x) {
		return Math.rint(x * roundFactor) / roundFactor;
	}
	
	private double roundUp(double x) {
		return Math.ceil(x * roundFactor) / roundFactor;
	}
	
	private double roundDown(double x) {
		return Math.floor(x * roundFactor) / roundFactor;
	}
	
	private double roundUp2(double x) {
		return Math.ceil(x * roundFactor * 2) / roundFactor / 2;
	}
	
	private double roundDown2(double x) {
		return Math.floor(x * roundFactor * 2) / roundFactor / 2;
	}
	
	protected double getThisMaxMeanDrag() {
		return roundDown(axis.maxOnAxis - sd2);
	}
	
	private double maxMeanDrag() {
		double maxMean = getThisMaxMeanDrag();
		if (linkedView != null && equalMeans)
			maxMean = Math.min(maxMean, linkedView.getThisMaxMeanDrag());
		return maxMean;
	}
	
	private double minSd2Drag() {
		return roundUp2(mean + 4.0 / roundFactor);
	}
	
	private double maxSd2Drag() {
		double maxSd2 = axis.maxOnAxis;
		if (linkedView != null && equalSds)
			maxSd2 = Math.min(maxSd2, axis.maxOnAxis + mean - linkedView.mean);
		return roundDown2(maxSd2);
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getSd() {
		return sd2 / 2;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null && dragType != NO_DRAG) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			int newXPos = dragPos.x - hitOffset;
			try {
				double newX = round(axis.positionToNumVal(newXPos));
				
				if (dragType == MEAN_DRAG) {
					double minMean = roundUp(axis.minOnAxis);
					double maxMean = maxMeanDrag();
					mean = Math.max(minMean, Math.min(maxMean, newX));
					if (linkedView != null && equalMeans)
						linkedView.mean = mean;
				}
				else {			//	dragType == SD_DRAG
					double minSd2 = minSd2Drag();
					double maxSd2 = maxSd2Drag();
					double newMeanPlusSd2 = Math.max(minSd2, Math.min(maxSd2, newX));
					sd2 = newMeanPlusSd2 - mean;
					if (linkedView != null && equalSds)
						linkedView.sd2 = sd2;
				}
				
				getApplet().notifyDataChange(this);
//				most of the work is done by SumTwoMeanApplet
			} catch (AxisException e) {
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragType == NO_DRAG)
			super.endDrag(startPos, endPos);
		else {
			lastDragType = dragType;
			dragType = NO_DRAG;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		requestFocus();
		super.mousePressed(e);
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN) {
			if (lastDragType == MEAN_DRAG) {
				double minMean = roundUp(axis.minOnAxis);
				double newMean = mean - 1.0 / roundFactor;
				if (newMean >= minMean) {
					mean = newMean;
					if (linkedView != null && equalMeans)
						linkedView.mean = mean;
					getApplet().notifyDataChange(this);
				}
			}
			else if (lastDragType == SD_DRAG) {
				double minSd2 = minSd2Drag();
				double newSd2 = sd2 - 2.0 / roundFactor;
				if (newSd2 >= minSd2) {
					sd2 = newSd2;
					if (linkedView != null && equalSds)
						linkedView.sd2 = sd2;
					getApplet().notifyDataChange(this);
				}
			}
		}
		else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP) {
			if (lastDragType == MEAN_DRAG) {
				double maxMean = maxMeanDrag();
				double newMean = mean + 1.0 / roundFactor;
				if (newMean <= maxMean) {
					mean = newMean;
					if (linkedView != null && equalMeans)
						linkedView.mean = mean;
					getApplet().notifyDataChange(this);
				}
			}
			else if (lastDragType == SD_DRAG) {
				double maxSd2 = maxSd2Drag();
				double newSd2 = sd2 + 2.0 / roundFactor;
				if (newSd2 <= maxSd2) {
					sd2 = newSd2;
					if (linkedView != null && equalSds)
						linkedView.sd2 = sd2;
					getApplet().notifyDataChange(this);
				}
			}
		}
	}
}