package exercise;

import java.awt.*;

import dataView.*;
import utils.*;

public class ProblemButton extends XPanel {
	
	static final public int CHECK = 0;
	static final public int TELL_ME = 1;
	static final public int ANOTHER = 2;
	
	private Problem theProblem;
	
	private XButton theButton;
	private int buttonType;
	
	public ProblemButton (Problem theProblem, XApplet applet, int buttonType, String label) {
		this.theProblem = theProblem;
		this.buttonType = buttonType;
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			theButton = new XButton(label, applet);
		add(theButton);
	}
	
	public ProblemButton (Problem theProblem, XApplet applet, int buttonType) {
		this(theProblem, applet, buttonType, (buttonType == CHECK) ? "Check"
																				: (buttonType == TELL_ME) ? "Tell Me"
																				: "Another problem");
	}
	
	private boolean localAction(Object target) {
		if (target == theButton) {
			switch (buttonType) {
				case CHECK:
					theProblem.checkAnswer();
					return true;
				case TELL_ME:
					theProblem.solveExercise();
					return true;
				case ANOTHER:
					theProblem.changeData();
					theProblem.changeQuestion();
					return true;
				default:
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
