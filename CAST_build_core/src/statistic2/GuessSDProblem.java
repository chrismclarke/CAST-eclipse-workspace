package statistic2;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import exercise.*;
import random.*;


public class GuessSDProblem extends Problem {
	static final private String kInitialMessage = "Estimate the standard deviation of this data set by eye, then type your estimate in the box above and click Check";
	static final private String kBadValueMessage = "Error! You must type a number in the answer box.";
	
	static final private double kSlopFactor = 0.25;
	
	private String aboutDataText;
	private String questionText;
	private int sdDecimals;
	private String baseKey, yKey;
	
	private Value randomValues[];
	private Value longestValues[];
	private String messageArray[] = {kInitialMessage, kBadValueMessage, null, null, null, null};
	
	private RandomUniform sampSizeGenerator, axisGenerator;
	private RandomRectangular dataMinGenerator, dataMaxGenerator;
	
	private BasicDataView dataView;
	private MultiHorizAxis axis;
	private double[] class0Start, classWidth;
	
	private NumValue exactAnswer;
	private double answerSlop;
	
	public GuessSDProblem(DataSet data, String aboutDataText, String questionText,
															String[] classInfo, int sampSizeMin, int sampSizeMax,
															int sdDecimals, String baseKey, String yKey) {
		super(data);
		this.aboutDataText = aboutDataText;
		this.questionText = questionText;
		this.sdDecimals = sdDecimals;
		this.baseKey = baseKey;
		this.yKey = yKey;
		
		class0Start = new double[classInfo.length];
		classWidth = new double[classInfo.length];
		for (int i=0 ; i<classInfo.length ; i++) {
			StringTokenizer st = new StringTokenizer(classInfo[i]);
			class0Start[i] = Double.parseDouble(st.nextToken());
			classWidth[i] = Double.parseDouble(st.nextToken());
		}
		
		sampSizeGenerator = new RandomUniform(1, sampSizeMin, sampSizeMax);
		
		randomValues = new Value[1];
		longestValues = new Value[1];
	
		randomValues[0] = new NumValue(1, 0);
		longestValues[0] = new NumValue(sampSizeMax, 0);
		
		dataMinGenerator = new RandomRectangular(1, 0.0, 0.3);
		dataMaxGenerator = new RandomRectangular(1, 0.6, 1.0);
	}
	
	public void setLinkedViewAxis(BasicDataView dataView, MultiHorizAxis axis) {
		this.dataView = dataView;
		this.axis = axis;
		int noOfAxes = axis.getNoOfAlternates();
		axisGenerator = new RandomUniform(1, 0, noOfAxes - 1);
	}
	
	public void solveExercise() {
		message.setText(4);
		answer.setToCorrectAnswer(exactAnswer);
		dataView.setShow4s(true);
		dataView.repaint();
//		((PropnWorkingPanel)working).setReferenceValue((NumValue)randomValues[1]);
	}
	
	public void checkAnswer() {
		NumValue attempt = answer.getAnswer();
		if (attempt == null) {
			message.setText(1);
			answer.markAnswer(AnswerPanel.UNKNOWN);
		}
		else if (Math.abs(attempt.toDouble() - exactAnswer.toDouble()) < answerSlop) {
			message.setText(2);
			answer.markAnswer(AnswerPanel.CORRECT);
			dataView.setShow4s(true);
		dataView.repaint();
		}
		else {
			message.setText(3);
			answer.markAnswer(AnswerPanel.WRONG);
			dataView.setShow4s(true);
		}
	}
	
	public void changeData() {
		dataDescription.setText(aboutDataText, randomValues);
		dataDescription.repaint();
		if (message != null)
			message.setText(0);
	}
	
	public void changeQuestion() {
		int sampleSize = sampSizeGenerator.generateOne();
		((NumValue)randomValues[0]).setValue(sampleSize);
		questionDescription.setText(questionText, randomValues);
		questionDescription.repaint();
		
		int axisAlternate = axisGenerator.generateOne();
		if (axis.labelsArranged())
			axis.setAlternateLabels(axisAlternate);
		else
			axis.setStartAlternate(axisAlternate);
		
		double axisMin = axis.minOnAxis;
		double axisMax = axis.maxOnAxis;
		
		double dataMin = axisMin + dataMinGenerator.generateOne() * (axisMax - axisMin);
		double dataMax = dataMin + dataMaxGenerator.generateOne() * (axisMax - dataMin);
		double dataMean = (dataMin + dataMax) * 0.5;
		double dataSD = (dataMax - dataMin) / 6.0;
		
		NumSampleVariable randVar = (NumSampleVariable)data.getVariable(baseKey);
		randVar.setSampleSize(sampleSize);
		randVar.generateNextSample();
		
		ScaledVariable yVar = (ScaledVariable)data.getVariable(yKey);
		yVar.setParam(0, dataMean);
		yVar.setParam(1, dataSD);
		
		dataView.changeClasses(class0Start[axisAlternate], classWidth[axisAlternate]);
		dataView.resetClasses();
		
		data.variableChanged("y");
		axis.repaint();
		
		questionDescription.setText(questionText, randomValues);
		
		exactAnswer = new NumValue(dataView.findSDFromHisto(), sdDecimals);
		answerSlop = exactAnswer.toDouble() * kSlopFactor;
		
		dataView.setMessages(messageArray, exactAnswer);
		
		if (message != null) {
			message.changeText(messageArray);
			message.setText(0);
			answer.reset();
		}
	}
	
	public TextCanvas createDataCanvas(int pixWidth, XApplet applet) {
		dataDescription = new TextCanvas(kNoSymbols, aboutDataText, longestValues, pixWidth);
		changeData();
		return dataDescription;
	}
	
	public TextCanvas createQuestionCanvas(int pixWidth, XApplet applet) {
		questionDescription = new TextCanvas(kNoSymbols, questionText, longestValues, pixWidth);
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