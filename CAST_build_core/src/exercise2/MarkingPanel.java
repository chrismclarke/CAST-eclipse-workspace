package exercise2;

import java.awt.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import imageUtils.*;

public class MarkingPanel extends XPanel implements ExerciseConstants {
	
	private ExerciseApplet exerciseApplet;
	
	private MarkCanvas tickCross;
	private XButton checkButton, giveAnswerButton, anotherQnButton, showAttemptButton;
	private XChoice hintChoice;
	
	public MarkingPanel(ExerciseApplet exerciseApplet, int hints) {
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
//		setOpaque(false);
		this.exerciseApplet = exerciseApplet;

		if (exerciseApplet.isPracticeMode()) {		
			checkButton = new XButton(exerciseApplet.translate("Check"), exerciseApplet);
			add(checkButton);
			
			tickCross = new MarkCanvas(exerciseApplet);
			add(tickCross);
			
			if (hints == NO_HINTS) {
				giveAnswerButton = new XButton(exerciseApplet.translate("Tell me"), exerciseApplet);
				add(giveAnswerButton);
			}
			else {
				XPanel answerPanel = new XPanel();
				answerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 2));
				
					hintChoice = new XChoice("Help:", XChoice.HORIZONTAL, exerciseApplet);
					hintChoice.addItem("No hints");
					hintChoice.addItem("Show hints");
				answerPanel.add(hintChoice);
				
					giveAnswerButton = new XButton(exerciseApplet.translate("Tell me"), exerciseApplet);
				answerPanel.add(giveAnswerButton);
					
				add(answerPanel);
			}
			
				anotherQnButton = new XButton(exerciseApplet.translate("Another question"), exerciseApplet);
			add(anotherQnButton);
		}
		else if (exerciseApplet.isAnswerMode()) {	
			checkButton = new XButton(exerciseApplet.translate("Check"), exerciseApplet);
			add(checkButton);
			
			tickCross = new MarkCanvas(exerciseApplet);
			add(tickCross);
			
			giveAnswerButton = new XButton(exerciseApplet.translate("Tell me"), exerciseApplet);
			add(giveAnswerButton);
			
			showAttemptButton = new XButton(exerciseApplet.translate("Show attempt"), exerciseApplet);
			add(showAttemptButton);
			
			int mark = exerciseApplet.getRecordedMark();
			ImageCanvas markCanvas = new ImageCanvas("mark/mark" + mark + ".gif", exerciseApplet);
			add(markCanvas);
		}
	}
	
	public void clear() {
		tickCross.setIcon(ANS_UNCHECKED);
		checkButton.enable();
		giveAnswerButton.enable();
		if (hintChoice != null)
			hintChoice.enable();
		if (showAttemptButton != null)
			showAttemptButton.enable();
	}
	
	public void doCheck() {
		exerciseApplet.check();
		tickCross.setIcon(exerciseApplet.result);
		checkButton.disable();
		if (exerciseApplet.result == ANS_CORRECT) {
			giveAnswerButton.disable();
			if (hintChoice != null)
				hintChoice.disable();
		}
		else
			giveAnswerButton.enable();
	}
	
	public void doShowAttempt() {
		String startStatus = exerciseApplet.getStartStatus();
		if (startStatus !=  null) {
			exerciseApplet.setStatus(startStatus);
			if (exerciseApplet.isAnswerMode()) {
				doCheck();
				showAttemptButton.disable();
			}
		}
	}
	
	private boolean localAction(Object target, boolean shiftDown, boolean controlDown) {
		if (target == checkButton) {
			doCheck();
			return true;
		}
		else if (target == giveAnswerButton) {
			exerciseApplet.showAnswer();
			checkButton.disable();
			tickCross.setIcon(ANS_CORRECT);
			giveAnswerButton.disable();
			if (hintChoice != null)
				hintChoice.disable();
			if (showAttemptButton != null)
				showAttemptButton.enable();
			return true;
		}
		else if (target == hintChoice) {
			exerciseApplet.showHints(hintChoice.getSelectedIndex() > 0);
			return true;
		}
		else if (target == anotherQnButton) {
			if (shiftDown && controlDown) {
				String randomSeed = JOptionPane.showInputDialog(this,
                                     "Type random number seed",
                                     "Random number seed",
                                     JOptionPane.QUESTION_MESSAGE);
				if (randomSeed != null) {
					long seed = Long.parseLong(randomSeed);
					if (seed != 0)
						exerciseApplet.setFixedQuestionSeed(seed);
				}
			}
			exerciseApplet.anotherQuestion();
			clear();
			return true;
		}
		else if (target == showAttemptButton) {
			doShowAttempt();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target, evt.shiftDown(), evt.controlDown());
	}
}
