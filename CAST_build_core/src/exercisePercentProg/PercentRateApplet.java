package exercisePercentProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import exercisePercent.*;


public class PercentRateApplet extends ExerciseApplet {
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	static final protected Color kAnswerLabelColor = new Color(0x990000);
	
	static final protected NumValue kOneValue = new NumValue(1.0, 0);
	static final protected NumValue kHundredValue = new NumValue(100.0, 0);
	
	static final protected int PROPN = 0;
	static final protected int PERCENT = 1;
	static final protected int RATE = 2;
	static final protected int RETURN_PERIOD = 3;
	
	protected MultTemplatePanel multTemplate;
	
	protected ResultValuePanel resultPanel;
	
//================================================

	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
			
			topPanel.add(getWorkingPanels(null));
				
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
					XLabel answerLabel = new XLabel("Answer:", XLabel.LEFT, this);
					answerLabel.setFont(getStandardBoldFont());
					answerLabel.setForeground(kAnswerLabelColor);
				answerPanel.add(answerLabel);
				
					resultPanel = new ResultValuePanel(this, "", "", 6);
					registerStatusItem("answer", resultPanel);
				answerPanel.add(resultPanel);
				
			topPanel.add(answerPanel);
			
			topPanel.add(createMarkingPanel(NO_HINTS));
				
		add("North", topPanel);
			
			XPanel messagePanel = new XPanel();
			messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
				
				message = new ExerciseMessagePanel(this);
			messagePanel.add(message);
		
		add("Center", messagePanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("nTrials", "int");
		registerParameter("nSuccess", "int");
		registerParameter("trialName", "string");
		registerParameter("successName", "string");
		registerParameter("nTarget", "int");
		registerParameter("returnPrefix", "string");
		registerParameter("returnSuffix", "string");
		registerParameter("propnDecimals", "int");
		registerParameter("questionType", "choice");
	}
	
	protected int getNTrials() {
		return getIntParam("nTrials");
	}
	
	protected int getNSuccess() {
		return getIntParam("nSuccess");
	}
	
	protected String getTrialName() {
		return getStringParam("trialName");
	}
	
	protected String getSuccessName() {
		return getStringParam("successName");
	}
	
	protected int getQuestionType() {
		return getIntParam("questionType");		//	0=propn, 1=percent, 2=rate, 3=returnPeriod
	}
	
	protected int getTargetTrials() {
		return getIntParam("nTarget");
	}
	
	private String getReturnPrefix() {
		return getStringParam("returnPrefix");
	}
	
	private String getReturnSuffix() {
		return getStringParam("returnSuffix");
	}
	
	protected int getPropnDecimals() {
		return getIntParam("propnDecimals");
	}
	
	protected int getQuestionExtraMask() {
		int mask = hasOption("propn") ? 1 : 0;
		if (hasOption("percent"))
			mask += 2;
		if (hasOption("rate"))
			mask += 4;
		if (hasOption("returnPeriod"))
			mask += 8;
		if (mask == 0)
			return -1;
		else
			return mask;
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
			multTemplate = new MultTemplatePanel(stdContext);
			multTemplate.lockBackground(kTemplateBackground);
			registerStatusItem("multTemplate", multTemplate);
		thePanel.add("Center", multTemplate);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		multTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		String label = null;
		String units = null;
		switch (getQuestionType()) {
			case PROPN:
				label = "Proportion of " + getTrialName() + "s that are " + getSuccessName() + "s is";
				break;
			case PERCENT:
				label = "Percentage of " + getTrialName() + "s that are " + getSuccessName() + "s is";
				units = "%";
				break;
			case RATE:
				label = "Rate of " + getTrialName() + "s that are " + getSuccessName() + "s is";
				units = getTrialName() + "s per " + getTargetTrials() + " " + getTrialName() + "s";
				break;
			case RETURN_PERIOD:
				label = getReturnPrefix();
				units = getReturnSuffix();
				break;
		}
		resultPanel.changeLabel(label);
		resultPanel.changeUnits(units);
		resultPanel.clear();
		resultPanel.invalidate();
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}
	
	
//-----------------------------------------------------------
	
	protected NumValue getCorrectAnswer() {
			int nTrials = getNTrials();
			int nSuccess = getNSuccess();
			double propn = nSuccess / (double)nTrials;
			int propnDecimals = getPropnDecimals();
			switch (getQuestionType()) {
				case PROPN:
					return new NumValue(propn, propnDecimals);
				case PERCENT:
					return new NumValue(propn * 100, propnDecimals - 2);
				case RATE:
					int nTarget = getTargetTrials();
					int rateDecimals = propnDecimals;
					if (nTarget > 50)
						rateDecimals --;
					if (nTarget > 5)
						rateDecimals --;
					return new NumValue(propn * nTarget, rateDecimals);
				default:
				case RETURN_PERIOD:
					double returnPeriod = 1 / propn;
					int decimals = (returnPeriod > 10) ? 0 : 1;
					return new NumValue(returnPeriod, decimals);
			}
	}
	
