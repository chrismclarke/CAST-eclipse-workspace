package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import formula.*;
import imageGroups.*;

import twoGroup.*;
import linMod.*;


public class PDiffCIApplet extends QuestionApplet {
	static final private NumValue maxProb = new NumValue(0.0, 4);
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private NumValue p1, p2, n1, n2, sd1, sd2, dSD;
	private int decimals;
	
	protected void loadParameterImages() {
		GroupsEqualsImages.loadGroups(this);
	}
	
	protected boolean testNotProb() {
		return false;
	}
	
	protected double questionProportion() {
		return 0.45;
	}
	
	protected void addQuestionFields(QuestionPanel question, StringTokenizer st) {
		decimals = Integer.parseInt(st.nextToken());
		
		XLabel x1Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		x1Edit.setFont(questionFont);
		x1Edit.setForeground(Color.red);
		question.add(QuestionLayout.S1, x1Edit);
		
		XLabel n1Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		n1Edit.setFont(questionFont);
		n1Edit.setForeground(Color.blue);
		question.add(QuestionLayout.N1, n1Edit);
		
		XLabel x2Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		x2Edit.setFont(questionFont);
		x2Edit.setForeground(Color.red);
		question.add(QuestionLayout.S2, x2Edit);
		
		XLabel n2Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		n2Edit.setFont(questionFont);
		n2Edit.setForeground(Color.blue);
		question.add(QuestionLayout.N2, n2Edit);
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 3));
		
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
			ProbCalcPanel prob = new ProbCalcPanel(maxProb.decimals, bigGreenContext);
		thePanel.add(prob);
		
				XPanel sdPanel = new InsetPanel(0, 5);
				sdPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					ProbSDCalcPanel sd = new ProbSDCalcPanel(maxProb.decimals, bigGreenContext);
				sdPanel.add(sd);
				sdPanel.lockBackground(kWorkingBackground);
		thePanel.add(sdPanel);
		
			DiffSDCalcPanel sdDiff = new DiffSDCalcPanel(DiffSDCalcPanel.PROPNS, maxProb, bigGreenContext);
		thePanel.add(sdDiff);
		
		
				XPanel plusMinusPanel = new InsetPanel(0, 5);
				plusMinusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					PlusMinusCalcPanel plusMinus = new PlusMinusCalcPanel(maxProb, bigGreenContext);
				plusMinusPanel.add(plusMinus);
				
				plusMinusPanel.lockBackground(kWorkingBackground);
		thePanel.add(plusMinusPanel);
		return thePanel;
	}
	
	protected String[] getAnswerStrings() {
		String answerString[] = new String[5];
		
		String calcString = getSDString("sd1", p1, n1, sd1) + " and " + getSDString("sd2", p2, n2, sd2)
								+ ". sd(p2-p1) = root(sqr(" + sd1.toString() + ") + sqr("
								+ sd2.toString() + ") =" + dSD.toString()
								+ ". The CI is (p2-p1) \u00b1 ";
		
		answerString[LinkedAnswerEditPanel.NONE] = "Evaluate a 95% confidence interval for prob2 - prob1.";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!  " + calcString
														+ answer.toString() + ".";
		answerString[LinkedAnswerEditPanel.WRONG] = "Wrong. Find sd1 = root(p1(1-p1)/n1) and sd2 = root(p2(1-p2)/n2). The sd of (p2-p1) is root(sd1 * sd1 + sd2 * sd2) and the CI is (p2-p1) \u00b1 twice this.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!  " + calcString
														+ answer.toString() + ".";
		return answerString;
	}
	
	private String getSDString(String sdName, NumValue p, NumValue n, NumValue sd) {
		NumValue q = new NumValue(1.0 - p.toDouble(), p.decimals);
		
		StringBuffer sb = new StringBuffer(50);
		sb.append(sdName);
		sb.append(" = root(");
		sb.append(p.toString());
		sb.append(" x ");
		sb.append(q.toString());
		sb.append(" / ");
		sb.append(n.toString());
		sb.append(") = ");
		sb.append(sd.toString());
		return sb.toString();
	}
	
	protected HypothesisTest findTestInfo(String tailString, String[] paramString) {
		return null;
	}
	
	protected String valueLabel() {
		double theDiff = p2.toDouble() - p1.toDouble();
		String diffString = new NumValue(theDiff, decimals).toString();
		return "95% CI is " + diffString + " \u00b1 ";
	}
	
	protected double getAccuracy(double slopValue) {
		return slopValue * answer.toDouble();
	}
	
	protected String[] getNewValueStrings(StringTokenizer st) {
		decimals = Integer.parseInt(st.nextToken());
		
		String p1String = nextRandomValue(st);
		String n1String = nextRandomValue(st);
		String p2String = nextRandomValue(st);
		String n2String = nextRandomValue(st);
		
		double p1 = Double.parseDouble(p1String);
		int n1 = Integer.parseInt(n1String);
		String s1String = new NumValue(p1 * n1, 0).toString();
		
		double p2 = Double.parseDouble(p2String);
		int n2 = Integer.parseInt(n2String);
		String s2String = new NumValue(p2 * n2, 0).toString();
		
		String valueString[] = {null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, s1String, s2String, n1String, n2String};
		return valueString;
	}
	
	protected void setupAnswer() {
		n1 = new NumValue(question.getValueString(QuestionLayout.N1));
		NumValue s1 = new NumValue(question.getValueString(QuestionLayout.S1));
		n2 = new NumValue(question.getValueString(QuestionLayout.N2));
		NumValue s2 = new NumValue(question.getValueString(QuestionLayout.S2));
		
		p1 = new NumValue(s1.toDouble() / n1.toDouble(), decimals);
		p2 = new NumValue(s2.toDouble() / n2.toDouble(), decimals);
		
		sd1 = new NumValue(Math.sqrt(p1.toDouble() * (1.0 - p1.toDouble()) / n1.toDouble()), decimals);
		sd2 = new NumValue(Math.sqrt(p2.toDouble() * (1.0 - p2.toDouble()) / n2.toDouble()), decimals);
		
		dSD = new NumValue(Math.sqrt(sd1.toDouble() * sd1.toDouble() + sd2.toDouble() * sd2.toDouble()), decimals);
		
		answer = new NumValue(2.0 * dSD.toDouble(), decimals);
	}
}