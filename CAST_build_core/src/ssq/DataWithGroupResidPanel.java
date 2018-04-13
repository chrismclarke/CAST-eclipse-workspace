package ssq;

import axis.*;
import dataView.*;


public class DataWithGroupResidPanel extends DataWithComponentsPanel {
	
	public DataWithGroupResidPanel(XApplet applet) {
		super(applet);
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new ResidsWithinGroupsView(data, applet, theHorizAxis, theVertAxis, xKey, yKey, lsKey, modelKey);
	}
}