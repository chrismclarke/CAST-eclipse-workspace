package exerciseProbProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;


import exerciseNumGraph.*;
import exerciseProb.*;


public class SumDiffTableApplet extends ExerciseApplet {
	static final private NumValue kZero = new NumValue(0, 0);
	static final private NumValue kOne = new NumValue(1, 0);
	
	private SumDiffTableView tableView;
	private PropnTemplatePanel propnTemplate;
	private XLabel columnLabel;
	private XVertLabel rowLabel;
	
	private ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 10));
			
				questionPanel = new QuestionPanel(this);
			topPanel.add(questionPanel);
			
			topPanel.add(getWorkingPanels(null));
			
				FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
				propnTemplate = new PropnTemplatePanel(translate("Proportion") + " =", stdContext);
				registerStatusItem("propnTemplate", propnTemplate);
			topPanel.add(propnTemplate);
			
				resultPanel = new ResultValuePanel(this, translate("Probability") + " =", 6);
				registerStatusItem("prob", resultPanel);
			topPanel.add(resultPanel);
			
		add("North", topPanel);
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 4));
			
			bottomPanel.add("North", createMarkingPanel(NO_HINTS));
			
				message = new ExerciseMessagePanel(this);
			bottomPanel.add("Center", message);
		
		add("Center", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("directionIndex", "int");
		registerParameter("directionNames", "array");
		registerParameter("replacement", "string");
		registerParameter("cardNames", "array");
		registerParameter("cardValues", "string");
		registerParameter("cutoff", "const");
		registerParameter("sumDiffType", "int");
	}
	
	private boolean isWithReplacement() {
		return "with".equals(getStringParam("replacement"));
	}
	
	private String[] getCardNames() {
		return getArrayParam("cardNames").getStrings();
		
//		String names[] = new String[2];
//		StringTokenizer st = new StringTokenizer(getStringParam("cardNames"), "*");
//		names[0] = st.nextToken();
//		names[1] = st.nextToken();
//		return names;
	}
	
	private NumValue[] getCardValues() {
		StringTokenizer st = new StringTokenizer(getStringParam("cardValues"));
		NumValue cardValues[] = new NumValue[st.countTokens()];
		for (int i=0 ; i<cardValues.length ; i++)
			cardValues[i] = new NumValue(st.nextToken());
		return cardValues;
	}
	
	private int getSumDiffType() {
		return getIntParam("sumDiffType");
//		return questionExtraVersion;			//	0=SUM, 1=DIFF, 2=ABS_DIFF
	}
	
	private int getDirection() {
		return getIntParam("directionIndex");
															//	0=GREATER, 1=GREATER_EQUAL, 2=LESS, 3=LESS_EQUAL, 4=EQUAL
	}
	
	private NumValue getCutoff() {
		return getNumValueParam("cutoff");
	}
	
	
//-----------------------------------------------------------

	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new XPanel();
			GridBagLayout gbl = new GridBagLayout();
			innerPanel.setLayout(gbl);
			
			columnLabel = new XLabel("", XLabel.CENTER, this);
			columnLabel.setFont(getBigBoldFont());
			rowLabel = new XVertLabel("", XLabel.CENTER, this);
			rowLabel.setFont(getBigBoldFont());
			tableView = new SumDiffTableView(new DataSet(), this);
			tableView.setFont(getBigBoldFont());
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridheight = gbc.gridwidth = 1;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.insets = new Insets(0,30,0,0);
			gbc.ipadx = gbc.ipady = 0;
			gbc.weightx = gbc.weighty = 0.0;
			innerPanel.add(columnLabel);
			gbl.setConstraints(columnLabel, gbc);
			
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(30,0,0,0);
			innerPanel.add(rowLabel);
			gbl.setConstraints(rowLabel, gbc);
			
			gbc.fill=GridBagConstraints.BOTH;
			gbc.gridx = 1;
			gbc.insets = new Insets(0,0,0,0);
			gbc.weightx = gbc.weighty = 1.0;
			innerPanel.add(tableView);
			gbl.setConstraints(tableView, gbc);
		
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		tableView.setRowColValues(getCardValues());
		tableView.setWithReplacement(isWithReplacement());
		tableView.setSumDiffType(getSumDiffType());
		tableView.setSelection(getDirection(), getCutoff().toDouble());
		tableView.setShowPopSamp(false);
		tableView.invalidate();
		tableView.repaint();
		
		columnLabel.setText(getCardNames()[0]);
		
		rowLabel.setText(getCardNames()[1]);
		
		propnTemplate.setValues(kZero, kOne);
		
		resultPanel.clear();
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
				messagePanel.insertText("Find the probability and type it in the answer box.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a probability in the answer box.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("Probabilities must be between zero and one.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				if (!isWithReplacement())
					messagePanel.insertText("The grey cells are impossible for sampling without replacement.\n");
				messagePanel.insertText("The yellow cells are combinations for which " + getSuccessString() + ".\n");
				
				messagePanel.insertText("The probability is the proportion of " + getPopnString() + " that are yellow.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have given the correct probability -- the proportion of " + getPopnString());
				messagePanel.insertText(" for which " + getSuccessString() + ".");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("You have given the wrong probability.");
				
				if (!isWithReplacement())
					messagePanel.insertText("\n#bullet#  Since this is sampling without replacement, the diagonal cells are impossible.");
				
				messagePanel.insertText("\n#bullet#  Count the number of cells for which " + getSuccessString() + " and divide by the total number of 'legal' cells.");
				break;
		}
	}
	
	private String getSuccessString() {
		int sumDiff = getSumDiffType();
		int direction = getDirection();
		String s = (sumDiff == 0) ? "the sum" : (sumDiff == 1) ? "the difference" : "the absolute difference";
		s += (direction == 0) ? " > " : (direction == 1) ? " >= " : (direction == 2) ? " < " : (direction == 3) ? " <= " : " = ";
		s += getCutoff();
		return s;
	}
	
	private String getPopnString() {
		if (!isWithReplacement())
			return "non-grey cells";
		else
			return "cells";
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
//-----------------------------------------------------------
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected double getCorrectPropn() {
		return tableView.countSelected() / (double)tableView.countPopn();
	}
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			tableView.setShowPopSamp(false);
			tableView.repaint();
		}
		return changed;
	}
	
	protected int assessAnswer() {
		double correct = getCorrectPropn();
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		else
			return (Math.abs(correct - attempt) < 0.001) ? ANS_CORRECT : ANS_WRONG;
	}
	
	protected void giveFeedback() {
		if (result == ANS_CORRECT)
			tableView.setShowPopSamp(true);
	}
	
	protected void showCorrectWorking() {
		tableView.setShowPopSamp(true);
		tableView.repaint();
		
		NumValue numer = new NumValue(tableView.countSelected(), 0);
		NumValue denom = new NumValue(tableView.countPopn(), 0);
		propnTemplate.setValues(numer, denom);
		
		resultPanel.showAnswer(propnTemplate.getResult());
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
}