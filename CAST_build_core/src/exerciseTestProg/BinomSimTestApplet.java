package exerciseTestProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import random.*;
import exercise2.*;
import formula.*;
import valueList.*;

import exerciseTest.*;


public class BinomSimTestApplet extends CoreBinomialApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
//	static final private NumValue kMaxCount = new NumValue(9999, 0);
	
	private RandomBinomial binomialGenerator;
	private NumSampleStatus sampleStatus;
	
	private HorizAxis countAxis;
//	private StackedDiscreteView countView;
	private CutoffPanel cutoffPanel;
	
	private OneValueView noOfSuccesses;
	
	private ParameterSlider nSlider, pSlider;
	private XButton sampleButton;
	
	private boolean disableDisplayUpdate = false;
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxSliderN", "int");
		registerParameter("minSimulations", "int");
		registerParameter("defaultSimulations", "int");
	}
	
	private int getMaxSliderN() {
		return getIntParam("maxSliderN");
	}
	
	private int getMinSimulations() {
		return getIntParam("minSimulations");
	}
	
	
	private int getDefaultSimulations() {
		return getIntParam("defaultSimulations");
	}
	
	private int getSliderN() {
		return (int)Math.round(nSlider.getParameter().toDouble());
	}
	
/*
	private NumValue getSliderPSuccess() {
		return pSlider.getParameter();
	}
*/
	
	public String getAxisInfo() {
		int n = getSliderN();
		int step = (n <= 10) ? 1 : (n <= 20) ? 2 : (n <= 50) ? 5 : (n <= 100) ? 10 : (n <= 200) ? 20 : (n <= 500) ? 50 : 100;
		
		String axisInfo = "-0.5 " + n + ".5 0 " + step;
		if (n % step != 0)
			axisInfo += " " + n;
		
		return axisInfo;
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
			binomialGenerator = new RandomBinomial(1, 10, 0.5);
			long initialSeed = nextSeed();
			binomialGenerator.setSeed(initialSeed);
			
			NumVariable countVar = new NumVariable("No of successes");
			sampleStatus = new NumSampleStatus(binomialGenerator, initialSeed, countVar, 0);
			registerStatusItem("sample", sampleStatus);
		data.addVariable("count", countVar);
		
		return data;
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(5, 0));
		
		thePanel.add("West", samplingPanel());
		
			XPanel simulationPanel = new XPanel();
			simulationPanel.setLayout(new BorderLayout(0, 10));
			
				cutoffPanel = new CutoffPanel(data, "count", this);
				cutoffPanel.setDiscrete(true);
			simulationPanel.add("North", cutoffPanel);
			
			simulationPanel.add("Center", empiricalDistnPanel(data));
		
		thePanel.add("Center", simulationPanel);
		
		return thePanel;
	}
		
//-----------------------------------------------------------
	
	protected String getPValueLabel() {
		return MText.expandText("Propn as 'extreme' (H#sub0#)");
	}
	
	protected String getPValuesPropnsString() {
		return translate("Proportions");
	}
	
	protected String getPvalueLongName() {
		return "proportion of simulated counts as extreme as " + getNSuccess();
	}
		
