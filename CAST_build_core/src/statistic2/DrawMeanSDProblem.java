package statistic2;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise.*;
import random.*;


public class DrawMeanSDProblem extends Problem {
	static final private String kInitialMessage = "Sketch a distribution that matches the mean and standard deviation given in the question, then click Check";
	
	private double meanSlopPropn = 0.25;
	private double sdSlopPropn = 0.25;
	
	private String aboutDataText;
	private String questionText;
//	private NumValue maxMean, maxSD;
		
	private Value randomValues[];
	private Value longestValues[];
	private String messageArray[] = {kInitialMessage, null, null, null, null, null};
	
	private RandomUniform sampSizeGenerator, axisGenerator;
	private RandomRectangular dataMinGenerator, dataMaxGenerator;
	
	private BasicDataView dataView;
	private MultiHorizAxis axis;
	private double[] class0Start, classWidth;
	
	private XChoice axisChoice;
	
	private double targetMean, targetSD, meanSlop;
	private int bestAxisAlternate;
	
	public DrawMeanSDProblem(DataSet data, String aboutDataText, String questionText,
													String[] classInfo, int sampSizeMin, int sampSizeMax,
													NumValue maxMean, NumValue maxSD) {
		super(data);
		this.aboutDataText = aboutDataText;
		this.questionText = questionText;
//		this.maxMean = maxMean;
//		this.maxSD = maxSD;
		
		class0Start = new double[classInfo.length];
		classWidth = new double[classInfo.length];
		for (int i=0 ; i<classInfo.length ; i++) {
			StringTokenizer st = new StringTokenizer(classInfo[i]);
			class0Start[i] = Double.parseDouble(st.nextToken());
			classWidth[i] = Double.parseDouble(st.nextToken());
		}
		
		sampSizeGenerator = new RandomUniform(1, sampSizeMin, sampSizeMax);
		
		randomValues = new Value[3];
		longestValues = new Value[3];
	
		randomValues[0] = new NumValue(1, 0);
		randomValues[1] = new NumValue(maxMean);
		randomValues[2] = new NumValue(maxSD);
		longestValues[0] = new NumValue(sampSizeMax, 0);
		longestValues[1] = maxMean;
		longestValues[2] = maxSD;
		
		dataMinGenerator = new RandomRectangular(1, 0.0, 0.3);
		dataMaxGenerator = new RandomRectangular(1, 0.6, 1.0);
	}
	
	public void setSlopPropns(double meanSlopPropn, double sdSlopPropn) {
		this.meanSlopPropn = meanSlopPropn;
		this.sdSlopPropn = sdSlopPropn;
	}
	
	public void setLinkedViewAxis(BasicDataView dataView, MultiHorizAxis axis,
																																		XChoice axisChoice) {
		this.dataView = dataView;
		this.axis = axis;
		this.axisChoice = axisChoice;
		int noOfAxes = axis.getNoOfAlternates();
		axisGenerator = new RandomUniform(1, 0, noOfAxes - 1);
	}
	
	public void solveExercise() {
		message.setText(4);
		answer.setToCorrectAnswer(null);
		changeAxis(bestAxisAlternate);
		axisChoice.select(bestAxisAlternate);
		
		dataView.initialise();
		dataView.setMeanSD(targetMean, targetSD);
		dataView.setShow4s(true);
		dataView.repaint();
	}
	
	public void checkAnswer() {
		double meanAttempt = dataView.findMeanFromHisto();
		
		if (Math.abs(meanAttempt - targetMean) >= meanSlop) {
			message.setText(2);
			answer.markAnswer(AnswerPanel.WRONG);
		}
		else {
			double sdAttempt = dataView.findSDFromHisto();
			if (Math.abs(sdAttempt - targetSD) >= sdSlopPropn * targetSD) {
				message.setText(3);
				answer.markAnswer(AnswerPanel.WRONG);
			}
			else{
				message.setText(1);
				answer.markAnswer(AnswerPanel.CORRECT);
			}
		}
		dataView.setShow4s(true);
		dataView.repaint();
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
		
		bestAxisAlternate = axisGenerator.generateOne();
		if (axis.labelsArranged())
			axis.setAlternateLabels(bestAxisAlternate);
		else
			axis.setStartAlternate(bestAxisAlternate);
		
		double axisMin = axis.minOnAxis;
		double axisMax = axis.maxOnAxis;
		
		double dataMin = axisMin + dataMinGenerator.generateOne() * (axisMax - axisMin);
		double dataMax = dataMin + dataMaxGenerator.generateOne() * (axisMax - dataMin);
		targetMean = (dataMin + dataMax) * 0.5;
		targetSD = (dataMax - dataMin) / 6.0;
		meanSlop = targetSD * meanSlopPropn;
		
		((NumValue)randomValues[1]).setValue(targetMean);
		((NumValue)randomValues[2]).setValue(targetSD);
		
		int axisAlternate = axisGenerator.generateOne();	//	so axis may not match target
		if (axis.labelsArranged())
			axis.setAlternateLabels(axisAlternate);
		else
			axis.setStartAlternate(axisAlternate);
		if (axisChoice != null)
			axisChoice.select(axisAlternate);
		
		dataView.changeClasses(class0Start[axisAlternate], classWidth[axisAlternate]);
		dataView.resetClasses();
		dataView.repaint();
		
		axis.repaint();
		
		questionDescription.setText(questionText, randomValues);
		
		messageArray[1] = "Good! The mean and standard deviation of your distribution are close enough to " + randomValues[1].toString() + " and " + randomValues[2].toString();
		messageArray[2] = "Not close enough! The mean of your distribution should be closer to " + randomValues[1].toString();
		messageArray[3] = "Not close enough! The mean of your distribution is OK, but its standard deviation is not close enough to " + randomValues[2].toString();
		messageArray[4] = "The distribution shown above has mean " + randomValues[1].toString() + " and standard deviation " + randomValues[2].toString();
		
		if (message != null) {
			message.changeText(messageArray);
			message.setText(0);
			answer.reset();
		}
	}
	
	public void changeAxis(int alternateIndex) {
		if (axis.setAlternateLabels(alternateIndex)) {
			dataView.changeClasses(class0Start[alternateIndex], classWidth[alternateIndex]);
			dataView.resetClasses();
			dataView.repaint();
			
			axis.repaint();
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