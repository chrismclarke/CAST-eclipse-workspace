package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;

import contin.*;


public class Chi2TestApplet extends ExerciseApplet {
	static final private int NO_EVIDENCE = 0;
	static final private int WEAK_EVIDENCE = 1;
	static final private int MODERATE_EVIDENCE = 2;
	static final private int STRONG_EVIDENCE = 3;
	static final private int EXCEPTIONAL_EVIDENCE = 4;
	
	static final private String[] kStrengthText = {"no", "weak", "moderately strong", "strong", "exceptionally strong"};
	
	static final private int kNoOfAltHypoth = 2;
	
	static final private NumValue kOneValue = new NumValue(1, 0);
	
	private RandomMultinomial generator;
	private SummaryDataSet summaryData;
	
	private ObsExpTableView continTable;
	private Chi2ValueView chi2ValueView;
	private Chi2PValueView chi2PValueView;
	
	private XChoice strengthChoice;
	private XChoice altHypothesisChoice;
	private int[] hypothPermutation;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
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
		registerParameter("yName", "string");
		registerParameter("xName", "string");
		registerParameter("yLabels", "string");
		registerParameter("xLabels", "string");
		registerParameter("xMarginProbs", "string");
		registerParameter("yConditProbs", "string");
		registerParameter("n", "int");
		registerParameter("maxExpected", "const");
		registerParameter("maxChi2", "const");
		registerParameter("dependentText", "string");
		registerParameter("independentText", "string");
	}
	
	private String getYName() {
		return getStringParam("yName");
	}
	
	private String getXName() {
		return getStringParam("xName");
	}
	
	private Value[] getYLabels() {
		return underscoreToSpaces(getStringParam("yLabels"));
	}
	
	private Value[] getXLabels() {
		return underscoreToSpaces(getStringParam("xLabels"));
	}
	
	private Value[] underscoreToSpaces(String labelString) {
		StringTokenizer st = new StringTokenizer(labelString);
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String oneLabelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(oneLabelString);
		}
		
		return label;
	}
	
	private int getNX() {
		StringTokenizer st = new StringTokenizer(getStringParam("xMarginProbs"));
		return st.countTokens();
	}
	
	private double[] getXMarginalProbs() {
		StringTokenizer st = new StringTokenizer(getStringParam("xMarginProbs"));
		int nx = st.countTokens();
		double p[] = new double[nx];
		for (int i=0 ; i<nx ; i++)
			p[i] = Double.parseDouble(st.nextToken());
		return p;
	}
	
	private double[][] getYConditProbs() {
		int nx = getNX();
		StringTokenizer st = new StringTokenizer(getStringParam("yConditProbs"));
		int ny = st.countTokens() / nx;
		
		double[][] conditProb = new double[nx][ny];
		for (int i=0 ; i<nx ; i++)
			for (int j=0 ; j<ny ; j++)
				conditProb[i][j] = Double.parseDouble(st.nextToken());
		return conditProb;
	}
	
	private int getN() {
		return getIntParam("n");
	}
	
	public NumValue getMaxExpected() {
		return getNumValueParam("maxExpected");
	}
	
	public NumValue getMaxChi2() {
		return getNumValueParam("maxChi2");
	}
	
	private String getAltHypothesisString(int index) {
		String s = (index == 0) ? getStringParam("dependentText")
																		: getStringParam("independentText");
//		s = s.replaceAll("#means are equal#", getEqualMeansText());
//		s = s.replaceAll("#means are different#", getDifferentMeansText());
		return s;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		CatVariable xVar = new CatVariable("X");
		data.addVariable("x", xVar);
		
		double p0[] = {1.0};
		generator = new RandomMultinomial(1, p0);
		BiCatSampleVariable yVar = new BiCatSampleVariable("Y", generator,
																										Variable.USES_REPEATS, data, "x");
		data.addVariable("y", yVar);
		
		summaryData = new SummaryDataSet(data, "y");
		
		return data;
	}
	
	protected void setDataForQuestion() {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		xVar.name = getXName();
		xVar.setLabels(getXLabels());
		int nx = xVar.noOfCategories();
		
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getYName();
		yVar.setLabels(getYLabels());
		int ny = yVar.noOfCategories();
		generator.setSampleSize(getN());
		double p[] = new double[nx * ny];
		double[][] yConditXProb = getYConditProbs();
		double[] xMarginalProb = getXMarginalProbs();
		for (int i=0 ; i< nx ; i++)
			for (int j=0 ; j<ny ; j++)
				p[i * ny + j] = xMarginalProb[i] * yConditXProb[i][j];
		generator.setProbs(p);
		
		summaryData.takeSample();
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", dataPanel(data));
		thePanel.add("Center", conclusionPanel(data));
		
		return thePanel;
	}
	
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 20));
		
//			continTable = new DataJointView(data, this, "y", "x", -1);
			continTable = new ObsExpTableView(data, this, "y", "x", getMaxExpected());
		thePanel.add("Center", continTable);
		
			XPanel statisticPanel = new XPanel();
			statisticPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
			
				chi2ValueView = new Chi2ValueView(data, this, continTable, Chi2ValueView.OVER_EXPECTED, kOneValue);
			statisticPanel.add(chi2ValueView);
			
				chi2PValueView = new Chi2PValueView(data, this, continTable);
			statisticPanel.add(chi2PValueView);
		
		thePanel.add("South", statisticPanel);
		
		return thePanel;
	}
	
	private XPanel conclusionPanel(DataSet data) {
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
		continTable.changedCategoryLabels();
		
		chi2ValueView.setMaxValue(getMaxChi2());
		chi2ValueView.invalidate();
		
		chi2PValueView.repaint();
		
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
				messagePanel.insertText("Use the popup menus to specify the correct conclusion from the test.\n");
				messagePanel.insertRedText("(The red values in the table are the expected cell counts, assuming independence.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				insertHypothesisAdvice(messagePanel);
				messagePanel.insertText("\nA p-value of " + getPValue() + " can be best interpreted as giving ");
				messagePanel.insertRedText(kStrengthText[getPValueType()] + " evidence");
				messagePanel.insertText(" of this.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have given the correct conclusion.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
/*
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
*/
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
		messagePanel.insertText("The p-value always describes evidence against the null hypothesis, and this null hypothesis is that "
								+ getYName() + " and " + getXName() + " are independent. The conclusion is therefore about the strength of evidence for ");
		messagePanel.insertRedText("these variables being related to each other");
		messagePanel.insertText(".");
	}
	
	
	protected int getMessageHeight() {
		return 150;
	}
	
//-----------------------------------------------------------
	
	
	private NumValue getPValue() {
		double pValue = chi2PValueView.getValue();
		int decimals = chi2PValueView.getDecimals();
		double factor = 1.0;
		for (int i=0 ; i<decimals ; i++) {
			pValue *= 10;
			factor /= 10;
		}
		return new NumValue(Math.rint(pValue) * factor, decimals);
	}
	
	private int getPValueType() {
		double pValue = getPValue().toDouble();
		if (pValue >= 0.1)
			return NO_EVIDENCE;
		else if (pValue >= 0.05)
			return WEAK_EVIDENCE;
		else if (pValue >= 0.01)
			return MODERATE_EVIDENCE;
		else if (pValue >= 0.001)
			return STRONG_EVIDENCE;
		else 
			return EXCEPTIONAL_EVIDENCE;
	}
	
	protected int assessAnswer() {
		int selectedIndex = altHypothesisChoice.getSelectedIndex();
		int selectedAltIndex = hypothPermutation[selectedIndex];
		if (selectedAltIndex != 0)
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
			if (hypothPermutation[i] == 0)
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