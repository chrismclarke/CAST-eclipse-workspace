package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;


public class NormalProbApplet extends CoreNormalProbApplet {
	
	protected NormalLookupPanel normalLookupPanel;
	
	protected ExpectedTemplatePanel expectedTemplate;
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		normalLookupPanel = new NormalLookupPanel(data, "distn", this, NormalLookupPanel.HIGH_AND_LOW);
		registerStatusItem("drag", normalLookupPanel);
		if (hasOption("nTemplate")) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 10));
				thePanel.add("Center", normalLookupPanel);
				
					FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
					expectedTemplate = new ExpectedTemplatePanel(stdContext);
					expectedTemplate.lockBackground(kTemplateBackground);
					registerStatusItem("expectedTemplate", expectedTemplate);
				thePanel.add("South", expectedTemplate);
				
			return thePanel;
		}
		else
			return normalLookupPanel;
	}
	
	protected void setDisplayForQuestion() {
		normalLookupPanel.resetPanel();
		
		if (expectedTemplate != null)
			expectedTemplate.setValues(kOneValue, kOneValue);
		
		data.variableChanged("distn");
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		double mean = getMean().toDouble();
		double sd = getSD().toDouble();
		normalDistn.setMean(mean);
		normalDistn.setSD(sd);
		normalDistn.setMinSelection(mean - sd);
		normalDistn.setMaxSelection(mean + sd);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		IntervalLimits limits = getLimits();
		
		if (isCountQuestionType()) {
			int n = getN();
			switch (result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the expected number then type it into the text-edit box above.\n(Drag the vertical red lines on the " + densityString() + " to display the required area.)");
					break;
				case ANS_INVALID:
					if (resultPanel.isClear()) {
						messagePanel.insertRedHeading("Error!\n");
						messagePanel.insertText("You must type a value into the Answer box above.");
					}
					else {
						messagePanel.insertRedHeading("Wrong!\n");
						messagePanel.insertText("The expected number cannot be less than zero or more than the total.");
					}
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("This probability is the area " + limits.areaAnswerString() + ".");
					messagePanel.insertText(" This expected number is " + n + " times this.");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("This probability is the area " + limits.areaAnswerString() + ".");
					messagePanel.insertText(" This expected number is " + n + " times this.");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					messagePanel.insertText("However you should be able to find the answer more accurately by typing into the red text-edit boxes above the normal curves.");
					break;
				case ANS_WRONG:
					messagePanel.insertRedHeading("Not close enough!\n");
					messagePanel.insertText("Find the area under the " + densityString() + " " + limits.areaAnswerString() + ".");
					messagePanel.insertText(" Then use the template to multiply by " + n + ".");
					break;
			}
		}
		else
			switch (result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the probability then type it into the text-edit box above.\n(Drag the vertical red lines on the " + densityString() + " to display the required area.)");
					break;
				case ANS_INCOMPLETE:
					messagePanel.insertRedHeading("Error!\n");
					messagePanel.insertText("You must type a probability into the Answer box above.");
					break;
				case ANS_INVALID:
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("This probability is the area " + limits.areaAnswerString() + ".");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("This probability is the area " + limits.areaAnswerString() + ".");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					messagePanel.insertText("However you should be able to find the answer correct to 4 decimal places by typing into the red text-edit boxes above the normal curves.");
					break;
				case ANS_WRONG:
					messagePanel.insertRedHeading("Not close enough!\n");
					messagePanel.insertText("Find the area under the " + densityString() + " " + limits.areaAnswerString() + ".");
					break;
			}
	}
	
	protected String densityString() {
		return "normal curve";
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		
		double attempt = getAttempt();
		if (isCountQuestionType())
			attempt /= getN();
		
		if (attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		
		IntervalLimits limits = getLimits();
		double correct = evaluateProbability(limits);
		
		if (Math.abs(correct - attempt) <= kEps)
			return ANS_CORRECT;
		else {
			double maxCloseError = normalLookupPanel.getPixError(limits.startVal, limits.endVal);
			
			if (Math.abs(correct - attempt) <= maxCloseError)
				return ANS_CLOSE;
			else
				return ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		IntervalLimits limits = getLimits();
		double prob = evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		if (!isCountQuestionType())
			resultPanel.showAnswer(probValue);
		else {
			int n = getN();
			int decimals = 4;
			while (n > 10) {
				n /= 10;
				decimals --;
			}
			resultPanel.showAnswer(new NumValue(prob * getN(), decimals));
		}
		
		NumValue start = limits.startVal;
		NumValue end = limits.endVal;
		normalLookupPanel.showAnswer(start, end);
		
		if (expectedTemplate != null)
			if (!isCountQuestionType())
				expectedTemplate.setValues(kNaNValue, kNaNValue);
			else
				expectedTemplate.setValues(new NumValue(getN(), 0), probValue);
	}
}