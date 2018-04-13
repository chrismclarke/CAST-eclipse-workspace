package sportProg;

import java.awt.*;

import dataView.*;
import utils.*;

import sport.*;


public class LeagueAndFinalsApplet extends LeagueTableApplet {
	static final private String FINAL_RANK_NAME_PARAM = "finalRankName";
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		TeamARankVariable finalRankVar = new TeamARankVariable(getParameter(FINAL_RANK_NAME_PARAM),
																"results", TeamARankVariable.FROM_FINALS);
		summaryData.addVariable("final", finalRankVar);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(40, 0));
		
			XPanel resultsPanel = new XPanel();
			resultsPanel.setLayout(new BorderLayout(40, 0));
		
				LeaguePointsView table = new LeaguePointsView(data, this, "results");
				table.setShowTeamA(true);
			resultsPanel.add("West", table);
			
				FinalResultsView finals = new FinalResultsView(data, this, "results");
			resultsPanel.add("Center", finals);
			
		thePanel.add("West", resultsPanel);
		
			XPanel summaryPanel = new XPanel();
			summaryPanel.setLayout(new ProportionLayout(0.5, 30, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			summaryPanel.add(ProportionLayout.LEFT, createSummaryPanel(summaryData, "rank",
															getParameter(RANK_AXIS_INFO_PARAM), STACKED));
			summaryPanel.add(ProportionLayout.RIGHT, createSummaryPanel(summaryData, "final",
															getParameter(RANK_AXIS_INFO_PARAM), STACKED));
		thePanel.add("Center", summaryPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(50, 0));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					LeagueResultsVariable results = (LeagueResultsVariable)data.getVariable("results");
					String team0Name = results.getTeamNames()[0].toString();
				
					TeamAValueView probView = new TeamAValueView(data, "results", this, "P(" + team0Name + " "
														+ translate("wins") + ") =", TeamAValueView.WIN_PROB, initialTeam0Prob.decimals);
					probView.setForeground(Color.blue);
				valuePanel.add(probView);
					probView = new TeamAValueView(data, "results", this, "  P(" + translate("draw") + ") =",
																										TeamAValueView.DRAW_PROB, initialTeam0Prob.decimals);
					probView.setForeground(Color.blue);
				valuePanel.add(probView);
					probView = new TeamAValueView(data, "results", this, "  P(" + translate("lose") + ") =",
																										TeamAValueView.LOSE_PROB, initialTeam0Prob.decimals);
					probView.setForeground(Color.blue);
				valuePanel.add(probView);
				
			sliderPanel.add(valuePanel);
			
				team0ProbSlider = new AnonParameterSlider(lowTeam0Prob, highTeam0Prob,
																							initialTeam0Prob, this);
			sliderPanel.add(team0ProbSlider);
		
		thePanel.add("Center", sliderPanel);
		
			XPanel rightPanel = createSamplingPanel();
				PropnATopView propnView = new PropnATopView(summaryData, "rank", this,
																																translate("P(top in league)") + " =");
			rightPanel.add(propnView);
				PropnATopView propnView2 = new PropnATopView(summaryData, "final", this,
																																translate("P(top of final)") + " =");
			rightPanel.add(propnView2);
		
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
}