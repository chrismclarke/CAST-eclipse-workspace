package exercise;

import java.awt.*;

import dataView.*;
import utils.*;
import imageGroups.*;

public class AnswerPanel extends XPanel {
	static public final int UNCHECKED = 0;
	static public final int CORRECT = 1;
	static public final int WRONG = 2;
	static public final int UNKNOWN = 3;
	
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL = 1;
	
	private XNumberEditPanel textEdit;
	TickCrossIcon tickCross;
	@SuppressWarnings("unused")
	private NumValue maxValue;
	
	private XPanel valuePanel;
	private XLabel label;
	
	private int currentAnswer = UNCHECKED;
	
	private XTextArea linkedMessage;
	
	public AnswerPanel(String labelString, NumValue maxValue, XApplet applet, int orientation,
																																				String unitsString) {
		TickCrossImages.loadCrossAndTick(applet);
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		if (maxValue != null) {
			this.maxValue = maxValue;
			int maxChars = maxValue.toString().length();
			
			Font displayFont = applet.getBigFont();
			setFont(displayFont);							//		this should not be needed
			
			valuePanel = new XPanel();
			label = new XLabel(labelString, XLabel.RIGHT, applet);
			label.setFont(displayFont);
			
			textEdit = new XNumberEditPanel("", maxChars, applet);
			textEdit.setFont(displayFont);
//			textEdit.lockBackground(Color.white);
//			textEdit.setTextListener(this);
			
			XLabel unitsLabel = null;
			if (unitsString != null) {
				unitsLabel = new XLabel(unitsString, XLabel.LEFT, applet);
				unitsLabel.setFont(displayFont);
			}
			
			if (orientation == HORIZONTAL)
				valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
			else
				valuePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
				valuePanel.add(label);
				if (unitsLabel == null)
					valuePanel.add(textEdit);
				else {
					XPanel ansPanel = new XPanel();
					ansPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
					ansPanel.add(textEdit);
					ansPanel.add(unitsLabel);
					valuePanel.add(ansPanel);
				}
			
			add(valuePanel);
			
		}
		
		XPanel tickPanel = new XPanel();
			tickPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			tickCross = new TickCrossIcon();
			tickPanel.add(tickCross);
		add(tickPanel);
	}
	
	public AnswerPanel(String labelString, NumValue maxValue, XApplet applet, int orientation) {
		this(labelString, maxValue, applet, orientation, null);
	}
	
	public AnswerPanel(String labelString, NumValue maxValue, XApplet applet) {
		this(labelString, maxValue, applet, HORIZONTAL);
	}
	
	public AnswerPanel(XApplet applet) {
		this(null, null, applet, HORIZONTAL);			//	 for applets with no numerical answer
	}
	
	public Insets insets() {
		return new Insets(2, 2, 2, 2);
	}
	
	public void setLinkedMessage(XTextArea linkedMessage) {
		this.linkedMessage = linkedMessage;
	}
	
	public NumValue getAnswer() {
		if (textEdit == null)
			return null;
		return textEdit.getNumValue();
	}
	
	public void markAnswer(int correctOrWrong) {
		if (currentAnswer != correctOrWrong) {
			tickCross.setAnswerMark((correctOrWrong == CORRECT) ? TickCrossIcon.CORRECT_ANSWER
																												: TickCrossIcon.WRONG_ANSWER);
			currentAnswer = correctOrWrong;
			
			if (correctOrWrong == WRONG && textEdit != null) {
				textEdit.requestFocus();
				textEdit.selectAll();
			}
		}
		repaint();
	}
	
	public void setToCorrectAnswer(NumValue correctValue) {
		if (textEdit != null)
			textEdit.setDoubleValue(correctValue);
		
		if (currentAnswer != CORRECT) {
			tickCross.setAnswerMark(TickCrossIcon.CORRECT_ANSWER);
			currentAnswer = CORRECT;
		}
		repaint();
	}
	
	public void reset() {
		tickCross.setAnswerMark(TickCrossIcon.NO_ANSWER);
		currentAnswer = UNCHECKED;
		if (textEdit != null) {
			textEdit.clearValue();
			textEdit.requestFocus();
		}
		repaint();
	}
	
	private boolean localAction(Object target) {
		if (target == textEdit) {
			if (currentAnswer != UNCHECKED) {
				tickCross.setAnswerMark(TickCrossIcon.NO_ANSWER);
				currentAnswer = UNCHECKED;
				if (linkedMessage != null)
					linkedMessage.setText(0);
			}
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
