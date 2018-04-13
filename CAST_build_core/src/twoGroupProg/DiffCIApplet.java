package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import models.*;
import coreSummaries.*;

import inferenceProg.*;
import twoGroup.*;


public class DiffCIApplet extends SampleIntervalApplet {
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	
	protected DataSet getData() {
		GroupsDataSet anovaData = new GroupsDataSet(this);
		NumSampleVariable error = (NumSampleVariable)anovaData.getVariable("error");
		error.generateNextSample();
		
		GroupsModelVariable model = (GroupsModelVariable)anovaData.getVariable("model");
		NumValue mean1 = model.getMean(0);
		NumValue mean2 = model.getMean(1);
		modelMean = new NumValue(mean2.toDouble() - mean1.toDouble(), Math.max(mean1.decimals, mean2.decimals));
		return anovaData;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		GroupsDataSet anovaData = (GroupsDataSet)sourceData;
		
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		int decimals = anovaData.getSummaryDecimals();
		
		double level = Double.parseDouble(getParameter(CONFIDENCE_LEVEL_PARAM));
		NumValue maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		DiffCIVariable ci = new DiffCIVariable(getParameter(MEAN_NAME_PARAM), level, anovaData,
					decimals, new IntervalValue(maxSummary.toDouble(), maxSummary.toDouble(), maxSummary.decimals));
		summaryData.addVariable("ci", ci);
		
		return summaryData;
	}
	
	private XPanel yNamePanel(DataSet data, VertAxis yAxis) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel yVariateName = new XLabel(anovaData.getYVarName(), XLabel.LEFT, this);
		yVariateName.setFont(yAxis.getFont());
		thePanel.add(yVariateName);
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data, String variableKey, String modelKey) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel viewPanel = new XPanel();
			viewPanel.setLayout(new AxisLayout());
			
				VertAxis yAxis = new VertAxis(this);
				String labelInfo = anovaData.getYAxisInfo();
				yAxis.readNumLabels(labelInfo);
			viewPanel.add("Left", yAxis);
			
				HorizAxis theGroupAxis = new HorizAxis(this);
				CatVariable groupVariable = data.getCatVariable();
				theGroupAxis.setCatLabels(groupVariable);
			viewPanel.add("Bottom", theGroupAxis);
			
				VerticalDotView theView = new VerticalDotView(data, this, yAxis, theGroupAxis, "y", "x", "model", 0.5);
				theView.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
				theView.setShow50PercentBand(true);
				theView.lockBackground(Color.white);
				
			viewPanel.add("Center", theView);
		
		thePanel.add("Center", viewPanel);
		thePanel.add("North", yNamePanel(data, yAxis));
		
		return thePanel;
	}
	
	protected boolean onlyShowSummaryScale() {
		return true;
	}
	
	protected void doTakeSample() {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		anovaData.resetLSEstimates();
		summaryData.takeSample();
	}
}