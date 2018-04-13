package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import models.*;
import formula.*;
import imageGroups.*;

import normal.*;
import twoGroup.*;


public class PDiffTestQnApplet extends QuestionApplet {
	static final private NumValue maxProb = new NumValue(0.0, 4);
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private NumValue p1, p2, n1, n2, sd1, sd2, dSD, zValue;
	private int decimals;
	
	private ContinTableDataSet data;
	
	public void setupApplet() {
		data = new ContinTableDataSet(this);
		super.setupApplet();
	}
	
	protected void loadParameterImages() {
		GroupsEqualsImages.loadGroups(this);
	}
	
	protected boolean testNotProb() {
		return true;
	}
	
	protected double questionProportion() {
		return 0.45;
	}
	
	protected void addQuestionFields(QuestionPanel question, StringTokenizer st) {
		decimals = Integer.parseInt(st.nextToken());
		
		String nextValString = st.nextToken();
		int succ1 = Integer.parseInt(nextValString);
		XLabel x1Edit = new XLabel(nextValString, XLabel.LEFT, this);
		x1Edit.setFont(questionFont);
		x1Edit.setForeground(Color.red);
		question.add(QuestionLayout.S1, x1Edit);
		
		nextValString = st.nextToken();
		int total1 = Integer.parseInt(nextValString);
		XLabel n1Edit = new XLabel(nextValString, XLabel.LEFT, this);
		n1Edit.setFont(questionFont);
		n1Edit.setForeground(Color.blue);
		question.add(QuestionLayout.N1, n1Edit);
		
		nextValString = st.nextToken();
		int succ2 = Integer.parseInt(nextValString);
		XLabel x2Edit = new XLabel(nextValString, XLabel.LEFT, this);
		x2Edit.setFont(questionFont);
		x2Edit.setForeground(Color.red);
		question.add(QuestionLayout.S2, x2Edit);
		
		nextValString = st.nextToken();
		int total2 = Integer.parseInt(nextValString);
		XLabel n2Edit = new XLabel(nextValString, XLabel.LEFT, this);
		n2Edit.setFont(questionFont);
		n2Edit.setForeground(Color.blue);
		question.add(QuestionLayout.N2, n2Edit);
		
		setDataValues(succ1, total1, succ2, total2);
	}
	
	protected XPanel bottomPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("North", answerEditPanel(HORIZ_ANSWER));
		thePanel.add("Center", super.bottomPanel());
		
		return thePanel;
	}
	
	protected XPanel lowerDataPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
		thePanel.add(new ProbCalcPanel(maxProb.decimals, bigGreenContext));
		return thePanel;
	}
	
	protected Color getLowerBackground() {
		return kWorkingBackground;
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			XPanel topPanel = new XPanel();
//			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 20));
			
				FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
			topPanel.add(new ProbSDCalcPanel(maxProb.decimals, bigGreenContext));
			
				XPanel diffSDPanel = new InsetPanel(0, 5);
				diffSDPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					DiffSDCalcPanel sd = new DiffSDCalcPanel(DiffSDCalcPanel.PROPNS, maxProb, bigGreenContext);
				diffSDPanel.add(sd);
				diffSDPanel.lockBackground(kWorkingBackground);
			topPanel.add(diffSDPanel);
			
				XPanel zPanel = new InsetPanel(0, 0, 0, 10);
				zPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				zPanel.add(new ZCalcPanel(bigGreenContext));
			topPanel.add(zPanel);
			
		thePanel.add("North", topPanel);
		
			XPanel normPanel = new InsetPanel(10, 4);
			normPanel.setLayout(new BorderLayout(0, 0));
			normPanel.add("Center", new DistnLookupPanel(DistnLookupPanel.Z_DISTN, this,
																								kWorkingBackground));
			normPanel.lockBackground(kWorkingBackground);
		thePanel.add("Center", normPanel);
		return thePanel;
	}
	
	protected String[] getAnswerStrings() {
		String answerString[] = new String[5];
		
		String calcString = getSDString("sd1", p1, n1, sd1) + " and " + getSDString("sd2", p2, n2, sd2)
								+ ". sd(p2-p1) = root(sqr(" + sd1.toString() + ") + sqr("
								+ sd2.toString() + ") =" + dSD.toString()
								+ ". Z = (p2-p1)/sd(p2-p1) = " + zValue.toString() + ", so the p-value is ";
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the p-value for the test and interpret it.";
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
		int alternativeTail;
		if (tailString.equals("low"))
			alternativeTail = HypothesisTest.HA_LOW;
		else if (tailString.equals("high"))
			alternativeTail = HypothesisTest.HA_HIGH;
		else
			alternativeTail = HypothesisTest.HA_NOT_EQUAL;
		return new DiffHypothesisTest(data, new NumValue(0.0, 0), alternativeTail,
																DiffHypothesisTest.DIFF_PROB_IMAGE, this);
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
	
	private void setDataValues(int succ1, int total1, int succ2, int total2) {
		CatVariable x = (CatVariable)data.getVariable("x");
		int cx[] = {total1, total2};
		x.setCounts(cx);
		CatVariable y = (CatVariable)data.getVariable("y");
		int cy[] = {succ1, total1 - succ1, succ2, total2 - succ2};
		y.setCounts(cy);
	}
	
	protected void setupAnswer() {
		int total1 = Integer.parseInt(question.getValueString(QuestionLayout.N1));
		int total2 = Integer.parseInt(question.getValueString(QuestionLayout.N2));
		int succ1 = Integer.parseInt(question.getValueString(QuestionLayout.S1));
		int succ2 = Integer.parseInt(question.getValueString(QuestionLayout.S2));
		
		setDataValues(succ1, total1, succ2, total2);
		data.variableChanged("y");
		
		n1 = new NumValue(total1, 0);
		n2 = new NumValue(total2, 0);
		
		p1 = new NumValue(succ1 / (double)total1, decimals);
		p2 = new NumValue(succ2 / (double)total2, decimals);
		
		sd1 = new NumValue(Math.sqrt(p1.toDouble() * (1.0 - p1.toDouble()) / total1), decimals);
		sd2 = new NumValue(Math.sqrt(p2.toDouble() * (1.0 - p2.toDouble()) / total2), decimals);
		
		dSD = new NumValue(Math.sqrt(sd1.toDouble() * sd1.toDouble() + sd2.toDouble() * sd2.toDouble()), decimals);
		
		zValue = new NumValue(test.evaluateStatistic(), 3);
		answer = new NumValue(test.evaluatePValue(), 4);
	}
}