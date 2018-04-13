package variance;

import axis.*;
import dataView.*;

import ssq.*;


public class QuadraticComponentsPanel extends DataWithComponentsPanel {
	public QuadraticComponentsPanel(XApplet applet) {
		super(applet);
	}
	
	private String quadKey;
	
	public void setupPanel(DataSet data, String xKey, String yKey, String linKey, String quadKey,
																		String modelKey, int initialComponentDisplay, XApplet applet) {
		this.quadKey = quadKey;
		setupPanel(data, xKey, yKey, linKey, modelKey, initialComponentDisplay, applet);
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new QuadraticComponentsView(data, applet, theHorizAxis, theVertAxis, xKey,
															yKey, lsKey, quadKey, modelKey, initialComponentDisplay);
	}
}