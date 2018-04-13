package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import exerciseEstim.*;


public class FindPropn95CiApplet extends FindPropnSeApplet {
	
	private CiResultPanel resultPanel;
	
	protected XPanel getAnswerPanel() {
		XPanel ansPanel = new InsetPanel(0, 10, 0, 0);
		ansPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		ansPanel.add(getCiPanel());
		return ansPanel;
	}
	
	protected CiResultPanel getCiPanel() {
		resultPanel = new CiResultPanel(this, "Interval is", null, 6,
																												getIntervalOrientation());
		resultPanel.setFont(getBigFont());
		registerStatusItem("ci", resultPanel);
		return resultPanel;
	}
	
	protected int getIntervalOrientation() {
		return CiResultPanel.HORIZONTAL;
	}
	
	protected void clearResult() {
		resultPanel.clear();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the template to evaluate the standard error of the sample proportion and use it to find an approximate 95% confidence interval.");
				messagePanel.insertText("\n(Hint: Although the calculation is simple, you will probably need to use a calculator.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type values for both ends of the confidence interval in the two boxes above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The lower limit of the interval must be less than the upper limit.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The standard error of the sample proportion is given by the formula,\n");
				insertSeFormula(messagePanel);
				messagePanel.insertText("\n");
				messagePanel.setAlignment(MessagePanel.LEFT_ALIGN);
				messagePanel.insertText("An approximate 95% confidence interval is the sample proportion, p = "
															+ new NumValue(evaluateProb(), maxPropn.decimals)
															+ ", #plusMinus# twice this standard error.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found an approximate 95% confidence interval for the sample proportion,\n");
				insertCiFormula(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!");
				analyseMistake(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!");
				analyseMistake(messagePanel);
				break;
		}
	}
	
	protected void analyseMistake(MessagePanel messagePanel) {
		String templateResultString = seTemplate.getResult().toString();
		String correctString = new NumValue(evaluateSe(), maxPropn.decimals).toString();
		
		if (templateResultString.equals(correctString)) {
			messagePanel.insertText("\nYou seem to have correctly evaluated the standard error in the template.");
			messagePanel.insertRedText("\nThe confidence interval should be the sample proportion, p = "
								+ new NumValue(evaluateProb(), maxPropn.decimals) + ", #plusMinus#2 times this.");
		}
		else
			analyseSeTemplate(messagePanel);
	}
	
	private MFormula ciFormula(FormulaContext context) {
		MFormula se = seFormula(context);
		MFormula plusMinus = new MBinary(MBinary.TIMES, new MText("2", context), se, context);
		MFormula p = new MText("p", context);
		return new MBinary(MBinary.PLUS_MINUS, p, plusMinus, context);
	}
	
	private void insertCiFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		MFormula ci = ciFormula(context);
		
		messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
		messagePanel.insertFormula(ci);
	}
	
//-----------------------------------------------------------
	
	protected double getLowAttempt() {
		return resultPanel.getLowAttempt().toDouble();
	}
	
	protected double getHighAttempt() {
		return resultPanel.getHighAttempt().toDouble();
	}
	
	protected double evaluatePlusMinus() {
		return 2 * evaluateSe();
	}
	
	private double evaluateCorrectLimit(double plusMinus) {
		double propn = evaluateProb();
		return propn + plusMinus;
	}
	
	
//-----------------------------------------------------------

	final protected int checkLimit(double attempt, double plusMinus) {
		double correct = evaluateCorrectLimit(plusMinus);
		double exactSlop = 0.5 * Math.pow(0.1, maxPropn.decimals);
		double approxSlop = exactSlop * 5;
		double absError = Math.abs(attempt - correct);
		return (absError < exactSlop) ? ANS_CORRECT
									: (absError < approxSlop) ? ANS_CLOSE
									: ANS_WRONG;
	}
	
	final protected int checkValidAnswer() {
		if (resultPanel.isIncomplete())
			return ANS_INCOMPLETE;
			
		double lowAttempt = getLowAttempt();
		double highAttempt = getHighAttempt();
		if (lowAttempt >= highAttempt)
			return ANS_INVALID;
			
		return ANS_UNCHECKED;
	}
	
	protected int assessAnswer() {
		int validityResult = checkValidAnswer();
		if (validityResult != ANS_UNCHECKED)
			return validityResult;
		
		double plusMinus = evaluatePlusMinus();
		
		double lowAttempt = getLowAttempt();
		int lowAns2 = checkLimit(lowAttempt, -plusMinus);
		int lowAns196 = checkLimit(lowAttempt, -plusMinus * 1.96 / 2);
		int lowAns = Math.min(lowAns2, lowAns196);		//	accept either 2 or 1.96
		
		double highAttempt = getHighAttempt();
		int highAns2 = checkLimit(highAttempt, plusMinus);
		int highAns196 = checkLimit(highAttempt, plusMinus * 1.96 / 2);
		int highAns = Math.min(highAns2, highAns196);
		
		return Math.max(lowAns, highAns);
	}
	
	protected void giveFeedback() {
	}
	
	protected void showResult() {
		double plusMinus = evaluatePlusMinus();
		double lowCorrect = evaluateCorrectLimit(-plusMinus);
		double highCorrect = evaluateCorrectLimit(plusMinus);
		int decimals = maxPropn.decimals;
		resultPanel.showAnswer(new NumValue(lowCorrect, decimals), new NumValue(highCorrect, decimals));
	}
}