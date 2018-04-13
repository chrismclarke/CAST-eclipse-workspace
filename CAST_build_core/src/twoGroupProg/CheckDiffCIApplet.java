package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import axis.*;
import distn.*;
import models.*;
import formula.*;
import imageGroups.*;


import inference.*;
import linMod.*;
import twoGroup.*;


public class CheckDiffCIApplet extends MultiCheckApplet {
	static final protected String ACCURACY_PARAM = "accuracy";
	
	static final private Color kGreenColor = new Color(0x006600);
	static final private Color kAlternateBackground = new Color(0xDDDDEE);
	
	private GroupsDataSet data;
	private AxisChoice yAxisInfo = new AxisChoice(null, 0, AxisChoice.VERTICAL);
	private NumValue maxWorkingVal = new NumValue("999.999");
	
	private Random generator = new Random();
	
	private double accuracyProportion;			//		accuracy < 1.0
	
	public void setupApplet() {
		GroupsEqualsImages.loadGroups(this);
		super.setupApplet();
	}
	
	protected DataSet createData() {
		data = new GroupsDataSet(this);
		return data;
	}
	
	protected void readAccuracy() {
		accuracyProportion = Double.parseDouble(getParameter(ACCURACY_PARAM));
	}
	
	protected String valueLabel() {
		double theDiff = data.getMean(1) - data.getMean(0);
		String diffString = new NumValue(theDiff, correctDecimals).toString();
		return "95% CI is " + diffString + " \u00b1 ";
	}
	
	protected void changeRandomParams(DataSet data) {
		axisGenerator.changeRandomAxis(yAxisInfo, null, this);
		
		double axisMin = yAxisInfo.axis.minOnAxis;
		double axisMax = yAxisInfo.axis.maxOnAxis;
		double axisRange = axisMax - axisMin;
		
		double minMean = axisMin + axisRange * 0.15;
		double maxMean = axisMax - axisRange * 0.15;
		
		double mean1 = minMean + generator.nextDouble() * (maxMean - minMean);
		double mean2 = minMean + generator.nextDouble() * (maxMean - minMean);
		
		double sd1 = Math.min(mean1 - axisMin, axisMax - mean1)
																* (2.0 + generator.nextDouble()) * 0.1;
		double sd2 = Math.min(mean2 - axisMin, axisMax - mean2)
																* (2.0 + generator.nextDouble()) * 0.1;
		
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
	}
	
	protected NumValue evalAnswer(DataSet data, int correctDecimals) {
		GroupsDataSet anovaData = (GroupsDataSet)data;
		double sd1 = anovaData.getSD(0);
		double sd2 = anovaData.getSD(1);
		int n1 = anovaData.getN(0);
		int n2 = anovaData.getN(1);
		
		double diffSD = Math.sqrt(sd1 * sd1 / n1 + sd2 * sd2 / n2);
		double t = TTable.quantile(0.975, Math.min(n1, n2) - 1);
		return new NumValue(t * diffSD, correctDecimals);
	}
	
	protected double evalExactSlop(NumValue answer, DataSet data) {
		return 0.0005 * answer.toDouble();
	}
	
	protected double evalApproxSlop(NumValue answer, DataSet data) {
		return (1.0 - accuracyProportion) * answer.toDouble();
	}
	
	protected String[] answerStrings(NumValue answer) {
		String[] answerString = new String[5];
		answerString[LinkedAnswerEditPanel.NONE] = "Evaluate a 95% confidence interval for mean2 - mean1.";
		answerString[LinkedAnswerEditPanel.EXACT] = "Correct!!";
		answerString[LinkedAnswerEditPanel.UNKNOWN] = "You have not typed a number.";
		answerString[LinkedAnswerEditPanel.WRONG] = "Your answer is not close enough. Find sd(mean1) and sd(mean2); sd(difference) is the root of the sum of their squares. Use min(n1, n2) - 1 df to find t.";
		answerString[LinkedAnswerEditPanel.CLOSE] = "Close enough!! The exact value is plus or minus "
																					+ answer.toString() + ".";
		return answerString;
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("Center", leftPanel(data));
		thePanel.add("East", rightPanel(data));
		
		return thePanel;
	}
	
	private XPanel leftPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dotPlotPanel(data));
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel viewPanel = new XPanel();
		viewPanel.setLayout(new AxisLayout());
		
			VertAxis yAxis = (VertAxis)yAxisInfo.axis;
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
	
	private XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 10));
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 10));
		
		thePanel.add(new GroupSummaryPanel(this, (GroupsDataSet)data, 0,
																					GroupSummaryPanel.HORIZONTAL));
		thePanel.add(new GroupSummaryPanel(this, (GroupsDataSet)data, 1,
																					GroupSummaryPanel.HORIZONTAL));
		
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
			SDCalcPanel sdCalc = new SDCalcPanel(maxWorkingVal, bigGreenContext);
			sdCalc.lockBackground(kAlternateBackground);
		thePanel.add(sdCalc);
		
			DiffSDCalcPanel sdDiff = new DiffSDCalcPanel(DiffSDCalcPanel.MEANS, maxWorkingVal, bigGreenContext);
		thePanel.add(sdDiff);
		
			XPanel tLookupPanel = new InsetPanel(0, 3);
			tLookupPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				TLookupPanel tLookup = new TLookupPanel(this, null);
			tLookupPanel.add(tLookup);
			
			tLookupPanel.lockBackground(kAlternateBackground);
		thePanel.add(tLookupPanel);
		
		thePanel.add(new PlusMinusCalcPanel(maxWorkingVal, bigGreenContext));
		
		return thePanel;
	}
}