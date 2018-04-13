package sportProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import coreGraphics.*;

import sport.*;


abstract public class CoreLeagueTableApplet extends XApplet {
	static final private String TABLE_NAME_PARAM = "tableName";
	static final private String DRAW_PROB_PARAM = "drawProb";
	static final private String TEAMA_PROB_PARAM = "teamAProb";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String TEAMS_PARAM = "teams";
	
	static final protected boolean STACKED = true;
	static final protected boolean JITTERED = false;
	
	protected NumValue lowTeam0Prob, highTeam0Prob, initialTeam0Prob;
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout());
			
		add("Center", dataPanel(data, summaryData));
			
		add("South", controlPanel(data, summaryData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		RandomResult generator = new RandomResult(getParameter(RANDOM_SEED_PARAM));
		double drawProb = Double.parseDouble(getParameter(DRAW_PROB_PARAM));
		StringTokenizer st = new StringTokenizer(getParameter(TEAMA_PROB_PARAM));
		lowTeam0Prob = new NumValue(st.nextToken());
		highTeam0Prob = new NumValue(st.nextToken());
		initialTeam0Prob = new NumValue(st.nextToken());
		
		st = new StringTokenizer(getParameter(TEAMS_PARAM));
		
		LeagueResultsVariable results;
		if (st.countTokens() == 1)
			results = new LeagueResultsVariable(getParameter(TABLE_NAME_PARAM),
						Integer.parseInt(st.nextToken()), 1, generator, drawProb, initialTeam0Prob.toDouble());
		else {
			int noOfTeams = st.countTokens();
			String teamName[] = new String[noOfTeams];
			for (int i=0 ; i<noOfTeams ; i++)
				teamName[i] = st.nextToken();
			results = new LeagueResultsVariable(getParameter(TABLE_NAME_PARAM),
												teamName, 1, generator, drawProb, initialTeam0Prob.toDouble());
		}
		
		data.addVariable("results", results);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "results");
		return summaryData;
	}
	
	abstract protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData);
	abstract protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData);
	
	protected XPanel createSummaryPanel(SummaryDataSet summaryData, String summaryKey,
															String axisInfo, boolean stackedNotJittered) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				CoreVariable rankVar = summaryData.getVariable(summaryKey);
				XLabel varNameLabel = new XLabel(rankVar.name, XLabel.LEFT, this);
				varNameLabel.setFont(getStandardBoldFont());
				
			topPanel.add(varNameLabel);
				
		thePanel.add("North", topPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(axisInfo);
			mainPanel.add("Left", vertAxis);
				
				DataView theView;
				if (stackedNotJittered) {
					theView = new StackedDiscreteView(summaryData, this, vertAxis, summaryKey);
					theView.setCrossSize(DataView.SMALL_CROSS);
				}
				else {
				
					theView = new DotPlotView(summaryData, this, vertAxis, 1.0);
					theView.setActiveNumVariable(summaryKey);
				}
				theView.lockBackground(Color.white);
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected XPanel createSamplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 3));
		
			XPanel topRightPanel = new XPanel();
			topRightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			
				takeSampleButton = new RepeatingButton(translate("Run League"), this);
			topRightPanel.add(takeSampleButton);
			
				accumulateCheck = new XCheckbox(translate("Accumulate"), this);
			topRightPanel.add(accumulateCheck);
			
		thePanel.add(topRightPanel);
			ValueCountView theCount = new ValueCountView(summaryData, this);
			theCount.setLabel("n = ");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	protected XPanel createSimpleSamplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 3));
		
			takeSampleButton = new RepeatingButton(translate("Run League"), this);
		thePanel.add(takeSampleButton);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}
	
	protected void doReset() {
		summaryData.setAccumulate(accumulateCheck.getState());
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			doReset();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}