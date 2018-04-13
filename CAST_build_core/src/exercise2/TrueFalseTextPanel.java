package exercise2;

import java.awt.*;


public class TrueFalseTextPanel extends TrueFalsePanel {
	private StatementTextPanel statementPanel;
	
	private String statement = "";
	
	public TrueFalseTextPanel(ExerciseApplet exerciseApplet, boolean isCorrect, String falseIcon,
					String falseRolloverIcon, String trueIcon, String trueRolloverIcon, String trueFalseIcon) {
		super(exerciseApplet, isCorrect, false);
		setIcons(falseIcon, falseRolloverIcon, trueIcon, trueRolloverIcon, trueFalseIcon);
		setupPanel();
	}
	
	public TrueFalseTextPanel(ExerciseApplet exerciseApplet, boolean isCorrect, boolean isBasicBox) {
		super(exerciseApplet, isCorrect, isBasicBox);
		
		setupPanel();
	}
	
	public TrueFalseTextPanel(ExerciseApplet exerciseApplet, boolean isCorrect) {
		this(exerciseApplet, isCorrect, true);
	}
	
	public void setTextBackground(Color c) {
		statementPanel.setTextBackground(c);
		statementPanel.repaint();
	}
	
//=================================================
	
public class StatementTextPanel extends MessagePanel {
	public StatementTextPanel(ExerciseApplet exerciseApplet) {
		super(null, exerciseApplet, NO_SCROLL);
		changeContent();
	}
	
	protected void fillContent() {
		insertText(statement);
	}
}
	
//=================================================
	
	public void changeStatement(String statement) {
		this.statement = statement;
		statementPanel.changeContent();
	}
	
	protected Component createStatement(ExerciseApplet exerciseApplet) {
		statementPanel = new StatementTextPanel(exerciseApplet);
		return statementPanel;
	}
	
}