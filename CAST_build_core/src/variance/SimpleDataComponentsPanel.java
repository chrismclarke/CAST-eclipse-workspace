package variance;

import axis.*;
import dataView.*;

import ssq.*;


public class SimpleDataComponentsPanel extends DataWithComponentsPanel {
	private double target;
	
	public SimpleDataComponentsPanel(double target, XApplet applet) {
		super(applet);
		this.target = target;
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new SimpleDataComponentsView(data, applet, theVertAxis, xKey, yKey, modelKey,
																			target, SimpleComponentVariable.FROM_TARGET);
	}
}