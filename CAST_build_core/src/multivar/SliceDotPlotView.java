package multivar;

import java.awt.*;

import dataView.*;
import axis.*;
import random.RandomBits;
import coreGraphics.*;


public class SliceDotPlotView extends MarginalDataView {
	
	static final private int kMaxJitter = 50;
	static final private int kMinDotPlotWidth = 40;
	static final protected int kArrowBorder = 20;
	
	protected int currentJitter = 0;
	protected int jittering[] = null;
	private double initialJittering = 0.0;
	private boolean jitteringInitialised = false;
	
	private double minSelect, selectRange;
	protected String yKey;
	
	private boolean selected = false;
	
	public SliceDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, double initialJittering,
							double minSelect, double maxSelect, String yKey, Insets border) {
		super(theData, applet, border, theAxis);
		this.initialJittering = initialJittering;
		setSelection(minSelect, maxSelect);
		this.yKey = yKey;
	}
	
	public SliceDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, double initialJittering,
							double minSelect, double maxSelect, String yKey) {
		this(theData, applet, theAxis, initialJittering, minSelect, maxSelect, yKey,
																															new Insets(5, 5, 5, kArrowBorder));
	}
	
	public void setSelection(double minSelect, double maxSelect) {
		this.minSelect = minSelect;
		this.selectRange = maxSelect - minSelect;
	}
	
	public int minDisplayWidth() {
		return kMinDotPlotWidth + kArrowBorder;
	}
	
	public Dimension getMinimumSize() {
		return vertNotHoriz ? new Dimension(minDisplayWidth(), 20)
								: new Dimension(20, minDisplayWidth());
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (Double.isNaN(theVal.toDouble()))
			return null;
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos = (currentJitter > 0 && jittering != null) ? ((currentJitter * jittering[index]) >> 14) : 0;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected void drawBackground(Graphics g) {
		try {
			int highPos = axis.numValToPosition(minSelect + selectRange);
			int lowPos = axis.numValToPosition(minSelect);
			Point p1 = translateToScreen(lowPos, -getDisplayBorderNearAxis() - 1, null);
			Point p2 = translateToScreen(highPos, getDisplayHeight(), null);
			
			g.setColor(Color.pink);
			fillRect(g, p1, p2);
		
			if (selected) {
				if (vertNotHoriz) {
					Point p = new Point(getSize().width - kArrowBorder / 2,
															(p2.y + p1.y) / 2);
					ModelGraphics.drawHandle(g, p, false);
				}
				else {
					Point p = new Point((p2.x + p1.x) / 2, getSize().height - kArrowBorder / 2);
					ModelGraphics.drawHandle(g, p, false, false);
				}
			}
		} catch (AxisException ex) {
		}
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		NumVariable variable = (NumVariable)getVariable(yKey);
		
		checkJittering();
		
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		Point thePoint = null;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				g.setColor(nextSel ? Color.red : Color.black);
				drawCross(g, thePoint);
			}
			index++;
		}
	}
	
	protected void checkJittering() {
		if (!jitteringInitialised) {
			setJitter(initialJittering);
			jitteringInitialised = true;
		}
		int dataLength = getNumVariable().noOfValues();
		if (currentJitter > 0 && (jittering == null || jittering.length != dataLength)) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
	}
	
	protected int getMaxJitter() {
		return Math.min(kMaxJitter, getSize().width - getViewBorder().left - getViewBorder().right);
	}
	
	public void setJitter(double fraction) {
		int maxJitter = getMaxJitter();
		currentJitter = (int)(fraction * maxJitter);
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	private int hitOffset;
	
	protected boolean canDrag() {
		return true;
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		if (hitPos.x + hitOffset < 0)
			return new HorizDragPosInfo(-hitOffset);
		else 
			try {
				int maxPos = axis.numValToPosition(axis.maxOnAxis - selectRange);
				if (hitPos.x + hitOffset > maxPos)
					return new HorizDragPosInfo(-hitOffset + maxPos);
				else
					return new HorizDragPosInfo(hitPos.x);
			} catch (Exception e) {
			}
		return null;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		
		int bottomSelPos = 0;
		try {
			bottomSelPos = axis.numValToPosition(minSelect);
		} catch (AxisException e) {
		}
		
		int topSelPos = axis.getAxisLength();
		try {
			topSelPos = axis.numValToPosition(minSelect + selectRange);
		} catch (AxisException e) {
		}
		
		if (hitPos.x >= bottomSelPos && hitPos.x <= topSelPos)
			return new HorizDragPosInfo(hitPos.x, 0, bottomSelPos - hitPos.x);
		return null;
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startPos;
		hitOffset = dragPos.hitOffset;
		repaint();
		selected = true;
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			selected = false;
			repaint();
		}
		else {
			selected = true;
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			int newMinPos = dragPos.x + hitOffset;
			boolean selectionChanged = false;
			try {
				minSelect = axis.positionToNumVal(newMinPos);
				selectionChanged = getData().setSelection(yKey, minSelect, minSelect + selectRange);
			} catch (AxisException e) {
			}
			if (!selectionChanged)
				repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selected = false;
		repaint();
	}
}
	
