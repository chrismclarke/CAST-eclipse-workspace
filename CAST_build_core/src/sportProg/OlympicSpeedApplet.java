package sportProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import valueList.*;

import time.*;
import sport.*;

public class OlympicSpeedApplet extends BasicSpeedApplet {
	static final private String SPEED_DISTN_PARAM = "speedDistn";
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		
		int sampleSize[] = readSampleSizes();
		
		NormalDistnVariable popnDistn = createPopulationVariable(data, "popn");
		popnDistn.setParams(getParameter(SPEED_DISTN_PARAM));
		
		createSimulationVariable(data, sampleSize, "popn", "p", "y");
		createExpectedVariable(data, sampleSize, "popn", "expected");
		
		return data;
	}
	
	protected SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "p");
		return summaryData;
	}
	
	protected TimeView getTimeView(DataSet theData, String dataName,
																TimeAxis timeAxis, VertAxis numAxis) {
		return new WinningTimeView(data, this, timeAxis, numAxis, null, null);
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		return timeSeriesPanel(data, "y", "expected");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		thePanel.add(createAlgorithmCheck());
		thePanel.add(createSampleButton());
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		OneValueView actual = new OneValueView(data, "y", this);
		thePanel.add(actual);
		return thePanel;
	}
}