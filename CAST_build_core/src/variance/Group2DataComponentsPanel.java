package variance;

import axis.*;
import dataView.*;

import ssq.*;


public class Group2DataComponentsPanel extends DataWithComponentsPanel {
	public Group2DataComponentsPanel(XApplet applet) {
		super(applet);
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new Group2DataComponentsView(data, applet, theHorizAxis, theVertAxis, xKey,
															yKey, lsKey, modelKey, TwoGroupComponentVariable.TOTAL);
	}
}