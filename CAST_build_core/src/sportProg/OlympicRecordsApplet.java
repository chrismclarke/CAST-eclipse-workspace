package sportProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import coreGraphics.*;

import sport.*;


public class OlympicRecordsApplet extends OlympicSpeedApplet {
	static final private String RECORDS_NAME_PARAM = "recordsName";
	
	static final private String RECORD_AXIS_INFO_PARAM = "recordAxis";
	
	protected SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.createSummaryData(sourceData);
		
		RecordsVariable records = new RecordsVariable(getParameter(RECORDS_NAME_PARAM), "y");
		summaryData.addVariable("records", records);
		return summaryData;
	}
	
	protected XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, timeSeriesPanel(data, "y", "expected"));
		
		thePanel.add(ProportionLayout.RIGHT, summaryPanel(summaryData));
		return thePanel;
	}
	
	private XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(RECORD_AXIS_INFO_PARAM));
			NumVariable winnerVar = (NumVariable)summaryData.getVariable("records");
			horizAxis.setAxisName(winnerVar.name);
		thePanel.add("Bottom", horizAxis);
		
			StackedDiscreteView winnerView = new StackedDiscreteView(summaryData, this, horizAxis, "records");
			winnerView.lockBackground(Color.white);
		thePanel.add("Center", winnerView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		thePanel.add(createAccumulateCheck());
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, super.valuePanel(data, summaryData));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
				OneValueView record = new OneValueView(summaryData, "records", this);
			rightPanel.add(record);
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		return thePanel;
	}
}