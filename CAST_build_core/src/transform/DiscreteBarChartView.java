package transform;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;

import distribution.*;


public class DiscreteBarChartView extends DataView {
	static final public int NO_DRAG = 0;
	static final public int DRAG_PROB = 1;
	static final public int DRAG_CUMULATIVE = 2;
	
	static final public int ALL_LAST_BAR = 0;			//	all final bar is shaded
	static final public int HALF_LAST_BAR = 1;		//	only half final bar is shaded
	
	static final private int kDragSlop = 150;
	
	private String distnKey;
	private HorizAxis nAxis;
	private VertAxis pAxis;
	private int dragType;
	private int lastBarShade = ALL_LAST_BAR;
	
	private Color distnColor = Color.gray;
	private Color highlightColor = new Color(0xCC0000);
	
	public DiscreteBarChartView(DataSet theData, XApplet applet, String distnKey, HorizAxis nAxis,
																												VertAxis pAxis, int dragType) {
		super(theData, applet, new Insets(0, 5, 0, 5));
		this.distnKey = distnKey;
		this.pAxis = pAxis;
		this.nAxis = nAxis;
		this.dragType = dragType;
	}
	
	public void setLastBarShade(int lastBarShade) {
		this.lastBarShade = lastBarShade;
	}
	
	public void paintView(Graphics g) {
		Point topLeft = null;
		DiscreteDistnVariable y = (DiscreteDistnVariable)getVariable(distnKey);
		
		int barSpacing = 0;
		try {
			int x0Pos = nAxis.numValToPosition(0.0);
			int x1Pos = nAxis.numValToPosition(1.0);
			barSpacing = x1Pos - x0Pos;
		} catch (AxisException e) {
		}
		
		int halfBarWidth = (barSpacing >= 20) ? 2
								: (barSpacing >= 10) ? 1
								: 0;
		
		int maxN = (int)Math.round(Math.floor(nAxis.maxOnAxis));
		int maxSelectionInt = (int)Math.round(Math.floor(y.getMaxSelection()));
		int minSelectionInt = (int)Math.round(Math.ceil(y.getMinSelection()));
		for (int i=0 ; i<=maxN ; i++)
			try {
				int x = nAxis.numValToPosition(i);
				double prob = y.getScaledProb(i);
				int ht = pAxis.numValToPosition(prob);
				topLeft = translateToScreen(x, ht, topLeft);
				
				if (i < minSelectionInt || i > maxSelectionInt)
					g.setColor(distnColor);
				else
					g.setColor(highlightColor);
				
				g.fillRect(topLeft.x - halfBarWidth, topLeft.y + 1, 2 * halfBarWidth + 1,
																						getSize().height - topLeft.y - 1);
				if (i == maxSelectionInt && lastBarShade == HALF_LAST_BAR) {
					g.setColor(distnColor);
					g.fillRect(topLeft.x - halfBarWidth, topLeft.y + 1, 2 * halfBarWidth + 1,
																						(getSize().height - topLeft.y - 1) / 2);
				}
			} catch (AxisException e) {
			}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		if (distnKey.equals(key))
			repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return dragType != NO_DRAG;
	}
	
	private int startDragValue = -1;
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		int hitVal;
		try {
			hitVal = (int)Math.round(nAxis.positionToNumVal(hitPos.x));
		} catch (AxisException e) {
			hitVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? 0 : (int)Math.round(nAxis.maxOnAxis);
		}
		return new DiscreteDragInfo(hitVal);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().setSelection(distnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		else {
			startDragValue = ((DiscreteDragInfo)startInfo).xValue;
			double lowVal =  (dragType == DRAG_PROB) ? startDragValue - 0.5 : Double.NEGATIVE_INFINITY;
			getData().setSelection(distnKey, lowVal, startDragValue + 0.5);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
//		getData().setSelection(distnKey, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
}