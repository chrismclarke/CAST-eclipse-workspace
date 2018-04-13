package inferenceProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreGraphics.*;

import inference.*;
//import randomStat.*;


public class AdjustLevelCIApplet extends MeanCIApplet {
	private ConfidenceLevelView levelView;
	
	public void setupApplet() {
		IntervalImages.loadIntervalImages(this);
		
		data = getData();
		summaryData = getSummaryData(data);
		setSummaryInfo(summaryData, "mean", "theory", data, "y", displayDecimals[0], meanName[0]);
		
		setLayout(new BorderLayout());
		
		XPanel dataPanel = new XPanel();
		dataPanel.setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																								ProportionLayout.TOTAL));
		dataPanel.add(ProportionLayout.LEFT, dataPanel(data, "y"));
		dataPanel.add(ProportionLayout.RIGHT, summaryPanel(summaryData, "mean", "theory"));
		
		add("Center", dataPanel);
		add("South", controlPanel());
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		summaryAxis = getAxis(summaryData, variableKey);
		thePanel.add("Bottom", summaryAxis);
		
		JitteredPlusCIView localView = new JitteredPlusCIView(summaryData, this, summaryAxis, modelKey, "ci", 1.0);
		summaryView = localView;
		thePanel.add("Center", summaryView);
		summaryView.lockBackground(Color.white);
		
		localView.setShowDensity(DataPlusDistnInterface.CONTIN_DISTN);
		localView.setDensityColor(Color.lightGray);
		
		return thePanel;
	}
	
	protected void setSummaryInfo(SummaryDataSet summaryData, String sumValueKey,
						String sumTheoryKey, DataSet sourceData, String sourceVarKey, int decimals,
						String summaryName) {
		super.setSummaryInfo(summaryData, sumValueKey, sumTheoryKey, sourceData, sourceVarKey,
																						decimals, summaryName);
//		-------------------------
		if (levelView != null)
			levelView.paint(levelView.getGraphics());
//		----------------  ( addition should not be necessary)
	}
	
	protected XPanel intervalPanel(SummaryDataSet summaryData, String ciKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
		
		CICalcPanel ciPanel = new CICalcPanel(summaryData, this, "ci");
		interval = ciPanel.interval;
		ciPanel.setFont(getBigFont());
		thePanel.add(ciPanel);
		
		XPanel levelPanel = new XPanel();			//		needed to centre levelView vertically
		levelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
		levelView = new ConfidenceLevelView(summaryData, ciKey, this);
		levelView.setForeground(Color.blue);
		levelView.setFont(getBigFont());
		levelPanel.add(levelView);
		
		thePanel.add(levelPanel);
		
		return thePanel;
	}
}