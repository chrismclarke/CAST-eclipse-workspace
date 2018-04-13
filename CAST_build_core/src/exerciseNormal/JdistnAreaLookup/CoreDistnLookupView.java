package exerciseNormal.JdistnAreaLookup;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


abstract public class CoreDistnLookupView extends DataView {
	static final private int kDragSlop = 150;
	static final public int kNoOfShades = AccurateDistnArtist.kNoOfShades;
	
	static final protected LabelValue kFinishTyping = new LabelValue("Waiting...");
	
	static protected final int MIN_SELECTED = 0;
	static protected final int MAX_SELECTED = 1;
	
	static protected final boolean DISCRETE = true;
	static protected final boolean CONTINUOUS = false;
	
	protected String distnKey;
	protected HorizAxis horizAxis;
	
	protected boolean highAndLow;
	protected boolean isDiscrete;
	
	protected LimitEditPanel editPanel;
	
	private boolean initialised = false;
	
	protected boolean lowPending = false;
	protected boolean highPending = false;
	
	protected NumValue tempVal = new NumValue(0.0, 4);
	
	protected boolean doingDrag = false;
	protected int extremeSelected;
	protected double otherExtreme;
	
	private boolean dragEnabled = true;
	
	public CoreDistnLookupView(DataSet theData, XApplet applet,
												HorizAxis horizAxis, String distnKey, boolean highAndLow, boolean isDiscrete) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.distnKey = distnKey;
		this.horizAxis = horizAxis;
		this.highAndLow = highAndLow;
		this.isDiscrete = isDiscrete;
	}
	
	public void setDragEnabled(boolean dragEnabled) {
		this.dragEnabled = dragEnabled;
	}
	
	public void setLinkedEdit(LimitEditPanel editPanel) {
		this.editPanel = editPanel;
	}
	
	public void setPending(boolean lowNotHigh, boolean pending) {
		if (lowNotHigh)
			lowPending = pending;
		else
			highPending = pending;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	abstract protected void doInitialisation(Graphics g);
	
	abstract public void paintView(Graphics g);
	
//	public void reset() {
//		initialised = false;
//	}

//-----------------------------------------------------------------------------------
	
	
	protected void doChangeVariable(Graphics g, String key) {
		if (key.equals(distnKey)) {
			initialised = false;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return dragEnabled;
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
		if (highAndLow) {
			DistnVariable v = (DistnVariable)getVariable(distnKey);
			double minOffset = Math.abs(hitVal - v.getMinSelection());
			double maxOffset = Math.abs(hitVal - v.getMaxSelection());
			hitExtreme = (minOffset < maxOffset) ? MIN_SELECTED : MAX_SELECTED;
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
		doingDrag = true;
		DistnVariable v = (DistnVariable)getVariable(distnKey);
		extremeSelected = ((HorizDragPosInfo)startPos).index;
		otherExtreme = (extremeSelected == MAX_SELECTED) ? v.getMinSelection()
																					: v.getMaxSelection();
		doDrag(null, startPos);
		if (editPanel != null)
			editPanel.noteChanged();
		
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
			
			if (newVal < otherExtreme) {
				extremeSelected = MIN_SELECTED;
				getData().setSelection(distnKey, newVal, otherExtreme);
				if (editPanel != null)
					editPanel.setLowValue(newVal);
			}
			else {
				extremeSelected = MAX_SELECTED;
				getData().setSelection(distnKey, otherExtreme, newVal);
				if (editPanel != null)
					editPanel.setHighValue(newVal);
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		if (isDiscrete) {
			DistnVariable v = (DistnVariable)getVariable(distnKey);
			double oldMin = v.getMinSelection();
			double newMin = Math.floor(oldMin) + 0.5;
			double oldMax = v.getMaxSelection();
			double newMax = Math.floor(oldMax) + 0.5;
			getData().setSelection(distnKey, newMin, newMax);
			if (editPanel != null) {
				editPanel.setLowValue(newMin);
				editPanel.setHighValue(newMax);
			}
		}
		repaint();
	}
}
	
