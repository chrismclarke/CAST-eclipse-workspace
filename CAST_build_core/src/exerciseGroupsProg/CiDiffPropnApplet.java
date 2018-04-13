package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;
import formula.*;


import expression.*;
import linMod.*;
import exerciseEstim.*;
import twoGroup.*;
import exerciseGroups.*;


public class CiDiffPropnApplet extends CoreCiDiffApplet {
	
	private XLabel title1, title2;
	private ProbCalcFormula prob1, prob2;
	private PropnSeTemplatePanel se1Template, se2Template;
	
	private DiffSDCalcPanel seDiffCalcTemplate = null;
	
	
	protected void addTypeDelimiters() {
		super.addTypeDelimiters();
		addType("nSuccess1", "*");
		addType("nSuccess2", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("nSuccess1") || baseType.equals("nSuccess2"))
			return Integer.valueOf(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("nSuccess1")) {		//	assumes n1 and pi1 already set
			int n1 = getN1();
			RandomBinomial rand = new RandomBinomial(1, n1, getPi1());
			rand.setSeed(nextSeed());
			int x1 = 0;
			while (x1 == 0 || x1 == n1)
				x1 = rand.generateOne();
			return Integer.valueOf(x1);
		}
		else if (baseType.equals("nSuccess2")) {		//	assumes n2 and pi2 already set
			int n2 = getN2();
			RandomBinomial rand = new RandomBinomial(1, n2, getPi2());
			rand.setSeed(nextSeed());
			int x2 = 0;
			while (x2 == 0 || x2 == n2)
				x2 = rand.generateOne();
			return Integer.valueOf(x2);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
//================================================
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("pi1", "const");
		registerParameter("pi2", "const");
		registerParameter("x1", "nSuccess1");
		registerParameter("x2", "nSuccess2");
		registerParameter("seDecimals", "int");
		registerParameter("pDecimals", "int");
	}
	
//	--------------------------------------------------
	
	private double getPi1() {
		return getDoubleParam("pi1");
	}
	private double getPi2() {
		return getDoubleParam("pi2");
	}
	
	private int getX1() {
		return getIntParam("x1");
	}
	
	private int getX2() {
		return getIntParam("x2");
	}
	
	private int getSeDecimals() {
		return getIntParam("seDecimals");
	}
	
	private int getPDecimals() {
		return getIntParam("pDecimals");
	}
	
//	--------------------------------------------------
	
	protected NumValue getMaxSe() {
		return new NumValue(1.0, getSeDecimals());
	}
	
	protected NumValue getMaxPlusMinus() {
		return new NumValue(1.0, getSeDecimals());
	}
	
//	private NumValue getMaxSampleMean() {
//		return getNumValueParam("maxSampleMean");
//	}
	
//	private NumValue getMaxSampleSd() {
//		return getNumValueParam("maxSampleSd");
//	}
	
	private NumValue getP1() {
		return new NumValue(((double)getX1()) / getN1(), getPDecimals());
	}
	
	private NumValue getP2() {
		return new NumValue(((double)getX2()) / getN2(), getPDecimals());
	}
	
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		return new DataSet();
	}
	
//-----------------------------------------------------------

	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			
			topPanel.add("North", groupsPanel());
				XPanel seCalcPanel = seCalcPanel();
				seCalcPanel.lockBackground(kWorkingBackground);
			topPanel.add("Center", seCalcPanel);
		
		thePanel.add("North", topPanel);
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 0));
			
				tLookupPanel = new TLookupPanel(this, TLookupPanel.QUANTILE_LOOKUP, TLookupPanel.NORMAL_ONLY);
				registerStatusItem("tLookupPanel", tLookupPanel);
//				tLookupPanel.lockBackground(kWorkingBackground);
			bottomPanel.add("Center", tLookupPanel);
			
				InsetPanel plusMinusPanel = new InsetPanel(6, 0);
				plusMinusPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
					FormulaContext bigGreenContext = new FormulaContext(kGreenColor, getBigFont(), this);
					tseTemplate = new PlusMinusCalcPanel(getMaxSe(), bigGreenContext);
					registerStatusItem("tseTemplate", tseTemplate);
				plusMinusPanel.add(tseTemplate);
