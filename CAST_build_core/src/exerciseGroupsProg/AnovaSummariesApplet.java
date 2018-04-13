package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;
import models.*;


public class AnovaSummariesApplet extends CoreAnovaApplet {
	static final private String RESID_SD_TEXT_PARAM = "residSdText";
	static final private String R_SQUARED_TEXT_PARAM = "rSquaredText";
	static final private String TOTAL_SD_TEXT_PARAM = "totalSdText";
	static final private String N_OBS_TEXT_PARAM = "individualsText";
	static final private String N_GROUPS_TEXT_PARAM = "factorLevelsText";
	
	static final private int RESID_SD = 0;
	static final private int R_SQUARED = 1;
	static final private int TOTAL_SD = 2;
	static final private int N_OBS = 3;
	static final private int N_GROUPS = 4;
	
	static final private int kNoOfQuestions = 5;
	
	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	static final private Color kCorrectColor = new Color(0x009900);
	static final private Color kWrongColor = new Color(0xFF0000);
	
	static final private NumValue kStartSd = new NumValue(1, 3);
	static final private NumValue kStartRSqr = new NumValue(50, 1);
	static final private NumValue kStartN = new NumValue(5, 0);
	
	static final private Font kIndexFont = new Font("serif", Font.BOLD, 28);
	
	static final private double kSdExactSlop = 0.002;
	static final private double kSdApproxSlop = 0.02;
		
	private StatementEditPanel[] subQuestionPanel;
	private int[] permutation;
	
	private int[] subMark = new int[kNoOfQuestions];
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("individuals", "string");
		registerParameter("sdDecimals", "int");
	}
	
	private String getIndividuals() {
		return getStringParam("individuals");
	}
	
	private int getSdDecimals() {
		return getIntParam("sdDecimals");
	}
	
	
