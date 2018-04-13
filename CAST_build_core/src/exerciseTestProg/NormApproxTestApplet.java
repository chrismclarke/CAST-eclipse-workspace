package exerciseTestProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import exercise2.*;
import expression.*;

import exerciseNormal.JdistnAreaLookup.*;


public class NormApproxTestApplet extends CoreBinomialApplet {
	static final private String kZAxisInfo = "-4 4 -4 1";
	
	static final private int kZDecimals = 4;
	static final private double kMaxZError = 0.00001;
	
//	private NormalLookupPanel normalLookupPanel;
	private ExpressionResultPanel zExpression;
	
	private boolean showingCorrectAnswer = false;
	
	
//-----------------------------------------------------------
	
	private NumValue getExpressionZ() {
		return zExpression.getAttempt();
	}
	
	public String getAxisInfo() {
		return null;
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable normalDistn = new NormalDistnVariable("Standard normal, Z");	//	default N(0,1)
		data.addVariable("normal", normalDistn);
		
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
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			zExpression = new ExpressionResultPanel("z: ", 2, 50, "z =", 6,
																									ExpressionResultPanel.HORIZONTAL, this);
			zExpression.setResultDecimals(kZDecimals);
			registerStatusItem("z", zExpression);
		thePanel.add("North", zExpression);
		
		thePanel.add("Center", zDistnPanel(data));
		
		return thePanel;
	}
	
	private XPanel zDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis zAxis = new HorizAxis(this);
			zAxis.readNumLabels(kZAxisInfo);
			zAxis.setAxisName("Standard normal, Z");
		thePanel.add("Bottom", zAxis);
		
			ContinDistnLookupView zView = new ContinDistnLookupView(data, this, zAxis, "normal", true);
			zView.lockBackground(Color.white);
			zView.setDragEnabled(false);
		thePanel.add("Center", zView);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		NumValue one = new NumValue(1, kZDecimals);
		zExpression.showAnswer(one, null);
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		data.setSelection("normal", -1.0, 1.0);
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all 3 steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Type an expression for the observed count's z-score");
		if (hasOption("continuityCorrection"))
			messagePanel.insertText(" (using a continuity correction)");
		messagePanel.insertText(", click ");
		messagePanel.insertBoldText("Calculate");
		messagePanel.insertText(", then use z to find the p-value for the test.\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		messagePanel.insertRedHeading("p-value is wrong!\n");
		
		NumValue zAttempt = getExpressionZ();
		if (zAttempt == null) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The cutoff z value has not been found and the p-value for the test is incorrect.");
			return;
		}
				
		int n = getNTrials();
		double p = getPSuccess().toDouble();
		NumValue cutoffWithCorrection = getCutoff(true);
		NumValue cutoffWithoutCorrection = getCutoff(false);
		
		double zCorrect = (cutoffWithCorrection.toDouble() - n * p) / Math.sqrt(n * p * (1 - p));
		double zClose = (cutoffWithoutCorrection.toDouble() - n * p) / Math.sqrt(n * p * (1 - p));
		if (!hasOption("continuityCorrection")) {
			double zTemp = zCorrect;
			zCorrect = zClose;
			zClose = zTemp;
		}
		
