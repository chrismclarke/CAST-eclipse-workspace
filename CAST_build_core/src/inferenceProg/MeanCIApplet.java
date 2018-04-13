package inferenceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;

import inference.*;


public class MeanCIApplet extends EstMeanDistnApplet {
	protected FirstValueView interval;
	
	public void setupApplet() {
		super.setupApplet();
		
		dataSDView.show(true);
		estMeanView.show(true);
		estSDView.show(true);
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		int noOfValues = ((NumVariable)sourceData.getVariable("y")).noOfValues();
		
		MeanCIVariable meanCI = new MeanCIVariable("ci", 1.0, noOfValues - 1, "y",
																									displayDecimals[0]);
		summaryData.addVariable("ci", meanCI);
		
		return summaryData;
	}
	
	protected void setSummaryInfo(SummaryDataSet summaryData, String sumValueKey,
						String sumTheoryKey, DataSet sourceData, String sourceVarKey, int decimals,
						String summaryName) {
		int noOfValues = ((NumVariable)sourceData.getVariable("y")).noOfValues();
		
		MeanCIVariable ciVar = (MeanCIVariable)summaryData.getVariable("ci");
		ciVar.setDecimals(decimals);
		ciVar.setDF(noOfValues - 1);
		super.setSummaryInfo(summaryData, sumValueKey, sumTheoryKey, sourceData, sourceVarKey,
																						decimals, summaryName);
//		-------------------------		//		changed for Netscape 4.5/Mac
		if (interval != null)
			interval.paint(interval.getGraphics());
//		----------------  ( addition should not be necessary)
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		summaryAxis = getAxis(summaryData, variableKey);
		thePanel.add("Bottom", summaryAxis);
		
		JitteredPlusCIView localView = new JitteredPlusCIView(summaryData, this, summaryAxis, modelKey, "ci", 1.0);
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		
		localView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
		localView.setDensityColor(Color.lightGray);
		
		summaryView = localView;
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		thePanel.add(intervalPanel(summaryData, "ci"));
		thePanel.add(dataChoicePanel());
		
		return thePanel;
	}
	
	protected XPanel intervalPanel(SummaryDataSet summaryData, String ciKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		interval = new FirstValueView(summaryData, ciKey, this);
		interval.setLabel("Interval estimate is");
		thePanel.add(interval);
		
		return thePanel;
	}
}