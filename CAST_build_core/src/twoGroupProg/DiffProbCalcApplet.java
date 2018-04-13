package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import distn.*;
import formula.*;
import imageGroups.*;


import normal.*;
import twoGroup.*;


public class DiffProbCalcApplet extends QuestionApplet {
	static final private String MAX_SD_PARAM = "maxSD";
	
	static final private String BELOW = "below";
	static final private String ABOVE = "above";
	static final private String BETWEEN = "between";
	
	static final private String DIFFERENCE = "difference";
//	static final private String SUM = "sum";
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private String dMeanString, dSDString, dString, d2String;
	private NumValue z, z2;
	
	private boolean differenceNotSum = true;
	
	protected void loadParameterImages() {
		GroupsEqualsImages.loadGroups(this);
	}
	
	protected boolean testNotProb() {
		return false;
	}
	
	protected void addQuestionFields(QuestionPanel question, StringTokenizer st) {
		differenceNotSum = DIFFERENCE.equals(st.nextToken());
		
		XLabel mu1Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		mu1Edit.setFont(questionFont);
		mu1Edit.setForeground(Color.blue);
		question.add(QuestionLayout.MU1, mu1Edit);
		
		XLabel sig1Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		sig1Edit.setFont(questionFont);
		sig1Edit.setForeground(Color.red);
		question.add(QuestionLayout.SIGMA1, sig1Edit);
		
		XLabel mu2Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		mu2Edit.setFont(questionFont);
		mu2Edit.setForeground(Color.blue);
		question.add(QuestionLayout.MU2, mu2Edit);
		
		XLabel sig2Edit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		sig2Edit.setFont(questionFont);
		sig2Edit.setForeground(Color.red);
		question.add(QuestionLayout.SIGMA2, sig2Edit);
		
		XLabel dEdit = new XLabel(st.nextToken(), XLabel.LEFT, this);
		dEdit.setFont(questionFont);
		question.add(QuestionLayout.D, dEdit);
		
		XLabel d2Edit = new XLabel(st.hasMoreTokens() ? st.nextToken() : "", XLabel.LEFT, this);
		d2Edit.setFont(questionFont);
		question.add(QuestionLayout.D2, d2Edit);
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
			topPanel.add(new DiffSDCalcPanel(new NumValue(getParameter(MAX_SD_PARAM)), bigGreenContext));
			
			topPanel.add(new ZCalcPanel(bigGreenContext));
		
		thePanel.add("North", topPanel);
		
		thePanel.add("Center", new DistnLookupPanel(DistnLookupPanel.Z_DISTN, this,
																								getBackground()));
		
		return thePanel;
	}
	
	protected String[] getAnswerStrings() {
		String answerString[] = new String[5];
		
		String zCalcString;
		if (tailString.equals(BETWEEN))
			zCalcString = getZString("Z1", dString, z) + " and " + getZString("Z2", d2String, z2);
		else
			zCalcString = getZString("Z", dString, z);
		
		String errorAdvice;
		if (tailString.equals(BELOW))
			errorAdvice = "Find z = (d - mean(d)) / sd(d). The probability is the area to the left of z on the diagram.";
		else if (tailString.equals(ABOVE))
			errorAdvice = "Find z = (d - mean(d)) / sd(d). The probability is the area to the right of z on the diagram (1.0 minus the area to the left of z).";
		else
			errorAdvice = "Find z1 = (d1 - mean(d)) / sd(d) and z2 = (d2 - mean(d)) / sd(d). The probability is the difference between the areas to the left of z1 and z2.";
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the probability to within "
																		+ approxString + " of the correct value.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!  " + zCalcString
										+ ", so the exact probability is " + answer.toString() + ".";
		answerString[LinkedAnswerEditPanel.WRONG] = "Wrong. " + errorAdvice;
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!  " + zCalcString
										+ ", so the exact probability is " + answer.toString() + ".";
		return answerString;
	}
	
	private String getZString(String zName, String xString, NumValue z) {
		StringBuffer sb = new StringBuffer(50);
		sb.append(zName);
		sb.append(" = (");
		sb.append(xString);
		sb.append(" - ");
		sb.append(dMeanString);
		sb.append(") / ");
		sb.append(dSDString);
		sb.append(" = ");
		sb.append(z.toString());
		return sb.toString();
	}
	
	protected HypothesisTest findTestInfo(String tailString, String[] paramString) {
		return null;
	}
	
	protected double getAccuracy(double slopValue) {
		return slopValue;
	}
	
	protected String[] getNewValueStrings(StringTokenizer st) {
		differenceNotSum = DIFFERENCE.equals(st.nextToken());
		
		String mu1String = nextRandomValue(st);
		String sig1String = nextRandomValue(st);
		String mu2String = nextRandomValue(st);
		String sig2String = nextRandomValue(st);
		
		double mu1 = Double.parseDouble(mu1String);
		NumValue sig1Value = new NumValue(sig1String);
		double sd1 = sig1Value.toDouble();
		
		double mu2 = Double.parseDouble(mu2String);
		NumValue sig2Value = new NumValue(sig2String);
		double sd2 = sig2Value.toDouble();
		
		double dMean = differenceNotSum ? (mu2 - mu1) : (mu1 + mu2);
		double dSD = Math.sqrt(sd1 * sd1 + sd2 * sd2);
		
		double d = dMean + (generator.nextDouble() * 5.0 - 2.5) * dSD;
		String dString = new NumValue(d, sig1Value.decimals).toString();
		
		String d2String = null;
		if (tailString.equals(BETWEEN))
			d2String = new NumValue(d + generator.nextDouble() * 4.0 * dSD, sig1Value.decimals).toString();
		
		String valueString[] = {null, null, null, null, null, null, null, null,
										mu1String, mu2String, sig1String, sig2String, dString, d2String};
		return valueString;
	}
	
	protected void setupAnswer() {
		NumValue mu1 = new NumValue(question.getValueString(QuestionLayout.MU1));
		NumValue sd1 = new NumValue(question.getValueString(QuestionLayout.SIGMA1));
		NumValue mu2 = new NumValue(question.getValueString(QuestionLayout.MU2));
		NumValue sd2 = new NumValue(question.getValueString(QuestionLayout.SIGMA2));
		
		double dMean = differenceNotSum ? (mu2.toDouble() - mu1.toDouble()) : (mu2.toDouble() + mu1.toDouble());
		double dSD = Math.sqrt(sd1.toDouble() * sd1.toDouble() + sd2.toDouble() * sd2.toDouble());
		
		dMeanString = new NumValue(dMean, Math.max(mu1.decimals, mu2.decimals)).toString();
		dSDString = new NumValue(dSD, Math.max(sd1.decimals, sd2.decimals) + 1).toString();
		
		dString = question.getValueString(QuestionLayout.D);
		double d = Double.parseDouble(dString);
		z = new NumValue((d - dMean) / dSD, 3);
		
		d2String = null;
		if (tailString.equals(BETWEEN)) {
			d2String = question.getValueString(QuestionLayout.D2);
			double d2 = Double.parseDouble(d2String);
			z2 = new NumValue((d2 - dMean) / dSD, 3);
		}
		
		double prob = 0.0;
		if (tailString.equals(BELOW))
			prob = NormalDistnVariable.stdCumProb(z.toDouble());
		else if (tailString.equals(ABOVE))
			prob = 1.0 - NormalDistnVariable.stdCumProb(z.toDouble());
		else
			prob = NormalDistnVariable.stdCumProb(z2.toDouble()) - NormalDistnVariable.stdCumProb(z.toDouble());
		
		answer = new NumValue(prob, 3);
	}
}