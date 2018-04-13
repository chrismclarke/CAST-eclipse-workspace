package sportProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;

import sport.*;


public class LeagueRangeApplet extends CoreLeagueTableApplet {
	static final private String RANGE_AXIS_INFO_PARAM = "rangeAxis";
	static final private String RANGE_NAME_PARAM = "rangeName";
	static final private String MAX_RANGE_PARAM = "maxRange";
	static final private String SD_AXIS_INFO_PARAM = "sdAxis";
	static final private String SD_NAME_PARAM = "sdName";
	static final private String MAX_SD_PARAM = "maxSD";
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		PointSpreadVariable rangeVar = new PointSpreadVariable(getParameter(RANGE_NAME_PARAM),
																			"results", PointSpreadVariable.RANGE);
		summaryData.addVariable("range", rangeVar);
		
		PointSpreadVariable sdVar = new PointSpreadVariable(getParameter(SD_NAME_PARAM),
																			"results", PointSpreadVariable.SD);
		summaryData.addVariable("sd", sdVar);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		thePanel.add("West", new LeaguePointsView(data, this, "results"));
		thePanel.add("Center", summaryPanel(summaryData));
		
		return thePanel;
	}
	
	private XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, createSummaryPanel(summaryData, "range",
														getParameter(RANGE_AXIS_INFO_PARAM), JITTERED));
		thePanel.add(ProportionLayout.RIGHT, createSummaryPanel(summaryData, "sd",
														getParameter(SD_AXIS_INFO_PARAM), JITTERED));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 10));
			leftPanel.add(new OneValueView(summaryData, "range", this, new NumValue(getParameter(MAX_RANGE_PARAM))));
			leftPanel.add(new OneValueView(summaryData, "sd", this, new NumValue(getParameter(MAX_SD_PARAM))));
		
		thePanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = createSamplingPanel();
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
}