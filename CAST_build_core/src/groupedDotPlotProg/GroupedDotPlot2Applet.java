package groupedDotPlotProg;

import dataView.*;

import dotPlot.*;


public class GroupedDotPlot2Applet extends GroupedDotPlotApplet {
	
	protected XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = super.dotPlotPanel(data);
		theDotPlot.setInitialFrame(GroupedDotPlotView.kEndFrame);
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = super.controlPanel();
		animateSlider.setValue(GroupedDotPlotView.kEndFrame);
		return thePanel;
	}
}