package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;
import models.*;


import expression.*;
import linMod.*;
import exerciseEstim.*;
import twoGroup.*;
import exerciseGroups.*;


public class CiDiffMeanApplet extends CoreCiDiffApplet {
	
	private RandomNormal generator;
	
	private HorizAxis groupAxis;
	private VertAxis yAxis;
//	private VerticalDotView theView;
	private GroupsSummaryPanel summaryPanel;
	
	private SDCalcPanel sdMeanCalcTemplate = null;
	private DiffSDCalcPanel seCalcTemplate = null;
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("mean1", "const");
		registerParameter("sd1", "const");
		registerParameter("mean2", "const");
		registerParameter("sd2", "const");
		registerParameter("axis", "string");
		registerParameter("maxSampleMean", "const");
		registerParameter("maxSampleSd", "const");
	}
	
//	--------------------------------------------------
	
	private NumValue getMean1() {
		return getNumValueParam("mean1");
	}
	
	private NumValue getSd1() {
		return getNumValueParam("sd1");
	}
	
	private NumValue getMean2() {
		return getNumValueParam("mean2");
	}
	
	private NumValue getSd2() {
		return getNumValueParam("sd2");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
//	--------------------------------------------------
	
	private NumValue getMaxSampleMean() {
		return getNumValueParam("maxSampleMean");
	}
	
	private NumValue getMaxSampleSd() {
		return getNumValueParam("maxSampleSd");
	}
	
	private LabelValue getMaxGroupName() {
		String g1Name = getGroup1Name();
		String g2Name = getGroup2Name();
		return new LabelValue(g1Name.length() > g2Name.length() ? g1Name : g2Name);			// not necessarily longest when displayed
	}
	
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addCatVariable("x", "X", "0 0", "G1 G2");
		data.addVariable("model", new GroupsModelVariable("Model", data, "x"));
		
		String randomParams = "1 0.0 1.0 " + nextSeed() + " 3.0";
		generator = new RandomNormal(randomParams);
		NumSampleVariable error = new NumSampleVariable("error", generator, 10);
		error.generateNextSample();
		data.addVariable("error", error);
		
		data.addVariable("y", new ResponseVariable("Y", data, "x", "error", "model", 9));		//	decimals not used, so set at 9
				
		return data;
	}
	
