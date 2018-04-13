package exerciseEstimProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class StErrorStatementApplet extends ExerciseApplet {
	static final private String STATEMENT_START_PARAM = "statementStart";
	static final private String STATEMENT_END_PARAM = "statementEnd";
	static final private String OPTIONS_TEMPLATE_PARAM = "options";
	static final private String NO_OF_STATEMENTS_PARAM = "noOfStatements";
		
	int noOfStatements;
	private String statementStartText[];
	private String statementEndText[];
	private StatementMenuPanel[] stErrorMessage;
	
	
	private int statementPermutation[];
	private Random random01;
	
//================================================
	
	protected void createDisplay() {
		noOfStatements = Integer.parseInt(getParameter(NO_OF_STATEMENTS_PARAM));
		statementStartText = new String[noOfStatements];
		statementEndText = new String[noOfStatements];
		stErrorMessage = new StatementMenuPanel[noOfStatements];
		
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
		registerParameter("q0_incDec", "choice");
		registerParameter("q0_sesd", "choice");
		registerParameter("q0_menu", "string");
		registerParameter("q1_meanPropn", "choice");
		registerParameter("q1_sesd", "choice");
		registerParameter("q1_menu", "string");
		registerParameter("q2_sdFirst", "choice");
		registerParameter("q2_menu", "string");
	}
	
	private boolean q0_isSampleSizeIncrease() {
		return getIntParam("q0_incDec") == 0;
	}
	
	private boolean q0_isSeNotSd() {
		return getIntParam("q0_sesd") == 0;
	}
	
	private String[] readMenuItems(String paramName) {
		StringTokenizer st = new StringTokenizer(getStringParam(paramName), "*");
		String[] menuItems = new String[st.countTokens()];
		for (int i=0 ; i<menuItems.length ; i++)
			menuItems[i] = st.nextToken();
		return menuItems;
	}
	
	private boolean q1_isSeNotSd() {
		return getIntParam("q1_sesd") == 0;
	}
	
	private boolean q2_isSdFirst() {
		return getIntParam("q2_sdFirst") == 0;
	}
	
	private String[] getMenuItems(int qnIndex) {
		switch (qnIndex) {
			case 0:		//	same/increase/decrease
				return readMenuItems("q0_menu");
			case 1:		//	var of values/var of estimate
				return readMenuItems("q1_menu");
			case 2:		//	equal /smaller/greater
				return readMenuItems("q2_menu");
			default:
				return null;
		}
	}
	
	private int getCorrectChoice(int qnIndex) {
		switch (qnIndex) {
			case 0:
				if (q0_isSeNotSd())
					return q0_isSampleSizeIncrease() ? 2 : 1;
				else
					return 0;
			case 1:
				return q1_isSeNotSd() ? 1 : 0;
			case 2:
				return q2_isSdFirst() ? 2 : 1;
			default:
				return -1;
		}
	}
	
	private String getAnswerString(int qnIndex) {
		switch (qnIndex) {
			case 0:
				if (q0_isSeNotSd())
					return "The more data that is collected, the more accurate the estimate and hence the smaller its standard error.";
				else
					return "The standard deviation describes the spread of values in the sample and this does not depend on the sample size.";
			case 1:
				return "The standard deviation describes the variability of sample values; the standard error of an estimator describes the variability of that estimator.";
			case 2:
				return "The sample mean is less variable than individual values from the population so the mean's standard error is less than the standard deviation.";
			default:
				return null;
		}
	}
	
	public String expandQuestion(String qnParamTemplate, String qnTemplate,
																															String qnExtraTemplate) {
		Object oldParam[] = cloneParameters();
		
		String questionText = super.expandQuestion(qnParamTemplate, qnTemplate, qnExtraTemplate);
		
		for (int i=0 ; i<noOfStatements ; i++) {
			String statementStart = getParameter(STATEMENT_START_PARAM + i);
			String statementEnd = getParameter(STATEMENT_END_PARAM + i);
			String paramTemplate = getParameter(OPTIONS_TEMPLATE_PARAM + i);
			expandOneQuestionString(paramTemplate, oldParam);
			
			statementStartText[i] = expandOneQuestionString(statementStart, oldParam);
			statementEndText[i] = (statementEnd == null) ? null
																			: expandOneQuestionString(statementEnd, oldParam);
		}
		
		return questionText;
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		random01 = new Random(nextSeed());
		statementPermutation = new int[noOfStatements];
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
		thePanel.add(new Separator(0.4, 10));
		
		for (int i=0 ; i<noOfStatements ; i++) {
			stErrorMessage[i] = new StatementMenuPanel(this, i + 1);
			registerStatusItem("stErrorMessage" + i, stErrorMessage[i]);
			thePanel.add(stErrorMessage[i]);
		}
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		permute(statementPermutation, random01);
		
		for (int i=0 ; i<noOfStatements ; i++) {
			int sourceIndex = statementPermutation[i];
			stErrorMessage[i].setStatement(statementStartText[sourceIndex], statementEndText[sourceIndex],
																																				getMenuItems(sourceIndex));
			
			stErrorMessage[i].setCorrectChoice(getCorrectChoice(sourceIndex));
			stErrorMessage[i].setAnswerString(getAnswerString(sourceIndex));
			stErrorMessage[i].setTextBackground(Color.white);
			
			stErrorMessage[i].invalidate();
		}
	}
	
	protected DataSet getData() {
		return null;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------

	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Complete the three statements above.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				for (int i=0 ; i<noOfStatements ; i++) {
					messagePanel.insertBoldRedText("\n" + (i+1) + ". ");
					messagePanel.insertText(stErrorMessage[i].getAnswerString());
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly completed all three statements.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedText("The wrong statements are highlighted in red.");
				for (int i=0 ; i<noOfStatements ; i++)
					if (!stErrorMessage[i].isCorrect()) {
						messagePanel.insertBoldRedText("\n" + (i+1) + ". ");
						messagePanel.insertText(stErrorMessage[i].getAnswerString());
					}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed)
			clearStatementBackground();
		return changed;
	}
	
	private void clearStatementBackground() {
		for (int i=0 ; i<noOfStatements ; i++)
			stErrorMessage[i].setTextBackground(Color.white);
	}
	
	private int countErrors() {
		int nErrors = 0;
		for (int i=0 ; i<noOfStatements ; i++)
			if (!stErrorMessage[i].isCorrect())
				nErrors ++;
		return nErrors;
	}
	
	protected int assessAnswer() {
		return (countErrors() == 0) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<noOfStatements ; i++)
			if (stErrorMessage[i].isCorrect())
				stErrorMessage[i].setTextBackground(kCorrectAnswerBackground);
			else
				stErrorMessage[i].setTextBackground(kWrongAnswerBackground);
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<stErrorMessage.length ; i++)
			stErrorMessage[i].showCorrectChoice();
			
		clearStatementBackground();
	}
	
	protected double getMark() {
		int nErrors = countErrors();
		return (nErrors == 0) ? 1 : (nErrors == 1) ? 0.3 : 0;
	}
	
}