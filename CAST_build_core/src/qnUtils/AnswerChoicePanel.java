package qnUtils;

import java.awt.*;

import dataView.*;
import utils.*;

import imageGroups.*;


public class AnswerChoicePanel extends XPanel {
	static public final int NONE = 0;
	static public final int CORRECT = 1;
	static public final int WRONG = 2;
	
	private XChoice theChoice;
	TickCrossIcon tickCross;
	private XLabel theLabel;
	
	private int correctOption, currentOption;
	private int mark = NONE;
	
	public AnswerChoicePanel(String label, String[] options, int correctOption,
																							XApplet applet) {
		this.correctOption = correctOption;
		
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
		
		theLabel = new XLabel(label, XLabel.RIGHT, applet);
		leftPanel.add(theLabel);
		theChoice = new XChoice(applet);
		for (int i=0 ; i<options.length ; i++)
			theChoice.addItem(options[i]);
		leftPanel.add(theChoice);
		currentOption = 0;
		
		theLabel.setFont(applet.getStandardFont());
		theChoice.setFont(applet.getStandardFont());
		
		add(leftPanel);
		
		tickCross = new TickCrossIcon();
		add(tickCross);
	}
	
	public Insets insets() {
		return new Insets(2, 2, 2, 2);
	}
	
	public void changeCorrectOption(int correctOption) {
		this.correctOption = correctOption;
	}
	
	public void setLabelText(String newText) {
		theLabel.setText(newText);
	}
	
	private void setMark(int newMark) {
		if (mark != newMark) {
			int tickType = (newMark == NONE) ? TickCrossIcon.NO_ANSWER
								: (newMark == CORRECT) ? TickCrossIcon.CORRECT_ANSWER
								: TickCrossIcon.WRONG_ANSWER;
			tickCross.setAnswerMark(tickType);
			mark = newMark;
		}
	}
	
	public void checkAnswer() {
		if (currentOption == correctOption)
			setMark(CORRECT);
		else
			setMark(WRONG);
	}
	
	public void setCorrectAnswer() {
		theChoice.select(correctOption);
		currentOption = correctOption;
		setMark(CORRECT);
		repaint();
	}
	
	public void reset() {
		theChoice.select(0);
		currentOption = 0;
		setMark(NONE);
		repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == theChoice) {
			int newOption = theChoice.getSelectedIndex();
			if (newOption != currentOption) {
				currentOption = newOption;
				setMark(NONE);
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
