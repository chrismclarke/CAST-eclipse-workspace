package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DragPropnDotPlotView extends DotPlotView {
//	static public final String DRAG_PROPN_DOTPLOT = "dragPropnDotPlot";
	
	static final private int kDragSlop = 20;
	
	private double minSelection, maxSelection;
	
	private String yKey;
	private double hitFactor;
	
	private FractionValueView fractionView = null;
	
	public DragPropnDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, int decimals,
											String yKey, double minSelection, double maxSelection) {
		super(theData, applet, theAxis, 1.0);
		hitFactor = 1.0;
		for (int i=0 ; i< decimals ; i++)
			hitFactor *= 10.0;
		this.yKey = yKey;
		setActiveNumVariable(yKey);
		this.minSelection = minSelection;
		this.maxSelection = maxSelection;
	}
	
	public void setLinkedFraction(FractionValueView fractionView) {
		this.fractionView = fractionView;
	}
	
	public double getMinSelection() {
		return minSelection;
	}
	
	public double getMaxSelection() {
		return maxSelection;
	}
	
	private void drawBackground(Graphics g) {
		int lowPos;
		try {
			lowPos = axis.numValToPosition(minSelection);
		} catch (AxisException e) {
			lowPos = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? -50
															: axis.axisLength + 50;
		}
		int lowScreenPos = translateToScreen(lowPos, 0, null).x;
		
		int highPos;
		try {
			highPos = axis.numValToPosition(maxSelection);
		} catch (AxisException e) {
			highPos = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? -50
															: axis.axisLength + 50;
		}
		int highScreenPos = translateToScreen(highPos, 0, null).x;
			
		g.setColor(Color.yellow);
		g.fillRect(lowScreenPos, 0, highScreenPos - lowScreenPos, getSize().height);
		
		if (doingDrag) {
			g.setColor(Color.red);
			int hitPos = (otherExtreme == minSelection) ? highScreenPos : lowScreenPos;
			g.drawLine(hitPos, 0, hitPos, getSize().height);
		}
		
		g.setColor(getForeground());
	}
	
	public void paintView(Graphics g) {
		drawBackground(g);
		
		super.paintView(g);
	}
	

//-----------------------------------------------------------------------------------
	
	static private final int MIN_SELECTED = 0;
	static private final int MAX_SELECTED = 1;
	
	private double otherExtreme;
	private boolean doingDrag = false;
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	private double round(double value) {
		return Math.rint(value * hitFactor) / hitFactor;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal;
		try {
			hitVal = round(axis.positionToNumVal(hitPos.x));
		} catch (AxisException e) {
			hitVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? round(axis.minOnAxis)
																														: round(axis.maxOnAxis);
		}
		
		int hitExtreme;
		
		if (maxSelection == Double.NEGATIVE_INFINITY)
			hitExtreme = MAX_SELECTED;
		else if (minSelection == Double.POSITIVE_INFINITY)
			hitExtreme = MIN_SELECTED;
		else {
			double startMin = (minSelection == Double.NEGATIVE_INFINITY) ? axis.minOnAxis
																																	: minSelection;
			double startMax = (maxSelection == Double.POSITIVE_INFINITY) ? axis.maxOnAxis
																																	: maxSelection;
//			double minOffset = hitVal - startMin;
//			double maxOffset = startMax - hitVal;
			hitExtreme = (hitVal < 0.5 * (startMin + startMax)) ? MIN_SELECTED : MAX_SELECTED;
		}
			
		return new HorizDragPosInfo(hitPos.x, hitExtreme, 0);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		return new HorizDragPosInfo(hitPos.x);
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
//			DistnVariable v = getDistnVariable();
			int hitExtreme = ((HorizDragPosInfo)startPos).index;
			otherExtreme = (hitExtreme == MAX_SELECTED) ? minSelection : maxSelection;
			doingDrag = true;
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
				newVal = round(axis.positionToNumVal(newAxisPos));
			} catch (AxisException e) {
				newVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? Double.NEGATIVE_INFINITY
																						: Double.POSITIVE_INFINITY;
			}
			
			minSelection = Math.min(newVal, otherExtreme);
			maxSelection = Math.max(newVal, otherExtreme);
			
			if (!getData().setSelection(yKey, minSelection, maxSelection)) {
				repaint();
				fractionView.repaint();
			}
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}
	
}
	
