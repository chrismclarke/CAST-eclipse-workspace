package percentile;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise.*;
import random.*;


abstract public class CorePercentileProblem extends Problem {
	static final private String kInitialMessage = "Type answer in the box above, then click Check";
	static final private String kBadValueMessage = "Error! You must type a number in the answer box.";
	
	private String aboutDataText;
	private String[] questionText;
	private int longestQuestionIndex;
	protected int answerDecimals;
	
	protected Value aboveLabel, belowLabel;
	
	protected Value randomValues[];
	protected Value longestValues[];
	protected int questionIndex;
	protected String messageArray[] = {kInitialMessage, kBadValueMessage, null, null, null, null, null};
								//	0=initial, 1=formatError, 2=correct, 3=close, 4=wrong, 5=cheatAnswer, 6=maxString
	
	private RandomUniform qnTextGenerator;
	private Random inequalityGenerator;
	
	protected NumValue exactAnswer;
//	protected double answerSlop;
	protected double lowExactAnswer, highExactAnswer, lowApproxAnswer, highApproxAnswer;
	
	public CorePercentileProblem(DataSet data, String aboutDataText, String[] questionText,
										String aboveBelowStrings, int longestQuestionIndex, int answerDecimals) {
		super(data);
		this.aboutDataText = aboutDataText;
		this.questionText = questionText;
		this.longestQuestionIndex = longestQuestionIndex;
		this.answerDecimals = answerDecimals;
		
		StringTokenizer st = new StringTokenizer(aboveBelowStrings, "#");
		belowLabel = new LabelValue(st.nextToken());
		aboveLabel = new LabelValue(st.nextToken());
		
		qnTextGenerator = new RandomUniform(1, 0, questionText.length - 1);
		questionIndex = 0;
		inequalityGenerator = new Random();
	}
	
	public void checkAnswer() {
		NumValue attempt = answer.getAnswer();
		if (attempt == null) {
			message.setText(1);
			answer.markAnswer(AnswerPanel.UNKNOWN);
		}
		else {
			double attemptVal = attempt.toDouble();
			if (attemptVal >= lowExactAnswer && attemptVal <= highExactAnswer) {
				message.setText(2);
				answer.markAnswer(AnswerPanel.CORRECT);
			}
			else if (attemptVal >= lowApproxAnswer && attemptVal <= highApproxAnswer) {
				message.setText(3);
				answer.markAnswer(AnswerPanel.CORRECT);
			}
			else {
				message.setText(4);
				answer.markAnswer(AnswerPanel.WRONG);
			}
		}
	}
	
	public void changeData() {
		dataDescription.setText(aboutDataText, randomValues);
		dataDescription.repaint();
		if (message != null)
			message.setText(0);
	}
	
	protected void changeQuestionInfo() {
		questionIndex = qnTextGenerator.generateOne();
		questionDescription.setText(questionText[questionIndex], randomValues);
		questionDescription.repaint();
		
		randomValues[0] = (inequalityGenerator.nextDouble() < 0.5) ? belowLabel : aboveLabel;
	}
	
	public TextCanvas createDataCanvas(int pixWidth, XApplet applet) {
		dataDescription = new TextCanvas(kNoSymbols, aboutDataText, longestValues, pixWidth);
		changeData();
		return dataDescription;
	}
	
	public TextCanvas createQuestionCanvas(int pixWidth, XApplet applet) {
		questionDescription = new TextCanvas(kNoSymbols, questionText[longestQuestionIndex],
																																longestValues, pixWidth);
		changeQuestion();
		return questionDescription;
	}
	
	public XTextArea createMessageArea(int pixWidth, String longestMessage, XApplet applet) {
		messageArray[5] = longestMessage;
		message = new XTextArea(messageArray, 0, pixWidth, applet);
		message.lockBackground(Color.white);
		message.setForeground(Color.red);
		return message;
	}
	
}