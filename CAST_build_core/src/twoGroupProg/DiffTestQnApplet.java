package twoGroupProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import qnUtils.*;
import random.*;
import models.*;
import formula.*;
import imageGroups.*;

import twoGroup.*;


public class DiffTestQnApplet extends CoreQuestionApplet {
	static final private String MAX_SUMMARY_PARAM = "maxSummary";
	static final private String ALTERNATIVE_PARAM = "alternative";
	
	static final private Color kGreenColor = new Color(0x006600);
	
	private GroupsDataSet data;
	
	private AxisGenerator axisGenerator;
	private SampleSizeGenerator sampleSizeGenerator;
	
	private AxisChoice yAxisInfo = new AxisChoice(null, 0, AxisChoice.VERTICAL);
	
	private NumValue maxSummary;
	private NumValue tValue;
	
	private HypothesisTest test;
	private HypothesisView hypotheses;
	private RandomUniform tailGenerator;
	
	public void setupApplet() {
		GroupsEqualsImages.loadGroups(this);
		maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		
		axisGenerator = new AxisGenerator(this);
		sampleSizeGenerator = new SampleSizeGenerator(this);
		initialiseGenerator();
		
		data = getData();
		
		String tailString = getParameter(ALTERNATIVE_PARAM);
		int alternativeTail = HypothesisTest.HA_NOT_EQUAL;
		if (tailString == null) {
			tailGenerator = new RandomUniform(1, 0, 2);
			alternativeTail = tailGenerator.generateOne();
		}
		else {
			if (tailString.equals("low"))
				alternativeTail = HypothesisTest.HA_LOW;
			else if (tailString.equals("high"))
				alternativeTail = HypothesisTest.HA_HIGH;
			else
				alternativeTail = HypothesisTest.HA_NOT_EQUAL;
		}
		test = new DiffHypothesisTest(data, new NumValue(0.0, 0), alternativeTail,
																DiffHypothesisTest.DIFF_MEAN_IMAGE, this);
		
		super.setupApplet();
	}
	
	protected boolean testNotProb() {
		return true;
	}
	
	protected GroupsDataSet getData() {
		GroupsDataSet anovaData = new GroupsDataSet(this);
		generateNewData(anovaData);
		return anovaData;
	}
	
	protected XPanel dataPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add("Center", dotPlotPanel());
			thePanel.add("East", summaryPanel());
		
		thePanel.add("Center", topPanel);
		thePanel.add("South", tWorkingPanel());
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel() {
		XPanel viewPanel = new XPanel();
		viewPanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = (VertAxis)yAxisInfo.axis;
			yAxis.lockBackground(getBackground());			//		Seems to have white background otherwise
		viewPanel.add("Left", yAxis);
		
			HorizAxis theGroupAxis = new HorizAxis(this);
			CatVariable groupVariable = data.getCatVariable();
			theGroupAxis.setCatLabels(groupVariable);
		viewPanel.add("Bottom", theGroupAxis);
		
			VerticalDotView theView = new VerticalDotView(data, this, yAxis, theGroupAxis, "y", "x", null, 0.2);
			theView.setMeanDisplay(VerticalDotView.MEAN_CHANGE);
			theView.lockBackground(Color.white);
			
		viewPanel.add("Center", theView);
		
		return viewPanel;
	}
	
	private XPanel summaryPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
		thePanel.add(new GroupSummaryPanel(this, data, 0, GroupSummaryPanel.VERTICAL));
		thePanel.add(new GroupSummaryPanel(this, data, 1, GroupSummaryPanel.VERTICAL));
		return thePanel;
	}
	
	private XPanel tWorkingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
//		thePanel.add(new Separator(0.7, 3));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(20, 0));
				XPanel hypothPanel = new XPanel();
				hypothPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					hypotheses = new HypothesisView(test, HypothesisView.VERTICAL, this);
				hypothPanel.add(hypotheses);
			bottomPanel.add("West", hypothPanel);
			
				XPanel tPanel = new InsetPanel(0, 0, 8, 0);
				tPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					FormulaContext bigGreenContext = new FormulaContext(Color.green, getBigFont(), this);
					XPanel tCalc = new linMod.TCalcPanel(new NumValue(-99.999, 3), bigGreenContext);
				tPanel.add(tCalc);
				
				tPanel.lockBackground(kWorkingBackground);
			bottomPanel.add("Center", tPanel);
			
		thePanel.add(bottomPanel);
		return thePanel;
	}
	
	protected XPanel workingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel topPanel = new XPanel();