		if (Math.abs(zCorrect - zAttempt.toDouble()) < kMaxZError) {
			messagePanel.insertText("(You have correctly specified the hypotheses and found the z-value for the cutoff.)\n");
			messagePanel.insertText("The p-value is found from a tail probability of the normal distribution but you have not evaluated it correctly.");
		}
		else if (Math.abs(zClose - zAttempt.toDouble()) < kMaxZError) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			if (hasOption("continuityCorrection"))
				messagePanel.insertText("You do not seem to have used a continuity correction to find the z cutoff.");
			else
				messagePanel.insertText("You seem to have used a continuity correction to find the z cutoff but the question asks you not to.");
		}
		else {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The cutoff z value has not been correcty evaluated.");
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		NumValue pSuccess = getPSuccess();
		int nTrials = getNTrials();
		int nSuccess = getNSuccess();
		String successName = getSuccessName();
		String successesName = getSuccessesName();
		messagePanel.insertText("There are n = " + nTrials + " " + getTrialsName()
														+ " and the null hypotheses value for the probability of " + successName
														+ " is #pi# = " + pSuccess + " so the number of " + successesName
														+ " is approx normal with mean n#pi# and standard deviation #sqrt#(n#pi#(1-#pi#)).\n");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The p-value is the probability of ");
				messagePanel.insertBoldText("less than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The p-value is the probability of ");
				messagePanel.insertBoldText("greater than or equal to ");
				messagePanel.insertText(nSuccess + " " + successesName + ".");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The p-value is the probability that the number of " + successesName + " is ");
				messagePanel.insertBoldText("at least as extreme as ");
				messagePanel.insertText(nSuccess + ". This is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the probability of ");
				messagePanel.insertText((evaluateProbability(0, nSuccess) < 0.5 ? "#le# " : "#ge# ") + nSuccess + " " + successesName + ".");
				break;
		}
		
		if (hasOption("continuityCorrection"))
			messagePanel.insertText(" To find it, the cutoff value is offset by 0.5 (the continuity correction) and translated into a z-score by subtracting the mean and dividing by the standard deviation. The p-value is a tail area of the standard normal distribution.");
		else
			messagePanel.insertText(" To find it, the cutoff value is translated into a z-score by subtracting the mean and dividing by the standard deviation. The p-value is found from the tail area of the standard normal distribution.");
	}
	
//-----------------------------------------------------------
	
	
	protected double evaluateProbability(int lowCount, int highCount) {		//	including both ends
		int n = getNTrials();
		double p = getPSuccess().toDouble();
		double mean = n * p;
		double sd = Math.sqrt(mean * (1 - p));
		
		double lowCountDouble = lowCount;
		double highCountDouble = highCount;
		if (hasOption("continuityCorrection")) {
			lowCountDouble -= 0.5;
			highCountDouble += 0.5;
		}
		
		double zLow = (lowCount <= 0) ? Double.NEGATIVE_INFINITY : (lowCountDouble - mean) / sd;
		double zHigh = (lowCount >= n) ? Double.POSITIVE_INFINITY : (highCountDouble - mean) / sd;
		
		return NormalTable.cumulative(zHigh) - NormalTable.cumulative(zLow);
	}
	
	private NumValue getCutoff(boolean withContinuityCorrection) {
		int n = getNTrials();
		int observedCount = getNSuccess();
		int tail = getTail();
		if (withContinuityCorrection) {
			double cutOffDouble = observedCount;
			if (tail == TAIL_LOW || tail == TAIL_LOW_EQ)
				cutOffDouble += 0.5;
			else if (tail == TAIL_HIGH || tail == TAIL_HIGH_EQ)
				cutOffDouble -= 0.5;
			else {
				double lowProb = evaluateProbability(0, observedCount);
				double highProb = evaluateProbability(observedCount, n);
				if (lowProb < highProb)
					cutOffDouble += 0.5;
				else
					cutOffDouble -= 0.5;
			}
			return new NumValue(cutOffDouble, 1);
		}
		else
			return new NumValue(observedCount, 0);
	}
	
	protected void showCorrectWorking() {
		int n = getNTrials();
		NumValue p = getPSuccess();
		
		NumValue cutOff = getCutoff(hasOption("continuityCorrection"));
			
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		String expressionString = "(" + cutOff + " - " + n + " * " + p + ") / sqrt(" + n + " * " + p + " * (1 - " + p + "))";
		zExpression.showAnswer(null, expressionString);
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		Value zAttempt = getExpressionZ();
		double absZ = Double.POSITIVE_INFINITY;
		if (zAttempt instanceof NumValue)
			absZ = Math.abs(((NumValue)zAttempt).toDouble());
		
		data.setSelection("normal", -absZ, absZ);
		
		if (showingCorrectAnswer)						//	don't reset answering button highlight when showing correct answer
			return false;
		else
			return super.noteChangedWorking();
	}
}