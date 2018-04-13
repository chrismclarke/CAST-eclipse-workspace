package variance;

import axis.*;
import dataView.*;

import ssq.*;


public class PureComponentsPanel extends DataWithComponentsPanel {
	public PureComponentsPanel(XApplet applet) {
		super(applet);
	}
	
	private String xCatKey, factorKey;
	
	public void setupPanel(DataSet data, String xKey, String xCatKey, String yKey, String linKey, String factorKey,
																		String modelKey, int initialComponentDisplay, XApplet applet) {
		this.xCatKey = xCatKey;
		this.factorKey = factorKey;
		setupPanel(data, xKey, yKey, linKey, modelKey, initialComponentDisplay, applet);
	}
	
	protected DataWithComponentView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
													String xKey, String yKey, String lsKey, String modelKey, int initialComponentDisplay,
													XApplet applet) {
		return new PureComponentsView(data, applet, theHorizAxis, theVertAxis, xKey, xCatKey,
															yKey, lsKey, factorKey, modelKey, initialComponentDisplay);
	}
}