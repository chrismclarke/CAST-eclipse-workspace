package exerciseTest;

import java.awt.*;
import java.util.*;

import exercise2.*;

import dataView.*;
import formula.*;
import utils.*;

import exerciseTestProg.*;


public class HypothesesPanel extends XPanel implements StatusInterface, ExerciseConstants {
	
	private ExerciseApplet applet;
	private XChoice nullInequalityChoice, altInequalityChoice;
	
	public HypothesesPanel(String parameter, NumValue constant, ExerciseApplet applet) {
		this.applet = applet;
		
		nullInequalityChoice = createInequalityChoice(parameter, MText.expandText("H#sub0#: "), constant, applet);
		altInequalityChoice = createInequalityChoice(parameter, MText.expandText("H#sub1#: "), constant, applet);
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		add(nullInequalityChoice);
		add(altInequalityChoice);
	}
	
	public void reset(String parameter, NumValue constant) {
		resetHypotheses(nullInequalityChoice, parameter, constant);
		resetHypotheses(altInequalityChoice, parameter, constant);
	}
	
	public void setCorrectTail(int tail) {
		switch (tail) {
			case CoreTestApplet.TAIL_LOW_EQ:
				nullInequalityChoice.select(0);
				altInequalityChoice.select(2);
				break;
			case CoreTestApplet.TAIL_LOW:
				nullInequalityChoice.select(5);
				altInequalityChoice.select(2);
				break;
			case CoreTestApplet.TAIL_HIGH_EQ:
				nullInequalityChoice.select(0);
				altInequalityChoice.select(4);
				break;
			case CoreTestApplet.TAIL_HIGH:
				nullInequalityChoice.select(3);
				altInequalityChoice.select(4);
				break;
			case CoreTestApplet.TAIL_BOTH:
				nullInequalityChoice.select(0);
				altInequalityChoice.select(1);
				break;
		}
	}
	
	public int assessHypotheses(int tail) {
		int correctNullChoice, correctAltChoice;
		switch (tail) {
			case CoreTestApplet.TAIL_LOW:
				correctNullChoice = 5;
				correctAltChoice = 2;
				break;
			case CoreTestApplet.TAIL_LOW_EQ:
				correctNullChoice = 0;
				correctAltChoice = 2;
				break;
			case CoreTestApplet.TAIL_HIGH:
				correctNullChoice = 3;
				correctAltChoice = 4;
				break;
			case CoreTestApplet.TAIL_HIGH_EQ:
				correctNullChoice = 0;
				correctAltChoice = 4;
				break;
			case CoreTestApplet.TAIL_BOTH:
			default:
				correctNullChoice = 0;
				correctAltChoice = 1;
				break;
		}
		int nullChoice = nullInequalityChoice.getSelectedIndex();
		int altChoice = altInequalityChoice.getSelectedIndex();
		
		if (correctAltChoice != altChoice)
			return ANS_WRONG;
		else if (correctNullChoice == 5 && correctAltChoice == 2 && nullChoice == 0)
			return ANS_CLOSE;
		else if (correctNullChoice == 3 && correctAltChoice == 4 && nullChoice == 0)
			return ANS_CLOSE;
		else if (correctNullChoice != nullChoice)
			return ANS_WRONG;
		else
			return ANS_CORRECT;
	}
	
	private XChoice createInequalityChoice(String parameter, String hypothesisString,
																											NumValue constant, XApplet applet) {
		XChoice theChoice = new XChoice(hypothesisString, XChoice.HORIZONTAL, applet);
		resetHypotheses(theChoice, parameter, constant);
		return theChoice;
	}
	
	public void resetHypotheses(XChoice theChoice, String parameter, NumValue constant) {
		theChoice.clearItems();
		theChoice.addItem(parameter + " = " + constant);
		theChoice.addItem(parameter + " " + MText.expandText("#ne# ") + constant);
		theChoice.addItem(parameter + " < " + constant);
		theChoice.addItem(parameter + " " + MText.expandText("#le# ") + constant);
		theChoice.addItem(parameter + " > " + constant);
		theChoice.addItem(parameter + " " + MText.expandText("#ge# ") + constant);
		theChoice.select(1);
	}
	
	public String getStatus() {
		return (nullInequalityChoice.getSelectedIndex() + " " + altInequalityChoice.getSelectedIndex());
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status);
		
		nullInequalityChoice.select(Integer.parseInt(st.nextToken()));
		
		altInequalityChoice.select(Integer.parseInt(st.nextToken()));
	}
	
	private boolean localAction(Object target) {
		if (target == nullInequalityChoice || target == altInequalityChoice) {
			applet.noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}