//-----------------------------------------------------------

	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 4));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 10));
			
			leftPanel.add("Center", dotPlotPanel(data));
			
				summaryPanel = new GroupsSummaryPanel(data, "y", "x", this);
			leftPanel.add("South", summaryPanel);
			
			leftPanel.lockBackground(kDataBackground);
		
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 10));
			FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
			
			if (hasOption(USE_SE_TEMPLATES_OPTION)) {
					sdMeanCalcTemplate = new SDCalcPanel(getMaxSe(), bigGreenContext);
					registerStatusItem("sdMeanCalcTemplate", sdMeanCalcTemplate);
				rightPanel.add(sdMeanCalcTemplate);
				
					seCalcTemplate = new DiffSDCalcPanel(DiffSDCalcPanel.MEANS, getMaxSe(), bigGreenContext);
					registerStatusItem("seCalcTemplate", seCalcTemplate);
				rightPanel.add(seCalcTemplate);
			}
			else {
				XPanel sePanel = new XPanel();
				sePanel.setLayout(new FixedSizeLayout(250, 120));
					seExpression = new ExpressionResultPanel(null, 2, 50, "se =", 6,
																										ExpressionResultPanel.VERTICAL, this);
					seExpression.setResultDecimals(getMaxSe().decimals);
					registerStatusItem("seExpression", seExpression);
				sePanel.add(seExpression);
				rightPanel.add(sePanel);
			}
					
				tLookupPanel = new TLookupPanel(this, TLookupPanel.QUANTILE_LOOKUP, TLookupPanel.T_ONLY);
				registerStatusItem("tLookupPanel", tLookupPanel);
			rightPanel.add(tLookupPanel);
			
				tseTemplate = new PlusMinusCalcPanel(getMaxSe(), bigGreenContext);
				registerStatusItem("tseTemplate", tseTemplate);
			rightPanel.add(tseTemplate);
			
			rightPanel.lockBackground(kWorkingBackground);
			
		thePanel.add("East", rightPanel);
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel viewPanel = new InsetPanel(10, 5);
		viewPanel.setLayout(new AxisLayout());
		
			yAxis = new VertAxis(this);
		viewPanel.add("Left", yAxis);
		
			groupAxis = new HorizAxis(this);
		viewPanel.add("Bottom", groupAxis);
		
			VerticalDotView theView = new VerticalDotView(data, this, yAxis, groupAxis, "y", "x", null, 0.2);
			theView.setMeanDisplay(VerticalDotView.MEAN_LINE);
			theView.lockBackground(Color.white);
			
		viewPanel.add("Center", theView);
		
		return viewPanel;
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
	
	protected void setDisplayForQuestion() {
		yAxis.readNumLabels(getAxisInfo());
		yAxis.setAxisName(getVarName());
		yAxis.invalidate();
		
		groupAxis.setCatLabels((CatVariable)data.getVariable("x"));
		String groupName = getGroupVarName();
		if (groupName != null)
			groupAxis.setAxisName(groupName);
		groupAxis.invalidate();
		
		summaryPanel.updateForNewData(getMaxGroupName(), getMaxSampleMean(), getMaxSampleSd());
		
		if (seExpression != null) {
			NumValue one = new NumValue(1, getMaxSe().decimals);
			seExpression.showAnswer(one, null);
		}
		else {
			sdMeanCalcTemplate.changeMaxValue(getMaxSampleSd(), getMaxSe());
			sdMeanCalcTemplate.setValues(kOneValue, 1);
			
			seCalcTemplate.changeMaxValue(getMaxSe());
			seCalcTemplate.setValues(kOneValue, kOneValue);
		}
		
		super.setDisplayForQuestion();
		
		data.variableChanged("error");
	}
	
	protected boolean group2First() {
		return summaryPanel.getMean(1) > summaryPanel.getMean(0);
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		generator.setSeed(nextSeed());
	}
	
	protected void setDataForQuestion() {
		int n1 = getN1();
		double mean1 = getMean1().toDouble();
		double sd1 = getSd1().toDouble();
		int n2 = getN2();
		double mean2 = getMean2().toDouble();
		double sd2 = getSd2().toDouble();
		
		GroupsModelVariable yDistn = (GroupsModelVariable)data.getVariable("model");
		yDistn.setMean(mean1, 0);
		yDistn.setSD(sd1, 0);
		yDistn.setMean(mean2, 1);
		yDistn.setSD(sd2, 1);
		
		CatVariable x = (CatVariable)data.getVariable("x");
		x.readLabels("#" + getGroup1Name() + "# #" + getGroup2Name() + "#");
		x.readValues(n1 + "@0 " + n2 + "@1");
		
		NumSampleVariable error = (NumSampleVariable)data.getVariable("error");
		error.setSampleSize(n1 + n2);
		error.generateNextSample();
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("First find the standard error of the difference between the sample means ");
				if (seExpression != null)
					messagePanel.insertText("by typing an expression for it into the box on the top right. (Note that\"sqrt(x)\" gives the square root of a value x.)\n");
				else
					messagePanel.insertText("using the templates on the top right to help.\n");
				messagePanel.insertText("Now use the t distribution (adjusting the degrees of freedom if necessary)");
				messagePanel.insertText(" to help find a " + getCiPercent() + "% CI for the difference between the means, typing it into the answer boxes.\n");
				messagePanel.insertText("(Type a proportion into the edit box in the middle of the distribution to specify the red central area.)");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must type values for both ends of the confidence interval in the two boxes above.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Invalid!\n");
				messagePanel.insertRedText("The lower limit of the interval must be less than the upper limit.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The standard error of the difference between the means is ");
				insertSeFormula(messagePanel);
				messagePanel.insertText(".\n" + getCiPercent() + "% of the ");
				messagePanel.insertText("t distribution with " + getDf() + " df");
				messagePanel.insertText(" lies between #plusMinus# " + new NumValue(getCorrectT(), 3) + ".\n");
				messagePanel.insertText("The confidence interval is therefore ");
				insertIntervalFormula(messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly found the end-points of the confidence interval.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!\n");
				messagePanel.insertRedText("The end-points of your confidence interval are close to the correct ones.\n");
				insertTHints(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The end-points of your confidence interval are not close enough to the correct ones.\n");
				insertTHints(messagePanel);
				break;
		}
	}
	
	private MFormula varFormula(NumValue sd, int n, FormulaContext context) {
		MFormula sdFormula = new MText(sd.toString(), context);
		MFormula varFormula = new MSuperscript(sdFormula, "2", context);
		MFormula nFormula = new MText(String.valueOf(n), context);
		
		return new MRatio(varFormula, nFormula, true, context);
	}
	
	private void insertSeFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		double sd1 = summaryPanel.getSd(0);
		double sd2 = summaryPanel.getSd(1);
		
		MFormula var1 = varFormula(new NumValue(sd1, getMaxSampleSd().decimals), getN1(), context);
		MFormula var2 = varFormula(new NumValue(sd2, getMaxSampleSd().decimals), getN2(), context);
		MFormula diffVar = new MBinary(MBinary.PLUS, var1, var2, context);
		MFormula se = new MRoot(diffVar, context);
		messagePanel.insertFormula(se);
	}
	
	private String meanDiffString() {
		return group2First() ? "x#bar##sub2# - x#bar##sub1#" : "x#bar##sub1# - x#bar##sub2#";
	}
	
	private void insertIntervalFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula t = new MConst(new NumValue(getCorrectT(), 3), context);
		MFormula se = new MText("se(" + meanDiffString() + ")", context);
		MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
		
		MFormula xBar = new MText(meanDiffString(), context);
		
		MFormula ci1 = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
		
		double mean1 = summaryPanel.getMean(0);
		double mean2 = summaryPanel.getMean(1);
		double meanDiff = group2First() ? mean2 - mean1 : mean1 - mean2;
		MFormula mean = new MConst(new NumValue(meanDiff, getMaxSampleMean().decimals), context);
		MFormula tseVal = new MConst(tseTemplate.getResult(), context);
		
		MFormula ci2 = new MBinary(MBinary.PLUS_MINUS, mean, tseVal, context);
		
		MFormula f = new MBinary(MBinary.EQUALS, ci1, ci2, context);
		
		messagePanel.insertFormula(f);
	}
	
	private void insertTHints(MessagePanel messagePanel) {
		int df = getDf();
		if (!tLookupPanel.isCorrectDf(df))
			messagePanel.insertText(MText.expandText("Firstly use a t distribution with min(n#sub1#, n#sub2#) - 1 = "  + df + " df."));
		else {
			messagePanel.insertText("You have used the correct degrees of freedom for the t distribution.\n");
			
			NumValue ciPercent = getCiPercent();
			NumValue ciLevel = new NumValue(ciPercent.toDouble() / 100, ciPercent.decimals + 2);
			if (!tLookupPanel.isCorrectConfidenceLevel(ciLevel))
				messagePanel.insertText("Next find the t-value for which " + ciPercent + "% of the area is between #plusMinus#t. (The red centre area in the distribution should be " + ciLevel + ".)");
			else {
				messagePanel.insertText("You have found the correct t-value for a " + ciPercent + "% confidence interval.");
				
				NumValue tInTemplate = tseTemplate.getT();
				boolean copiedT = effectivelySame(tInTemplate.toDouble(), getCorrectT(), tInTemplate.decimals);
				
//				int n1 = getN1();
//				int n2 = getN2();
				
				NumValue calculatedSe = (seExpression != null) ? seExpression.getAttempt() : seCalcTemplate.getResult();
				boolean correctSe1 = effectivelySame(calculatedSe.toDouble(), evaluateCorrectSe());		//	when it is calculated
				NumValue seInTemplate = tseTemplate.getSe();
				boolean correctSe2 = effectivelySame(seInTemplate.toDouble(), evaluateCorrectSe(), seInTemplate.decimals);		//	when used to find the plus-minus value
				if (!copiedT || !correctSe1 || !correctSe2) {
					if (!copiedT)
						messagePanel.insertText(" However you do not seem to have used this value in the template for the \"#plusMinus#\" value.");
					if (!correctSe1)
						messagePanel.insertText("\nYou have not calculated the standard error of the difference correctly.");
					else if (!correctSe2)
						messagePanel.insertText("\nYou have not used the correct standard error in the template for the \"#plusMinus#\" value.");
				}
				else {
					messagePanel.insertText(" And you seem to have found the \"#plusMinus#\" value correctly in the template.\nThe confidence interval should be between (" + meanDiffString() + " - this value) to (" + meanDiffString() + " + this value),\n");
					messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
					
					FormulaContext context = new FormulaContext(null, null, this);
					MFormula se = new MText("se(" + meanDiffString() + ")", context);
					MFormula t = new MText("t", context);
					MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
					
					MFormula xBar = new MText(meanDiffString(), context);
					MFormula ci = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
					
					messagePanel.insertFormula(ci);
				}
			}
		}
	}
	
