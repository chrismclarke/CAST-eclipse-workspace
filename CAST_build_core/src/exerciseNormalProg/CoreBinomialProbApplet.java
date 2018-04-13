package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;

import exerciseNormal.*;


abstract public class CoreBinomialProbApplet extends CoreLookupApplet {
	static final protected double kMinCum = 0.001;
	
	protected ResultValuePanel resultPanel;	
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 9));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, translate("Probability") + " =", 6);
				registerStatusItem("prob", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "choice");				//	always registered as int
		registerParameter("intervalType", "choice");
		registerParameter("cut-offs", "cut-offs");
		registerParameter("varName", "string");
		registerParameter("maxValue", "const");
		registerParameter("nTrials", "int");
		registerParameter("pSuccess", "const");
		registerParameter("trialsName", "string");
	}
	
	protected void addTypeDelimiters() {
		addType("cut-offs", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("cut-offs")) {
			int questionType = intervalType();
			StringTokenizer pst = new StringTokenizer(valueString, ",");
			int start = Integer.parseInt(pst.nextToken());
			int end = Integer.parseInt(pst.nextToken());
			int total = getNTrials();
			return new DiscreteIntervalLimits(start, end, questionType / 3, questionType % 3, total);
		}
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("cut-offs")) {
			int questionType = intervalType();
			int lowType = questionType / 3;
			int highType = questionType % 3;
			
			int total = getNTrials();
			Dimension minMax = getMinMax(paramString, total);
			int xMin = minMax.width;
			int xMax = minMax.height;
			
			RandomInteger generator = new RandomInteger(xMin, xMax, 1, nextSeed());
			int xLow, xHigh;
			do {
				xLow = generator.generateOne();
				xHigh = generator.generateOne();
			} while (xLow == xHigh);
			
			if (xLow > xHigh) {
				int temp = xLow;
				xLow = xHigh;
				xHigh = temp;
			}
			
			if (lowType == DiscreteIntervalLimits.EXCLUDED)
				xLow = Math.max(0, xLow - 1);
			if (highType == DiscreteIntervalLimits.EXCLUDED)
				xHigh = Math.min(total, xHigh + 1);
			
			return new DiscreteIntervalLimits(xLow, xHigh, lowType, highType, total);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
	abstract protected Dimension getMinMax(String minMaxString, int total);
	
	
	protected int intervalType() {
		return getIntParam("intervalType");
	}
	
	protected DiscreteIntervalLimits getLimits() {
		return (DiscreteIntervalLimits)getObjectParam("cut-offs");
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	public NumValue getMaxValue() {
		return getNumValueParam("maxValue");
	}
	
	protected int getNTrials() {
		return getIntParam("nTrials");
	}
	
	protected NumValue getPSuccess() {
		return getNumValueParam("pSuccess");
	}
	
	protected String getTrialsName() {
		return getStringParam("trialsName");
	}
	
//-----------------------------------------------------------
	
	
	abstract protected DataSet getData();
	
	abstract protected XPanel getWorkingPanels(DataSet data);
	
	abstract protected void setDisplayForQuestion();
	abstract protected void setDataForQuestion();
	
//-----------------------------------------------------------
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	
	abstract protected int getMessageHeight();
	
//-----------------------------------------------------------
	
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	abstract protected double evaluateProbability(DiscreteIntervalLimits limits);
	abstract protected boolean isClose(double attempt, double correct, DiscreteIntervalLimits limits);
	abstract protected boolean isCorrect(double attempt, double correct, DiscreteIntervalLimits limits);
	
	protected int assessAnswer() {
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = getAttempt();
			if (attempt < 0 || attempt > 1)
				return ANS_INVALID;
			else {
				DiscreteIntervalLimits limits = getLimits();
				double correct = evaluateProbability(limits);
				if (isCorrect(attempt, correct, limits))
					return ANS_CORRECT;
				else if (isClose(attempt, correct, limits))
					return ANS_CLOSE;
				else
					return ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
	}
	
	abstract protected void showCorrectWorking();
	
	
	protected double getMark() {
		int markType = assessAnswer();
		return (markType == ANS_CORRECT) ? 1 : (markType == ANS_CLOSE) ? 0.8 : 0;
	}
	
}