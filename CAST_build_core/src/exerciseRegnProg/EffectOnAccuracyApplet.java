package exerciseRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;



public class EffectOnAccuracyApplet extends ExerciseApplet {
	static final private int kNStatements = 3;
	
	private TrueFalseTextPanel statementCheck[] = new TrueFalseTextPanel[kNStatements];
	private int checkPermutation[] = {0, 1, 2};
	
	private Random random01;		//	for permuatation
	private RandomInteger randomCorrect;		// for correct/false options
	
//================================================
	
	protected void createDisplay() {
		random01 = new Random(nextSeed());
		
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
		registerParameter("nDirection", "int");
		registerParameter("nText", "string");		//	indexed by nDirection in param
		registerParameter("sigmaDirection", "int");
		registerParameter("sigmaText", "string");		//	indexed by sigmaDirection in param
		registerParameter("xSpreadDirection", "int");
		registerParameter("xSpreadText", "string");		//	indexed by xSpreadDirection in param
	}
	
	private boolean statementIsCorrect(int i) {		//	not permuted
		int direction = getIntParam((i == 0) ? "nDirection" : (i == 1) ? "sigmaDirection" : "xSpreadDirection");
		return direction == 0;		//	first direction must be correct one
	}
	
	private String getStatementText(int i) {		//	not permuted
		return getStringParam((i == 0) ? "nText" : (i == 1) ? "sigmaText" : "xSpreadText");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 30);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
		
		for (int i=0 ; i<kNStatements ; i++) {
			statementCheck[i] = new TrueFalseTextPanel(this, true);
			registerStatusItem("statementCheck" + i, statementCheck[i]);
			thePanel.add(statementCheck[i]);
		}
			
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		repermute(checkPermutation, kNStatements, random01);		//	tries to avoid previous selection
		
		for (int i=0 ; i<kNStatements ; i++) {
			int perm = checkPermutation[i];
			statementCheck[i].changeStatement(getStatementText(perm));
			statementCheck[i].setCorrect(statementIsCorrect(perm));
			statementCheck[i].setTextBackground(Color.white);
		}
		
		for (int i=0 ; i<kNStatements ; i++)
			statementCheck[i].setState(false);
	}
	
	protected DataSet getData() {
		return null;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
		randomCorrect.setSeed(nextSeed());
	}
	
	protected void setDataForQuestion() {
	}
	
//-----------------------------------------------------------

	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Which, if any, of the above statements are correct?\n(Click the checkboxes to select the correct statements.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				for (int i=0 ; i<kNStatements ; i++) {
					if (i > 0)
						messagePanel.insertText("\n");
					int iRaw = getRawIndex(i);
					messagePanel.insertBoldBlueText(getStatementName(iRaw) + ": ");
					messagePanel.insertText(getFeedback(iRaw, statementIsCorrect(iRaw)));
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have identified the correct statements.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				for (int i=0 ; i<kNStatements ; i++) {
					if (statementCheck[i].checkCorrect() == ANS_WRONG) {
						int iRaw = getRawIndex(i);
						messagePanel.insertBoldText(getStatementName(iRaw) + ": ");
						messagePanel.insertBoldRedText(getFeedback(iRaw, statementIsCorrect(iRaw)) + "\n");
					}
				}
				break;
		}
	}
	
	private int getRawIndex(int permIndex) {
		for (int i=0 ; i<kNStatements ; i++)
			if (checkPermutation[i] == permIndex)
				return i;
		return 0;
	}
	
	private String getStatementName(int i) {		//	not permuted
		return (i == 0) ? "Sample size" : (i == 1) ? "Spread around LS line" : "Spread of x-values";
	}
	
	private String getFeedback (int i, boolean isCorrect) {		//	not permuted
		if (isCorrect)
			return (i == 0) ? "With more data, estimates become less variable (and hence more accurate)."
											: (i == 1) ? "If the response is less variable (and hence the crosses are closer to the LS line), estimates will become more accurate."
											: "When the x-values are more spread out, the slope of the least squares line can be more precisely estimated.";
		else
			return (i == 0) ? "With less data, estimates become more variable (and hence less accurate)."
											: (i == 1) ? "If the response is more variable (and hence the crosses are further above and below the LS line), estimates will become less accurate."
											: "When the x-values do not vary much, there is little information about how changes to x will affect the response.";
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed)
			clearCheckBackground();
		return changed;
	}
	
	private void clearCheckBackground() {
		for (int i=0 ; i<kNStatements ; i++)
			statementCheck[i].setTextBackground(Color.white);
	}
	
	private int numberWrong() {
		int nWrong = 0;
		for (int i=0 ; i<kNStatements ; i++)
			if (statementCheck[i].checkCorrect() == ANS_WRONG)
				nWrong ++;
		return nWrong;
	}
	
	protected int assessAnswer() {
		return (numberWrong() == 0) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<kNStatements ; i++)
			if (statementCheck[i].checkCorrect() == ANS_CORRECT)
				statementCheck[i].setTextBackground(kCorrectAnswerBackground);
			else
				statementCheck[i].setTextBackground(kWrongAnswerBackground);
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<kNStatements ; i++)
			statementCheck[i].showAnswer();
			
		clearCheckBackground();
	}
	
	protected double getMark() {
		int nWrong = numberWrong();
		return (nWrong == 0) ? 1 : (nWrong == 1) ? 0.7 : 0;
	}
	
}