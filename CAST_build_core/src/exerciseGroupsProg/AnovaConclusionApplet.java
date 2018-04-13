package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;
import models.*;


public class AnovaConclusionApplet extends CoreAnovaApplet {
	static final private String NONE_EQUAL_PARAM = "noneEqual";
	static final private String AT_LEAST_TWO_DIFF_PARAM = "atLeastTwoDiff";
	static final private String ALL_EQUAL_PARAM = "allEqual";
	static final private String AT_LEAST_TWO_EQUAL_PARAM = "atLeastTwoEqual";
	
	static final private int NONE_EQUAL = 0;
	static final private int AT_LEAST_TWO_DIFF = 1;
	static final private int ALL_EQUAL = 2;
//	static final private int AT_LEAST_TWO_EQUAL = 3;
	
	static final private int NO_EVIDENCE = 0;
	static final private int WEAK_EVIDENCE = 1;
	static final private int MODERATE_EVIDENCE = 2;
	static final private int STRONG_EVIDENCE = 3;
	static final private int EXCEPTIONAL_EVIDENCE = 4;
	
	static final private String[] kStrengthText = {"no", "weak", "moderately strong", "strong", "exceptionally strong"};
	static final private int kNoOfAltHypoth = 4;
	
	private XChoice strengthChoice;
	private XChoice altHypothesisChoice;
	private int[] hypothPermutation;
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("equalMeans", "string");
		registerParameter("differentMeans", "string");
	}
	
	private String getEqualMeansText() {
		return getStringParam("equalMeans");
	}
	
	private String getDifferentMeansText() {
		return getStringParam("differentMeans");
	}
	
	private String getAltHypothesisString(int index) {
		String s = (index == NONE_EQUAL) ? getParameter(NONE_EQUAL_PARAM)
								: (index == AT_LEAST_TWO_DIFF) ? getParameter(AT_LEAST_TWO_DIFF_PARAM)
								: (index == ALL_EQUAL) ? getParameter(ALL_EQUAL_PARAM)
								: getParameter(AT_LEAST_TWO_EQUAL_PARAM);
		s = s.replaceAll("#means are equal#", getEqualMeansText());
		return s.replaceAll("#means are different#", getDifferentMeansText());
	}
	
	
//-----------------------------------------------------------
	
	
	protected Color getAnovaTableBackground() {
		return null;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", getAnovaTable(data));
		thePanel.add("Center", getConclusionPanel(data));
		
		return thePanel;
	}
	
	
	private XPanel getConclusionPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.CENTER, 0));
			
			XPanel innerPanel = new InsetPanel(10, 5);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 10));
				
				XLabel conclusionText = new XLabel("Conclusion", XLabel.LEFT, this);
				conclusionText.setFont(getBigBoldFont());
			innerPanel.add(conclusionText);
				
				XPanel menusPanel = new InsetPanel(10, 5);
				menusPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.CENTER, 0));
					strengthChoice = new XChoice("There is", "evidence", XChoice.HORIZONTAL, this);
					for (int i=0 ; i<kStrengthText.length ; i++)
						strengthChoice.addItem(kStrengthText[i]);
					registerStatusItem("strengthChoice", strengthChoice);
				menusPanel.add(strengthChoice);
				
					hypothPermutation = new int[kNoOfAltHypoth];
					altHypothesisChoice = new XChoice("that", XChoice.HORIZONTAL, this);
					registerStatusItem("altHypothesisChoice", altHypothesisChoice);
				menusPanel.add(altHypothesisChoice);
				
			innerPanel.add(menusPanel);
			innerPanel.lockBackground(kAnswerBackground);
			
		thePanel.add(innerPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		strengthChoice.select(0);
		
		permute(hypothPermutation);
		altHypothesisChoice.clearItems();
		for (int i=0 ; i<kNoOfAltHypoth ; i++)
			altHypothesisChoice.addItem(getAltHypothesisString(hypothPermutation[i]));
		altHypothesisChoice.select(0);
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the popup menus to specify the correct conclusion from the test.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				insertHypothesisAdvice(messagePanel);
				messagePanel.insertText("\nA p-value of " + new NumValue(getPValue(), 4) + " can be best interpreted as giving ");
				messagePanel.insertRedText(kStrengthText[getPValueType()] + " evidence");
				messagePanel.insertText(" of this.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have given the correct conclusion.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				int selectedIndex = altHypothesisChoice.getSelectedIndex();
				int selectedAltIndex = hypothPermutation[selectedIndex];
				if (selectedAltIndex != 1)
					insertHypothesisAdvice(messagePanel);
				
				int correctConclusion = getPValueType();
				int attempt = strengthChoice.getSelectedIndex();
				if (correctConclusion != attempt) {
					if (selectedAltIndex != 1)
						messagePanel.insertText("\n");
					messagePanel.insertText("The smaller the p-value, the stronger the evidence for different group means.");
				}
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close enough!\n");
				messagePanel.insertText("Your conclusion is close enough to the correct one, though it might have been better to express the evidence for different means as being ");
				messagePanel.insertRedText(kStrengthText[getPValueType()]);
				messagePanel.insertText(".");
				break;
		}
	}
	
	private void insertHypothesisAdvice(MessagePanel messagePanel) {
		messagePanel.insertText("The p-value always describes evidence against the null hypothesis, and this null hypothesis is that all of "
								+ getEqualMeansText() + ". The conclusion is therefore about the strength of evidence for ");
		messagePanel.insertRedText("at least two of these group means being different");
		messagePanel.insertText(".");
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	
	private double getPValue() {
		SsqVariable explainedSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[1]);
		SsqVariable residSsqVar = (SsqVariable)summaryData.getVariable(BasicComponentVariable.kComponentKey[2]);
		double explainedSsq = explainedSsqVar.doubleValueAt(0);
		int explainedDf = explainedSsqVar.getDF();
		double residSsq = residSsqVar.doubleValueAt(0);
		int residDf = residSsqVar.getDF();
		
		double f = (explainedSsq / explainedDf) / (residSsq / residDf);
		
		return 1 - FTable.cumulative(f, explainedDf, residDf);
	}
	
	private int getPValueType() {
		double pValue = getPValue();
		
		return (pValue > 0.1) ? NO_EVIDENCE : (pValue > 0.05) ? WEAK_EVIDENCE : (pValue > 0.01) ? MODERATE_EVIDENCE : (pValue > 0.002) ? STRONG_EVIDENCE : EXCEPTIONAL_EVIDENCE;
	}
	
	protected int assessAnswer() {
		int selectedIndex = altHypothesisChoice.getSelectedIndex();
		int selectedAltIndex = hypothPermutation[selectedIndex];
		if (selectedAltIndex != 1)
			return ANS_WRONG;
		
		int correctConclusion = getPValueType();
		int attempt = strengthChoice.getSelectedIndex();
		
		int result = ANS_WRONG;
		if (correctConclusion == attempt)
			result = ANS_CORRECT;
		else if (Math.abs(correctConclusion - attempt) <= 1)
			result = ANS_CLOSE;
		
		return result;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		int correctConclusion = getPValueType();
		strengthChoice.select(correctConclusion);
		
		for (int i=0 ; i<kNoOfAltHypoth ; i++)
			if (hypothPermutation[i] == 1)
				altHypothesisChoice.select(i);
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT || assessAnswer() == ANS_CLOSE) ? 1 : 0;
	}
	
	
//-----------------------------------------------------------

	
	private boolean localAction(Object target) {
		if (target == altHypothesisChoice || target == strengthChoice) {
			noteChangedWorking();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (localAction(evt.target))
			return true;
		else
			return super.action(evt, what);
	}
	
}