//-----------------------------------------------------------
	
	
	protected Color getAnovaTableBackground() {
		return kTableBackgroundColor;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", getAnovaTable(data));
		thePanel.add("Center", getQuestionParts(data));
		
		return thePanel;
	}
	
	
	private XPanel getQuestionParts(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 5));
		
		permutation = new int[kNoOfQuestions];
		subQuestionPanel = new StatementEditPanel[kNoOfQuestions];
		for (int i=0 ; i<kNoOfQuestions ; i++) {
			subQuestionPanel[i] = new StatementEditPanel(this, i+1);
			subQuestionPanel[i].setInlineEdit(true);
			subQuestionPanel[i].setIndexColor(kIndexFont, Color.black);
			registerStatusItem("statement" + i, subQuestionPanel[i]);
			
			thePanel.add(subQuestionPanel[i]);
		}
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		permute(permutation);
		
		SsqVariable totalSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[0]);
		SsqVariable explainedSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[1]);
		SsqVariable residSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[2]);
		double totalSsq = totalSsqVar.doubleValueAt(0);
		int totalDf = totalSsqVar.getDF();
		double explainedSsq = explainedSsqVar.doubleValueAt(0);
		int explainedDf = explainedSsqVar.getDF();
		double residSsq = residSsqVar.doubleValueAt(0);
		int residDf = residSsqVar.getDF();
		
		setItemText(subQuestionPanel[permutation[RESID_SD]], getParameter(RESID_SD_TEXT_PARAM),
																										kStartSd, 5, StatementEditPanel.POSITIVE);
		NumValue correctResidSd = new NumValue(Math.sqrt(residSsq / residDf), getSdDecimals());
		subQuestionPanel[permutation[RESID_SD]].setCorrectValue(correctResidSd,
						correctResidSd.toDouble() * kSdExactSlop, correctResidSd.toDouble() * kSdApproxSlop);
		
		NumValue correctRSquared = new NumValue(explainedSsq / totalSsq * 100.0, 1);
		setItemText(subQuestionPanel[permutation[R_SQUARED]], getParameter(R_SQUARED_TEXT_PARAM),
																										kStartRSqr, 4, StatementEditPanel.POSITIVE);
		subQuestionPanel[permutation[R_SQUARED]].setCorrectValue(correctRSquared, 0.1, 1.0);
		
		NumValue correctTotalSd = new NumValue(Math.sqrt(totalSsq / totalDf), getSdDecimals());
		setItemText(subQuestionPanel[permutation[TOTAL_SD]], getParameter(TOTAL_SD_TEXT_PARAM),
																										kStartSd, 5, StatementEditPanel.POSITIVE);
		subQuestionPanel[permutation[TOTAL_SD]].setCorrectValue(correctTotalSd,
						correctTotalSd.toDouble() * kSdExactSlop, correctTotalSd.toDouble() * kSdApproxSlop);
		
		setItemText(subQuestionPanel[permutation[N_OBS]], getParameter(N_OBS_TEXT_PARAM), kStartN, 3,
																									StatementEditPanel.INTEGER);
		subQuestionPanel[permutation[N_OBS]].setCorrectValue(new NumValue(totalDf + 1, 0), 0.0, 0.0);
		
		setItemText(subQuestionPanel[permutation[N_GROUPS]], getParameter(N_GROUPS_TEXT_PARAM), kStartN, 3,
																									StatementEditPanel.INTEGER);
		subQuestionPanel[permutation[N_GROUPS]].setCorrectValue(new NumValue(explainedDf + 1, 0), 0.0, 0.0);
		
		showBlackIndices();
	}
	
	private void setItemText(StatementEditPanel editPanel, String itemText, NumValue startValue,
																										int nEditChars, int editType) {
		itemText = itemText.replaceAll("#yVarName#", getYVarName());
		itemText = itemText.replaceAll("#xVarName#", getXVarName());
		itemText = itemText.replaceAll("#individuals#", getIndividuals());
		
			CatVariable xVar = (CatVariable)data.getVariable("x");
			int nGroups = xVar.noOfCategories();
			RandomInteger r = new RandomInteger(0, nGroups - 1, 1, nextSeed());
			String randomGroup = xVar.getLabel(r.generateOne()).toString();
		itemText = itemText.replaceAll("#groupName#", randomGroup);
		
		int editStartIndex = itemText.indexOf("#edit#");
		String startText = itemText.substring(0, editStartIndex);
		String endText = itemText.substring(editStartIndex + 6);
		editPanel.setStatement(startText, endText, startValue, nEditChars, editType);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type values into the edit boxes above to complete the sentences.\n(You many need to use a calculator to find some values.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				for (int i=0 ; i<kNoOfQuestions ; i++) {
					messagePanel.insertBoldText((i+1) + ". ");
					int actualIndex = 0;
					for (int j=0 ; j<kNoOfQuestions ; j++)
						if (permutation[j] == i)
							actualIndex = j;
					insertSubAnswer(actualIndex, messagePanel);
				}
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly completed all of the statements.");
				break;
			case ANS_CLOSE:
			case ANS_WRONG:
				if (result == ANS_CLOSE)
					messagePanel.insertRedHeading("Mostly correct!\n");
				else
					messagePanel.insertRedHeading("Wrong!\n");	
				for (int i=0 ; i<kNoOfQuestions ; i++)
					if (subMark[i] != ANS_CORRECT) {
						messagePanel.insertBoldRedText((i+1) + ". ");
						if (subMark[i] == ANS_CLOSE)
							messagePanel.insertRedText("This answer is not close enough. ");
						else
							messagePanel.insertRedText("This answer is not correct. ");
						int actualIndex = 0;
						for (int j=0 ; j<kNoOfQuestions ; j++)
							if (permutation[j] == i)
								actualIndex = j;
						insertSubAnswer(actualIndex, messagePanel);
					}
				break;
		}
	}
	
	private void insertSubAnswer(int itemIndex, MessagePanel messagePanel) {
		switch (itemIndex) {
			case RESID_SD:
				messagePanel.insertText("The standard deviation within each factor level is the square root of the mean residual sum of squares.\n");
				break;
			case R_SQUARED:
				messagePanel.insertText("The explained sum of squares divided by the total sum of squares gives the proportion of variation explained by the factor. Multiply by 100 to express as a percentage.\n");
				break;
			case TOTAL_SD:
				messagePanel.insertText("The overall standard deviation of the response is the square root of the mean total sum of squares.\n");
				break;
			case N_OBS:
				messagePanel.insertText("The total degrees of freedom are (n - 1) so the number of values is the total degrees of freedom plus one.\n");
				break;
			case N_GROUPS:
				messagePanel.insertText("The explained degrees of freedom are the number of factor levels minus one, so add one to this to get the number of groups.\n");
				break;
			default:
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 170;
	}
	
//-----------------------------------------------------------
	
	
	protected int assessAnswer() {
		int nClose = 0;
		int nWrong = 0;
		for (int i=0 ; i<kNoOfQuestions ; i++) {
			subMark[i] = subQuestionPanel[i].assessAttempt();
			if (subMark[i] == ANS_CLOSE)
				nClose ++;
			else if (subMark[i] == ANS_WRONG)
				nWrong ++;
		}
		if (nClose == 0 && nWrong == 0)
			return ANS_CORRECT;
		else if (nWrong <= 1 && (nClose + nWrong) <= 2)
			return ANS_CLOSE;
		else
			return ANS_WRONG;
	}
	
	protected void giveFeedback() {
		for (int i=0 ; i<kNoOfQuestions ; i++) {
			subQuestionPanel[i].setIndexColor(kIndexFont, subMark[i] == ANS_CORRECT ? kCorrectColor : kWrongColor);
			subQuestionPanel[i].repaint();
		}
	}
	
	protected void showCorrectWorking() {
		for (int i=0 ; i<kNoOfQuestions ; i++)
			subQuestionPanel[i].showCorrectValue();
		showBlackIndices();
	}
	
	protected double getMark() {
		if (assessAnswer() == ANS_CLOSE) {
			double mark = 1.0;
			for (int i=0 ; i<kNoOfQuestions ; i++)
				if (subMark[i] == ANS_CLOSE)
					mark -= 0.2;
				else if (subMark[i] == ANS_WRONG)
					mark -= 0.4;
			return mark;
		}
		else
			return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
	private void showBlackIndices() {
		for (int i=0 ; i<kNoOfQuestions ; i++) {
			subQuestionPanel[i].setIndexColor(kIndexFont, Color.black);
			subQuestionPanel[i].repaint();
		}
	}
	
	
//-----------------------------------------------------------

	
	public boolean noteChangedWorking() {
		showBlackIndices();
		return super.noteChangedWorking();
	}
}