//				plusMinusPanel.lockBackground(kWorkingBackground);
			bottomPanel.add("East", plusMinusPanel);
			bottomPanel.lockBackground(kDataBackground);
			
		thePanel.add("Center", bottomPanel);
		
		return thePanel;
	}
	
	
	
	private XPanel seCalcPanel() {
		XPanel thePanel = new InsetPanel(0, 7);
		thePanel.setLayout(new BorderLayout(0, 0));
		FormulaContext bigGreenContext = new FormulaContext(null, getBigFont(), this);
		
		if (hasOption(USE_SE_TEMPLATES_OPTION)) {				
				seDiffCalcTemplate = new DiffSDCalcPanel("se =", getMaxSe(), bigGreenContext);
				registerStatusItem("seDiffCalcTemplate", seDiffCalcTemplate);
			thePanel.add("Center", seDiffCalcTemplate);
		}
		else {
			XPanel sePanel = new InsetPanel(80, 0);
			sePanel.setLayout(new FixedSizeLayout(250, 120));
				seExpression = new ExpressionResultPanel(null, 2, 50, "se =", 6,
																									ExpressionResultPanel.VERTICAL, this);
				seExpression.setResultDecimals(getMaxSe().decimals);
				registerStatusItem("seExpression", seExpression);
			sePanel.add(seExpression);
			thePanel.add("Center", sePanel);
		}
		return thePanel;
	}
	
	private XPanel groupsPanel() {
		int horizInset = hasOption(USE_SE_TEMPLATES_OPTION) ? 0 : 120;
		XPanel thePanel = new InsetPanel(horizInset, 0);
		GridBagLayout gbl = new GridBagLayout();
		thePanel.setLayout(gbl);
		GridBagConstraints titleConstraints = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0,0,0,0), 5, 0);
		GridBagConstraints propnConstraints = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0,0,0,0), 10, 5);
		GridBagConstraints seConstraints = new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0,0,0,0), 5, 5);
		
			title1 = new XLabel("Group1", XLabel.CENTER, this);
			FormulaContext stdContext = new FormulaContext(null, kWorkingBackground, getBigFont(), this);
			prob1 = new ProbCalcFormula(1, stdContext);
			if (hasOption(USE_SE_TEMPLATES_OPTION)) {
				se1Template = new PropnSeTemplatePanel(MText.expandText("se#sub1# ="),
															new NumValue(0, getSeDecimals()), PropnSeTemplatePanel.SE_ONLY, stdContext);
				registerStatusItem("se1Template", se1Template);
				}
		addGroupRow(title1, prob1, se1Template, titleConstraints, propnConstraints, seConstraints, gbl, thePanel);
		
			titleConstraints.gridy ++;
			propnConstraints.gridy ++;
			seConstraints.gridy ++;
			
			title2 = new XLabel("Group2", XLabel.CENTER, this);
			prob2 = new ProbCalcFormula(2, stdContext);
			if (hasOption(USE_SE_TEMPLATES_OPTION)) {
				se2Template = new PropnSeTemplatePanel(MText.expandText("se#sub2# ="),
															new NumValue(0, getSeDecimals()), PropnSeTemplatePanel.SE_ONLY, stdContext);
				registerStatusItem("se2Template", se2Template);
				}
		addGroupRow(title2, prob2, se2Template, titleConstraints, propnConstraints, seConstraints, gbl, thePanel);
		
		thePanel.lockBackground(kWorkingBackground);
		return thePanel;
	}
	
	private void addGroupRow(XLabel title, ProbCalcFormula prob, PropnSeTemplatePanel seTemplate,
				GridBagConstraints titleConstraints, GridBagConstraints propnConstraints, GridBagConstraints seConstraints,
				GridBagLayout gbl, XPanel thePanel) {
			title.setFont(getBigBoldFont());
		thePanel.add(title);
		gbl.setConstraints(title, titleConstraints);
		
		thePanel.add(prob);
		gbl.setConstraints(prob, propnConstraints);
		
		if (seTemplate != null) {
			thePanel.add(seTemplate);
			gbl.setConstraints(seTemplate, seConstraints);
		}
	}
	
	protected int getMessageHeight() {
		return 200;
	}
	
	
	protected void setDisplayForQuestion() {
		title1.setText(getGroup1Name());
		prob1.setRatio(getX1(), getN1(), getPDecimals());
		if (se1Template != null)
			se1Template.setValues(kOneValue, kOneValue, kOneValue);
		
		title2.setText(getGroup2Name());
		prob2.setRatio(getX2(), getN2(), getPDecimals());
		NumValue onePropnValue = new NumValue(1, getSeDecimals());
		if (se2Template != null)
			se2Template.setValues(kOneValue, kOneValue, kOneValue);
		
		if (seExpression != null)
			seExpression.showAnswer(onePropnValue, null);
		else {
			seDiffCalcTemplate.changeMaxValue(onePropnValue);
			seDiffCalcTemplate.setValues(onePropnValue, onePropnValue);
		}
		
		super.setDisplayForQuestion();
	}
	
	protected boolean group2First() {
		return getP2().toDouble() > getP1().toDouble();
	}
	
	protected void setDataForQuestion() {
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("First find the standard error of the difference between the sample proportions ");
				if (seExpression != null)
					messagePanel.insertText("by typing an expression for it into the box above the normal distribution.");
				else
					messagePanel.insertText("using the templates above the normal distribution to help.\n");
				messagePanel.insertText("Now use the normal distribution to help find a " + getCiPercent()
																+ "% CI for the difference between the proportions, typing it into the answer boxes.\n");
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
				if (hasOption(USE_SE_TEMPLATES_OPTION)) {
					messagePanel.insertText("The standard errors of the separate proportions are\n");
					insertOneSeFormula(getP1(), getN1(), messagePanel);
					messagePanel.insertText(" and ");
					insertOneSeFormula(getP2(), getN2(), messagePanel);
					messagePanel.insertText("\nThe standard error of the difference is ");
					insertSimpleSeFormula(messagePanel);
				}
				else {
					messagePanel.insertText("The standard error of the difference between the proportions is\n");
					insertSeFormula(messagePanel);
				}
				messagePanel.insertText("\n" + getCiPercent() + "% of the ");
				messagePanel.insertText("normal distribution lies between #plusMinus# " + new NumValue(getCorrectT(), 3) + ".\n");
				messagePanel.insertText("The confidence interval is therefore\n");
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
	
	private MFormula varFormula(NumValue p, int n, FormulaContext context) {
		MFormula varFormula = new MText(p.toString() + "(1 - " + p.toString() + ")", context);
		MFormula nFormula = new MText(String.valueOf(n), context);
		
		return new MRatio(varFormula, nFormula, true, context);
	}
	
	private void insertOneSeFormula(NumValue p, int n, MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula var = varFormula(p, n, context);
		MFormula sd = new MRoot(var, context);
		messagePanel.insertFormula(sd);
	}
	
	private void insertSimpleSeFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula var1 = new MText("se#sub1##sup2#", context);
		MFormula var2 = new MText("se#sub2##sup2#", context);
		MFormula diffVar = new MBinary(MBinary.PLUS, var1, var2, context);
		MFormula se = new MRoot(diffVar, context);
		messagePanel.insertFormula(se);
	}
	
	private void insertSeFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		NumValue p1 = getP1();
		NumValue p2 = getP2();
		
		MFormula var1 = varFormula(p1, getN1(), context);
		MFormula var2 = varFormula(p2, getN2(), context);
		MFormula diffVar = new MBinary(MBinary.PLUS, var1, var2, context);
		MFormula se = new MRoot(diffVar, context);
		messagePanel.insertFormula(se);
	}
	
	private String propnDiffString() {
		return group2First() ? "p#sub2# - p#sub1#" : "p#sub1# - p#sub2#";
	}
	
	private NumValue getPropnDiff() {
		double p1 = getP1().toDouble();
		double p2 = getP2().toDouble();
		double propnDiff = group2First() ? p2 - p1 : p1 - p2;
		return new NumValue(propnDiff, getPDecimals());
	}
	
	private void insertIntervalFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula t = new MConst(new NumValue(getCorrectT(), 3), context);
		MFormula se = new MText("se(" + propnDiffString() + ")", context);
		MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
		
		MFormula estimate = new MText(propnDiffString(), context);
		
		MFormula ci1 = new MBinary(MBinary.PLUS_MINUS, estimate, tse, context);
		
		NumValue propnDiff = getPropnDiff();
		MFormula mean = new MConst(propnDiff, context);
		MFormula tseVal = new MConst(tseTemplate.getResult(), context);
		
		MFormula ci2 = new MBinary(MBinary.PLUS_MINUS, mean, tseVal, context);
		
		MFormula f = new MBinary(MBinary.EQUALS, ci1, ci2, context);
		
		messagePanel.setAlignment(false);
		messagePanel.insertFormula(f);
		messagePanel.setAlignment(true);
	}
	
	private void insertTHints(MessagePanel messagePanel) {
		NumValue ciPercent = getCiPercent();
		NumValue ciLevel = new NumValue(ciPercent.toDouble() / 100, ciPercent.decimals + 2);
		if (!tLookupPanel.isCorrectConfidenceLevel(ciLevel))
			messagePanel.insertText("Next find the z-value for which " + ciPercent + "% of the area is between #plusMinus#z. (The red centre area in the distribution should be " + ciLevel + ".)");
		else {
			messagePanel.insertText("You have found the correct z-value for a " + ciPercent + "% confidence interval.");
			
			NumValue tInTemplate = tseTemplate.getT();
			boolean copiedT = effectivelySame(tInTemplate.toDouble(), getCorrectT(), tInTemplate.decimals);
			
			NumValue calculatedSe = (seExpression != null) ? seExpression.getAttempt() : seDiffCalcTemplate.getResult();
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
				messagePanel.insertText(" And you seem to have found the \"#plusMinus#\" value correctly in the template.\nThe confidence interval should be between (" + propnDiffString() + " - this value) to (" + propnDiffString() + " + this value),\n");
				messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
				
				FormulaContext context = new FormulaContext(null, null, this);
				MFormula se = new MText("se(" + propnDiffString() + ")", context);
				MFormula t = new MText("z", context);
				MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
				
				MFormula estimate = new MText(propnDiffString(), context);
				MFormula ci = new MBinary(MBinary.PLUS_MINUS, estimate, tse, context);
				
				messagePanel.insertFormula(ci);
			}
		}
	}
	
