package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;


public class BinomialProbApplet extends CoreBinomialProbApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	
	static final private double kEpsP = 0.000001;
	
	private BinomialLookupPanel binomialLookupPanel;
	private ParameterSlider nSlider, pSlider;
	
	private boolean disableDisplayUpdate = false;
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxSliderN", "int");
	}
	
	private int getMaxSliderN() {
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
	
	protected Dimension getMinMax(String minMaxString, int total) {
		double minCum = kMinCum;
		double maxCum = 1 - kMinCum;
		StringTokenizer st = new StringTokenizer(minMaxString, ":");
		if (st.hasMoreTokens())
			minCum = Double.parseDouble(st.nextToken());
		if (st.hasMoreTokens())
			maxCum = Double.parseDouble(st.nextToken());
		
		BinomialDistnVariable tempBin = createTempBinomial();
		double cum = 0;
		int xMin = -1;
		while (cum < minCum) {
			xMin ++;
			cum += tempBin.getScaledProb(xMin);
		}
		cum = 1;
		int xMax = total + 1;
		while (cum > maxCum) {
			xMax --;
			cum -= tempBin.getScaledProb(xMax);
		}
		return new Dimension(xMin, xMax);
	}
	
	private BinomialDistnVariable createTempBinomial() {
		BinomialDistnVariable tempBin = new BinomialDistnVariable("");
		tempBin.setCount(getNTrials());
		tempBin.setProb(getPSuccess().toDouble());
		return tempBin;
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			BinomialDistnVariable binomialDistn = new BinomialDistnVariable("Binomial");
		data.addVariable("binomial", binomialDistn);
		
		return data;
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
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		BinomialDistnVariable binomialDistn = (BinomialDistnVariable)data.getVariable("binomial");
		int nTrials = getMaxSliderN() / 2;
		double pSuccess = 0.5;
		binomialDistn.setCount(nTrials);
		binomialDistn.setProb(pSuccess);
		binomialDistn.setMinSelection(Math.floor(nTrials / 4.0) + 0.5);
		binomialDistn.setMaxSelection(Math.floor(nTrials * 3 / 4.0) + 0.5);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		DiscreteIntervalLimits limits = getLimits();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("#bullet#  Use the sliders to set the binomial distribution parameters.\n#bullet#  Drag the vertical red lines to highlight the relevant bars on the bar chart.\n#bullet#  Then type the probability into the text-edit box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability into the answer box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The answer is " + limits.probAnswerString() + ", as highlighted in the bar chart.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Correct!\n");
				messagePanel.insertText("This is " + limits.probAnswerString() + ".");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertText("Your answer is correct to 3 decimal places, but you should be able to get the 4th decimal place correct too.");
				break;
			case ANS_WRONG:
				int sliderN = (int)Math.round(nSlider.getParameter().toDouble());
				double sliderP = pSlider.getParameter().toDouble();
				
				int nTrials = getNTrials();
				double pSuccess = getPSuccess().toDouble();
				if (sliderN != nTrials || Math.abs(sliderP - pSuccess) > kEpsP) {
					messagePanel.insertRedHeading("Wrong!");
					if (sliderN != nTrials)
						messagePanel.insertRedText("\nThere is a total of " + nTrials + " " + getTrialsName() + " but the 'n' slider is set to " + sliderN + ".");
					if (Math.abs(sliderP - pSuccess) > kEpsP)
						messagePanel.insertRedText("\nThe probability of " + getVarName() + " is " + getPSuccess() + " but the '#pi#' slider is set to " + pSlider.getParameter() + ".");
				}
				else {
					int first = getLimits().getFirst();
					int last = getLimits().getLast();
					
					int panelFirst = (int)Math.round(Math.ceil(binomialLookupPanel.getLowValue().toDouble()));
					int panelLast = (int)Math.round(Math.floor(binomialLookupPanel.getHighValue().toDouble()));
					
					if (first != panelFirst || last != panelLast) {
						messagePanel.insertRedHeading("Wrong!");
						if (first != panelFirst)
							messagePanel.insertRedText("\nThe lowest selected bar should be for the count " + first + " but it is " + panelFirst + ".");
						if (last != panelLast)
							messagePanel.insertRedText("\nThe highest selected bar should be for the count " + last + " but it is " + panelLast + ".");
					}
					else {
						messagePanel.insertRedHeading("Error!\n");
						messagePanel.insertText("You have not correctly copied the probability into the Answer box.");
					}
				}
				
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	
	protected double evaluateProbability(DiscreteIntervalLimits limits) {
		BinomialDistnVariable tempBin = createTempBinomial();
		int first = limits.getFirst();
		int last = limits.getLast();
		return tempBin.getCumulativeProb(last + 0.5) - tempBin.getCumulativeProb(first - 0.5);
	}
	
	protected boolean isClose(double attempt, double correct, DiscreteIntervalLimits limits) {
		return Math.round(correct * 1000) == Math.round(attempt * 1000);
	}
	
	protected boolean isCorrect(double attempt, double correct, DiscreteIntervalLimits limits) {
		return Math.round(correct * 10000) == Math.round(attempt * 10000);
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
		
		DiscreteIntervalLimits limits = getLimits();
		double prob = evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		resultPanel.showAnswer(probValue);
		
		NumValue first = new NumValue(limits.getFirst() - 0.5, 1);
		NumValue last = new NumValue(limits.getLast() + 0.5, 1);
		binomialLookupPanel.showAnswer(first, last);
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
		return localAction(evt.target);
	}
}