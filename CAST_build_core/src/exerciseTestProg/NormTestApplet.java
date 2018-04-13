package exerciseTestProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import exercise2.*;
import expression.*;

import exerciseNormal.JdistnAreaLookup.*;


public class NormTestApplet extends CoreNormalApplet {
	static final private String kZAxisInfo = "-4 4 -4 1";
	
	static final private int kZDecimals = 4;
	static final private double kMaxZError = 0.00001;
	
	protected ExpressionResultPanel testStatExpression;
	
	private boolean showingCorrectAnswer = false;
	
	
//-----------------------------------------------------------
	
	protected NumValue getTestStat() {
		return testStatExpression.getAttempt();
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable distnVar = new NormalDistnVariable(getDistnName());	//	default N(0,1)
		data.addVariable("distn", distnVar);
		
		return data;
	}
		
//-----------------------------------------------------------
	
	protected String getPValueLabel() {
		return "p-value";
	}
	
	protected String getPValuesPropnsString() {
		return "P-values";
	}
	
	protected String getPvalueLongName() {
		return "p-value";
	}
		
//-----------------------------------------------------------
	
	protected String getTestStatLetter() {
		return  "z";
	}
	
	protected String getSdString() {
		return  "#sigma#";
	}
	
	protected String getTestStatName() {
		return  translate("z-score");
	}
	
	protected String getDistnName() {
		return  "Standard normal, Z";
	}
	
	protected String getShortDistnName() {
		return  "N(0,1)";
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", statisticPanel());
		thePanel.add("Center", zDistnPanel(data));
		
		return thePanel;
	}
	
	protected XPanel statisticPanel() {
		String statLetter = getTestStatLetter();
		testStatExpression = new ExpressionResultPanel(null, 2, 50, statLetter + " =", 6,
																								ExpressionResultPanel.HORIZONTAL, this);
		testStatExpression.setResultDecimals(kZDecimals);
		registerStatusItem("testStatistic", testStatExpression);
		return testStatExpression;
	}
	
	private XPanel zDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis zAxis = new HorizAxis(this);
			zAxis.readNumLabels(kZAxisInfo);
			zAxis.setAxisName(getDistnName());
		thePanel.add("Bottom", zAxis);
		
			ContinDistnLookupView zView = new ContinDistnLookupView(data, this, zAxis, "distn", true);
			zView.lockBackground(Color.white);
			zView.setDragEnabled(false);
		thePanel.add("Center", zView);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		NumValue one = new NumValue(1, kZDecimals);
		testStatExpression.showAnswer(one, null);
		
		data.variableChanged("distn");		//	to get the distribution lookup panel to reset the heights of the pdfs
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		data.setSelection("distn", -1.0, 1.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all 3 steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		insertPValueInstructions(messagePanel);
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertPValueInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("#bullet#  Type an expression for the observed mean's " + getTestStatName());
		messagePanel.insertText(", click ");
		messagePanel.insertBoldText("Calculate");
		messagePanel.insertText(", then use this test statistic to find the p-value for the test. (Use the function sqrt() to find a square root.)\n");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		messagePanel.insertRedHeading("p-value is wrong!\n");
		
		NumValue zAttempt = getTestStat();
		if (zAttempt == null) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The " + getTestStatLetter() + " test statistic has not been found and the p-value for the test is incorrect.");
			return;
		}
				
		int n = getSampleSize();
		double mean = getObservedMean().toDouble();
		
		double zCorrect = (mean - getNullMean().toDouble()) / getSd().toDouble() * Math.sqrt(n);
		
		if (Math.abs(zCorrect - zAttempt.toDouble()) < kMaxZError) {
			messagePanel.insertText("(You have correctly specified the hypotheses and found the " + getTestStatLetter() + " test statistic.)\n");
			messagePanel.insertText("The p-value is found from a tail probability of the " + getShortDistnName() + " distribution but you have not evaluated it correctly.");
		}
		else {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The " + getTestStatLetter() + " test statistic has not been correcty evaluated.");
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		NumValue nullMean = getNullMean();
		int n = getSampleSize();
		NumValue mean = getObservedMean();
		messagePanel.insertText("The test statistic is " + getTestStatLetter() + " = (" + getObservedMean()
									+ " - #mu#) / (" + getSdString() + "/#sqrt#n) where n = " + n + ", " + getSdString() + " = " + getSd()
									+ " and #mu# = " + nullMean + ". If H#sub0# is true, " + getTestStatLetter()
									+ " has a " + getShortDistnName() + " distribution. ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("less than or equal to");
				messagePanel.insertText(" this " + getTestStatLetter() + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("greater than or equal to");
				messagePanel.insertText(" this " + getTestStatLetter() + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("at least as extreme as");
				messagePanel.insertText(" this " + getTestStatLetter() + ". It is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the probability of being ");
				messagePanel.insertText((cumulativeProbability(mean.toDouble()) < 0.5) ? "#le# " : "#ge# " + getTestStatLetter() + ".");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double mean) {
		NumValue nullMean = getNullMean();
		int n = getSampleSize();
		NumValue sigma = getSd();
		
		return NormalTable.cumulative((mean - nullMean.toDouble()) * Math.sqrt(n) / sigma.toDouble());
	}
	
	protected void showCorrectWorking() {
		NumValue nullMean = getNullMean();
		int n = getSampleSize();
		NumValue sigma = getSd();
		NumValue mean = getObservedMean();
			
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		String expressionString = "(" + mean + " - " + nullMean + ") / (" + sigma + "/ sqrt(" + n + "))";
		testStatExpression.showAnswer(null, expressionString);
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		Value testStat = getTestStat();
		double absStat = Double.POSITIVE_INFINITY;
		if (testStat instanceof NumValue)
			absStat = Math.abs(((NumValue)testStat).toDouble());
		
		data.setSelection("distn", -absStat, absStat);
		
		if (showingCorrectAnswer)						//	don't reset answering button highlight when showing correct answer
			return false;
		else
			return super.noteChangedWorking();
	}
}