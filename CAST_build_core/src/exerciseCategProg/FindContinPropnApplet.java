package exerciseCategProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


import exerciseNumGraph.*;


public class FindContinPropnApplet extends CoreContinApplet {
	
	static final private int JOINT_PROB = 0;
	static final private int X_MARGINAL_PROB = 1;
	static final private int X_CONDIT_PROB = 2;
	static final private int Y_MARGINAL_PROB = 3;
	static final private int Y_CONDIT_PROB = 4;
	
	static final protected double kEps = 0.0005;
	static final protected double kRoughEps = 0.005;
	static final private int kPropnDecimals = 3;
	
	private PropnTemplatePanel propnTemplate;
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("probType", "choice");
	}
	
	private int getProbType() {
		return getIntParam("probType");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getTemplatePanel() {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
			XPanel templatePanel = new InsetPanel(10, 5);
			templatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				FormulaContext stdContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
				propnTemplate = new PropnTemplatePanel(null, 5, stdContext);
				registerStatusItem("propnTemplate", propnTemplate);
			
			templatePanel.add(propnTemplate);
			
			templatePanel.lockBackground(kTemplateBackground);
		thePanel.add(templatePanel);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		theTable.clearPropnIndices();
		
		propnTemplate.setValues(new NumValue(1, 0), new NumValue(1, 0));
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Type the probability above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a proportion into the text box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Proportions must be between 0 and 1.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				insertSolution(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("Your answer is correct. ");
				insertSolution(messagePanel);
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Not close enough!\n");
				messagePanel.insertText("Your answer is close, but try to specify the proportion correct to "
																																		+ kPropnDecimals + " decimal digits.\n");
				insertHelp(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				insertHelp(messagePanel);
				break;
		}
	}
	
	private void insertSolution(MessagePanel messagePanel) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		String jointString, xValueString, yValueString;
		switch (getProbType()) {
			case JOINT_PROB:
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the joint proportion for " + jointString + ".\n");
				messagePanel.insertText("The proportion is therefore the count for the cell " + jointString
																		+ " divided by the total count for the table, " + yVar.noOfValues() + ".");
				break;
			case X_CONDIT_PROB:
				yValueString = yVar.name + " is " + yVar.getLabel(y);
				xValueString = "(" + xVar.name + " = " + xVar.getLabel(x) + ")";
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the conditional proportion for " + xValueString
																								+ ", given that " + yValueString + ".\n");
				messagePanel.insertText("The proportion is therefore the count for the cell " + jointString
										+ " divided by the number of times that " + yValueString + ", " + yVar.getCounts()[y] + ".");
				break;
			case Y_CONDIT_PROB:
				xValueString = xVar.name + " is " + xVar.getLabel(x);
				yValueString = "(" + yVar.name + " = " + yVar.getLabel(y) + ")";
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the conditional proportion for " + yValueString
																								+ ", given that " + xValueString + ".\n");
				messagePanel.insertText("The proportion is therefore the count for the cell " + jointString
										+ " divided by the number of times that " + xValueString + ", " + xVar.getCounts()[x] + ".");
				break;
			case X_MARGINAL_PROB:
				xValueString = "(" + xVar.name + " = " + xVar.getLabel(x) + ")";
				messagePanel.insertText("The question asks for the marginal proportion for " + xValueString
																			+ ", not distinguishing between different values of " + yVar.name + ".\n");
				messagePanel.insertText("The proportion is therefore the marginal count for " + xValueString
																		+ " divided by the total count for the table, " + yVar.noOfValues() + ".");
				break;
			case Y_MARGINAL_PROB:
				yValueString = "(" + yVar.name + " = " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the marginal proportion for " + yValueString
																		+ ", not distinguishing between different values of " + xVar.name + ".\n");
				messagePanel.insertText("The proportion is therefore the marginal count for " + yValueString
																		+ " divided by the total count for the table, " + yVar.noOfValues() + ".");
				break;
		}
	}
	
	private void insertHelp(MessagePanel messagePanel) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		String jointString, xValueString, yValueString;
		switch (getProbType()) {
			case JOINT_PROB:
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the joint probability of " + jointString
																											+ ", so divide its cell count by the table total.");
				break;
			case X_CONDIT_PROB:
				yValueString = "(" + yVar.name + " = " + yVar.getLabel(y) + ")";
				xValueString = "(" + xVar.name + " = " + xVar.getLabel(x) + ")";
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the conditional probability that " + xValueString
																			+ ", given that " + yValueString + ".\nDivide the " + jointString
																			+ " by the marginal total for " + yValueString + ".");
				break;
			case Y_CONDIT_PROB:
				yValueString = "(" + yVar.name + " = " + yVar.getLabel(y) + ")";
				xValueString = "(" + xVar.name + " = " + xVar.getLabel(x) + ")";
				jointString = "(" + xVar.getLabel(x) + ", " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the conditional probability that " + yValueString
																			+ ", given that " + xValueString + ".\nDivide the " + jointString
																			+ " by the marginal total for " + xValueString + ".");
				break;
			case X_MARGINAL_PROB:
				xValueString = "(" + xVar.name + " = " + xVar.getLabel(x) + ")";
				messagePanel.insertText("The question asks for the marginal probability for " + xValueString
																			+ ", not distinguishing between different values of " + yVar.name + ".\n");
				messagePanel.insertText(".\nDivide the marginal count for " + xValueString + " by the table total.");
				break;
			case Y_MARGINAL_PROB:
				yValueString = "(" + yVar.name + " = " + yVar.getLabel(y) + ")";
				messagePanel.insertText("The question asks for the marginal probability for " + yValueString
																			+ ", not distinguishing between different values of " + xVar.name + ".\n");
				messagePanel.insertText(".\nDivide the marginal count for " + yValueString + " by the table total.");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	private int getCorrectNumerator() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		switch (getProbType()) {
			case JOINT_PROB:
			case X_CONDIT_PROB:
			case Y_CONDIT_PROB:
				int[][] jointCounts = theTable.getCounts();
				return jointCounts[x][y];
			case X_MARGINAL_PROB:
				int[] xCounts = xVar.getCounts();
				return xCounts[x];
			case Y_MARGINAL_PROB:
				int[] yCounts = yVar.getCounts();
				return yCounts[y];
		}
		return 0;
	}
	
	private int getCorrectDenominator() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		switch (getProbType()) {
			case JOINT_PROB:
			case X_MARGINAL_PROB:
			case Y_MARGINAL_PROB:
				int totalCount = yVar.noOfValues();
				return totalCount;
			case X_CONDIT_PROB:
				int[] yCounts = yVar.getCounts();
				return yCounts[y];
			case Y_CONDIT_PROB:
				int[] xCounts = xVar.getCounts();
				return xCounts[x];
		}
		return 0;
	}
	
	protected double getCorrect() {
		return getCorrectNumerator() / (double)getCorrectDenominator();
	}
	