//-----------------------------------------------------------
	
	protected double evaluateCorrectSe() {
		double sd1 = summaryPanel.getSd(0);
		double sd2 = summaryPanel.getSd(1);
		int n1 = getN1();
		int n2 = getN2();
		
		return Math.sqrt(sd1 * sd1 / n1 + sd2 * sd2 / n2);
	}
	
	protected double evaluateEstimate() {
		return summaryPanel.getMean(0) - summaryPanel.getMean(1);
	}
	
	protected int getDf() {
		return Math.min(getN1(), getN2()) - 1;
	}
	
	
//-----------------------------------------------------------

	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		int n1 = getN1();
		int n2 = getN2();
		
		double sd1 = summaryPanel.getSd(0);
		double sd2 = summaryPanel.getSd(1);
		
		double se1 = Math.sqrt(sd1 * sd1 / n1);
		double se2 = Math.sqrt(sd2 * sd2 / n2);
		
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		if (seExpression != null) {
			String expressionString = "sqrt(" + sd1 + "^2 / " + n1 + " + " + sd2 + "^2 / " + n2 + ")";
			seExpression.showAnswer(null, expressionString);
		}
		else {
			NumValue sd1Val = new NumValue(sd1, getMaxSampleSd().decimals);
			sdMeanCalcTemplate.setValues(sd1Val, n1);
			
			int seDecimals = getMaxSe().decimals;
			seCalcTemplate.setValues(new NumValue(se1, seDecimals), new NumValue(se2, seDecimals));
		}
		showingCorrectAnswer = false;
	}
}