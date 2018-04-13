package exerciseTestProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;

import exerciseNormalProg.*;
import exerciseTest.*;


abstract public class CoreTestApplet extends CoreLookupApplet {
														//		needs to extend CoreLookupApplet so the distn lookup panels can call back
	static final private Color kSpacerColor = Color.gray;
	
	static final protected double kEpsP = 0.000001;
	static final private int kPvalueDecimals = 4;
	
	static final public int TAIL_LOW = 0;
	static final public int TAIL_LOW_EQ = 1;
	static final public int TAIL_HIGH = 2;
	static final public int TAIL_HIGH_EQ = 3;
	static final public int TAIL_BOTH = 4;
	
	private ResultValuePanel resultPanel;
	protected HypothesesPanel hypothesesPanel;
	private XChoice conclusionChoice;
	
	private int hypothResult, pValueResult, conclusionResult;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 9));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(answersPanel());
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("tail", "int");
		registerParameter("conclusions", "array");
		registerParameter("conclusionEnding", "string");
	}
	
	protected int getTail() {
		return getIntParam("tail");
	}
	
	private String getConclusionEnding() {
		return getStringParam("conclusionEnding");
	}
	
	private String[] getConclusions() {
		String ending = getConclusionEnding();
		
		StringArray conclusions = getArrayParam("conclusions");
		String s[] = conclusions.getStrings();
		for (int i=0 ; i<s.length ; i++) {
			s[i] = s[i].replaceAll("%", "#");
			if (ending != null)
				s[i] += ending;
			s[i] = MText.expandText(s[i]);
		}
		return s;
	}
	
//-----------------------------------------------------------
	
	
	abstract protected DataSet getData();
	
	abstract protected String getPValueLabel();
	abstract protected String getPValuesPropnsString();
	abstract protected String getPvalueLongName();
	
	abstract protected String parameterName();
	abstract protected String parameterLongName();
	
	abstract protected NumValue nullParamValue();
		
//-----------------------------------------------------------
	
	private XPanel topAnswersPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new EqualSpacingLayout());
		
			XPanel leftPanel = new InsetPanel(10, 5);
			leftPanel.setLayout(new BorderLayout(0, 3));
			
				XLabel hypothLabel = new XLabel("Hypotheses", XLabel.CENTER, this);
				hypothLabel.setFont(getBigBoldFont());
			leftPanel.add("North", hypothLabel);
			
//				NumValue consts[] = {new NumValue(10, 1), new NumValue(12, 1)};
				hypothesesPanel = new HypothesesPanel(MText.expandText(parameterName()), nullParamValue(), this);
				registerStatusItem("hypoth", hypothesesPanel);
		
			leftPanel.add("Center", hypothesesPanel);
		
		thePanel.add(leftPanel);
		
			Separator s = new Separator(0.9, 0, Separator.VERTICAL);
			s.setForeground(kSpacerColor);
		thePanel.add(s);
		
			XPanel rightPanel = new InsetPanel(10, 5);
			rightPanel.setLayout(new BorderLayout(0, 3));
			
				XLabel pValueLabel = new XLabel(getPValueLabel(), XLabel.CENTER, this);
				pValueLabel.setFont(getBigBoldFont());
			rightPanel.add("North", pValueLabel);
			
				resultPanel = new ResultValuePanel(this, null, 6);
				registerStatusItem("prob", resultPanel);
			rightPanel.add("Center", resultPanel);
		
		thePanel.add(rightPanel);
		
		return thePanel;
	}
	
	private XPanel answersPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		
		thePanel.add(topAnswersPanel());
		
			Separator s = new Separator(0.9, 3, Separator.HORIZONTAL);
			s.setForeground(kSpacerColor);
		thePanel.add(s);
		
			XPanel bottomPanel = new InsetPanel(10, 5);
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 3));
			
				XLabel conclusionLabel = new XLabel("Conclusion", XLabel.CENTER, this);
				conclusionLabel.setFont(getBigBoldFont());
			bottomPanel.add("North", conclusionLabel);
			
				conclusionChoice = new XChoice(this);
//				updateConclusions();
				registerStatusItem("conclusion", conclusionChoice);
			bottomPanel.add("Center", conclusionChoice);
		
		thePanel.add(bottomPanel);
		
		thePanel.lockBackground(kAnswerBackground);
		
		return thePanel;
	}
	
	private void updateConclusions() {
		conclusionChoice.clearItems();
		String[] conclusions = getConclusions();
		for (int i=0 ; i<conclusions.length ; i++)
			conclusionChoice.addItem(conclusions[i]);
	}
	
