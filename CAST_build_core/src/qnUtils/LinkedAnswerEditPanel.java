package qnUtils;

import dataView.*;
import utils.*;
import imageGroups.*;

public class LinkedAnswerEditPanel extends AnswerEditPanel {
	static public final int CLOSE = FIRST_UNUSED;
	
	private XTextArea linkedText;
	double approxSlop;
	
	public LinkedAnswerEditPanel(String label, NumValue correctValue, double exactSlop,
												double approxSlop, NumValue initialValue, int maxChars,
												XTextArea linkedText, XApplet applet) {
		super(label, correctValue, exactSlop, initialValue, maxChars, applet);
		this.linkedText = linkedText;
		this.approxSlop = approxSlop;
	}
	
	public LinkedAnswerEditPanel(String paramString, String exactSlopString,
																										XTextArea linkedText, XApplet applet) {
		super(paramString, applet);
		
		approxSlop = Double.parseDouble(exactSlopString);
		this.linkedText = linkedText;
	}
	
	public void setLinkedText(XTextArea linkedText) {
		this.linkedText = linkedText;
	}
	
	public void reset(NumValue correctValue, double exactSlop, double approxSlop) {
		this.approxSlop = approxSlop;
		reset(correctValue, exactSlop);
	}
	
	protected int getTickType(int newAnswer) {
		if (newAnswer == CLOSE)
			return TickCrossIcon.CORRECT_ANSWER;
		else
			return super.getTickType(newAnswer);
	}
	
	protected void changeAnswerTo(int newAnswer) {
		linkedText.setText(newAnswer);
	}
	
	protected int wrongAnswerType(double attempt, double correctValue) {
			if (attempt >= correctValue - approxSlop && attempt <= correctValue + approxSlop)
				return CLOSE;
			else
				return WRONG;
	}
}
