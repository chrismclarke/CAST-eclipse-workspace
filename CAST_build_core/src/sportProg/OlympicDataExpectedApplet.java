package sportProg;

import dataView.*;
import axis.*;
import distn.*;
import imageGroups.*;

import time.*;
import sampling.*;
import sport.*;


public class OlympicDataExpectedApplet extends OlympicDataApplet {
	static final private String SPEED_DISTN_PARAM = "speedDistn";
	
	protected DataSet createData() {
		DataSet data = super.createData();
		
		int sampleSize[] = readSampleSizes();
		
		NormalDistnVariable popnDistn = createPopulationVariable(data, "popn");
		popnDistn.setParams(getParameter(SPEED_DISTN_PARAM));
		
		createExpectedVariable(data, sampleSize, "popn", "expected");
		
		return data;
	}
	
	protected TimeView getTimeView(DataSet theData, String dataName,
																TimeAxis timeAxis, VertAxis numAxis) {
		return new WinningTimeView(data, this, timeAxis, numAxis, "expected", "popn");
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		return timeSeriesPanel(data, "y", "expected");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		
		MeanSDImages.loadMeanSD(this);
		
		SummaryView popnMean = new SummaryView(data, this, "popn", null, SummaryView.MEAN, 5, SummaryView.POPULATION);
		thePanel.add(popnMean);
		
		SummaryView popnSD = new SummaryView(data, this, "popn", null, SummaryView.SD, 5, SummaryView.POPULATION);
		thePanel.add(popnSD);
		
		return thePanel;
	}
}