//-----------------------------------------------------------
	
	private XPanel samplingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("North", sliderPanel());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				sampleButton = new RepeatingButton(translate("Take sample"), this);
			mainPanel.add(sampleButton);
			
				noOfSuccesses = new OneValueView(data, "count", this);
			mainPanel.add(noOfSuccesses);
			
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		int maxN = getMaxSliderN();
		int startN = maxN / 2;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
			nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0),
																							new NumValue(startN, 0), "n", this);
			registerStatusItem("n", nSlider);
		thePanel.add(nSlider);
		
			String pi = MText.expandText("#pi#");
			pSlider = new ParameterSlider(new NumValue(0, 2), new NumValue(1, 2),
																								new NumValue(0.5, 2), pi, this);
			pSlider.setForeground(Color.blue);
			registerStatusItem("p", pSlider);
			
		thePanel.add(pSlider);
		return thePanel;
	}
	
	private XPanel empiricalDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			countAxis = new HorizAxis(this);
		thePanel.add("Bottom", countAxis);
		
			StackedDiscreteView countView = new StackedDiscreteView(data, this, countAxis, "count");
			countView.setRetainLastSelection(true);
			countView.lockBackground(Color.white);
			cutoffPanel.setLinkedView(countView);
		thePanel.add("Center", countView);
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		double pSuccess = 0.5;
		pSlider.setParameter(pSuccess);
		int maxN = getMaxSliderN();
		int startN = maxN / 2;
		
		disableDisplayUpdate = true;		//	do disable slider change events in action() -- they reset selection and flash
		nSlider.changeLimits(kZeroValue, new NumValue(maxN, 0), new NumValue(startN, 0), maxN);
		disableDisplayUpdate = false;
		
		countAxis.readNumLabels(getAxisInfo());
		String varName = getVarName();
		if (varName.length() > 0)
			countAxis.setAxisName("Distn of " + varName);
		countAxis.invalidate();
		
		cutoffPanel.clearCutoff();
		
		noOfSuccesses.reset(new LabelValue(String.valueOf(getMaxSliderN())));
		
		binomialGenerator.setParameters(startN, 0.5);
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		clearSample();
		data.variableChanged("count");
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all 3 steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Perform a simulation to estimate the probability of getting as 'extreme' a count as that observed, assuming that H#sub0# is true. (Do at least " + getMinSimulations() + " repetitions.)\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		int sliderN = (int)Math.round(nSlider.getParameter().toDouble());
		double sliderP = pSlider.getParameter().toDouble();
		
		int nTrials = getNTrials();
		double pSuccess = getPSuccess().toDouble();
		messagePanel.insertRedHeading("The proportion as extreme is wrong!\n");
		if (sliderN != nTrials || Math.abs(sliderP - pSuccess) > kEpsP) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			if (sliderN != nTrials)
				messagePanel.insertRedText("\nThere is a total of " + nTrials + " " + getTrialsName() + " but the 'n' slider is set to " + sliderN + ".");
			if (Math.abs(sliderP - pSuccess) > kEpsP)
				messagePanel.insertRedText("\nThe probability of " + getSuccessName() + " is " + getPSuccess() + " but the '#pi#' slider is set to " + pSlider.getParameter() + ".");
		}
		else {
			int minSimulations = getMinSimulations();
			NumVariable countVar = (NumVariable)data.getVariable("count");
			int n = countVar.noOfValues();
			if (n < minSimulations) {
				messagePanel.insertText("(You have correctly specified the hypotheses and set n and #pi# correctly.)\n");
				messagePanel.insertText("You have not performed enough repeats of the simulation. You should perform at least " + minSimulations + " repetitions but have only done " + n + ".");
			}
			else {
				messagePanel.insertText("(You have correctly specified the hypotheses and correctly performed the simulation.)\n");
				
				NumValue attemptCutoff = cutoffPanel.getCutoff();
				boolean lowTail = lowTailHighlight();
				int attemptCutoffInt = lowTail ? (int)Math.round(Math.floor(attemptCutoff.toDouble()))
																									: (int)Math.round(Math.ceil(attemptCutoff.toDouble()));
				
				if (attemptCutoffInt != getNSuccess())
					messagePanel.insertText("You have not used the correct cutoff value to find which counts are more 'extreme' than what was observed.");
				else
					messagePanel.insertText("You have not correctly found the proportion of more 'extreme' counts from the simulation.");
			}
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		NumValue pSuccess = getPSuccess();
		int nTrials = getNTrials();
		int nSuccess = getNSuccess();
		String successName = getSuccessName();
		String successesName = getSuccessesName();
		messagePanel.insertText("There are n = " + nTrials + " " + getTrialsName()
								+ " and the H#sub0# value for the probability of " + successName
								+ " is #pi# = " + pSuccess + ". ");
		
		NumVariable countVar = (NumVariable)data.getVariable("count");
		int n = countVar.noOfValues();
		messagePanel.insertText("The diagram shows results from " + n + " simulations with these parameters. ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The more 'extreme' samples are those with ");
				messagePanel.insertBoldText("less than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The more 'extreme' samples are those with ");
				messagePanel.insertBoldText("greater than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The more 'extreme' samples are those whose number of " + successesName + " is ");
				messagePanel.insertBoldText("at least as extreme as ");
				messagePanel.insertText(nSuccess + ". This is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the proportion with ");
				messagePanel.insertText((evaluateProbability(0, nSuccess) < 0.5 ? "#le# " : "#ge# ") + nSuccess + " " + successesName + ".");
				break;
		}
	}
	
	
