package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JtableLookup.*;


public class NormalTableProbApplet extends NormalZProbApplet {
	
	static protected void insertTableMessageContent(MessagePanel messagePanel, DataSet data,
																																CoreNormalProbApplet applet) {
		ContinDistnVariable xDistn = (ContinDistnVariable)data.getVariable("distn");
		
		IntervalLimits limits = applet.getLimits();
		IntervalLimits zLimits = limits.translateToZ(xDistn);
		
		double prob = applet.evaluateProbability(limits);
		NumValue probValue = new NumValue(prob, 4);
		
		if (applet.isCountQuestionType()) {
			int n = applet.getN();
			switch (applet.result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the expected number then type it into the text-edit box above.\n");
					break;
				case ANS_INCOMPLETE:
					messagePanel.insertRedHeading("Error!\n");
					messagePanel.insertText("You must type a value in the Answer box above.");
					break;
				case ANS_INVALID:
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertText("The expected number cannot be less than zero or more than the total.");
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("The template shows how to find a z-score and the table highlights the z-scores for the question. ");
					messagePanel.insertText("\nThe probability that the z-score is " + zLimits.generalAnswerString() + " is exactly " + probValue.toString() + ". The expected value is " + n + " times this.");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("The probability that the z-score is " + zLimits.generalAnswerString() + " is exactly " + probValue.toString() + ". The expected value is " + n + " times this.");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					if (applet.hasOption("interpolate"))
						messagePanel.insertText("This is close enough, but you can find the answer more accurately by interpolating between adjacent values in the table.");
					else
						messagePanel.insertText("This is close enough, but you should be able to find the expected number a little more accurately from the tables.");
					break;
				case ANS_WRONG:
					addWrongMessage(messagePanel, limits, TABLE, applet);
					messagePanel.insertText(" Finally use the template to multiply by " + n + ".");
					break;
			}
		}
		else
			switch (applet.result) {
				case ANS_UNCHECKED:
					messagePanel.insertText("Find the probability then type it into the text-edit box above.\n");
					break;
				case ANS_INCOMPLETE:
					messagePanel.insertRedHeading("Error!\n");
					messagePanel.insertText("You must type a value in the Answer box above.");
					break;
				case ANS_INVALID:
					messagePanel.insertRedHeading("Wrong!\n");
					messagePanel.insertText("Probabilities cannot be less than zero or more than one.");
					break;
				case ANS_TOLD:
					messagePanel.insertRedHeading("Answer\n");
					messagePanel.insertText("The template shows how to find a z-score and the table highlights the probability of a lower value. ");
					messagePanel.insertText("\nThe probability that the z-score is " + zLimits.generalAnswerString() + " is exactly " + probValue.toString() + ".");
					break;
				case ANS_CORRECT:
					messagePanel.insertRedHeading("Correct!\n");
					messagePanel.insertText("The probability that the z-score is " + zLimits.generalAnswerString() + " is exactly " + probValue.toString() + ".");
					break;
				case ANS_CLOSE:
					messagePanel.insertRedHeading("Good!\n");
					if (applet.hasOption("interpolate"))
						messagePanel.insertText("This is close enough, but you can find the answer correct to 4 decimal places by interpolating between adjacent values in the table.");
					else
						messagePanel.insertText("This is close, but you should be able to find the probability a little more accurately from the tables.");
					break;
				case ANS_WRONG:
					addWrongMessage(messagePanel, limits, TABLE, applet);
					break;
			}
	}
	
	private TablePanel tableLookupPanel;
	private DiffTemplatePanel diffTemplate;
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			zTemplate = new ZTemplatePanel(stdContext);
			zTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("zTemplate", zTemplate);
		thePanel.add("North", zTemplate);
		
			tableLookupPanel = new TablePanel(data, "z", this);
			registerStatusItem("tableSelection", tableLookupPanel);
		thePanel.add("Center", tableLookupPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
			
				diffTemplate = new DiffTemplatePanel(true, stdContext);
				diffTemplate.lockBackground(kTemplateBackground);
				registerStatusItem("diffTemplate", diffTemplate);
			bottomPanel.add("Center", diffTemplate);
		
			if (hasOption("nTemplate")) {
				expectedTemplate = new ExpectedTemplatePanel(stdContext);
				expectedTemplate.lockBackground(kTemplateBackground);
				registerStatusItem("expectedTemplate", expectedTemplate);
				bottomPanel.add("South", expectedTemplate);
			}
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		tableLookupPanel.scrollToSelection();
	}
	
	protected void setDefaultSelection(NormalDistnVariable zDistn) {
		zDistn.setMinSelection(0.0);
		zDistn.setMaxSelection(0.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		insertTableMessageContent(messagePanel, data, this);
	}
	
//-----------------------------------------------------------
	
	protected double maxExactError(NumValue startZ, NumValue endZ) {
		return tableLookupPanel.getRoundingError(startZ, endZ, hasOption("interpolate") ? 0.002 : 0.005);
	}
	
	protected double maxCloseError(NumValue startZ, NumValue endZ) {
		return tableLookupPanel.getRoundingError(startZ, endZ, hasOption("interpolate") ? 0.01 : 0.02);
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
		
		ContinDistnVariable distn = (ContinDistnVariable)data.getVariable("distn");
		IntervalLimits zLimits = limits.translateToZ(distn);
		
		NumValue meanVal = distn.getMean();
		NumValue sdVal = distn.getSD();
		
		tableLookupPanel.showAnswer(zLimits.startVal, zLimits.endVal);
		
		if (limits.startVal == null)
			zTemplate.setValues(limits.endVal, meanVal, sdVal);
		else
			zTemplate.setValues(limits.startVal, meanVal, sdVal);
		
		NumValue startCum = (limits.startVal == null) ? null
										: new NumValue(distn.getCumulativeProb(limits.startVal.toDouble()), 4);
		NumValue endCum = (limits.endVal == null) ? null
										: new NumValue(distn.getCumulativeProb(limits.endVal.toDouble()), 4);
		if (startCum == null)
			diffTemplate.setValues(endCum, kZeroValue);
		else if (endCum == null)
			diffTemplate.setValues(kOneValue, startCum);
		else if (startCum.toDouble() < endCum.toDouble())
			diffTemplate.setValues(endCum, startCum);
		else
			diffTemplate.setValues(kOneValue, startCum, endCum);
		
		if (expectedTemplate != null)
			if (!isCountQuestionType())
				expectedTemplate.setValues(kNaNValue, kNaNValue);
			else
				expectedTemplate.setValues(new NumValue(getN(), 0), probValue);
	}
}