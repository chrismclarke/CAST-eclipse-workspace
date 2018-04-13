package exercise;

import dataView.*;
import utils.*;


abstract public class Problem {
	static final protected SymbolCoding kNoSymbols[] = new SymbolCoding[0];
	
	protected DataSet data;
	
	protected AnswerPanel answer;
	protected XPanel working;
	protected XTextArea message;
	protected TextCanvas dataDescription, questionDescription;
	
	public Problem(DataSet data) {
		this.data = data;
	}
	
	public void setLinkedComponents(AnswerPanel answer, XPanel working) {
		this.answer = answer;
		this.working = working;
		if (answer != null && message != null)
			answer.setLinkedMessage(message);
	}
	
	abstract public void checkAnswer();
	abstract public void solveExercise();
	abstract public void changeData();
	abstract public void changeQuestion();
	abstract public TextCanvas createDataCanvas(int pixWidth, XApplet applet);
	abstract public TextCanvas createQuestionCanvas(int pixWidth, XApplet applet);
	abstract public XTextArea createMessageArea(int pixWidth, String longestMessage, XApplet applet);
	
}