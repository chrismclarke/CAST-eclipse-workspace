package exercise2;

import java.awt.*;
import javax.swing.border.*;

import utils.*;
import dataView.*;


public class StatementEditPanel extends MessagePanel implements ExerciseConstants, StatusInterface {
	static final public int INTEGER = 0;
	static final public int POSITIVE = 1;
	static final public int REAL = 2;
	
	private String statementStartText;
	private String statementEndText;
	private NumValue startValue;
	private int nEditChars;
	private int editType;
	
	private NumValue correctValue = null;
	private double correctSlop, approxSlop;
	
	private XNumberEditPanel edit;
	
	private boolean inlineEdit = false;
	
	public StatementEditPanel(ExerciseApplet exerciseApplet, int index) {
		super(null, exerciseApplet, NO_SCROLL);
		if (index > 0)
			setBorder(new StatementBorder(0, 50, 0, 0, index));
		changeContent();
	}
	
	public void setInlineEdit(boolean inlineEdit) {
		this.inlineEdit = inlineEdit;
	}
	
	public void setStatement(String statementStartText, String statementEndText,
																				NumValue startValue, int nEditChars, int editType) {
		this.statementStartText = statementStartText;
		this.statementEndText = statementEndText;
		this.startValue = startValue;
		this.nEditChars = nEditChars;
		this.editType = editType;
		changeContent();
	}
	
	public void setIndexColor(Font numberFont, Color numberColor) {
		Border b = getBorder();
		if (b != null && b instanceof StatementBorder)
			((StatementBorder)b).setNumberFont(numberFont, numberColor);
	}
	
	protected void fillContent() {
		if (statementStartText != null) {
			insertText(statementStartText);
			if (!inlineEdit) {
				insertText("\n");
				setAlignment(MessagePanel.CENTER_ALIGN);
			}
			edit = new XNumberEditPanel(null, startValue.toString(), applet);
			edit.setColumns(nEditChars);
			switch (editType) {
				case INTEGER:
					edit.setIntegerType(0, 99999);
					break;
				case POSITIVE:
					edit.setDoubleType(0, Double.POSITIVE_INFINITY);
					break;
				default:
					break;
			}
			insertEdit(edit);
			if (statementEndText != null) {
				if (!inlineEdit) {
					insertText("\n");
					setAlignment(MessagePanel.LEFT_ALIGN);
				}
				insertText(statementEndText);
			}
		}
	}
	
	public void setCorrectValue(NumValue correctValue, double correctSlop, double approxSlop) {
		this.correctValue = correctValue;
		this.correctSlop = correctSlop;
		this.approxSlop = approxSlop;
	}
	
	public void showCorrectValue() {
		edit.setDoubleValue(correctValue);
	}
	
	public int assessAttempt() {
		double attempt = edit.getDoubleValue();
		double correct = correctValue.toDouble();
		if (Math.abs(attempt - correct) <= correctSlop)
			return ANS_CORRECT;
		else if (Math.abs(attempt - correct) <= approxSlop)
			return ANS_CLOSE;
		else
			return ANS_WRONG;
	}
	
	public String getStatus() {
		return edit.getStatus();
	}
	
	public void setStatus(String status) {
		edit.setStatus(status);
	}
	
	private boolean localAction(Object target) {
		if (target instanceof XNumberEditPanel) {
			((ExerciseApplet)applet).noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}