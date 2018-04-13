package exerciseTestProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.JdistnAreaLookup.*;


public class BinomialTestApplet extends CoreBinomialApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	
	private BinomialLookupPanel binomialLookupPanel;
	private ParameterSlider nSlider, pSlider;
	
	private boolean disableDisplayUpdate = false;
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxSliderN", "int");
	}
	
	protected int getMaxSliderN() {
		return getIntParam("maxSliderN");
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
			BinomialDistnVariable binomialDistn = new BinomialDistnVariable("Binomial");
		data.addVariable("binomial", binomialDistn);
		
		return data;
	}
		
//-----------------------------------------------------------
	
	protected String getPValueLabel() {
		return "p-value";
	}
	
	protected String getPValuesPropnsString() {
		return "P-values";
	}
	
	protected String getPvalueLongName() {
		return "p-value";
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			int maxN = getMaxSliderN();
		thePanel.add("North", sliderPanel(maxN, maxN / 2));
		
			binomialLookupPanel = new BinomialLookupPanel(data, "binomial", this, CoreLookupPanel.HIGH_AND_LOW);
			registerStatusItem("drag", binomialLookupPanel);
		thePanel.add("Center", binomialLookupPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel(int maxN, int startN) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 5));
		
			nSlider = new ParameterSlider(new NumValue(1, 0), new NumValue(maxN, 0),
									new NumValue(startN, 0), "n", this);
			registerStatusItem("n", nSlider);
		thePanel.add("Left", nSlider);
		
			String pi = MText.expandText("#pi#");
			pSlider = new ParameterSlider(new NumValue(0, 2), new NumValue(1, 2),
										new NumValue(0.5, 2), pi, this);
			pSlider.setForeground(Color.blue);
			registerStatusItem("p", pSlider);
			
		thePanel.add("Right", pSlider);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		BinomialDistnVariable binomialDistn = (BinomialDistnVariable)data.getVariable("binomial");
		
		pSlider.setParameter(binomialDistn.getProb());
		int maxN = getMaxSliderN();
		
		disableDisplayUpdate = true;		//	do disable slider change events in action() -- they reset selection and flash
		nSlider.changeLimits(kZeroValue, new NumValue(maxN, 0), new NumValue(binomialDistn.getCount(), 0), maxN);
		disableDisplayUpdate = false;
		
		binomialLookupPanel.resetPanel();
		
		data.variableChanged("binomial");
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		BinomialDistnVariable binomialDistn = (BinomialDistnVariable)data.getVariable("binomial");
		int nTrials = getMaxSliderN() / 2;
		double pSuccess = 0.5;
		binomialDistn.setCount(nTrials);
		binomialDistn.setProb(pSuccess);
		binomialDistn.setMinSelection(Math.floor(nTrials / 4.0) + 0.5);
		binomialDistn.setMaxSelection(nTrials - Math.floor(nTrials / 4.0) - 0.5);
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all 3 steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Find the p-value for the test using an appropriate binomial distn.\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		int sliderN = (int)Math.round(nSlider.getParameter().toDouble());
		double sliderP = pSlider.getParameter().toDouble();
		
		int nTrials = getNTrials();
		double pSuccess = getPSuccess().toDouble();
		messagePanel.insertRedHeading("p-value is wrong!\n");
		if (sliderN != nTrials || Math.abs(sliderP - pSuccess) > kEpsP) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			if (sliderN != nTrials)
				messagePanel.insertRedText("\nThere is a total of " + nTrials + " " + getTrialsName() + " but the 'n' slider is set to " + sliderN + ".");
			if (Math.abs(sliderP - pSuccess) > kEpsP)
				messagePanel.insertRedText("\nThe probability of " + getSuccessName() + " is " + getPSuccess() + " but the '#pi#' slider is set to " + pSlider.getParameter() + ".");
		}
		else {
			messagePanel.insertText("(You have correctly specified the hypotheses and set the binomial distribution parameters correctly.)\n");
			messagePanel.insertText("The p-value is found from a tail probability of this distribution but you have not evaluated it correctly.");
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		NumValue pSuccess = getPSuccess();
		int nTrials = getNTrials();
		int nSuccess = getNSuccess();
		String successName = getSuccessName();
		String successesName = getSuccessesName();
		messagePanel.insertText("There are n = " + nTrials + " " + getTrialsName()
								+ " and the null hypotheses value for the probability of " + successName
								+ " is #pi# = " + pSuccess + ". ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The p-value is the probability of ");
				messagePanel.insertBoldText("less than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The p-value is the probability of ");
				messagePanel.insertBoldText("greater than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The p-value is the probability that the number of " + successesName + " is ");
				messagePanel.insertBoldText("at least as extreme as ");
				messagePanel.insertText(nSuccess + ". This is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the probability of ");
				messagePanel.insertText((evaluateProbability(0, nSuccess) < 0.5 ? "#le# " : "#ge# ") + nSuccess + " " + successesName + ".");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	
	protected double evaluateProbability(int lowCount, int highCount) {		//	including both ends
		BinomialDistnVariable tempBin = new BinomialDistnVariable("");
		tempBin.setCount(getNTrials());
		tempBin.setProb(getPSuccess().toDouble());
		
		return tempBin.getCumulativeProb(highCount + 0.5) - tempBin.getCumulativeProb(lowCount - 0.5);
	}
	
	protected void showCorrectWorking() {
		int correctN = getNTrials();
		double correctP = getPSuccess().toDouble();
		nSlider.setParameter(correctN, false);
		pSlider.setParameter(correctP, false);
				//	postEvent=false so noteChangedWorking() is not called (it would set result to ANS_UNCHECKED)
		
		BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable("binomial");
		adjustPSuccess(y);
		adjustNSuccess(y);
		data.variableChanged("binomial");
		
		int observedCount = getNSuccess();
		
		if (lowTailHighlight())
			binomialLookupPanel.showAnswer(new NumValue(-0.5, 1), new NumValue(observedCount + 0.5, 1));
		else
			binomialLookupPanel.showAnswer(new NumValue(observedCount - 0.5, 1), new NumValue(correctN + 0.5, 1));
		
		showCorrectAnswer();
	}
	
//-----------------------------------------------------------
	
	private void adjustPSuccess(BinomialDistnVariable y) {
		double newP = pSlider.getParameter().toDouble();
		y.setProb(newP);
	}
	
	private void adjustNSuccess(BinomialDistnVariable y) {
		binomialLookupPanel.resetPanel();
		int newN = (int)Math.round(nSlider.getParameter().toDouble());
		y.setCount(newN);
		double minSel = y.getMinSelection();
		double maxSel = y.getMaxSelection();
		if (maxSel > newN + 0.6)
			data.setSelection("binomial", Math.min(minSel, newN - 0.5), Math.min(maxSel, newN + 0.5));
	}
	
	private boolean localAction(Object target) {
		BinomialDistnVariable y = (BinomialDistnVariable)data.getVariable("binomial");
		if (target == pSlider) {
			adjustPSuccess(y);
			data.variableChanged("binomial");
			noteChangedWorking();
			return true;
		}
		else if (target == nSlider && !disableDisplayUpdate) {
			adjustNSuccess(y);
			data.variableChanged("binomial");
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