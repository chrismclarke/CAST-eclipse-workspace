package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import twoGroup.*;


public class SampleDiff2Applet extends CoreDiffApplet {
	static final private String JITTER_PARAM = "jitterPropn";
	
	protected CoreModelDataSet readData() {
		return new GroupsDataSet(this);
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		TwoGroupSummary diff = new TwoGroupSummary(translate("Difference between means"),
									sourceData.getSummaryDecimals(), TwoGroupSummary.DIFFERENCE);
		summaryData.addVariable("difference", diff);
		summaryData.takeSample();
		
		return summaryData;
	}
	
	protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData) {
	}
	
	protected double getLeftProportion() {
		return 0.45;
	}
	
	protected XPanel dataPanel(CoreModelDataSet data) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new AxisLayout());
			
				VertAxis theNumAxis = new VertAxis(this);
				String labelInfo = anovaData.getYAxisInfo();
				theNumAxis.readNumLabels(labelInfo);
			displayPanel.add("Left", theNumAxis);
			
				HorizAxis theGroupAxis = new HorizAxis(this);
				CatVariable groupVariable = data.getCatVariable();
				theGroupAxis.setCatLabels(groupVariable);
				theGroupAxis.setAxisName(data.getXVarName());
			displayPanel.add("Bottom", theGroupAxis);
			
				double jitterPropn = Double.parseDouble(getParameter(JITTER_PARAM));
				VerticalDotView theDotPlot = new VerticalDotView(data, this, theNumAxis, theGroupAxis,
																															"y", "x", "model", jitterPropn);
				theDotPlot.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
				theDotPlot.setShow50PercentBand(true);
				theDotPlot.lockBackground(Color.white);
			displayPanel.add("Center", theDotPlot);
		
		thePanel.add("Center", displayPanel);
		
			XLabel responseNameLabel = new XLabel(data.getYVarName(), XLabel.LEFT, this);
			responseNameLabel.setFont(theNumAxis.getFont());
			
		thePanel.add("North", responseNameLabel);
		
		return thePanel;
	}
	
	protected String getSummaryYAxisInfo(CoreModelDataSet data) {
		return null;
	}
	
	protected XPanel summaryPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL,
																							ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.TOP, summaryStatisticPanel(data));
		thePanel.add(ProportionLayout.BOTTOM, summaryPlotPanel(data, summaryData));
		
		return thePanel;
	}
	
	private XPanel summaryStatisticPanel(CoreModelDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		String maxSummaryString = getParameter(MAX_SUMMARY_PARAM);
		
		GroupSummaryView mean1View = new GroupSummaryView(data, this, GroupSummaryView.X_BAR, 0,
																												maxSummaryString, data.getSummaryDecimals());
		mean1View.setForeground(Color.blue);
		thePanel.add(mean1View);
		
		GroupSummaryView mean2View = new GroupSummaryView(data, this, GroupSummaryView.X_BAR, 1, maxSummaryString,
																																				data.getSummaryDecimals());
		mean2View.setForeground(Color.blue);
		thePanel.add(mean2View);
		
		GroupSummary2View meanDiffView = new GroupSummary2View(data, this, GroupSummary2View.X_BAR_DIFF,
																													maxSummaryString, data.getSummaryDecimals());
		meanDiffView.setForeground(Color.red);
		thePanel.add(meanDiffView);
		
		return thePanel;
	}
	
	protected XPanel summaryPlotPanel(CoreModelDataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theAxis = new HorizAxis(this);
			theAxis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			theAxis.setAxisName(summaryData.getVariable("difference").name);
			theAxis.setForeground(Color.red);
		thePanel.add("Bottom", theAxis);
		
			GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
			double meanDiff = yDistn.getMean(1).toDouble() - yDistn.getMean(0).toDouble();
			StackedWithArrowView summaryDotPlot = new StackedWithArrowView(summaryData, this, theAxis, "difference", meanDiff,
																													"groups/muDiffGray.png");
			summaryDotPlot.setForeground(Color.red);
			summaryDotPlot.lockBackground(Color.white);
		thePanel.add("Center", summaryDotPlot);
		
		return thePanel;
	}
}