package exerciseCategProg;

import java.awt.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;
import expression.*;


import exerciseCateg.*;


public class ContinExpectedApplet extends CoreContinApplet {
	static final private String TEMPLATE_OPTION = "useTemplate";
	
	static final private NumValue kOneValue = new NumValue(1, 0);
	
	static final protected double kEps = 0.0005;
	static final protected double kRoughEps = 0.005;
//	static final private int kPropnDecimals = 3;
	
	private ExpectedTemplatePanel expectedTemplate;
	private ExpressionResultPanel expectedExpression = null;
	
	private boolean showingCorrectAnswer = false;
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("maxExpected", "const");
	}
	
	private NumValue getMaxExpected() {
		return getNumValueParam("maxExpected");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getTemplatePanel() {
		XPanel thePanel = new InsetPanel(0, 20, 0, 10);
		if (hasOption(TEMPLATE_OPTION)) {
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				XPanel templatePanel = new InsetPanel(10, 5);
				templatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					
					FormulaContext stdContext = new FormulaContext(kTemplateColor, getBigFont(), this);
					expectedTemplate = new ExpectedTemplatePanel("E(x,y) =", 4, stdContext, kOneValue);
					registerStatusItem("expectedTemplate", expectedTemplate);
				
				templatePanel.add(expectedTemplate);
				
				templatePanel.lockBackground(kTemplateBackground);
			thePanel.add(templatePanel);
		}
		else {
			thePanel.setLayout(new BorderLayout(0, 0));
				XPanel expressionPanel = new InsetPanel(10, 10);
				expressionPanel.setLayout(new BorderLayout(0, 0));
					expectedExpression = new ExpressionResultPanel(null, 2, 15, "E(x,y) =", 6,
																										ExpressionResultPanel.HORIZONTAL, this);
					registerStatusItem("expectedExpression", expectedExpression);
				expressionPanel.add("Center", expectedExpression);
				expressionPanel.lockBackground(kTemplateBackground);
			thePanel.add("Center", expressionPanel);
		}
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		theTable.setSelectedRowCol(getCriticalX(), getCriticalY());
		if (expectedExpression == null) {
			expectedTemplate.setValues(1, 1, 1);
			expectedTemplate.changeMaxValue(getMaxExpected());
		}
		else {
			expectedExpression.showAnswer(kOneValue, null);
			expectedExpression.setResultDecimals(getMaxExpected().decimals);
		}
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				CatVariable yVar = (CatVariable)data.getVariable("y");
				CatVariable xVar = (CatVariable)data.getVariable("x");
				messagePanel.insertText("Type the expected cell count for this combination of "
																	+ xVar.name + " and " + yVar.name + " in the above text box.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a value into the text box above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Expected counts cannot be negative.");
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
				messagePanel.insertText("Your answer is close, but try to specify the expected count correct to "
																																		+ getMaxExpected().decimals + " decimal digits.\n");
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
		messagePanel.insertText("The expected count for " + xVar.name + " = '" + xVar.getLabel(x) + "' and "
					+ yVar.name + " = '" + yVar.getLabel(y) + "' is (the row total) times (the column total) divided by (the overall total for the table), "
					+ xVar.getCounts()[x] + " #times# " + yVar.getCounts()[y] + " / " + yVar.noOfValues() + ".");
	}
	
	private void insertHelp(MessagePanel messagePanel) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
//		int x = getCriticalX();
//		int y = getCriticalY();
		messagePanel.insertText("The expected count for any value of " + xVar.name + " and "
					+ yVar.name + " is the row total times the column total divided by the overall total for the table.");
	}
	
//-----------------------------------------------------------
	
	protected double getCorrect() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		
		double total = yVar.noOfValues();
		int[] yCounts = yVar.getCounts();
		double rowTotal = yCounts[y];
		int[] xCounts = xVar.getCounts();
		double colTotal = xCounts[x];
		
		return rowTotal * colTotal / total;
	}
	
//-----------------------------------------------------------
	
	protected void highlightTableRowCol() {
		theTable.showExpectedRowCol();
		theTable.repaint();
	}
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if (attempt <= 0.0)
			return ANS_INVALID;
		else {
			double correct = getCorrect();
			if (Math.abs(correct - attempt) <= kEps)
				return ANS_CORRECT;
			else
				return (Math.abs(correct - attempt) <= kRoughEps) ? ANS_CLOSE : ANS_WRONG;
		}
	}
	
	protected void showCorrectWorking() {
		highlightTableRowCol();
		
		resultPanel.showAnswer(new NumValue(getCorrect(), getMaxExpected().decimals));
		
		CatVariable yVar = (CatVariable)data.getVariable("y");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int x = getCriticalX();
		int y = getCriticalY();
		
		int total = yVar.noOfValues();
		int[] yCounts = yVar.getCounts();
		int rowTotal = yCounts[y];
		int[] xCounts = xVar.getCounts();
		int colTotal = xCounts[x];
		
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		if (expectedExpression == null)
			expectedTemplate.setValues(rowTotal, colTotal, total);
		else {
			String expressionString = rowTotal + " * " + colTotal + " / " + total;
			expectedExpression.showAnswer(null, expressionString);
		}
		showingCorrectAnswer = false;
	}
	
//-----------------------------------------------------------
	
	
	public boolean noteChangedWorking() {
		boolean changed = (showingCorrectAnswer) ? false : super.noteChangedWorking();
		if (changed) {
			theTable.setSelectedRowCol(getCriticalX(), getCriticalY());
			theTable.repaint();
		}
		return changed;
	}
	
}