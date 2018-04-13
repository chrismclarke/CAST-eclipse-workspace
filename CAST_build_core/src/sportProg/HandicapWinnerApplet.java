package sportProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomNormal;
import valueList.*;
import coreGraphics.*;

import sport.*;


public class HandicapWinnerApplet extends XApplet {
	static final private String ACTUAL_AXIS_INFO_PARAM = "actualAxis";
	static final private String ABILITY_AXIS_INFO_PARAM = "abilityAxis";
	
	static final private String ACTUAL_VAR_NAME_PARAM = "actualName";
	static final private String ABILITY_VAR_NAME_PARAM = "abilityName";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String MAX_ABILITY_PARAM = "maxAbility";
	
	static final private String HANDICAPPED_VAR_NAME_PARAM = "handicappedName";
	static final private String HANDICAP_PARAM = "handicap";
	
	static final private String WINNER_NAME_PARAM = "winnerName";
	
	static final private String ABILITY_SCALING_PARAM = "abilityScaling";
	static final private String ABILITY_DATA_PARAM = "abilityData";
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private XLabel yVariateName;
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	public void setupApplet() {
		NumValue maxAbility = new NumValue(getParameter(MAX_ABILITY_PARAM));
		
		data = getData(maxAbility.decimals);
		summaryData = getSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(getDataViewProportion(), 30));
			
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new BorderLayout(0, 10));
			dataPanel.add("Center", dataPanel(data, "ability", "score"));
			
			dataPanel.add("South", winnerValuePanel(data, summaryData, "winner", maxAbility));
			
		add(ProportionLayout.LEFT, dataPanel);
		
			XPanel summaryPanel = new XPanel();
			summaryPanel.setLayout(new BorderLayout(0, 10));
			summaryPanel.add("Center", summaryPanel(data, summaryData, "winner"));
			
			summaryPanel.add("North", controlPanel(data, summaryData));
		
		add(ProportionLayout.RIGHT, summaryPanel);
	}
	
	protected double getDataViewProportion() {
		return 0.5;
	}
	
	protected DataSet getData(int dataDecimals) {
		DataSet data = new DataSet();
		
		MeanAbilityVariable ability = new MeanAbilityVariable(getParameter(ABILITY_VAR_NAME_PARAM),
															getParameter(ABILITY_SCALING_PARAM));
		ability.setPatterned(getParameter(ABILITY_DATA_PARAM), dataDecimals);
		data.addVariable("ability", ability);
		
		RandomNormal generator = new RandomNormal(ability.noOfValues(), 0.0, 1.0, 3.0);
		generator.setSeed(Long.parseLong(getParameter(RANDOM_SEED_PARAM)));
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		data.addVariable("error", error);
		
		PerturbedScoreVariable rawScore = new PerturbedScoreVariable(getParameter(ACTUAL_VAR_NAME_PARAM),
																			ability, error, dataDecimals + 2);
		data.addVariable("score", rawScore);
		
		HandicapScoreVariable handicapScore = new HandicapScoreVariable(
						getParameter(HANDICAPPED_VAR_NAME_PARAM), rawScore, ability,
						getParameter(HANDICAP_PARAM), dataDecimals + 2);
		data.addVariable("handicapped", handicapScore);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "error");
		
		MinimumVariable winner = new MinimumVariable(getParameter(WINNER_NAME_PARAM),
																			"handicapped", "ability");
		
		summaryData.addVariable("winner", winner);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, String xKey, String yKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel plotPanel = new XPanel();
			plotPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(ABILITY_AXIS_INFO_PARAM));
				NumVariable xVar = (NumVariable)data.getVariable(xKey);
				horizAxis.setAxisName(xVar.name);
			plotPanel.add("Bottom", horizAxis);
			
				VertAxis vertAxis = new VertAxis(this);
				vertAxis.readNumLabels(getParameter(ACTUAL_AXIS_INFO_PARAM));
			plotPanel.add("Left", vertAxis);
			
				DataView theView = new WinnerScatterView(data, this, horizAxis, vertAxis, "ability", "score", "handicapped");
				theView.lockBackground(Color.white);
			plotPanel.add("Center", theView);
		
		thePanel.add("Center", plotPanel);
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				NumVariable yVar = (NumVariable)data.getVariable(yKey);
				yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
				yVariateName.setFont(vertAxis.getFont());
			topPanel.add(yVariateName);
		thePanel.add("North", topPanel);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data, SummaryDataSet summaryData,
																								String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(ABILITY_AXIS_INFO_PARAM));
			NumVariable winnerVar = (NumVariable)summaryData.getVariable(variableKey);
			horizAxis.setAxisName(winnerVar.name);
		thePanel.add("Bottom", horizAxis);
		
			StackedDiscreteView winnerView = new StackedDiscreteView(summaryData, this, horizAxis, "winner");
			winnerView.lockBackground(Color.white);
		thePanel.add("Center", winnerView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 3));
		
			takeSampleButton = new RepeatingButton(translate("Run Tournament"), this);
		thePanel.add(takeSampleButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
			ValueCountView theCount = new ValueCountView(summaryData, this);
			theCount.setLabel(translate("No of samples") + " = ");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	protected XPanel winnerValuePanel(DataSet data, SummaryDataSet summaryData,
																String summaryKey, NumValue maxAbility) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		thePanel.add(new OneValueView(summaryData, summaryKey, this, maxAbility));
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}