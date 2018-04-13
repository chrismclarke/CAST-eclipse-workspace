package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import sport.*;
import simulation.*;


public class PropnCIXApplet extends XApplet {
	static final private String TABLE_NAME_PARAM = "tableName";
	static final private String DRAW_PROB_PARAM = "drawProb";
//	static final private String TEAMA_PROB_PARAM = "initialProbIndex";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String TEAM_COUNT_PARAM = "teamCount";
	
	static final private String RANK_NAME_PARAM = "rankName";
	static final private String WIN_PROB_NAME_PARAM = "teamAWinProbName";
	static final private String WINNER_NAME_PARAM = "winnerName";
	static final private String WINNER_LABELS_PARAM = "winnerLabels";
	
	static final private String MATCH_WIN_PARAM = "matchWinProb";
	static final private String INITIAL_INDEX_PARAM = "initialProbIndex";
	
	static final private String CI_DECIMALS_PARAM = "ciDecimals";
	
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	
	private int initialTeam0ProbIndex;
	private NumValue[] pMatchWin;
	
	private XButton resetButton;
	private RepeatingButton nextButton;
	private XChoice algorithmChoice;
	private int algChoiceIndex = 0;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private PropnCIsView theTable, theIntervalDisplay;
	private ClearOneValueView winProbValue, rankValue, winnerValue;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		add("West", leftPanel(data, summaryData));
		add("Center", mainPanel(data, summaryData));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		RandomResult generator = new RandomResult(getParameter(RANDOM_SEED_PARAM));
		double drawProb = Double.parseDouble(getParameter(DRAW_PROB_PARAM));
		
		StringTokenizer st = new StringTokenizer(getParameter(MATCH_WIN_PARAM));
		pMatchWin = new NumValue[st.countTokens()];
		for (int i=0 ; i<pMatchWin.length ; i++)
			pMatchWin[i] = new NumValue(st.nextToken());
			
		initialTeam0ProbIndex = Integer.parseInt(getParameter(INITIAL_INDEX_PARAM));
		
		LeagueResultsVariable results = new LeagueResultsVariable(getParameter(TABLE_NAME_PARAM),
								Integer.parseInt(getParameter(TEAM_COUNT_PARAM)), 1, generator,
								drawProb, pMatchWin[initialTeam0ProbIndex].toDouble());
		
		data.addVariable("results", results);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "results");
		
		TeamARankVariable rankVar = new TeamARankVariable(getParameter(RANK_NAME_PARAM),
																"results", TeamARankVariable.FROM_LEAGUE);
		summaryData.addVariable("rank", rankVar);
		
		WinProbAVariable winProb = new WinProbAVariable(getParameter(WIN_PROB_NAME_PARAM),
												"results", pMatchWin[initialTeam0ProbIndex].decimals);
		summaryData.addVariable("winProb", winProb);
		
		RankOneVariable rankOne = new RankOneVariable(getParameter(WINNER_NAME_PARAM), summaryData,
															"rank", getParameter(WINNER_LABELS_PARAM));
		summaryData.addVariable("winner", rankOne);
		
		summaryData.setAccumulate(true);
		return summaryData;
	}
	
	private XPanel leftPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", resultPanel(data, summaryData));
		
		thePanel.add("South", bottomPanel(data, summaryData));
		
		return thePanel;
	}
	
	private XPanel bottomPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																		VerticalLayout.VERT_CENTER, 3));
		winProbValue = new ClearOneValueView(summaryData, "winProb", this, pMatchWin[initialTeam0ProbIndex]);
		thePanel.add(winProbValue);
		
			XPanel teamAResultPanel = new XPanel();
			teamAResultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
			rankValue = new ClearOneValueView(summaryData, "rank", this, new NumValue(99, 0));
			teamAResultPanel.add(rankValue);
			winnerValue = new ClearOneValueView(summaryData, "winner", this);
			teamAResultPanel.add(winnerValue);
		
		thePanel.add(teamAResultPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				nextButton = new RepeatingButton(translate("Next run"), this);
			buttonPanel.add(nextButton);
			
				resetButton = new XButton(translate("Reset"), this);
			buttonPanel.add(resetButton);
		
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	private XPanel resultPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			int ciDecimals = Integer.parseInt(getParameter(CI_DECIMALS_PARAM));
			theTable = new PropnCIsListView(summaryData, this,
										"winProb", "winner", pMatchWin, initialTeam0ProbIndex, ciDecimals);
		thePanel.add("Center", theTable);
		
		return thePanel;
	}
	
	private XPanel mainPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", new XLabel(translate("Confidence Interval"), XLabel.LEFT, this));
		
			XPanel ciPanel = new XPanel();
			ciPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
				String labelInfo = getParameter(X_AXIS_INFO_PARAM);
				theHorizAxis.readNumLabels(labelInfo);
				theHorizAxis.setAxisName(getParameter(WIN_PROB_NAME_PARAM));
			ciPanel.add("Bottom", theHorizAxis);
			
				VertAxis theVertAxis = new VertAxis(this);
				labelInfo = getParameter(Y_AXIS_INFO_PARAM);
				theVertAxis.readNumLabels(labelInfo);
			ciPanel.add("Left", theVertAxis);
			
				theIntervalDisplay = new PropnCIsDrawView(summaryData, this, "winProb", "winner",
											pMatchWin, initialTeam0ProbIndex, theHorizAxis, theVertAxis);
				theIntervalDisplay.lockBackground(Color.white);
			ciPanel.add("Center", theIntervalDisplay);
		
		thePanel.add("Center", ciPanel);
		
			XPanel algChoicePanel = new XPanel();
			algChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			algChoicePanel.add(new XLabel(translate("Algorithm") + ":", XLabel.LEFT, this));
				
				algorithmChoice = new XChoice(this);
				algorithmChoice.addItem(translate("From normal approx"));
				algorithmChoice.addItem(translate("Exact"));
			algChoicePanel.add(algorithmChoice);
			
		thePanel.add("South", algChoicePanel);
		
		return thePanel;
	}
	
	public void selectWinProb(int probIndex) {
		if (theTable != null)
			theTable.selectProb(probIndex);
		if (theIntervalDisplay != null)
			theIntervalDisplay.selectProb(probIndex);
		
		LeagueResultsVariable resultsVar = (LeagueResultsVariable)data.getVariable("results");
		resultsVar.setTeam0Prob(pMatchWin[probIndex].toDouble());
		
		winProbValue.setClear(true);
		rankValue.setClear(true);
		winnerValue.setClear(true);
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
		winProbValue.setClear(false);
		rankValue.setClear(false);
		winnerValue.setClear(false);
	}
	
	private void doReset() {
		summaryData.setSingleSummaryFromData();
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
			doTakeSample();
			return true;
		}
		else if (target == resetButton) {
			doReset();
			return true;
		}
		else if (target == algorithmChoice) {
			int newChoice = algorithmChoice.getSelectedIndex();
			if (newChoice != algChoiceIndex) {
				algChoiceIndex = newChoice;
				theTable.setAccurateAlgorithm(algChoiceIndex > 0);
				theIntervalDisplay.setAccurateAlgorithm(algChoiceIndex > 0);
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}