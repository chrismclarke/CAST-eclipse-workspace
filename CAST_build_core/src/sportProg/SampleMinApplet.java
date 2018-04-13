package sportProg;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import randomStatProg.*;
import sport.*;


public class SampleMinApplet extends SampleMean2Applet {
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		MinimumVariable min = new MinimumVariable(getParameter(MEAN_NAME_PARAM), "y", "y");
		
		summaryData.addVariable("mean", min);
		
		return summaryData;
	}
	
	protected void setTheoryParameters(SummaryDataSet summaryData, String theoryKey) {
	}
	
	protected XPanel summaryPanel(DataSet data, String variableKey, String modelKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = getAxis(data, variableKey);
		thePanel.add("Bottom", theHorizAxis);
		
		DotPlotView localView = new DotPlotView(data, this, theHorizAxis, 1.0);
		thePanel.add("Center", localView);
		localView.lockBackground(Color.white);
		
		return thePanel;
	}
}