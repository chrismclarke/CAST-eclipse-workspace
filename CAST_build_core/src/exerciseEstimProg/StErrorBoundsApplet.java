package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class StErrorBoundsApplet extends ExerciseApplet {
//	static final private String STATEMENT_START_PARAM = "statementStart";
//	static final private String STATEMENT_END_PARAM = "statementEnd";
	
//	private String statementStartText;
//	private String statementEndText;
	private StatementMenuPanel stErrorMessage;
	
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 4));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
		
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new BorderLayout(0, 4));
				
				answerPanel.add("Center", getWorkingPanels(null));
				answerPanel.add("South", createMarkingPanel(NO_HINTS));
			
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
		registerParameter("sesd", "choice");
		registerParameter("statementType", "choice");
		registerParameter("baseSd", "choice");
		registerParameter("options", "array");
		registerParameter("units", "string");
		registerParameter("seFactor", "int");
		registerParameter("statementStart0", "string");
		registerParameter("statementStart1", "string");
		registerParameter("statementStart2", "string");
		registerParameter("statementEnd", "string");
	}
	
	private boolean isSeNotSd() {
		return getIntParam("sesd") == 0;
	}
	
	private int getStatementType() {
		return getIntParam("statementType");
	}
	
	private int getBaseSd() {
		return getIntParam("baseSd");
	}
	
	private String[] getMenuItems() {
		return getArrayParam("options").getStrings();
//		StringTokenizer st = new StringTokenizer(getStringParam("options"), "*");
//		String[] menuItems = new String[st.countTokens()];
//		for (int i=0 ; i<menuItems.length ; i++)
//			menuItems[i] = st.nextToken();
//		return menuItems;
	}
	
	private String getUnits() {
		return getStringParam("units");
	}
	
	private int getSeFactor() {
		return getIntParam("seFactor");
	}
	
	private String getStatementStartText() {
		switch (getStatementType()) {
			case 0:
				return getStringParam("statementStart0");
			case 1:
				return getStringParam("statementStart1");
			case 2:
			default:
				return getStringParam("statementStart2");
		}
	}
	
	private String getStatementEndText() {
		return getStringParam("statementEnd");
	}
	
	private int getCorrectChoice() {
		int correctIndex = 1 + getBaseSd();
		if (isSeNotSd())
			correctIndex ++;
			
		if (getStatementType() == 0)		//	interval for mean
			correctIndex --;
		
		return correctIndex;
	}
	
	private String getAnswerString() {
		if (isSeNotSd() && getStatementType() == 0)
			return "The question provides the standard error of the sample mean and the statement is also about the mean.\nThe sample mean has about 95% probabiity of being within two standard errors of the population mean.";
		else if (!isSeNotSd() && getStatementType() != 0)
			return "The question provides the standard deviation of the sample and the statement is also about the variability of individual values.\nFrom the 70-95-100 rule of thumb, approximately 95% of sample and population values will be within two standard deviations of the mean.";
		else if (!isSeNotSd() && getStatementType() == 0)
			return "The statement is about the variation (accuracy) of the sample mean so it must be answered using the standard error of the mean.\nThe standard error is the sample standard deviation divided by #sqrt#n (i.e. s/" + getSeFactor() + ") and there is approximately 95% probability of the sample mean being under twice this (2s/" + getSeFactor() + ") from the population mean.";
		else
			return "The statement is about the variation of individual values so it must be answered using the sample standard deviation. However the standard error of the sample mean is provided in the question, not the sample standard deviation, s.\nThe standard error of the mean is s/#sqrt#n (i.e. s/" + getSeFactor() + ") so we can find the sample standard deviation by multiplying the standard error by " + getSeFactor() + ".\nAbout 95% of the sample (and population) should be within twice this of the population mean.";
	}
	
/*
	public String expandQuestion(String qnParamTemplate, String qnTemplate,
																															String qnExtraTemplate) {
		Object oldParam[] = cloneParameters();
		
		String questionText = super.expandQuestion(qnParamTemplate, qnTemplate, qnExtraTemplate);
		
		String statementStart = getParameter(STATEMENT_START_PARAM + questionVersion);
		String statementEnd = getParameter(STATEMENT_END_PARAM + questionVersion);
		
		statementStartText = expandOneQuestionString(statementStart, oldParam);
		statementEndText = (statementEnd == null) ? null
																			: expandOneQuestionString(statementEnd, oldParam);
		
		return questionText;
	}
*/
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
		thePanel.add(new Separator(0.4, 10));
		
		stErrorMessage = new StatementMenuPanel(this, 0);
		registerStatusItem("stErrorMessage", stErrorMessage);
		thePanel.add(stErrorMessage);
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		stErrorMessage.setStatement(getStatementStartText(), getStatementEndText(), getMenuItems(), getUnits());
			
		stErrorMessage.setCorrectChoice(getCorrectChoice());
		stErrorMessage.setAnswerString(getAnswerString());
			
		stErrorMessage.invalidate();
	}
	
	protected DataSet getData() {
		return null;
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------

	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Complete the statement above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(stErrorMessage.getAnswerString());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your statement correctly describes ");
				messagePanel.insertText((getStatementType() == 0) ? "the spread of the sample mean" : "variability of individual values in the sample (and population)" + ".");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText(stErrorMessage.getAnswerString());
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
	
//-----------------------------------------------------------
	
	protected void giveFeedback() {
	}
	
	protected int assessAnswer() {
		return stErrorMessage.isCorrect() ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void showCorrectWorking() {
		stErrorMessage.showCorrectChoice();
	}
	
	protected double getMark() {
		return stErrorMessage.isCorrect() ? 1 : 0;
	}
	
}