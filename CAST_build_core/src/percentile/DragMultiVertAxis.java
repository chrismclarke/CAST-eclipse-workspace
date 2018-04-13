package percentile;

import dataView.*;
import axis.*;


public class DragMultiVertAxis extends MultiVertAxis {
	
//	private boolean doingDrag = false;
	
	public DragMultiVertAxis(XApplet applet, int noOfAlternateLabels) {
		super(applet, noOfAlternateLabels);
									//	last alternate should be a single label for the dragged value
	}
	
	public void setDragValue(NumValue dragVal) {
		AxisLabel axisLabel = getAxisLabel(2, 0);
		axisLabel.label = dragVal;
		axisLabel.position = (dragVal.toDouble() / 100.0 - minOnAxis) / (maxOnAxis - minOnAxis);
//		System.out.println("Setting drag value to: " + dragVal.toString() + ", pos = "
//											+ axisLabel.position + ", width = " + axisLabel.labelWidth);
		repaint();
	}
}