//-----------------------------------------------------------
	
	
	protected double evaluateProbability(int lowCount, int highCount) {		//	including both ends
		NumVariable countVar = (NumVariable)data.getVariable("count");
		int n = countVar.noOfValues();
		return countBetween(lowCount, highCount) / (double)n;
	}
	
	protected boolean correctModelSetup() {
		int minSimulations = getMinSimulations();
		NumVariable countVar = (NumVariable)data.getVariable("count");
		int n = countVar.noOfValues();
		if (n < minSimulations)
			return false;
		
		int correctN = getNTrials();
		double correctP = getPSuccess().toDouble();
		int attemptN = (int)Math.round(nSlider.getParameter().toDouble());
		double attemptP = pSlider.getParameter().toDouble();
		
		return (correctN == attemptN) && (Math.abs(correctP - attemptP) < 0.000001);
	}
	
	private int countBetween(int lowCount, int highCount) {
		NumVariable countVar = (NumVariable)data.getVariable("count");
		int n = countVar.noOfValues();
		int x = 0;
		for (int i=0 ; i<n ; i++) {
			int count = (int)Math.round(countVar.doubleValueAt(i));
			if (count >= lowCount && count <= highCount)
				x ++;
		}
		return x;
	}
	
	private void clearSample() {
		NumVariable countVar = (NumVariable)data.getVariable("count");
		countVar.clearData();
		long initialSeed = nextSeed();
		binomialGenerator.setSeed(initialSeed);
		sampleStatus.noteResetSample(initialSeed);
		
		if (cutoffPanel != null)
			cutoffPanel.clearCutoff();
	}
	
	protected void showCorrectWorking() {
		int correctN = getNTrials();
		double correctP = getPSuccess().toDouble();
		int attemptN = (int)Math.round(nSlider.getParameter().toDouble());
		double attemptP = pSlider.getParameter().toDouble();
		boolean wrongN = correctN != attemptN;
		boolean wrongP = Math.abs(correctP - attemptP) > 0.000001;
		
		if (wrongN || wrongP) {
			nSlider.setParameter(correctN, false);
			pSlider.setParameter(correctP, false);
					//	postEvent=false so noteChangedWorking() is not called (it would set result to ANS_UNCHECKED)
			binomialGenerator.setParameters(correctN, correctP);
		}
		
		if (wrongN) {
			countAxis.readNumLabels(getAxisInfo());
			countAxis.invalidate();
		}

		if (wrongN || wrongP)
			clearSample();
		
		NumVariable countVar = (NumVariable)data.getVariable("count");
		if (countVar.noOfValues() < getMinSimulations()) {
			int simCount = getDefaultSimulations();
			for (int i=countVar.noOfValues() ; i<simCount ; i++) {
				int count = binomialGenerator.generateOne();
				countVar.addValue(new NumValue(count, 0));
			}
			data.variableChanged("count");
		}
		
		int nSuccess = getNSuccess();
		int tail = getTail();
		if (tail == TAIL_BOTH) {
			int lowCount = countBetween(0, nSuccess);
			int highCount = countBetween(nSuccess, correctN);
			tail = (lowCount <= highCount) ? TAIL_LOW : TAIL_HIGH;
		}
		if (tail == TAIL_LOW || tail == TAIL_LOW_EQ)
			cutoffPanel.setCorrectCutoff(new NumValue(nSuccess + 0.5, 1), CutoffPanel.LOW_TAIL);
		else
			cutoffPanel.setCorrectCutoff(new NumValue(nSuccess - 0.5, 1), CutoffPanel.HIGH_TAIL);
		
		showCorrectAnswer();
	}
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if ((target == pSlider || target == nSlider) && !disableDisplayUpdate) {
			int newN = (int)Math.round(nSlider.getParameter().toDouble());
			double newP = pSlider.getParameter().toDouble();
			binomialGenerator.setParameters(newN, newP);
			
			if (target == nSlider) {
				countAxis.readNumLabels(getAxisInfo());
				countAxis.invalidate();
				
				cutoffPanel.clearCutoff();
			}
			
			clearSample();
			data.variableChanged("count");
			noteChangedWorking();
			return true;
		}
		else if (target == sampleButton) {
			NumVariable countVar = (NumVariable)data.getVariable("count");
			int count = binomialGenerator.generateOne();
			countVar.addValue(new NumValue(count, 0));
			int n = countVar.noOfValues();
			data.valuesAdded(n);
			data.setSelection(n - 1);
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
}