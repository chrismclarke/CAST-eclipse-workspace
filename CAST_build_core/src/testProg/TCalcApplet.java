package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import test.*;
import qnUtils.*;
import distn.*;
import formula.*;


public class TCalcApplet extends QuestionApplet {
	private NumValue tValue;
	
	protected boolean testNotProb() {
		return true;
	}
	
	protected void addQuestionFields(QuestionPanel question, StringTokenizer st) {
		XLabel nEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		nEdit.setFont(questionFont);
		question.add(QuestionLayout.N, nEdit);
		
		XLabel xBarEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		xBarEdit.setFont(questionFont);
		xBarEdit.setForeground(Color.blue);
		question.add(QuestionLayout.XBAR, xBarEdit);
		
		XLabel sEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		sEdit.setFont(questionFont);
		sEdit.setForeground(Color.red);
		question.add(QuestionLayout.S, sEdit);
		
		XLabel meanEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		meanEdit.setFont(questionFont);
		meanEdit.setForeground(Color.blue);
		question.add(QuestionLayout.MEAN, meanEdit);
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 16));
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
		thePanel.add("North", new test.TCalcPanel(bigContext));
		thePanel.add("Center", new DistnLookupPanel(DistnLookupPanel.T_DISTN, this,
																									getBackground()));
		
		return thePanel;
	}
	
	protected String[] getAnswerStrings() {
		String nString = question.getValueString(QuestionLayout.N);
		int n = Integer.parseInt(nString);
		
		String solnString = "t = (xBar - mu) / (s / root(n)) = " + tValue.toString()
										+ ", with " + String.valueOf(n-1)
										+ " degrees of freedom, and the exact tail area is "
										+ answer.toString() + ".";
		String adviceString;
		switch (test.getTestTail()) {
			case HypothesisTest.HA_LOW:
				adviceString = "The p-value is the tail area to the left of t.";
				break;
			case HypothesisTest.HA_HIGH:
				adviceString = "The p-value is the tail area to the right of t.";
				break;
			case HypothesisTest.HA_NOT_EQUAL:
			default:
				adviceString = "The p-value is the sum of the two tail areas further from zero than t.";
				break;
		}
		
		String accuracyString = (new NumValue(approxSlop * 100, 0)).toString();
		
		String answerString[] = new String[5];
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the p-value to within " + accuracyString
										+ "% of the correct value. Then select your conclusion.";
		answerString[LinkedAnswerEditPanel.EXACT] = "P-value is correct!  " + solnString;
		answerString[LinkedAnswerEditPanel.WRONG] = "P-value is wrong.  Find t = (xBar - mu) / (s / root(n)). " + adviceString;
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "P-value is close enough!  " + solnString;
		return answerString;
	}
	
	protected HypothesisTest findTestInfo(String tailString, String[] paramString) {
		int testTail;
		if (tailString.equals("low"))
			testTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			testTail = HypothesisTest.HA_HIGH;
		else
			testTail = HypothesisTest.HA_NOT_EQUAL;
		return new UnivarHypothesisTest(null, null, new NumValue(paramString[0]), testTail, HypothesisTest.MEAN, this);
	}
	
	protected String[] getNewValueStrings(StringTokenizer st) {
		String nString = nextRandomValue(st);
		String xBarString = nextRandomValue(st);
		String sString = nextRandomValue(st);
		String muString = nextRandomValue(st);
		
		String valueString[] = {muString, null, null, null, xBarString, sString, nString, null};
		return valueString;
	}
	
	protected void setupAnswer() {
		String nString = question.getValueString(QuestionLayout.N);
		int n = Integer.parseInt(nString);
		
		String xBarString = question.getValueString(QuestionLayout.XBAR);
		double xBar = Double.parseDouble(xBarString);
		
		String sString = question.getValueString(QuestionLayout.S);
		double s = Double.parseDouble(sString);
		
		String muString = question.getValueString(QuestionLayout.MEAN);
		double mu = Double.parseDouble(muString);
		
		double t = (xBar - mu) / s * Math.sqrt(n);
		tValue = new NumValue(t, 3);
		
		double pValue = TTable.cumulative(tValue.toDouble(), n - 1);
//		System.out.println("t = " + tValue.toDouble() + ", lowerTail = " + pValue);
//		System.out.println("tail = " + test.getTestTail());
		switch (test.getTestTail()) {
			case HypothesisTest.HA_HIGH:
				pValue = 1.0 - pValue;
				break;
			case HypothesisTest.HA_NOT_EQUAL:
				pValue = 2.0 * Math.min(pValue, 1.0 - pValue);
				break;
			default:
		}
		answer = new NumValue(pValue, 4);
	}
}