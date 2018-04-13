package exerciseEstimProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;
import random.*;



public class PopnSampleApplet extends ExerciseApplet {
	static final private String POP_SAMP_TEXT_PARAM = "popSampText";
	static final private String N_DESCRIPTIONS_PARAM = "nDescriptions";
	static final private String SUMMARY_PARAM = "summaryMessage";
	static final private String TITLE_PARAM = "title";
	static final private String SCENARIO_PARAM = "scenario";
	static final private String POPN_DESCRIPTION_PARAM = "popnDescription";
	static final private String SAMPLE_DESCRIPTION_PARAM = "sampleDescription";
	static final private String POPN_MESSAGES_PARAM = "popnMessages";
	static final private String SAMPLE_MESSAGES_PARAM = "sampleMessages";
	
	static final private int POPN = 0;
	static final private int SAMPLE = 1;
	
	static final private int kDisplayDescriptions = 3;
	
	private String menuItems[];
	
	private String title[], scenario[];
	private String statement[][], answerMessage[][], errorMessage[][];
	
	private int[] popnSample = new int[kDisplayDescriptions];
	
	private StatementMenuPanel[] popSampMessage;
	
	private int checkPermutation[];
	
	private Random random01;
	private AlmostRandomInteger randomResult;
	
//================================================
	
	protected void registerParameterTypes() {
	}
	
	protected void createDisplay() {
		setupStatements();
		
		setLayout(new BorderLayout(0, 8));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 8));
			
			mainPanel.add("North", getWorkingPanels(data));
				
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new BorderLayout(0, 4));
				
				bottomPanel.add("North", createMarkingPanel(NO_HINTS));
				
					XPanel messagePanel = new XPanel();
					messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
						
						message = new ExerciseMessagePanel(this);
					messagePanel.add(message);
				bottomPanel.add("Center", messagePanel);
			
			mainPanel.add("Center", bottomPanel);
		
		add("Center", mainPanel);
	}
	
	private void setupStatements() {
		int nStatements = Integer.parseInt(getParameter(N_DESCRIPTIONS_PARAM));
		
		random01 = new Random(nextSeed());
		randomResult = new AlmostRandomInteger(0, 1, nextSeed());
		checkPermutation = new int[nStatements];
		permute(checkPermutation, random01);
		
		popnSample = new int[nStatements];
		
		title = new String[nStatements];
		scenario = new String[nStatements];
		statement = new String[nStatements][2];
		answerMessage = new String[nStatements][2];
		errorMessage = new String[nStatements][2];
		
		for (int i=0 ; i<nStatements ; i++) {
			title[i] = getParameter(TITLE_PARAM + i);
			scenario[i] = MText.expandText(getParameter(SCENARIO_PARAM + i));
			statement[i][POPN] = MText.expandText(getParameter(POPN_DESCRIPTION_PARAM + i));
			statement[i][SAMPLE] = MText.expandText(getParameter(SAMPLE_DESCRIPTION_PARAM + i));
			
			StringTokenizer st = new StringTokenizer(getParameter(POPN_MESSAGES_PARAM + i), "#");
			answerMessage[i][POPN] = MText.expandText(st.nextToken());
			if (st.hasMoreTokens())
				errorMessage[i][POPN] = MText.expandText(st.nextToken());
			
			st = new StringTokenizer(getParameter(SAMPLE_MESSAGES_PARAM + i), "#");
			answerMessage[i][SAMPLE] = st.nextToken();
			if (st.hasMoreTokens())
				errorMessage[i][SAMPLE] = st.nextToken();
		}
		
		StringTokenizer st2 = new StringTokenizer(getParameter(POP_SAMP_TEXT_PARAM), "#");
		menuItems = new String[st2.countTokens()];
		for (int i=0 ; i<2 ; i++)
			menuItems[i] = st2.nextToken();
	}
		
//===============================================================
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
		
		popSampMessage = new StatementMenuPanel[kDisplayDescriptions];
		for (int i=0 ; i<kDisplayDescriptions ; i++) {
			popSampMessage[i] = new StatementMenuPanel(this, i + 1);
			registerStatusItem("popnSample" + i, popSampMessage[i]);
			thePanel.add(popSampMessage[i]);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		repermute(checkPermutation, kDisplayDescriptions, random01);
		for (int i=0 ; i<kDisplayDescriptions ; i++)
			popnSample[i] = randomResult.generateOne();
		
		for (int i=0 ; i<kDisplayDescriptions ; i++) {
			int index = checkPermutation[i];
			popSampMessage[i].setStatement(scenario[index] + "\n" + statement[index][popnSample[i]],
																															null, menuItems);
			
			popSampMessage[i].setCorrectChoice(popnSample[i] == POPN ? 0 : 1);
			popSampMessage[i].setTextBackground(Color.white);
			popSampMessage[i].invalidate();
		}
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		return null;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
		randomResult.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		boolean firstStatement = true;
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Identify which of the statements above describe population parameters and which describe sample statistics\n(Use the pop-up menus to correctly complete the three statements.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertRedText(getParameter(SUMMARY_PARAM) + "\n");
				for (int i=0 ; i<kDisplayDescriptions ; i++) {
					int index = checkPermutation[i];
					if (!firstStatement)
						messagePanel.insertText("\n");
					messagePanel.insertBoldBlueText(statementTitle(i, index));
					messagePanel.insertText(answerMessage[index][popnSample[i]]);
					firstStatement = false;
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly identified the statements referring to populations and samples.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText(getParameter(SUMMARY_PARAM) + "\n");
				for (int i=0 ; i<kDisplayDescriptions ; i++)
					if (!popSampMessage[i].isCorrect()) {
						int index = checkPermutation[i];
						if (!firstStatement)
							messagePanel.insertText("\n");
						messagePanel.insertBoldBlueText(statementTitle(i, index));
						messagePanel.insertText(answerMessage[index][popnSample[i]]);
						if (errorMessage[index][popnSample[i]] != null)
							messagePanel.insertText(errorMessage[index][popnSample[i]]);
						firstStatement = false;
					}
				break;
		}
	}
	
	private String statementTitle(int i, int index) {
		return (i + 1) + ". (" + title[index] + " " + MText.translateUnicode("en") + " " + menuItems[popnSample[i]] + ") ";
	}
	
	protected int getMessageHeight() {
		return 180;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed)
			clearStatementBackground();
		return changed;
	}
	
	private void clearStatementBackground() {
		for (int i=0 ; i<kDisplayDescriptions ; i++)
			popSampMessage[i].setTextBackground(Color.white);
	}
	
	private int countWrongAnswers() {
		int nWrong = 0;
		for (int i=0 ; i<kDisplayDescriptions ; i++)
			if (!popSampMessage[i].isCorrect())
				nWrong ++;
		return nWrong;
	}
	
	protected int assessAnswer() {
		return (countWrongAnswers() == 0) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<kDisplayDescriptions ; i++)
			popSampMessage[i].setTextBackground(popSampMessage[i].isCorrect() ? kCorrectAnswerBackground
																																		: kWrongAnswerBackground);
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<kDisplayDescriptions ; i++)
			popSampMessage[i].showCorrectChoice();
			
		clearStatementBackground();
	}
	
	protected double getMark() {
		int nWrong = countWrongAnswers();
		return (nWrong == 0) ? 1 : (nWrong == 1) ? 0.5 : 0;
	}
	
}