//-----------------------------------------------------------
	
	protected void resetAnswer() {
		hypothesesPanel.reset(MText.expandText(parameterName()), nullParamValue());
		updateConclusions();
		conclusionChoice.select(0);
		resultPanel.clear();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				insertInstructions(messagePanel);
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("The hypotheses have been correctly specified, but you must type a value into the \"" + getPValueLabel() + "\" box.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText(getPValuesPropnsString() + " cannot be less than zero or more than one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertBoldText("Hypotheses: ");
				insertHypothesesMessage(messagePanel);
				messagePanel.insertBoldText("\n" + getPValueLabel() + ": ");
				insertPvalueMessage(messagePanel);
				messagePanel.insertBoldText("\nConclusion: ");
				insertConclusionMessage(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Correct!\n");
				messagePanel.insertText("You have correctly performed all steps in the test.");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
				if (hypothResult == ANS_WRONG) {
					messagePanel.insertRedHeading("Hypotheses are wrong!\n");
					messagePanel.insertText("The null and alternative hypotheses have been incorrectly specified.");
				}
				else if (pValueResult == ANS_WRONG)
					insertWrongPValueMessage(messagePanel);
				else {
					if (pValueResult == ANS_CLOSE) {
						messagePanel.insertRedHeading("The " + getPvalueLongName() + " is close!\n");
						messagePanel.insertText("You have correctly specified the hypotheses and the " + getPvalueLongName() + " is correct to 3 decimal places, but you should be able to get the 4th decimal digit correct too.\n");
					}
					if (conclusionResult == ANS_CORRECT)
						messagePanel.insertRedHeading("Conclusion is OK!\n");
					else if (conclusionResult == ANS_CLOSE) {
						messagePanel.insertRedHeading("Conclusion is close!\n");
						if (pValueResult == ANS_CORRECT)
							messagePanel.insertText("(You correctly specified the hypotheses and found the " + getPvalueLongName() + ".)\n");
						messagePanel.insertText("Your conclusion from the " + getPvalueLongName() + " is acceptable but an adjacent choice from the pop-up menu would be a better conclusion.");
					}
					else {
						messagePanel.insertRedHeading("Conclusion is wrong!\n");
						if (pValueResult == ANS_CORRECT)
							messagePanel.insertText("(You correctly specified the hypotheses and found the " + getPvalueLongName() + ".)\nHowever your conclusion from the " + getPvalueLongName() + " is not correct.");
					}
				}
				
				break;
		}
	}
	
	abstract protected void insertInstructions(MessagePanel messagePanel);
	
	abstract protected void insertWrongPValueMessage(MessagePanel messagePanel);
	
	private void insertConclusionMessage(MessagePanel messagePanel) {
		double pValue = getCorrectPValue();
		messagePanel.insertText("Since the " + getPvalueLongName() + " is " + new NumValue(pValue, 4) + " and this is ");
		if (pValue > 0.1)
			messagePanel.insertText("greater than 0.1, there is no evidence that H#sub0# does not hold (i.e. that H#sub1# is true).");
		else if (pValue > 0.05)
			messagePanel.insertText("between 0.05 and 0.1, there is only weak evidence that H#sub0# does not hold (i.e. that H#sub1# is true).");
		else if (pValue > 0.01)
			messagePanel.insertText("between 0.01 and 0.05, there is moderately strong evidence that H#sub0# does not hold (i.e. that H#sub1# is true).");
		else if (pValue > 0.001)
			messagePanel.insertText("between 0.001 and 0.01, there is very strong evidence that H#sub0# does not hold (i.e. that H#sub1# is true).");
		else
			messagePanel.insertText("less than 0.001, it is almost certain that H#sub0# does not hold (i.e. that H#sub1# is true).");
	}
	
	abstract protected void insertPvalueMessage(MessagePanel messagePanel);
	
	private void insertHypothesesMessage(MessagePanel messagePanel) {
		NumValue theta0 = nullParamValue();
		String theta = parameterName();
		String thetaLong = parameterLongName();
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The alternative hypothesis, H#sub1#, is that " + thetaLong + " is ");
				messagePanel.insertBoldText("less than ");
				messagePanel.insertText(theta0 + ". ");
				if (tail == TAIL_LOW)
					messagePanel.insertText("From the wording of the question, H#sub0# is that " + theta + " #ge# " + theta0 + " (though " + theta + " = " + theta0 + " is also acceptable).");
				else
					messagePanel.insertText("From the wording of the question, H#sub0# is that " + theta + " equals " + theta0 + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The alternative hypothesis, H#sub1#, is that " + thetaLong + " is ");
				messagePanel.insertBoldText("greater than ");
				messagePanel.insertText(theta0 + ". ");
				if (tail == TAIL_HIGH)
					messagePanel.insertText("From the wording of the question, H#sub0# is that " + theta + " #le# " + theta0 + " (though " + theta + " = " + theta0 + " is also acceptable).");
				else
					messagePanel.insertText("From the wording of the question, H#sub0# is that " + theta + " equals " + theta0 + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The alternative hypothesis, H#sub1#, is that " + thetaLong + " is ");
				messagePanel.insertBoldText("not equal to ");
				messagePanel.insertText(theta0 + " and H#sub0# is therefore that " + theta + " equals " + theta0 + ".");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 160;
	}
	
//-----------------------------------------------------------
	
	protected boolean correctModelSetup() {						//		overriden for simulations
		return true;
	}
	
	private boolean isClose(double attempt, double correct) {
		return Math.round((correct - attempt) * 500) == 0;
	}
	
	private boolean isCorrect(double attempt, double correct) {
		return Math.round((correct - attempt) * 5000) == 0;
	}
	
	abstract protected double getCorrectPValue();
	abstract protected boolean lowTailHighlight();
	
	private int getCorrectConclusionIndex(double correctPvalue) {
		int correctP = (int)Math.round(correctPvalue * 10000);
							//	based on scaled p-value to make conclusion based on p-value rounded as displayed in applet
		return (correctP > 1000) ? 0 : (correctP > 500) ? 1
															: (correctP > 100) ? 2 : (correctP > 10) ? 3 : 4;
	}
	
	protected void showCorrectAnswer() {
		double correctPvalue = getCorrectPValue();
		NumValue pValue = new NumValue(correctPvalue, kPvalueDecimals);
		
		resultPanel.showAnswer(pValue);
		hypothesesPanel.setCorrectTail(getTail());
		conclusionChoice.select(getCorrectConclusionIndex(pValue.toDouble()));
	}
	
	private double getAttemptPValue() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected int assessAnswer() {
		hypothResult = hypothesesPanel.assessHypotheses(getTail());
		
		if (hypothResult == ANS_WRONG)
			return ANS_WRONG;
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
			
		double attempt = getAttemptPValue();
		if (attempt < 0 || attempt > 1)
			return (pValueResult = ANS_INVALID);		//	'=' is assignment not equality test
		
		double correct = getCorrectPValue();
		if (correctModelSetup()) {
			if (isCorrect(attempt, correct))
				pValueResult = ANS_CORRECT;
			else if (isClose(attempt, correct))
				pValueResult =  ANS_CLOSE;
			else
				return (pValueResult = ANS_WRONG);		//	'=' is assignment not equality test
		}
		else
			return (pValueResult = ANS_WRONG);
		
		int correctConclusionIndex = getCorrectConclusionIndex(attempt);
		int conclusionIndex = conclusionChoice.getSelectedIndex();
		if (correctConclusionIndex == conclusionIndex)
			conclusionResult = ANS_CORRECT;
		else if (correctConclusionIndex > 0 && Math.abs(correctConclusionIndex - conclusionIndex) == 1)
			conclusionResult = ANS_CLOSE;
		else
			return (conclusionResult = ANS_WRONG);	//	'=' is assignment not equality test
		
		if (pValueResult == ANS_CLOSE || conclusionResult == ANS_CLOSE)
			return ANS_CLOSE;
		else
			return ANS_CORRECT;
	}
	
	protected double getMark() {
		assessAnswer();
		if (hypothResult == ANS_WRONG)
			return 0.0;
		else {
			double mark = 0.3;
			if (pValueResult == ANS_WRONG)
				return mark;
			if (pValueResult == ANS_CLOSE)
				mark = 0.6;
			else
				mark = 0.7;
			
			if (conclusionResult == ANS_WRONG)
				return mark;
			else if (conclusionResult == ANS_CLOSE)
				return mark + 0.25;
			else
				return mark + 0.3;
		}
	}
	
	protected void giveFeedback() {
	}
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == conclusionChoice) {
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