//			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				DiffMeanCalcPanel diffMean = new DiffMeanCalcPanel(maxSummary, bigContext);
			topPanel.add(diffMean);
			
				XPanel p0 = new InsetPanel(0, 5);
				p0.setLayout(new BorderLayout(0, 0));
				
					FormulaContext greenContext = new FormulaContext(kGreenColor, getBigFont(), this);
					SDCalcPanel sdCalc = new SDCalcPanel(maxSummary, greenContext);
				p0.add("Center", sdCalc);
				
				p0.lockBackground(kWorkingBackground);
			topPanel.add(p0);
			
				DiffSDCalcPanel diffSD = new DiffSDCalcPanel(DiffSDCalcPanel.MEANS, maxSummary, greenContext);
			topPanel.add(diffSD);
			
		thePanel.add("North", topPanel);
		
			XPanel p1 = new InsetPanel(5, 5);
			p1.setLayout(new BorderLayout(0, 0));
				DistnLookupPanel distn = new DistnLookupPanel(DistnLookupPanel.T_DISTN, this,
																							kWorkingBackground);
			p1.add("Center", distn);
				
			p1.lockBackground(kWorkingBackground);
		thePanel.add("Center", p1);
		
		return thePanel;
	}
	
	private void generateNewData(DataSet data) {
		axisGenerator.changeRandomAxis(yAxisInfo, null, this);
		
		double axisMin = yAxisInfo.axis.minOnAxis;
		double axisMax = yAxisInfo.axis.maxOnAxis;
		double axisRange = axisMax - axisMin;
		
		double minMean = axisMin + axisRange * 0.35;
		double maxMean = axisMax - axisRange * 0.35;
		
		double mean1 = minMean + generator.nextDouble() * (maxMean - minMean);
		double mean2 = minMean + generator.nextDouble() * (maxMean - minMean);
		
		double sd1 = Math.min(mean1 - axisMin, axisMax - mean1) * 0.33;
		double sd2 = Math.min(mean2 - axisMin, axisMax - mean2) * 0.33;
		
		GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
		yDistn.setMean(mean1, 0);
		yDistn.setSD(sd1, 0);
		yDistn.setMean(mean2, 1);
		yDistn.setSD(sd2, 1);
		
		int newSampleSize = sampleSizeGenerator.getNewSampleSize();
		int n1 = 3 + (int)Math.round((newSampleSize - 6) * generator.nextDouble());
		int n2 = newSampleSize - n1;
		
		CatVariable x = (CatVariable)data.getVariable("x");
		x.readValues(n1 + "@0 " + n2 + "@1");
		
		NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
		error.setSampleSize(newSampleSize);
		error.generateNextSample();
		((GroupsDataSet)data).resetLSEstimates();
		data.variableChanged("error");
		
		if (tailGenerator != null) {
			int alternativeTail = tailGenerator.generateOne();
			test.setTestTail(alternativeTail);
			hypotheses.repaint();
		}
	}
	
	protected void generateQuestion() {
		generateNewData(data);
		
		setupAnswer();
		
		valueEdit.reset(answer, getAccuracy(exactSlop), getAccuracy(approxSlop));
		message.changeText(getAnswerStrings());
		
		double ans = answer.toDouble();
		int conclusionOption = (ans > 0.1) ? 0
									: (ans > 0.05) ? 1
									: (ans > 0.01) ? 2 : 3;
		conclusionPanel.changeCorrectOption(conclusionOption);
		conclusionPanel.reset();
	}
	
	protected void setupAnswer() {
		tValue = new NumValue(test.evaluateStatistic(), 3);
		answer = new NumValue(test.evaluatePValue(), 4);
	}
	
	protected String[] getAnswerStrings() {
		int df = Math.min(data.getN(0), data.getN(1)) - 1;
		
		String solnString = "t = (m2 - m1) / sd(d) = " + tValue.toString()
										+ ", with " + String.valueOf(df)
										+ " degrees of freedom, and the exact tail area is "
										+ answer.toString() + ".";
		String adviceString;
		switch (test.getTestTail()) {
			case HypothesisTest.HA_LOW:
				adviceString = "The p-value is the tail area to the left of t.";
				break;
			case HypothesisTest.HA_HIGH:
				adviceString = "The p-value is the tail area to the right of t.";
				break;
			case HypothesisTest.HA_NOT_EQUAL:
			default:
				adviceString = "The p-value is the sum of the two tail areas further from zero than t.";
				break;
		}
		
		String accuracyString = (new NumValue(approxSlop * 100, 0)).toString();
		
		String answerString[] = new String[5];
		
		answerString[LinkedAnswerEditPanel.NONE] = "Find the p-value to within " + accuracyString
										+ "% of the correct value. Then select your conclusion.";
		answerString[LinkedAnswerEditPanel.EXACT] = "P-value is correct!  " + solnString;
		answerString[LinkedAnswerEditPanel.WRONG] = "P-value is wrong.  Find t = (m2 - m1) / sd(d). " + adviceString;
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "Error! You have not typed a valid number.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "P-value is close enough!  " + solnString;
		return answerString;
	}
	
	protected XPanel bottomPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
//			thePanel.add("North", new Separator(0.85, 2));
			XPanel corePanel = new XPanel();
			corePanel.setLayout(new BorderLayout());
				
				corePanel.add("North", answerEditPanel(HORIZ_ANSWER));
				corePanel.add("Center", super.bottomPanel());
			thePanel.add("Center", corePanel);
		return thePanel;
	}
}