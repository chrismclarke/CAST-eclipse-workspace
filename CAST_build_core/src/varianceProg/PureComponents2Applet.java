package varianceProg;

import java.awt.*;

import dataView.*;


public class PureComponents2Applet extends PureComponentsApplet {
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		return thePanel;
	}
}