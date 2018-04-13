package sportProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;

import time.*;
import sport.*;


public class OlympicDataApplet extends BasicSpeedApplet {
	protected DataSet createData() {
		DataSet data = new DataSet();
		createRealDataVariable(data, "y");
		return data;
	}
	
	protected SummaryDataSet createSummaryData(DataSet sourceData) {
		return null;
	}
	
	protected TimeView getTimeView(DataSet theData, String dataName,
																TimeAxis timeAxis, VertAxis numAxis) {
		return new WinningTimeView(data, this, timeAxis, numAxis, null, null);
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		return timeSeriesPanel(data, "y", null);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
		OneValueView actual = new OneValueView(data, "y", this);
		thePanel.add(actual);
		OneValueView label = new OneValueView(data, "label", this);
		thePanel.add(label);
		return thePanel;
	}
}