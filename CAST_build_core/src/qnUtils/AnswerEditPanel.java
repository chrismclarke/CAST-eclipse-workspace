package qnUtils;

import java.awt.*;

import dataView.*;
import utils.*;
import imageGroups.*;

public class AnswerEditPanel extends XPanel {
	TickCrossIcon tickCross;
	private NumValue correctValue;
	private NumValue initialValue;
	private double slop;
	
	private XNumberEditPanel editPanel;
	
	static public final int NONE = 0;
	static public final int EXACT = 1;
	static public final int WRONG = 2;
	static public final int UNKNOWN = 3;
	static public final int FIRST_UNUSED = 4;
	
	private int currentAnswer = NONE;
	
	public AnswerEditPanel(String label, NumValue correctValue, double slop,
												NumValue initialValue, int maxChars, XApplet applet) {
		initialise(label, correctValue, slop, initialValue, maxChars, applet);
	}
	
	public AnswerEditPanel(String paramString, XApplet applet) {
		LabelEnumeration tokens = new LabelEnumeration(paramString);
		String label = (String)tokens.nextElement();
		NumValue correctValue = new NumValue((String)tokens.nextElement());
		double slop = Double.parseDouble((String)(tokens.nextElement()));
		NumValue initialValue = new NumValue((String)tokens.nextElement());
		int maxChars = (tokens.hasMoreElements())
							? Integer.parseInt((String)(tokens.nextElement())) : 8;
		initialise(label, correctValue, slop, initialValue, maxChars, applet);
	}
	
	protected void initialise(String label, NumValue correctValue, double slop,
												NumValue initialValue, int maxChars, XApplet applet) {
		this.correctValue = correctValue;
		this.slop = slop;
		this.initialValue = initialValue;
		
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		Font displayFont = applet.getStandardFont();
		setFont(displayFont);							//		this should not be needed
		
			editPanel = new XNumberEditPanel(label, initialValue.toString(), maxChars, applet);
			editPanel.setFont(displayFont);
		add(editPanel);
		
		XPanel tickPanel = new XPanel();
			tickPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			tickCross = new TickCrossIcon();
			tickPanel.add(tickCross);
		add(tickPanel);
	}
	
	public void setVerticalValue() {
		editPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
	}
	
	public Insets insets() {
		return new Insets(2, 2, 2, 2);
	}
	
	public void setLabelText(String newText) {
		editPanel.setLabelText(newText);
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (editPanel != null)
			editPanel.setFont(f);
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		if (editPanel != null)
			editPanel.setForeground(c);
	}
	
/*
	public void lockBackground(Color c) {
		super.lockBackground(c);
		if (editPanel != null)
			editPanel.lockBackground(c);
	}
*/
	
	private void setAnswer(int newAnswer) {
		if (currentAnswer != newAnswer) {
			changeAnswerTo(newAnswer);
			tickCross.setAnswerMark(getTickType(newAnswer));
			currentAnswer = newAnswer;
		}
	}
	
	public int getCurrentAnswer() {
		return currentAnswer;
	}
	
	protected int getTickType(int newAnswer) {
		switch (newAnswer) {
			case NONE:
				return TickCrossIcon.NO_ANSWER;
			case EXACT:
				return TickCrossIcon.CORRECT_ANSWER;
			case WRONG:
				return TickCrossIcon.WRONG_ANSWER;
			default:
			case UNKNOWN:
				return TickCrossIcon.UNKNOWN_ANSWER;
		}
	}
	
	protected void changeAnswerTo(int newAnswer) {		//		e.g. this may change a text message
	}
	
	protected int wrongAnswerType(double attempt, double correctValue) {		//		e.g. this may subdivide class of inexact answers
		return WRONG;
	}
	
	public void checkAnswer() {
		try {
			double answer = editPanel.getDoubleValue();
			if (answer >= correctValue.toDouble() - slop && answer <= correctValue.toDouble() + slop)
				setAnswer(EXACT);
			else
				setAnswer(wrongAnswerType(answer, correctValue.toDouble()));
		} catch (NumberFormatException e) {
			setAnswer(UNKNOWN);
		}
	}
	
	public void setCorrectAnswer() {
		editPanel.setDoubleValue(correctValue);
		setAnswer(EXACT);
		repaint();
	}
	
	public void reset(NumValue correctValue, double slop) {
		this.correctValue = correctValue;
		this.slop = slop;
		reset();
	}
	
	public void reset() {
		editPanel.setDoubleValue(initialValue);
		setAnswer(NONE);
		repaint();
	}

	
	private boolean localAction(Object target) {
		if (target == editPanel) {
			setAnswer(TickCrossIcon.NO_ANSWER);
			repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
