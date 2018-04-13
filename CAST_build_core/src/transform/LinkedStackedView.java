package transform;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class LinkedStackedView extends StackedDiscreteView {
	
	private String rawKey, distnKey;
	
	public LinkedStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																String yKey, double step, String rawKey, String distnKey) {
		super(theData, applet, theAxis, yKey, step);
		this.rawKey = rawKey;
		this.distnKey = distnKey;
	}
	
	public LinkedStackedView(DataSet theData, XApplet applet, NumCatAxis theAxis, String yKey,
																				String rawKey, String distnKey) {
		this(theData, applet, theAxis, yKey, 1.0, rawKey, distnKey);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		selectDistn(startInfo);
		return super.startDrag(startInfo);
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		selectDistn(toPos);
		super.doDrag(fromPos, toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		selectDistn(endPos);
		super.endDrag(startPos, endPos);
	}
	
	private void selectDistn(PositionInfo posInfo) {
		if (posInfo == null)
			getData().setSelection(distnKey, -1.0, -1.0);
		else {
			int hitIndex = ((IndexPosInfo)posInfo).itemIndex;
			NumVariable percentileVariable = (NumVariable)(getData().getVariable(rawKey));
			double percentile = percentileVariable.doubleValueAt(hitIndex);
			getData().setSelection(distnKey, Double.NEGATIVE_INFINITY, percentile + 0.5);
			
		}
	}
}