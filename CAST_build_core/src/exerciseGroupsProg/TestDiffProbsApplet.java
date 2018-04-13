package exerciseGroupsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import random.*;
import exercise2.*;
import formula.*;
import expression.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;
import exerciseEstim.*;
import twoGroup.*;
import exerciseGroups.*;
import exerciseTestProg.*;


public class TestDiffProbsApplet extends CoreTestApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	static final private NumValue kOneValue = new NumValue(1, 0);
//	static final private int kZDecimals = 4;
	static final private double kMaxTError = 0.00001;
	static final private String kZAxisInfo = "-4 4 -4 1";
//	static final private NumValue kMaxZValue = new NumValue(99, kZDecimals);
	
	static final private Color kWorkingBackground = new Color(0xE9E9FF);
	
	static final private String USE_SE_TEMPLATES_OPTION = "seTemplates";
	
	private XLabel title1, title2;
	private ProbCalcFormula prob1, prob2;
	private PropnSeTemplatePanel se1Template, se2Template;
	
	private ExpressionResultPanel seExpression = null;
	private DiffSDCalcPanel seDiffCalcTemplate = null;
	
//	private ExpressionResultPanel testStatExpression = null;
	private ZTemplatePanel testStatTemplate = null;
//	private XNumberEditPanel dfEdit;
	
	private boolean showingCorrectAnswer = false;
	
	private class	Successes {
		public int x1, x2;
		public Successes(int x1, int x2) {
			this.x1 = x1;
			this.x2 = x2;
		}
	}
	
	protected void addTypeDelimiters() {
		addType("successes", "*");
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
		if (baseType.equals("successes")) {		//	assumes n1, n2 and pi1 already set
			Random randomU = new Random(nextSeed());
			double r = randomU.nextDouble();
			double pMin = (r < 0.25) ? 0    : (r < 0.5) ? 0.01 : (r < 0.75) ? 0.05 : 0.1;
			double pMax = (r < 0.25) ? 0.01 : (r < 0.5) ? 0.05 : (r < 0.75) ? 0.1  : 1.0;
			
			double pValue = pMin + randomU.nextDouble() * (pMax - pMin);
			
			double cumulative = pValue;
			switch (getTail()) {
				case TAIL_LOW:
				case TAIL_LOW_EQ:
					break;
				case TAIL_HIGH:
				case TAIL_HIGH_EQ:
					cumulative = 1 - cumulative;
					break;
				case TAIL_BOTH:
					cumulative = cumulative / 2;
					if (new Random(nextSeed()).nextDouble() > 0.5)
						cumulative = 1 - cumulative;
					break;
			}
			double z = NormalTable.quantile(cumulative);
			
			int n1 = getN1();
			int n2 = getN2();
			double pi1 = getPi1();
			RandomBinomial rand = new RandomBinomial(1, n1, pi1);
			rand.setSeed(nextSeed());
			int x1=0, x2=0;
			for (int i=0 ; i<10 ; i++) {
				while (x1 == 0 || x1 == n1)
					x1 = rand.generateOne();
				double p1 = ((double)x1) / n1;
				double se = Math.sqrt(p1 * (1 - p1) * (1.0 / n1 + 1.0 / n2));		//	only approx since we don't know p2 yet
				double x2Double = n2 * (p1 - se * z);
				x2 = (int)Math.round((z > 0) ? Math.ceil(x2Double) : Math.floor(x2Double));
				if (x2 >= 0 && x2 <= n2)
					break;
			}
			return new Successes(x1, x2);
		}
		else if (baseType.equals("nSuccess1")) {
			Successes succ = getSuccesses();
			return Integer.valueOf(succ.x1);
		}
		else if (baseType.equals("nSuccess2")) {
			Successes succ = getSuccesses();
			return Integer.valueOf(succ.x2);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("groupVarName", "string");
		registerParameter("group1Name", "string");
		registerParameter("group2Name", "string");
		registerParameter("successes", "successes");		//	random object that is not directly used
		registerParameter("pi1", "const");
		registerParameter("n1", "int");
		registerParameter("n2", "int");
		registerParameter("x1", "nSuccess1");		//	random object that gets its value from "successes"
		registerParameter("x2", "nSuccess2");		//	random object that gets its value from "successes"
		registerParameter("seDecimals", "int");
		registerParameter("pDecimals", "int");
	}
	
	protected String getGroupVarName() {
		return getStringParam("groupVarName");
	}
	
	protected String getGroup1Name() {
		return getStringParam("group1Name");
	}
	
	protected String getGroup2Name() {
		return getStringParam("group2Name");
	}
	
	private Successes getSuccesses() {
		return (Successes)getObjectParam("successes");
	}
	
	private double getPi1() {
		return getDoubleParam("pi1");
	}
	
	private int getN1() {
		return getIntParam("n1");
	}
	
	private int getN2() {
		return getIntParam("n2");
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
	
	private NumValue getP1() {
		return new NumValue(((double)getX1()) / getN1(), getPDecimals());
	}
	
	private NumValue getP2() {
		return new NumValue(((double)getX2()) / getN2(), getPDecimals());
	}
	
	private NumValue getMaxSe() {
		return new NumValue(1.0, getSeDecimals());
	}
	
//-----------------------------------------------------------
	
	public NumValue getMaxValue() {			//	only needed for CoreTestApplet
		return null;
	}
	
	public String getAxisInfo() {			//	only needed for CoreLookupApplet
		return null;
	}
	
	public String getVarName() {			//	only needed for CoreLookupApplet
		return null;
	}
	
	protected NumValue nullParamValue() {			//	only needed for CoreNormalApplet
		return kZeroValue;
	}
	
//-----------------------------------------------------------
	
	protected String parameterName() {
		return "#pi##sub1# - #pi##sub2#";
	}
	
	protected String getShortDistnName() {
		return  "normal";
	}
	
	protected String getSeString() {
		return  "se(" + getDiffMeanString() + ")";
	}
	
	protected String getDiffMeanString() {
		return  "p#sub1# - p#sub2#";
	}
	
	protected String parameterLongName() {
		return parameterName();
	}
	
	protected String getPvalueLongName() {
		return "p-value";
	}
	
	protected String getPValuesPropnsString() {
		return "P-values";
	}
	
	protected String getPValueLabel() {
		return "p-value";
	}
	
//-----------------------------------------------------------
	
	private double getSeDiff() {
		int n1 = getN1();
		double p1 = getP1().toDouble();
		int n2 = getN2();
		double p2 = getP2().toDouble();
		return Math.sqrt(p1*(1 - p1)/n1 + p2 * (1 - p2)/n2);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable distnVar = new NormalDistnVariable("Normal distribution");
		data.addVariable("distn", distnVar);
		
		return data;
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
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("North", groupsPanel());
			XPanel seCalcPanel = seCalcPanel();
			seCalcPanel.lockBackground(kWorkingBackground);
		thePanel.add("Center", seCalcPanel);
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
	
	protected XPanel zTemplatePanel() {
		FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		testStatTemplate = new ZTemplatePanel("z =", 4, stdContext);
		registerStatusItem("testStatistic", testStatTemplate);
		return testStatTemplate;
	}
	
	private XPanel zDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel distnPanel = new XPanel();
			distnPanel.setLayout(new AxisLayout());
			
				HorizAxis zAxis = new HorizAxis(this);
				zAxis.readNumLabels(kZAxisInfo);
				zAxis.setAxisName("Normal distribution");
			distnPanel.add("Bottom", zAxis);
			
				ContinDistnLookupView zView = new ContinDistnLookupView(data, this, zAxis, "distn", true);
				zView.lockBackground(Color.white);
				zView.setDragEnabled(false);
			distnPanel.add("Center", zView);
		
		thePanel.add("Center", distnPanel);
		
		thePanel.add("West", zTemplatePanel());
		
		return thePanel;
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
		
		testStatTemplate.setValues(kOneValue, kOneValue, kOneValue);
		testStatTemplate.displayResult();		//	needed to update density highlighting
		
		data.variableChanged("distn");		//	to get the distribution lookup panel to reset the heights of the pdfs
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		data.setSelection("distn", -1.0, 1.0);
	}
		
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability() {
		double seDiff = getSeDiff();
		return NormalTable.cumulative((getP1().toDouble() - getP2().toDouble()) / seDiff);
	}
	
	protected void showCorrectWorking() {
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
		
		double se = evaluateCorrectSe();
		
		testStatTemplate.setValues(p1, p2, new NumValue(se, getMaxSe().decimals));
		testStatTemplate.displayResult();		//	needed to update density highlighting
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
	protected double evaluateCorrectSe() {
		double p1 = getP1().toDouble();
		double p2 = getP2().toDouble();
		int n1 = getN1();
		int n2 = getN2();
		
		return Math.sqrt(p1 * (1 - p1) / n1 + p2 * (1 - p2) / n2);
	}
	
	protected double getCorrectPValue() {
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return cumulativeProbability();
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return 1 - cumulativeProbability();
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability();
				return 2 * Math.min(pLower, 1 - pLower);
		}
	}
	
	protected boolean lowTailHighlight() {
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return true;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return false;
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability();
				return (pLower <= 0.5);
		}
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		if (!hasOption(USE_SE_TEMPLATES_OPTION)) {
			messagePanel.insertText("#bullet#  Type an expression for the standard error for the difference between the proportions, then click ");
			messagePanel.insertBoldText("Calculate");
			messagePanel.insertText(". (Use the function sqrt() to find a square root.)\n");
		}
		else
			messagePanel.insertText("#bullet#  Use the templates at the top to calculate the standard errors of the two proportions and of their difference.\n");
		messagePanel.insertText("#bullet#  Calculate the z test statistic from the sample proportions and standard errors.\n");
		messagePanel.insertText("#bullet#  Find the p-value for the test from the test statistic and normal distribution.\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		messagePanel.insertRedHeading("p-value is wrong!\n");
		
		NumValue tAttempt = getTestStat();
		if (tAttempt == null) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The t test statistic has not been found and the p-value for the test is incorrect.");
			return;
		}
		
		double seDiff = getSeDiff();
		double zCorrect = (getP1().toDouble() - getP2().toDouble()) / seDiff;
		
		if (Math.abs(zCorrect - tAttempt.toDouble()) < kMaxTError) {
			messagePanel.insertText("(You have correctly specified the hypotheses and found the t test statistic.)\n");
			messagePanel.insertText("The p-value is found from a tail probability of the " + getShortDistnName() + " distribution but you have not evaluated it correctly.");
		}
		else {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The t test statistic has not been correcty evaluated.");
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		messagePanel.insertText("The test statistic is z = (" + getDiffMeanString()
									+ ") / " + getSeString() + ". If H#sub0# is true, t"
									+ " has a " + getShortDistnName() + " distribution. ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("less than or equal to");
				messagePanel.insertText(" this z.");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("greater than or equal to");
				messagePanel.insertText(" this z.");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("at least as extreme as");
				messagePanel.insertText(" this z. It is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the probability of being ");
				messagePanel.insertText((cumulativeProbability() < 0.5) ? "#le# " : "#ge# z.");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	protected NumValue getTestStat() {
		return testStatTemplate.getResult();
	}
	
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