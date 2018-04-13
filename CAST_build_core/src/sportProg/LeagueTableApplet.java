package sportProg;

import java.awt.*;

import dataView.*;
import utils.*;

import sport.*;


public class LeagueTableApplet extends CoreLeagueTableApplet {
	static final protected String RANK_AXIS_INFO_PARAM = "rankAxis";
	static final private String RANK_NAME_PARAM = "rankName";
	
	static final private Color kResultBackground = new Color(0xDDDDDD);
	
	protected AnonParameterSlider team0ProbSlider;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		LeagueResultsVariable results = (LeagueResultsVariable)data.getVariable("results");
		results.setAdjustmentType(LeagueResultsVariable.RESOLVE_TIES);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = super.getSummaryData(sourceData);
		
		TeamARankVariable rankVar = new TeamARankVariable(getParameter(RANK_NAME_PARAM),
																"results", TeamARankVariable.FROM_LEAGUE);
		summaryData.addVariable("rank", rankVar);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.7, 30, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			XPanel resultsPanel = new XPanel();
			resultsPanel.setLayout(new BorderLayout(30, 0));
			resultsPanel.add("Center", new LeagueResultsView(data, this, "results"));
				LeaguePointsView table = new LeaguePointsView(data, this, "results");
				table.setShowTeamA(true);
				table.setKeepFooter(true);
			resultsPanel.add("East", table);
			
		thePanel.add(ProportionLayout.LEFT, resultsPanel);
		thePanel.add(ProportionLayout.RIGHT, createSummaryPanel(summaryData, "rank",
															getParameter(RANK_AXIS_INFO_PARAM), STACKED));
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(0, 20, 0, 0);
		thePanel.setLayout(new BorderLayout(50, 0));
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				XPanel valuePanel = new XPanel();
				valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					LeagueResultsVariable results = (LeagueResultsVariable)data.getVariable("results");
					String team0Name = results.getTeamNames()[0].toString();
					
					TeamAValueView probView = new TeamAValueView(data, "results", this, "P(" + team0Name
																					+ " " + translate("wins") + ") =", TeamAValueView.WIN_PROB,
																					initialTeam0Prob.decimals);
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
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
			rightPanel.add(createSamplingPanel());
				
				XPanel propnPanel = new InsetPanel(12, 4);
				propnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					PropnATopView propnView = new PropnATopView(summaryData, "rank", this, "P(" + team0Name
																															+ " " + translate("is top") + ") =");
					propnView.setFont(getBigFont());
					
				propnPanel.add(propnView);
					
				propnPanel.lockBackground(kResultBackground);
			rightPanel.add(propnPanel);
		
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == team0ProbSlider) {
			NumValue team0Prob = team0ProbSlider.getParameter();
			LeagueResultsVariable results = (LeagueResultsVariable)data.getVariable("results");
			results.setTeam0Prob(team0Prob.toDouble());
			results.clearSample();
			
			summaryData.clearData();
			summaryData.variableChanged("rank");
			
			data.variableChanged("results");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}