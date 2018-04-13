package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import valueList.*;

import distribution.*;
import randomStat.*;
import simulation.*;
import sport.*;
import sportProg.*;


public class BinomHalfTestApplet extends CoreLeagueTableApplet {
	static final private String RANK_NAME_PARAM = "rankName";
	static final private String FINAL_RANK_NAME_PARAM = "finalRankName";
	
//	static final private String kProbAxisInfo = "0 1 0 0.2";
	static final private NumValue kMaxRank = new NumValue(99, 0);
	
	private ParameterSlider team0ProbSlider;
	
	private DataSet binomialDataSet;
	private HorizAxis nAxis;
	private DiscreteProbValueView probView;
	private LeagueFinalsView summaryView;
	
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
		
		TeamARankVariable finalRankVar = new TeamARankVariable(getParameter(FINAL_RANK_NAME_PARAM),
																"results", TeamARankVariable.FROM_FINALS);
		summaryData.addVariable("final", finalRankVar);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("West", leftPanel(data, summaryData));
		thePanel.add("Center", rightPanel(data, summaryData));
		return thePanel;
	}
	
	private XPanel leftPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																			VerticalLayout.VERT_CENTER, 40));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 2));
		
			topPanel.add(new OneValueView(summaryData, "rank", this, kMaxRank));
			topPanel.add(new OneValueView(summaryData, "final", this, kMaxRank));
		thePanel.add(topPanel);
		
		thePanel.add(createSamplingPanel());
		
			LeagueResultsVariable results = (LeagueResultsVariable)data.getVariable("results");
			String team0Name = results.getTeamNames()[0].toString();
			team0ProbSlider = new ParameterSlider(lowTeam0Prob, highTeam0Prob, initialTeam0Prob,
																		"P(" + team0Name + " " + translate("wins match") + ")", this);
			
		thePanel.add(team0ProbSlider);
		
		return thePanel;
	}
	
	private XPanel rightPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
			summaryView = new LeagueFinalsView(summaryData, this, "rank", "final");
		thePanel.add("North", summaryView);
		
		
		binomialDataSet = new DataSet();
		BinomialDistnVariable binVar = new BinomialDistnVariable("binomial distn");
		binVar.setCount(0);
		binVar.setProb(0.5);
		binomialDataSet.addVariable("binomial", binVar);
		
			XPanel barChartPanel = new XPanel();
			barChartPanel.setLayout(new AxisLayout());
			
				nAxis = new HorizAxis(this);
				nAxis.readNumLabels("0 1 0 1.000");
			barChartPanel.add("Bottom", nAxis);
			
				DiscreteProbView barChart = new DiscreteProbView(binomialDataSet, this, "binomial",
																							null, null, nAxis, DiscreteProbView.NO_DRAG);
				barChart.lockBackground(Color.white);
				barChart.setHighlightColor(Color.red);
				barChart.setDensityColor(Color.blue);
			barChartPanel.add("Center", barChart);
		
		thePanel.add("Center", barChartPanel);
		
			XPanel probViewPanel = new XPanel();
			probViewPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 7));
			
				probView = new DiscreteProbValueView(binomialDataSet, "binomial", this, 999);
				probView.setForeground(Color.red);
				probView.setFont(getStandardBoldFont());
				probView.setPrintWithHalves(false);
			probViewPanel.add(probView);
				
				BinomPValueView pValueView = new BinomPValueView(binomialDataSet, this, probView);
				pValueView.setFont(getStandardBoldFont());
			probViewPanel.add(pValueView);
			
		thePanel.add("South", probViewPanel);
		
		return thePanel;
	}
	
	private boolean fixCountAxis() {
		BinomialDistnVariable binVar = (BinomialDistnVariable)binomialDataSet.getVariable("binomial");
		int n = binVar.getCount();
		
		int oldN = (int)Math.round(nAxis.maxOnAxis);
		if (n == oldN)
			return n < 1.5;
		else {
			int nStep = (n <= 10) ? 1
							: (n <= 20) ? 2
							: (n <= 50) ? 5
							: (n <= 100) ? 10
							: (n <= 200) ? 20
							: 50;
			nAxis.readNumLabels("0 " + (n<1 ? 1 : n) + " 0 " + nStep);
			return true;
		}
	}
	
	private void fixBinomialTotal() {
		summaryView.updateCounts();
		int[][] n = summaryView.getCounts();
		int nTotal = n[0][1] + n[1][0];
		BinomialDistnVariable binVar = (BinomialDistnVariable)binomialDataSet.getVariable("binomial");
		binVar.setCount(nTotal);
	}
	
	private void fixBinomialSelection() {
		summaryView.updateCounts();
		int[][] n = summaryView.getCounts();
		int n01 = n[0][1];
		int n10 = n[1][0];
		int nTotal = n01 + n10;
		double min = Double.NEGATIVE_INFINITY;
		double max = Double.POSITIVE_INFINITY;
		if (nTotal == 0)
			max = 0.5;
		else if (n01 <= n10)
			max = n01 + 0.5;
		else
			min = n01 - 0.5;
		
		BinomialDistnVariable binVar = (BinomialDistnVariable)binomialDataSet.getVariable("binomial");
		binVar.setMinSelection(min);
		binVar.setMaxSelection(max);
		
		binomialDataSet.variableChanged("binomial");		//		redraws barchart for new axis
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected void doTakeSample() {
		super.doTakeSample();
		
		fixBinomialTotal();
		if (fixCountAxis())
			fixBinomialSelection();
	}
	
	protected void doReset() {
		super.doReset();
		
		fixBinomialTotal();
		if (fixCountAxis())
			fixBinomialSelection();
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
		
			fixBinomialTotal();
			if (fixCountAxis())
				fixBinomialSelection();
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