//-----------------------------------------------------------
	
	protected void highlightTableRowCol() {
		int probType = getProbType();
		int numerX = (probType == Y_MARGINAL_PROB) ? -1 : getCriticalX();
		int numerY = (probType == X_MARGINAL_PROB) ? -1 : getCriticalY();
		int denomX = (probType == Y_CONDIT_PROB) ? getCriticalX() : -1;
		int denomY = (probType == X_CONDIT_PROB) ? getCriticalY() : -1;
		
		theTable.setPropnIndices(numerX, numerY, denomX, denomY);
		theTable.repaint();
	}
	
	protected int assessAnswer() {
		double attemptPropn = getAttempt();
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attemptPropn < 0.0 || attemptPropn > 1)
			return ANS_INVALID;
		else {
			double correctPropn = getCorrect();
			if (Math.abs(correctPropn - attemptPropn) <= kEps)
				return ANS_CORRECT;
			else
				return (Math.abs(correctPropn - attemptPropn) <= kRoughEps) ? ANS_CLOSE : ANS_WRONG;
		}
	}
	
	protected void showCorrectWorking() {
		highlightTableRowCol();
		
		int numer = getCorrectNumerator();
		int denom = getCorrectDenominator();
		resultPanel.showAnswer(new NumValue(numer / (double)denom, kPropnDecimals));
		
		if (propnTemplate != null)
			propnTemplate.setValues(new NumValue(numer, 0), new NumValue(denom, 0));
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			theTable.clearPropnIndices();
			theTable.repaint();
		}
		return changed;
	}
	
}