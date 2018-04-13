package qnUtils;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;


abstract public class QuestionApplet extends CoreQuestionApplet {
	static final private String INITIAL_PARAM = "initialValues";
	static final private String PARAM_RANGE_PARAM = "paramRange";
	static final private String QUESTION_PARAM = "question";
	
	protected QuestionPanel question;
	
	protected HypothesisTest test;
	private HypothesisView hypothesisView;
	
	abstract protected void addQuestionFields(QuestionPanel question, StringTokenizer st);
	abstract protected HypothesisTest findTestInfo(String tailString, String[] paramString);
	abstract protected String[] getNewValueStrings(StringTokenizer st);
	
	protected XPanel dataPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		StringTokenizer st = new StringTokenizer(getParameter(INITIAL_PARAM));
		
		question = new QuestionPanel(this, QUESTION_PARAM, PARAM_RANGE_PARAM);
		question.setFont(getStandardFont());
		
		tailString = st.nextToken();
		
		addQuestionFields(question, st);
		
		thePanel.add("Center", question);
		
		XPanel lowerPanel = new InsetPanel(0, 3, 0, 5);
			lowerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
			if (testNotProb()) {
				test = findTestInfo(tailString, question.getValueStrings());
				hypothesisView = new HypothesisView(test, this);
				lowerPanel.add(hypothesisView);
			}
			
			lowerPanel.add(lowerDataPanel());
		
			lowerPanel.lockBackground(getLowerBackground());
		thePanel.add("South", lowerPanel);
		return thePanel;
	}
	
	protected Color getLowerBackground() {
		return kAnswerBackground;
	}
	
	protected XPanel lowerDataPanel() {
		XPanel thePanel = answerEditPanel(VERT_ANSWER);
		thePanel.lockBackground(getLowerBackground());
		return thePanel;
	}
	
	protected void generateQuestion() {
		String paramRange = question.getNextRange();
		StringTokenizer st = new StringTokenizer(paramRange);
		
		tailString = st.nextToken();
		
		String valueString[] = getNewValueStrings(st);
		question.showNextQuestion(valueString);
		
		if (testNotProb())
			test = findTestInfo(tailString, valueString);
		
		setupAnswer();
		valueEdit.reset(answer, getAccuracy(exactSlop), getAccuracy(approxSlop));
		message.changeText(getAnswerStrings());
		
		if (testNotProb()) {
			double ans = answer.toDouble();
			int conclusionOption = (ans > 0.1) ? 0
										: (ans > 0.05) ? 1
										: (ans > 0.01) ? 2 : 3;
			conclusionPanel.changeCorrectOption(conclusionOption);
			conclusionPanel.reset();
			
			hypothesisView.setHypothesis(test);
			hypothesisView.repaint();
		}
	}
}