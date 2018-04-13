package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;



public class BinomialCheckApplet extends ExerciseApplet {
	static final private String N_VARS_PARAM = "nVars";
	static final private String VARIABLE_TEXT_PARAM = "variable";
	static final private String N_VARS_DISPLAYED_PARAM = "nVarsDisplayed";
	
	private boolean isBinomial[];
	private String variableName[];
	private String variableText[];
	private String variableFeedback[];
	
	private TrueFalseTextPanel binomialCheck[] = null;
	private int checkPermutation[];
	
	
	private Random random01;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 8));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 10));
		
			mainPanel.add("North", getWorkingPanels(null));
				
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
	
	protected void registerParameterTypes() {
	}
	
	protected void readQuestions() {
		random01 = new Random(nextSeed());
		
		int nVars = Integer.parseInt(getParameter(N_VARS_PARAM));
		variableText = new String[nVars];
		variableName = new String[nVars];
		variableFeedback = new String[nVars];
		isBinomial = new boolean[nVars];
		for (int i=0 ; i<nVars ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(VARIABLE_TEXT_PARAM + i), "#");
			isBinomial[i] = Boolean.parseBoolean(st.nextToken());
			variableName[i] = st.nextToken();
			variableText[i] = st.nextToken();
			variableFeedback[i] = st.nextToken();
		}
			
		checkPermutation = new int[nVars];
		for (int i=0 ; i<nVars ; i++)
			checkPermutation[i] = i;
		
		super.readQuestions();
	}
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 3));
		
		int nVarsDisplayed = Integer.parseInt(getParameter(N_VARS_DISPLAYED_PARAM));
		binomialCheck = new TrueFalseTextPanel[nVarsDisplayed];
		for (int i=0 ; i<nVarsDisplayed ; i++) {
			binomialCheck[i] = new TrueFalseTextPanel(this, true);
			registerStatusItem("binomialCheck" + i, binomialCheck[i]);
			thePanel.add(binomialCheck[i]);
		}
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		int nChecks = binomialCheck.length;
		repermute(checkPermutation, nChecks, random01);		//	tries to avoid previous selection
		
		for (int i=0 ; i<nChecks ; i++) {
			binomialCheck[i].changeStatement(variableText[checkPermutation[i]]);
			binomialCheck[i].setCorrect(isBinomial[checkPermutation[i]]);
			binomialCheck[i].setTextBackground(Color.white);
		}
		
		for (int i=0 ; i<binomialCheck.length ; i++)
			binomialCheck[i].setState(false);
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
				messagePanel.insertText("Which of the above variables have binomial distributions?\n(Click the checkboxes to select the binomial variables.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				for (int i=0 ; i<binomialCheck.length ; i++) {
					int varIndex = checkPermutation[i];
					messagePanel.insertBoldBlueText(variableName[varIndex] + ": ");
					messagePanel.insertText(variableFeedback[varIndex]);
					if (i < 3)
						messagePanel.insertText("\n");
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have identified the correct binomial variables.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("No!");
				for (int i=0 ; i<binomialCheck.length ; i++) {
					int varIndex = checkPermutation[i];
					boolean attempt = binomialCheck[i].isSelected();
					boolean correct = isBinomial[varIndex];
					
					if (attempt != correct) {
						messagePanel.insertBoldText("\n" + variableName[varIndex]);
						messagePanel.insertBoldRedText(correct ? " is " : " is not ");
						messagePanel.insertBoldText("binomial: ");
						messagePanel.insertText(variableFeedback[varIndex]);
					}
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 180;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed)
			clearCheckBackground();
		return changed;
	}
	
	private void clearCheckBackground() {
		int nChecks = binomialCheck.length;
		for (int i=0 ; i<nChecks ; i++)
			binomialCheck[i].setTextBackground(Color.white);
	}
	
	private int numberWrong() {
		int nWrong = 0;
		for (int i=0 ; i<binomialCheck.length ; i++)
			if (binomialCheck[i].checkCorrect() == ANS_WRONG)
				nWrong ++;
		return nWrong;
	}
	
	protected int assessAnswer() {
		return (numberWrong() == 0) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<binomialCheck.length ; i++)
			if (binomialCheck[i].checkCorrect() == ANS_CORRECT)
				binomialCheck[i].setTextBackground(kCorrectAnswerBackground);
			else
				binomialCheck[i].setTextBackground(kWrongAnswerBackground);
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<binomialCheck.length ; i++)
			binomialCheck[i].showAnswer();
			
		clearCheckBackground();
	}
	
	protected double getMark() {
		int nWrong = numberWrong();
		return (nWrong == 0) ? 1 : (nWrong == 1) ? 0.7 : 0;
	}
	
}