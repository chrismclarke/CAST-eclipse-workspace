package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import models.*;

import twoGroup.*;


public class SampleHeightApplet extends CoreDiffApplet {
	static final private String MAX_HEIGHT_PARAM = "maxHeight";
	
	protected CoreModelDataSet readData() {
		return new GroupsDataSet(this);
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		TwoGroupSummary diff = new TwoGroupSummary("Male minus female",
									sourceData.getSummaryDecimals(), TwoGroupSummary.DIFFERENCE);
		summaryData.addVariable("difference", diff);
		
		TwoGroupSummary male = new TwoGroupSummary("Male height",
									sourceData.getSummaryDecimals(), TwoGroupSummary.MEAN2);
		summaryData.addVariable("male", male);
		
		TwoGroupSummary female = new TwoGroupSummary("Female height",
									sourceData.getSummaryDecimals(), TwoGroupSummary.MEAN1);
		summaryData.addVariable("female", female);
		
		summaryData.takeSample();
		
		NormalDistnVariable diffTheory = new NormalDistnVariable("diffTheory");
		diffTheory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("diffTheory", diffTheory);
		
		NormalDistnVariable maleTheory = new NormalDistnVariable("maleTheory");
		maleTheory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("maleTheory", maleTheory);
		
		NormalDistnVariable femaleTheory = new NormalDistnVariable("femaleTheory");
		femaleTheory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("femaleTheory", femaleTheory);
		
		setTheoryParams(sourceData, summaryData);
		
		return summaryData;
	}
	
	protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData) {
		NormalDistnVariable diffTheory = (NormalDistnVariable)summaryData.getVariable("diffTheory");
		NormalDistnVariable maleTheory = (NormalDistnVariable)summaryData.getVariable("maleTheory");
		NormalDistnVariable femaleTheory = (NormalDistnVariable)summaryData.getVariable("femaleTheory");
		
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		double s1 = model.getSD(0).toDouble();
		double s2 = model.getSD(1).toDouble();
		
		diffTheory.setMean(model.getMean(1).toDouble() - model.getMean(0).toDouble());
		diffTheory.setSD(Math.sqrt(s1 * s1 + s2 * s2));
		
		maleTheory.setMean(model.getMean(1).toDouble());
		maleTheory.setSD(s2);
		
		femaleTheory.setMean(model.getMean(0).toDouble());
		femaleTheory.setSD(s1);
	}
	
	protected double getLeftProportion() {
		return 0.5;
	}
	
	protected XPanel dataPanel(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		HeightsView theView = new HeightsView(anovaData, this, "y", "x", new NumValue(getParameter(MAX_HEIGHT_PARAM)));
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected String getSummaryYAxisInfo(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		return anovaData.getYAxisInfo();
	}
	
	protected XPanel summaryPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.33, 5, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.TOP, groupPlotPanel(anovaData, summaryData, "male",
																									"maleTheory", 1));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
			bottomPanel.add(ProportionLayout.TOP, groupPlotPanel(anovaData, summaryData, "female",
																									"femaleTheory", 0));
		
			bottomPanel.add(ProportionLayout.BOTTOM, differencePlotPanel(summaryData,
																						"difference", "diffTheory"));
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}
}