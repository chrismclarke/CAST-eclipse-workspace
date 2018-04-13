package sportProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import sport.*;


public class TennisMatchApplet extends XApplet {
	static final private String NO_OF_SETS_PARAM = "noOfSets";
	static final private String DISPLAY_RECORDS_PARAM = "displayRecords";
	static final private String SERVER_WIN_PROBS_PARAM = "serverWinProbs";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	
	static final private NumValue kZeroValue = new NumValue(0.0, 2);
	static final private NumValue kOneValue = new NumValue(1.0, 2);
	private NumValue pAWinsServe, pBWinsServe;
	private RandomPoint generator;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private XButton nextPointButton, nextMatchButton;
	private ParameterSlider pAWinsServeSlider, pBWinsServeSlider;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(0, 20));
			
		add("Center", dataPanel(data, summaryData));
			
		add("South", controlPanel(data, summaryData));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(SERVER_WIN_PROBS_PARAM));
		pAWinsServe = new NumValue(st.nextToken());
		pBWinsServe = new NumValue(st.nextToken());
		generator = new RandomPoint(getParameter(RANDOM_SEED_PARAM),
																	pAWinsServe.toDouble(), pBWinsServe.toDouble());
		
		int noOfSets = Integer.parseInt(getParameter(NO_OF_SETS_PARAM));
		int noOfDisplayRecords = Integer.parseInt(getParameter(DISPLAY_RECORDS_PARAM));
		TennisMatchVariable match = new TennisMatchVariable("Tennis Match", noOfSets,
																				noOfDisplayRecords, generator);
		
		data.addVariable("match", match);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "match");
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("Center", new TennisPointsView(data, this, "match"));
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 30, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																			VerticalLayout.VERT_CENTER, 4));
				
				pAWinsServeSlider = new ParameterSlider(kZeroValue, kOneValue, pAWinsServe,
																		translate("P(A wins serve)"), this);
			sliderPanel.add(pAWinsServeSlider);
				pBWinsServeSlider = new ParameterSlider(kZeroValue, kOneValue, pBWinsServe,
																		translate("P(B wins serve)"), this);
			sliderPanel.add(pBWinsServeSlider);
		
		thePanel.add(ProportionLayout.LEFT, sliderPanel);
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																			VerticalLayout.VERT_CENTER, 4));
				nextPointButton = new RepeatingButton(translate("Simulate next point"), this);
			buttonPanel.add(nextPointButton);
				nextMatchButton = new XButton(translate("Start new match"), this);
			buttonPanel.add(nextMatchButton);
		
		thePanel.add(ProportionLayout.RIGHT, buttonPanel);
		return thePanel;
	}
	
	protected void doStartNewMatch() {
		summaryData.takeSample();
	}

	
	private boolean localAction(Object target) {
		if (target == nextMatchButton) {
			doStartNewMatch();
			nextPointButton.enable();
			return true;
		}
		else if (target == nextPointButton) {
			TennisMatchVariable match = (TennisMatchVariable)data.getVariable("match");
			match.generateNextPoint();
			if (match.matchFinished())
				nextPointButton.disable();
			data.variableChanged("match");
			return true;
		}
		else if (target == pAWinsServeSlider || target == pBWinsServeSlider) {
			generator.setServerWinProbs(pAWinsServeSlider.getParameter().toDouble(), pBWinsServeSlider.getParameter().toDouble());
			doStartNewMatch();
			nextPointButton.enable();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}