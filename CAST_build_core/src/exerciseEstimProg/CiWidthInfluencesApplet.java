package exerciseEstimProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;


public class CiWidthInfluencesApplet extends ExerciseApplet {
	static final private String STATEMENT_START_PARAM = "statementStart";
	static final private String STATEMENT_END_PARAM = "statementEnd";
	static final private String MESSAGES_PARAM = "messages";
	static final private String NO_OF_STATEMENTS_PARAM = "noOfStatements";
	
	int noOfStatements;
	private String statementStartText[];
	private String statementEndText[];
	private String answerMessage[];
	private StatementMenuPanel[] statementPanel;
	
	
	private int statementPermutation[];
	private Random random01;
	
//================================================
	
	protected void createDisplay() {
		noOfStatements = Integer.parseInt(getParameter(NO_OF_STATEMENTS_PARAM));
		statementStartText = new String[noOfStatements];
		statementEndText = new String[noOfStatements];
		answerMessage = new String[noOfStatements];
		statementPanel = new StatementMenuPanel[noOfStatements];
		
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
		registerParameter("ciLevel", "choice");
		registerParameter("sampleSize", "choice");
		registerParameter("spread", "choice");
		registerParameter("menuLevel", "array");
		registerParameter("menuSampleSize", "array");
		registerParameter("menuSpread", "array");
		registerParameter("popnName", "string");				//	optional for some qns
		registerParameter("successName", "string");			//	only used for propn version of applet
		registerParameter("trialsName", "string");			//	only used for propn version of applet
	}
	
	private int getStatementOption(int statementIndex) {
																//	0 => ciLevel, 1 => sampleSize, 2 => spread
		String paramName = (statementIndex == 0) ? "ciLevel"
												: (statementIndex == 1) ? "sampleSize"
												: "spread";
		return getIntParam(paramName) / 2;
	}
	
	private String[] getMenuItems(int statementIndex) {
																//	0 => ciLevel, 1 => sampleSize, 2 => spread
		String paramName = (statementIndex == 0) ? "menuLevel"
												: (statementIndex == 1) ? "menuSampleSize"
												: "menuSpread";
		
		StringArray menuItems = getArrayParam(paramName);
		return menuItems.getStrings();
		
//		StringTokenizer st = new StringTokenizer(getStringParam(paramName), "*");
//		String[] menuItems = new String[st.countTokens()];
//		for (int i=0 ; i<menuItems.length ; i++)
//			menuItems[i] = st.nextToken();
//		return menuItems;
	}
	
	private int getCorrectChoice(int statementIndex) {
		return getStatementOption(statementIndex) + 1;	//	2nd or 3rd of the options
	}
	
	public String expandQuestion(String qnParamTemplate, String qnTemplate,
																															String qnExtraTemplate) {
		Object oldParam[] = cloneParameters();
		
		String questionText = super.expandQuestion(qnParamTemplate, qnTemplate, qnExtraTemplate);
		
		for (int i=0 ; i<noOfStatements ; i++) {
			String statementStart = getParameter(STATEMENT_START_PARAM + i);
			String statementEnd = getParameter(STATEMENT_END_PARAM + i);
			String message = getParameter(MESSAGES_PARAM + i);
			
			statementStartText[i] = expandOneQuestionString(statementStart, oldParam);
			statementEndText[i] = (statementEnd == null) ? null
																			: expandOneQuestionString(statementEnd, oldParam);
			answerMessage[i] = expandOneQuestionString(message, oldParam);
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
			statementPanel[i] = new StatementMenuPanel(this, i + 1);
			registerStatusItem("ciMessage" + i, statementPanel[i]);
			thePanel.add(statementPanel[i]);
		}
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		permute(statementPermutation, random01);
		
		for (int i=0 ; i<noOfStatements ; i++) {
			int sourceIndex = statementPermutation[i];
			statementPanel[i].setStatement(statementStartText[sourceIndex], statementEndText[sourceIndex],
																																				getMenuItems(sourceIndex));
			
			statementPanel[i].setCorrectChoice(getCorrectChoice(sourceIndex));
			statementPanel[i].setAnswerString(answerMessage[sourceIndex]);
			statementPanel[i].setTextBackground(Color.white);
			
			statementPanel[i].invalidate();
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
					messagePanel.insertText(statementPanel[i].getAnswerString());
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly completed all three statements.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedText("The wrong statements are highlighted in red.");
				for (int i=0 ; i<noOfStatements ; i++)
					if (!statementPanel[i].isCorrect()) {
						messagePanel.insertBoldRedText("\n" + (i+1) + ". ");
						messagePanel.insertText(statementPanel[i].getAnswerString());
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
			statementPanel[i].setTextBackground(Color.white);
	}
	
	private int countErrors() {
		int nErrors = 0;
		for (int i=0 ; i<noOfStatements ; i++)
			if (!statementPanel[i].isCorrect())
				nErrors ++;
		return nErrors;
	}
	
	protected int assessAnswer() {
		return (countErrors() == 0) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<noOfStatements ; i++)
			if (statementPanel[i].isCorrect())
				statementPanel[i].setTextBackground(kCorrectAnswerBackground);
			else
				statementPanel[i].setTextBackground(kWrongAnswerBackground);
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<statementPanel.length ; i++)
			statementPanel[i].showCorrectChoice();
			
		clearStatementBackground();
	}
	
	protected double getMark() {
		int nErrors = countErrors();
		return (nErrors == 0) ? 1 : (nErrors == 1) ? 0.3 : 0;
	}
	
}