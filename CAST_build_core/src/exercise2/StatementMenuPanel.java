package exercise2;

import java.awt.*;

import utils.*;


public class StatementMenuPanel extends MessagePanel implements ExerciseConstants, StatusInterface {
	private String statementStartText = null;
	private String statementEndText = null;
	private String menuUnits = null;
	private String[] menuItems = null;
	private String answerString = null;
	
	private XChoice optionChoice;
	private int correctChoice = -1;
	
	private boolean inlineMenu = false;
	
	public StatementMenuPanel(ExerciseApplet exerciseApplet, int index) {
		super(null, exerciseApplet, NO_SCROLL);
		if (index > 0)
			setBorder(new StatementBorder(0, 50, 0, 0, index));
		changeContent();
	}
	
	public void setInlineMenu(boolean inlineMenu) {
		this.inlineMenu = inlineMenu;
	}
	
	public void setStatement(String statementStartText, String statementEndText,
																																String[] menuItems) {
		setStatement(statementStartText, statementEndText, menuItems, null);
	}
	
	public void setStatement(String statementStartText, String statementEndText,
																								String[] menuItems, String menuUnits) {
		this.statementStartText = statementStartText;
		this.statementEndText = statementEndText;
		this.menuItems = menuItems;
		this.menuUnits = menuUnits;
		changeContent();
	}
	
	public void setAnswerString(String answerString) {
		this.answerString = answerString;
	}
	
	public String getAnswerString() {
		return answerString;
	}
	
	protected void fillContent() {
		if (statementStartText != null && menuItems != null) {
			insertText(statementStartText);
			if (!inlineMenu) {
				insertText("\n");
				setAlignment(MessagePanel.CENTER_ALIGN);
			}
			XChoice optionChoice = (menuUnits == null) ? new XChoice(applet)
																: new XChoice(null, menuUnits, XChoice.HORIZONTAL, applet);
			optionChoice.addItem("Select an option...");
			for (int i=0 ; i<menuItems.length ; i++)
				optionChoice.addItem(menuItems[i]);
			addOptionMenu(optionChoice);
			if (statementEndText != null) {
				if (!inlineMenu) {
					insertText("\n");
					setAlignment(MessagePanel.LEFT_ALIGN);
				}
				insertText(statementEndText);
			}
		}
	}
	
	protected void addOptionMenu(XChoice optionChoice) {
		this.optionChoice = optionChoice;
		if (menuUnits != null)
			optionChoice.lockBackground(Color.white);
		insertMenu(optionChoice);
	}
	
	public void setTextBackground(Color c) {
		super.setTextBackground(c);
		if (menuUnits != null)
			optionChoice.lockBackground(c);
	}
	
	public void setCorrectChoice(int correctChoice) {
		this.correctChoice = correctChoice;
	}
	
	public void showCorrectChoice() {
		optionChoice.select(correctChoice + 1);
	}
	
	public boolean isCorrect() {
		return optionChoice.getSelectedIndex() == correctChoice + 1;
	}
	
	public String getStatus() {
		return optionChoice.getStatus();
	}
	
	public void setStatus(String status) {
		optionChoice.setStatus(status);
	}
	
	private boolean localAction(Object target) {
		if (target == optionChoice) {
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