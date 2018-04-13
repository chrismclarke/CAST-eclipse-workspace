package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class DistnDensityView extends PixelDensityView {
//	static final public String DISTN_DENSITY = "distnDensity";
	
	static final public boolean SHOW_MEANSD = true;
	static final public boolean NO_SHOW_MEANSD = false;
	
	static final public int NO_DRAG = 0;
	static final public int SYMMETRIC_DRAG = 1;
	static final public int MIN_MAX_DRAG = 2;
	static final public int MAX_DRAG = 3;
	
	static final private int kMeanLineHeight = 16;
	static final private int kSDLineHeight = kMeanLineHeight / 2;
	static final private int kArrowHead = 4;
	
	static final private int kDragSlop = 150;
	
	private Color mainColor = Color.blue;
	private Color tailColor = Color.lightGray;
	
	private int dragType = MIN_MAX_DRAG;
	private boolean showMeanSD = NO_SHOW_MEANSD;
	
	private int pixelTop[];
	
	public DistnDensityView(DataSet theData, XApplet applet, HorizAxis horizAxis, VertAxis probAxis,
																										String distnKey, boolean showMeanSD, int dragType) {
		super(theData, applet, horizAxis, probAxis, distnKey);
		this.showMeanSD = showMeanSD;
		this.dragType = dragType;
	}
	
	public DistnDensityView(DataSet theData, XApplet applet, HorizAxis horizAxis,
																																	VertAxis probAxis, String distnKey) {
		this(theData, applet, horizAxis, probAxis, distnKey, NO_SHOW_MEANSD, NO_DRAG);
	}
	
	public void setColors(Color mainColor, Color tailColor) {
		this.mainColor = mainColor;
		this.tailColor = tailColor;
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			if (distnKey != null) {
				if (pixelTop == null || pixelTop.length != (prob.length - 2))
					pixelTop = new int[prob.length - 2];
				double pixelFactor = horizAxis.getAxisLength() / (horizAxis.maxOnAxis - horizAxis.minOnAxis);
				Point topLeft = null;
				for (int i=0 ; i<pixelTop.length ; i++) {
					int vertPos = probAxis.numValToRawPosition(prob[i + 1] * pixelFactor) - 1;
					topLeft = translateToScreen(i, vertPos, topLeft);
					pixelTop[i] = topLeft.y;
				}
			}
			return true;
		}
		else
			return false;
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		if (distnKey != null) {
			ContinDistnVariable y = (ContinDistnVariable)getVariable(distnKey);
			int axisLength = horizAxis.getAxisLength();
			
			int minSelPos = 0;
			try {
				minSelPos = horizAxis.numValToPosition(y.getMinSelection());
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_LOW_ERROR)
					minSelPos = 0;
				else
					minSelPos = axisLength - 1;
			}
			int maxSelPos = 0;
			try {
				maxSelPos = horizAxis.numValToPosition(y.getMaxSelection());
			} catch (AxisException e) {
				if (e.axisProblem == AxisException.TOO_LOW_ERROR)
					maxSelPos = 0;
				else
					maxSelPos = axisLength - 1;
			}
			
//			double pixelFactor = axisLength / (horizAxis.maxOnAxis - horizAxis.minOnAxis);
			Point zeroPos = translateToScreen(0, 0, null);
			
//			Point topLeft = null;
			g.setColor(tailColor);
			for (int i=0 ; i<=minSelPos ; i++)
				g.drawLine(zeroPos.x + i, pixelTop[i], zeroPos.x + i, zeroPos.y + 1);
			
			g.setColor(mainColor);
			for (int i=minSelPos + 1 ; i<=maxSelPos ; i++)
				g.drawLine(zeroPos.x + i, pixelTop[i], zeroPos.x + i, zeroPos.y + 1);
			
			g.setColor(tailColor);
			for (int i=maxSelPos + 1 ; i<axisLength ; i++)
				g.drawLine(zeroPos.x + i, pixelTop[i], zeroPos.x + i, zeroPos.y + 1);
			
			if (showMeanSD) {
				double mean = y.getMean().toDouble();
				double sd = y.getSD().toDouble();
				
				int meanPos = horizAxis.numValToRawPosition(mean);
				int meanSDPos = horizAxis.numValToRawPosition(mean + sd);
				Point meanOnAxis = translateToScreen(meanPos, 0, null);
				Point meanSDLineEnd = translateToScreen(meanSDPos, kSDLineHeight, null);
				
				g.setColor(Color.black);
				g.drawLine(meanOnAxis.x, meanSDLineEnd.y, meanSDLineEnd.x, meanSDLineEnd.y);
				g.drawLine(meanSDLineEnd.x, meanSDLineEnd.y, meanSDLineEnd.x - kArrowHead, meanSDLineEnd.y - kArrowHead);
				g.drawLine(meanSDLineEnd.x, meanSDLineEnd.y, meanSDLineEnd.x - kArrowHead, meanSDLineEnd.y + kArrowHead);
				
				g.setColor(Color.black);
				g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x, meanOnAxis.y - kMeanLineHeight);
				g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x - kArrowHead, meanOnAxis.y - kArrowHead);
				g.drawLine(meanOnAxis.x, meanOnAxis.y, meanOnAxis.x + kArrowHead, meanOnAxis.y - kArrowHead);
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	static private final int MIN_SELECTED = 0;
	static private final int MAX_SELECTED = 1;
	
	private double otherExtreme;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return distnKey != null && dragType != NO_DRAG;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal;
		try {
			hitVal = horizAxis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			hitVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? horizAxis.minOnAxis : horizAxis.maxOnAxis;
		}
		
		int hitExtreme = MAX_SELECTED;
		if (dragType == MIN_MAX_DRAG) {
			DistnVariable v = getDistnVariable();
			double minOffset = Math.abs(hitVal - v.getMinSelection());
			double maxOffset = Math.abs(hitVal - v.getMaxSelection());
			if (minOffset < maxOffset)
				hitExtreme = MIN_SELECTED;
		}
		return new HorizDragPosInfo(hitPos.x, hitExtreme, 0);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x < 0)
			hitPos.x = 0;
		else if (hitPos.x >= horizAxis.getAxisLength())
			hitPos.x = horizAxis.getAxisLength() - 1;
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			DistnVariable v = getDistnVariable();
			int hitExtreme = ((HorizDragPosInfo)startPos).index;
			otherExtreme = (hitExtreme == MAX_SELECTED) ? v.getMinSelection()
																						: v.getMaxSelection();
			doDrag(null, startPos);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			
			int newAxisPos = dragPos.x;
			double newVal;
			try {
				newVal = horizAxis.positionToNumVal(newAxisPos);
			} catch (AxisException e) {
				newVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? horizAxis.minOnAxis
																						: horizAxis.maxOnAxis;
			}
			if (dragType == SYMMETRIC_DRAG) {
				double distnMean = getDistnVariable().getMean().toDouble();
				if (newVal < distnMean)
					getData().setSelection(distnKey, newVal, 2.0 * distnMean - newVal);
				else
					getData().setSelection(distnKey, 2.0 * distnMean - newVal, newVal);
			}
			else if (newVal < otherExtreme)
				getData().setSelection(distnKey, newVal, otherExtreme);
			else
				getData().setSelection(distnKey, otherExtreme, newVal);
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		repaint();
	}
}
	
