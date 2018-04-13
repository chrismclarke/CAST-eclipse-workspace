package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;

import exerciseEstim.*;


public class GenericCiApplet extends ExerciseApplet {
//	static final private String VAR_NAME_PARAM = "varName";
	
	private CiResultPanel resultPanel;
	
	private int lowResult, highResult;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
		
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				
//				answerPanel.add(getWorkingPanels(null));
				
					resultPanel = new CiResultPanel(this, "Interval is", 6);
					resultPanel.setFont(getBigFont());
				
				answerPanel.add(resultPanel);
					
				answerPanel.add(createMarkingPanel(NO_HINTS));
			
			bottomPanel.add("North", answerPanel);
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add("Center", messagePanel);
		
		add("Center", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("intervalWording", "int");
		registerParameter("estimate", "const");
		registerParameter("se", "const");
	}
	
	public boolean isIntervalType() {
		return getIntParam("intervalWording") == 0;
	}
	
	public NumValue getEstimate() {
		return getNumValueParam("estimate");
	}
	
	public NumValue getSe() {
		return getNumValueParam("se");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		return null;
	}
	
	protected void setDisplayForQuestion() {
		if (isIntervalType())
			resultPanel.changeLabel("Interval is", "to");
		else
			resultPanel.changeLabel("... between", "and");
		resultPanel.invalidate();
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumValue estimate = getEstimate();
		NumValue se = getSe();
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Find the confidence interval.\n");
				messagePanel.insertBoldBlueText("(Hint: The calculation is simple enough to be done in your head, but a calculator may help.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type the lower and upper values for the confidence interval into the boxes above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("An approximate 95% confidence interval for any parameter can be found from an estimate #plusMinus#2 standard errors.");
				messagePanel.insertText("\nFor this question, the interval is\n");
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				messagePanel.insertText("(" + estimate + " - 2 #times# " + se + ")   to   (" + estimate + " + 2 #times# " + se + ")");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("The interval that you have given is correct (or as close as could be expected).");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
				messagePanel.insertRedHeading((result == ANS_CLOSE) ? "Close!\n" : "Not close enough!\n");
				switch (lowResult) {
					case ANS_CORRECT:
						messagePanel.insertText("Your lower limit for the interval is correct.");
						break;
					case ANS_CLOSE:
						messagePanel.insertRedText("Your lower limit for the interval is close to the correct value. It should be\n");
						messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
						messagePanel.insertRedText("(" + estimate + " - 2 #times# " + se + ")\n");
						messagePanel.setAlignment(MessagePanel.LEFT_ALIGN);
						break;
					case ANS_WRONG:
						messagePanel.insertRedText("Your lower limit for the interval is wrong. Use\n");
						messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
						messagePanel.insertRedText("(estimate - 2 #times# se)\n");
						messagePanel.setAlignment(MessagePanel.LEFT_ALIGN);
				}
				switch (highResult) {
					case ANS_CORRECT:
						messagePanel.insertText("Your upper limit for the interval is correct.");
						break;
					case ANS_CLOSE:
						messagePanel.insertRedText("Your upper limit for the interval is close to the correct value. It should be\n");
						messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
						messagePanel.insertRedText("(" + estimate + " + 2 #times# " + se + ")");
						break;
					case ANS_WRONG:
						messagePanel.insertRedText("Your upper limit for the interval is wrong. Use\n");
						messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
						messagePanel.insertRedText("(estimate + 2 #times# se)");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
	
//-----------------------------------------------------------
	
	private NumValue getLowCorrect() {
		int decimals = Math.max(getEstimate().decimals, getSe().decimals);
		double estimate = getEstimate().toDouble();
		double se = getSe().toDouble();
		
		return new NumValue(estimate - 2 * se, decimals);
	}
	
	private NumValue getHighCorrect() {
		int decimals = Math.max(getEstimate().decimals, getSe().decimals);
		double estimate = getEstimate().toDouble();
		double se = getSe().toDouble();
		
		return new NumValue(estimate + 2 * se, decimals);
	}
	
	protected int assessAnswer() {
		NumValue lowCorrect = getLowCorrect();
		NumValue highCorrect = getHighCorrect();
		int decimals = lowCorrect.decimals;
		
		double correctSlop = 0.5 * Math.pow(0.1, decimals);
		double approxSlop = 2 * Math.pow(0.1, decimals);
		
		double lowAttempt = resultPanel.getLowAttempt().toDouble();
		double highAttempt = resultPanel.getHighAttempt().toDouble();
		
		double lowError = Math.abs(lowAttempt - lowCorrect.toDouble());
		lowResult = resultPanel.isIncomplete() ? ANS_INCOMPLETE
								: (lowError < correctSlop) ? ANS_CORRECT
								: (lowError < approxSlop) ? ANS_CLOSE
								: ANS_WRONG;
		
		double highError = Math.abs(highAttempt - highCorrect.toDouble());
		highResult = resultPanel.isIncomplete() ? ANS_INCOMPLETE
								: (highError < correctSlop) ? ANS_CORRECT
								: (highError < approxSlop) ? ANS_CLOSE
								: ANS_WRONG;
		
		return Math.max(lowResult, highResult);
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
//		NumValue estimateVal = getEstimate();
//		NumValue seVal = getSe();
		
		resultPanel.showAnswer(getLowCorrect(), getHighCorrect());
	}
	
	protected double getMark() {
		int theResult = assessAnswer();
		return (theResult == ANS_CORRECT) ? 1 : (theResult == ANS_CLOSE) ? 0.3 : 0;
	}
}