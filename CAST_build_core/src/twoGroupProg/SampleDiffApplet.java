package twoGroupProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import distn.*;
import models.*;

import twoGroup.*;


public class SampleDiffApplet extends CoreDiffApplet {
	static final private String JITTER_PARAM = "jitterPropn";
	
	private XLabel responseNameLabel;
	
	private VertAxis theNumAxis;
	private HorizAxis theGroupAxis;
	
	protected CoreModelDataSet readData() {
		return new GroupsDataSet(this);
	}
	
	protected SummaryDataSet getSummaryData(CoreModelDataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		TwoGroupSummary diff = new TwoGroupSummary("Difference between means",
									sourceData.getSummaryDecimals(), TwoGroupSummary.DIFFERENCE);
		summaryData.addVariable("difference", diff);
		summaryData.takeSample();
		
		NormalDistnVariable diffTheory = new NormalDistnVariable("theory");
		diffTheory.setDecimals(sourceData.getSummaryDecimals());
		summaryData.addVariable("theory", diffTheory);
		setTheoryParams(sourceData, summaryData);
		
		return summaryData;
	}
	
	protected void setTheoryParams(CoreModelDataSet data, SummaryDataSet summaryData) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		
		NormalDistnVariable diffTheory = (NormalDistnVariable)summaryData.getVariable("theory");
		
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		diffTheory.setMean(model.getMean(1).toDouble() - model.getMean(0).toDouble());
		
		double s1 = model.getSD(0).toDouble();
		double s2 = model.getSD(1).toDouble();
		int n1 = anovaData.getN(0);
		int n2 = anovaData.getN(1);
		diffTheory.setSD(Math.sqrt(s1 * s1 / n1 + s2 * s2 / n2));
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
			
				theNumAxis = new VertAxis(this);
				String labelInfo = anovaData.getYAxisInfo();
				theNumAxis.readNumLabels(labelInfo);
			displayPanel.add("Left", theNumAxis);
			
				theGroupAxis = new HorizAxis(this);
				CatVariable groupVariable = data.getCatVariable();
				theGroupAxis.setCatLabels(groupVariable);
				theGroupAxis.setAxisName(data.getXVarName());
			displayPanel.add("Bottom", theGroupAxis);
			
				double jitterPropn = Double.parseDouble(getParameter(JITTER_PARAM));
				VerticalDotView theDotPlot = new VerticalDotView(data, this, theNumAxis, theGroupAxis, "y", "x", "model",
																																		jitterPropn);
				theDotPlot.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
				theDotPlot.lockBackground(Color.white);
			displayPanel.add("Center", theDotPlot);
		
		thePanel.add("Center", displayPanel);
		
			responseNameLabel = new XLabel(data.getYVarName(), XLabel.LEFT, this);
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
		thePanel.add(ProportionLayout.BOTTOM, summaryPlotPanel(summaryData));
		
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
		
		GroupSummaryView mean2View = new GroupSummaryView(data, this, GroupSummaryView.X_BAR, 1,
																						maxSummaryString, data.getSummaryDecimals());
		mean2View.setForeground(Color.blue);
		thePanel.add(mean2View);
		
		GroupSummary2View meanDiffView = new GroupSummary2View(data, this, GroupSummary2View.X_BAR_DIFF,
																									maxSummaryString, data.getSummaryDecimals());
		meanDiffView.setForeground(Color.red);
		thePanel.add(meanDiffView);
		
		return thePanel;
	}
	
	private XPanel summaryPlotPanel(SummaryDataSet summaryData) {
		return differencePlotPanel(summaryData, "difference", "theory");
	}
}