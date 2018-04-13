package curveInteract;

import java.awt.*;

import dataView.*;


public class ParamTestsRemoveView extends ParamTestsView {
//	static final public String PARAM_TEST_REMOVE_VIEW = "paramTestRemoveView";
	
	static final private int kBoxLeftRight = 5;
	static final private int kBoxSize = 12;
	
	static final private Color kLightGray = new Color(0x999999);
	static final private Color kDimCheckColor = new Color(0x666666);
	
	private SummaryDataSet summaryData;
	
	private int hierarchy[][];		//	must be ordered with interactions after main effects
																//	intercept is indexed by zero
	
	protected boolean enabledCheck[] = null;
	
	private int selectedIndex = -1;
	private boolean doingDrag = false;
	
	public ParamTestsRemoveView(DataSet theData, XApplet applet,
							String modelKey, String yKey, String paramName[], NumValue maxParam,
							NumValue maxSE, NumValue maxT, SummaryDataSet summaryData, int[][] hierarchy) {
		super(theData, applet, modelKey, yKey, paramName, null, maxParam, maxSE, maxT);
		this.summaryData = summaryData;
		this.hierarchy = hierarchy;
		doEnabling();
	}
	
	public ParamTestsRemoveView(DataSet theData, XApplet applet,
							String modelKey, String yKey, String paramName[], NumValue maxParam,
							NumValue maxSE, NumValue maxT, SummaryDataSet summaryData) {
		this(theData, applet, modelKey, yKey, paramName, maxParam, maxSE, maxT, summaryData, null);
	}
	
	public void setConstraint(int paramIndex, boolean constrainedZero) {
		super.setConstraint(paramIndex, constrainedZero);
		summaryData.redoLastSummary();
		doEnabling();
		getData().variableChanged(modelKey);
	}
	
	private void doEnabling() {
		if (hierarchy == null)
			return;
		for (int i=0 ; i<hierarchy.length ; i++) {
			if (hierarchy[i] == null)
				enableCheck(i, true);
			else {
				int requiredIndex[] = hierarchy[i];
				if (!isConstrained(i))
					for (int j=0 ; j<requiredIndex.length ; j++)
						enableCheck(requiredIndex[j], false);
				else {
					boolean canEnable = true;
					for (int j=0 ; j<requiredIndex.length ; j++)
						if (isConstrained(requiredIndex[j]))
							canEnable = false;
					enableCheck(i, canEnable);
				}
			}
		}
	}
	
	protected int leftCheckBorder() {			//	for checkboxes to remove variables
		return kBoxSize + 2 * kBoxLeftRight;
	}

//-----------------------------------------------------------------------------------
	
	protected void enableCheck(int paramIndex, boolean enabled) {
		if (enabledCheck == null) {
			enabledCheck = new boolean[paramName.length];
			for (int i=1 ; i<paramName.length ; i++)
				enabledCheck[i] = true;
		}
		enabledCheck[paramIndex] = enabled;
	}
	
	private boolean isEnabled(int paramIndex) {
		return enabledCheck == null || enabledCheck[paramIndex];
	}
	
	public void drawLeftChecks(Graphics g) {
		for (int i=1 ; i<paramName.length ; i++)
			if (paramName[i] != null) {
				int paramBaseline = getParamBaseline(i);
				boolean constrainedToZero = constraints != null && !Double.isNaN(constraints[i]);
				boolean buttonDown = doingDrag && (i == selectedIndex);
				
				Color topLeftColor = buttonDown	? kLightGray : Color.white;
				Color bottomRightColor = buttonDown	? Color.white : kLightGray;
				
				int boxLeft = tableLeft - kBoxSize - kBoxLeftRight;
				int boxTop = paramBaseline - (kBoxSize - 2);
				
				if (isEnabled(i)) {
					g.setColor(Color.white);
					g.fillRect(boxLeft + 1, boxTop + 1, kBoxSize - 1, kBoxSize - 1);
					
					g.setColor(topLeftColor);
					g.drawLine(boxLeft - 1, boxTop - 1, boxLeft - 1, boxTop + kBoxSize + 1);
					g.drawLine(boxLeft - 1, boxTop - 1, boxLeft + kBoxSize + 1, boxTop - 1);
					
					g.setColor(bottomRightColor);
					g.drawLine(boxLeft, boxTop + kBoxSize + 1, boxLeft + kBoxSize + 1, boxTop + kBoxSize + 1);
					g.drawLine(boxLeft + kBoxSize + 1, boxTop, boxLeft + kBoxSize + 1, boxTop + kBoxSize + 1);
				}
				
				g.setColor(isEnabled(i) ? Color.black : kDimCheckColor);
				g.drawRect(boxLeft, boxTop, kBoxSize, kBoxSize);
				if (buttonDown)
					g.drawRect(boxLeft + 1, boxTop + 1, kBoxSize - 2, kBoxSize - 2);
				
				if (!constrainedToZero) {
					g.drawLine(boxLeft, boxTop, boxLeft + kBoxSize, boxTop + kBoxSize);
					g.drawLine(boxLeft, boxTop + kBoxSize, boxLeft + kBoxSize, boxTop);
				}
		}
	}
	
	protected boolean meaningfulTest(int paramIndex) {
		return paramIndex > 0 && isEnabled(paramIndex) && !isConstrained(paramIndex);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		if (x < tableLeft - kBoxSize - 2 * kBoxLeftRight || x >= tableLeft)
			return null;
		
		int rowSpacing = getParamBaseline(1) - getParamBaseline(0);
		int top = (getParamBaseline(0) + 2 + getParamBaseline(1) - kBoxSize + 2) / 2;
		
		if (y < top)
			return null;
		
		for (int i=1 ; i<paramName.length ; i++)
			if (paramName[i] != null) {
				top += rowSpacing;
				if (y < top)
					return isEnabled(i) ? new IndexPosInfo(i) : null;
			}
		
		return null;
		
//		int hitIndex = (y - top) / rowSpacing + 1;
//		
//		if (hitIndex < 1 || hitIndex >= paramName.length)
//			return null;
//		else
//			return new IndexPosInfo(hitIndex);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		IndexPosInfo hitItem = (IndexPosInfo)getInitialPosition(x, y);
		if (hitItem == null)
			return null;
		int hitIndex = hitItem.itemIndex;
		
		if (selectedIndex == hitIndex)
			return hitItem;
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof IndexPosInfo) {
			selectedIndex = ((IndexPosInfo)startInfo).itemIndex;
			doingDrag = true;
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			doingDrag = false;
			repaint();
		}
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (doingDrag) {
			setConstraint(selectedIndex, !isConstrained(selectedIndex));
			doingDrag = false;
		}
		repaint();
	}
}