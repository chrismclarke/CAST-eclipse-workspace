package exerciseBivarProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;



public class ExplanResponseApplet extends ExerciseApplet {
private XChoice explanChoice, respChoice, obsExpChoice;
	private int currentExplanIndex, currentRespIndex, currentObsExpIndex;
	
	private TrueFalseTextPanel trueFalseText;
	
	private boolean swapVariables;
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 10));
			
			questionPanel = new QuestionPanel(this);
			mainPanel.add("North", questionPanel);
			
			mainPanel.add("Center", getWorkingPanels(null));
			
			mainPanel.add("South", createMarkingPanel(NO_HINTS));
		
		add("North", mainPanel);
				
			message = new ExerciseMessagePanel(this);
		
		add("Center", message);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("explanatory", "string");
		registerParameter("response", "string");
		registerParameter("experimental", "boolean");
		registerParameter("causal", "boolean");
		registerParameter("message", "string");
		registerParameter("errorCausal", "string");
		registerParameter("answer", "string");
	}
	
	private String getExplanatory() {
		return getStringParam("explanatory");
	}
	
	private String getResponse() {
		return getStringParam("response");
	}
	
	private boolean isExperimental() {
		return getBooleanParam("experimental");
	}
	
	private boolean isCausal() {
		return getBooleanParam("causal");
	}
	
	private String getCausalText() {
		return getStringParam("message");
	}
	
	private String getAnswer() {
		return getStringParam("answer");
	}
	
	private String getCausalErrorText() {
		return getStringParam("errorCausal");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
			
			XPanel explanPanel = new XPanel();
			explanPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				explanChoice = new XChoice("Explanatory:", XChoice.HORIZONTAL, this);
			registerStatusItem("explanChoice", explanChoice);
			explanPanel.add(explanChoice);
			
		thePanel.add(explanPanel);
			
			XPanel respPanel = new XPanel();
			respPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				respChoice = new XChoice("Response:", XChoice.HORIZONTAL, this);
			registerStatusItem("respChoice", respChoice);
			respPanel.add(respChoice);
			
		thePanel.add(respPanel);
			
			XPanel obsExpPanel = new XPanel();
			obsExpPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				obsExpChoice = new XChoice("Data type:", XChoice.HORIZONTAL, this);
				obsExpChoice.addItem("Observational");
				obsExpChoice.addItem("Experimental");
			registerStatusItem("obsExpChoice", obsExpChoice);
			obsExpPanel.add(obsExpChoice);
			
		thePanel.add(obsExpPanel);
		
			trueFalseText = new TrueFalseTextPanel(this, false, false);
			registerStatusItem("statementTruth", trueFalseText);
		thePanel.add(trueFalseText);
		
		return thePanel;
	}
	
	private void setupVariableMenu(XChoice choice) {
		choice.clearItems();
		if (swapVariables) {
			choice.addItem(getResponse());
			choice.addItem(getExplanatory());
		}
		else {
			choice.addItem(getExplanatory());
			choice.addItem(getResponse());
		}
		choice.select(0);
		choice.invalidate();
	}
	
	protected void setDisplayForQuestion() {
		swapVariables = new Random(nextSeed()).nextDouble() > 0.5;
		
		setupVariableMenu(explanChoice);
		currentExplanIndex = 0;
		
		setupVariableMenu(respChoice);
		currentRespIndex = 0;
		
		obsExpChoice.select(0);
		currentObsExpIndex = 0;
		
		trueFalseText.changeStatement(getCausalText());
		trueFalseText.setState(false);
		trueFalseText.setCorrect(isCausal());
		trueFalseText.invalidate();
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the pop-up menus to specify the explanatory and response variables and to specify whether the data are observational or experimental.\n");
				messagePanel.insertText("Finally decide whether the boxed statement is true. (Click to toggle the setting.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText(getAnswer());
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText(getAnswer());
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!");
				int selectedExplan = explanChoice.getSelectedIndex();
				if (swapVariables)
					selectedExplan = 1 - selectedExplan;
				int selectedResponse = respChoice.getSelectedIndex();
				if (swapVariables)
					selectedResponse = 1 - selectedResponse;
					
				if (selectedExplan == selectedResponse)
					messagePanel.insertText("\nThe explanatory variable cannot be the same as the response.");
				else if (selectedExplan != 0 || selectedResponse != 1)
					messagePanel.insertText("\nYou have specified the explanatory variable and response the wrong way round.");
				if ((obsExpChoice.getSelectedIndex() == 1) && !isExperimental())
					messagePanel.insertText("\nThe explanatory variable is not controlled, so the data are not experimental.");
				else if ((obsExpChoice.getSelectedIndex() != 1) && isExperimental())
					messagePanel.insertText("\nThe explanatory variable is controlled by the experimenter, so the data are not observational.");
				
				if (((obsExpChoice.getSelectedIndex() == 1) == isExperimental()) && (trueFalseText.checkCorrect() != ANS_CORRECT)) {
					messagePanel.insertText("\nYour conclusion about whether the relationship is causal is wrong. ");
					messagePanel.insertText(getCausalErrorText());
					messagePanel.insertText(" (Click to toggle the setting.)");
				}
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 100;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		int selectedExplan = explanChoice.getSelectedIndex();
		if (swapVariables)
			selectedExplan = 1 - selectedExplan;
		int selectedResponse = respChoice.getSelectedIndex();
		if (swapVariables)
			selectedResponse = 1 - selectedResponse;
		
		boolean error = false;
		if (selectedExplan != 0 || selectedResponse != 1)
			error = true;
		if ((obsExpChoice.getSelectedIndex() == 1) != isExperimental())
			error = true;
		if (trueFalseText.checkCorrect() != ANS_CORRECT)
			error = true;
		
		return error ? ANS_WRONG : ANS_CORRECT;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		int correctExplan = swapVariables ? 1 : 0;
		int correctResponse = swapVariables ? 0 : 1;
		
		explanChoice.select(correctExplan);
		respChoice.select(correctResponse);
		obsExpChoice.select(isExperimental() ? 1 : 0);
		trueFalseText.showAnswer();
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
	
	
//-----------------------------------------------------------
	
	private boolean localAction(Object target) {
		if (target == explanChoice) {
			int newChoice = explanChoice.getSelectedIndex();
			if (newChoice != currentExplanIndex) {
				currentExplanIndex = newChoice;
				noteChangedWorking();
			}
			return true;
		}
		else if (target == respChoice) {
			int newChoice = respChoice.getSelectedIndex();
			if (newChoice != currentRespIndex) {
				currentRespIndex = newChoice;
				noteChangedWorking();
			}
			return true;
		}
		else if (target == obsExpChoice) {
			int newChoice = obsExpChoice.getSelectedIndex();
			if (newChoice != currentObsExpIndex) {
				currentObsExpIndex = newChoice;
				noteChangedWorking();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}