	protected void insertComment(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("\n(You may find parts of the template useful for any calculation required.)");
				break;
			case ANS_TOLD:
				messagePanel.insertText("\nThe template shows the calculation.");
				break;
		}
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type a value in the Answer box above.");
				break;
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the answer above, correct to ");
				int decimals = getCorrectAnswer().decimals;
				if (decimals == 0)
					messagePanel.insertText("the nearest whole number.");
				else if (decimals == 1)
					messagePanel.insertText("1 decimal place.");
				else
					messagePanel.insertText(decimals + " decimal places.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				switch (getQuestionType()) {
					case PROPN:
						messagePanel.insertText("The proportion is the number with " + getSuccessName() + " divided by the total number of " + getTrialName() + "s.");
						break;
					case PERCENT:
						messagePanel.insertText("The percentage is the proportion with " + getSuccessName() + " times 100.");
						break;
					case RATE:
						messagePanel.insertText("The rate per " + getTargetTrials() + " is the propn with " + getSuccessName() + " multiplied by " + getTargetTrials() + ".");
						break;
					default:
					case RETURN_PERIOD:
						messagePanel.insertText("The return period is the inverse of the proportion with " + getSuccessName() + ".");
						break;
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("The correct answer is " + getCorrectAnswer() + ".");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				int correctDecimals = getCorrectAnswer().decimals;
				messagePanel.insertText("Your answer is correct ");
				if (correctDecimals == 0)
					messagePanel.insertText("to the nearest ten.");
				else if (correctDecimals == 1)
					messagePanel.insertText("to the nearest whole number.");
				else
					messagePanel.insertText("to " + (correctDecimals - 1) + " decimal places.");
				messagePanel.insertRedText(" Try to get it correct to one more digit.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not close enough!\n");
				switch (getQuestionType()) {
					case PROPN:
						messagePanel.insertRedText("Divide the number of " + getSuccessName() + "s by the number of " + getTrialName() + "s.");
						break;
					case PERCENT:
						messagePanel.insertRedText("The percentage is the proportion with " + getSuccessName() + " times 100.");
						break;
					case RATE:
						messagePanel.insertRedText("Find the proportion of " + getSuccessName() + "s then multiply by " + getTargetTrials() + ".");
						break;
					default:
					case RETURN_PERIOD:
						messagePanel.insertRedText("The return period is the inverse of the proportion of " + getSuccessName() + "s.");
						break;
				}
				break;
		}
		insertComment(messagePanel);
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = resultPanel.getAttempt().toDouble();
			NumValue correctVal = getCorrectAnswer();
			double correct = correctVal.toDouble();		//	round to displayed decimals
			double factor = 1.0;
			for (int i=0 ; i<correctVal.decimals ; i++) {
				correct *= 10.0;
				factor *= 0.1;
			}
			correct *= factor;
			
			int decimals = correctVal.decimals;
			double correctSlop = 0.5 * Math.pow(10.0, -decimals);
			double approxSlop = 0.5 * Math.pow(10.0, 1 - decimals);
			
			return (Math.abs(correct - attempt) < correctSlop) ? ANS_CORRECT
								: (Math.abs(correct - attempt) < approxSlop) ? ANS_CLOSE : ANS_WRONG;
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue correctVal = getCorrectAnswer();
		resultPanel.showAnswer(correctVal);
		
		NumValue nTrials = new NumValue(getNTrials(), 0);
		NumValue nSuccess = new NumValue(getNSuccess(), 0);
		if (multTemplate != null)
			switch (getQuestionType()) {
				case PROPN:
					multTemplate.setValues(nSuccess, nTrials, kOneValue);
					break;
				case PERCENT:
					multTemplate.setValues(nSuccess, nTrials, kHundredValue);
					break;
				case RATE:
					NumValue nTarget = new NumValue(getTargetTrials(), 0);
					multTemplate.setValues(nSuccess, nTrials, nTarget);
					break;
				default:
				case RETURN_PERIOD:
					multTemplate.setValues(nTrials, nSuccess, kOneValue);
					break;
			}
	}
	
	protected double getMark() {
		int ans = assessAnswer();
		return (ans == ANS_CORRECT) ? 1 : (ans == ANS_CLOSE) ? 0.7 : 0;
	}
}