//-----------------------------------------------------------
	
	protected double evaluateCorrectSe() {
		double p1 = getP1().toDouble();
		double p2 = getP2().toDouble();
		int n1 = getN1();
		int n2 = getN2();
		
		return Math.sqrt(p1 * (1 - p1) / n1 + p2 * (1 - p2) / n2);
	}
	
	protected double evaluateEstimate() {
		return getP1().toDouble() - getP2().toDouble();
	}
	
	protected int getDf() {
		return -1;			//	for normal
	}
	
	
//-----------------------------------------------------------

	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		int n1 = getN1();
		int n2 = getN2();
		
		NumValue p1 = getP1();
		NumValue p2 = getP2();
		
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		if (seExpression != null) {
			String expressionString = "sqrt(" + p1 + "*(1 - " + p1 + ") / " + n1 + " + " + p2 + "*(1 - " + p2 + ") / " + n2 + ")";
			seExpression.showAnswer(null, expressionString);
		}
		else {
			NumValue q1 = new NumValue(1 - p1.toDouble(), p1.decimals);
			se1Template.setValues(p1, q1, new NumValue(n1, 0));
			NumValue q2 = new NumValue(1 - p2.toDouble(), p2.decimals);
			se2Template.setValues(p2, q2, new NumValue(n2, 0));
			
			int seDecimals = getMaxSe().decimals;
			NumValue se1 = new NumValue(Math.sqrt(p1.toDouble() * q1.toDouble() / n1), seDecimals);
			NumValue se2 = new NumValue(Math.sqrt(p2.toDouble() * q2.toDouble() / n2), seDecimals);
			
			seDiffCalcTemplate.setValues(se1, se2);
		}
		showingCorrectAnswer = false;
	}
	
	
//-----------------------------------------------------------
	
	
	protected int assessAnswer() {
		int assessment = super.assessAnswer();
		
		if (getCiPercent().toString().equals("95") && (assessment == ANS_CLOSE || assessment == ANS_WRONG)) {
			double lowAttempt = getLowAttempt();
			double highAttempt = getHighAttempt();
			double se = evaluateCorrectSe();
			int assessment2 = checkAttempt(lowAttempt, highAttempt, 2.0 * se);
			assessment = Math.min(assessment, assessment2);
		}
		
		return assessment;
	}
}