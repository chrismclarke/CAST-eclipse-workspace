package sportProg;

import java.awt.*;

import dataView.*;

import sport.*;


public class SimpleLeagueTableApplet extends CoreLeagueTableApplet {
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(30, 0));
		thePanel.add("Center", new LeagueResultsView(data, this, "results"));
			LeaguePointsView table = new LeaguePointsView(data, this, "results");
			table.setKeepFooter(true);
		thePanel.add("East", table);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		return createSimpleSamplingPanel();
	}
}