package exerciseGroupsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;
import expression.*;

import exerciseNormal.JdistnAreaLookup.*;
import exerciseGroups.*;
import exerciseTestProg.*;


public class TestDiffMeansApplet extends CoreTestApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	static final private NumValue kOneValue = new NumValue(1, 0);
	static final private int kZDecimals = 4;
	static final private double kMaxTError = 0.00001;
	static final private int kDefaultDf = 20;
	static final private String kZAxisInfo = "-4 4 -4 1";
	static final private NumValue kMaxTValue = new NumValue(99, kZDecimals);
	
	static final private String USE_T_TEMPLATE_OPTION = "tTemplate";
	
	private ExpressionResultPanel testStatExpression = null;
	private DiffTTemplatePanel testStatTemplate = null;
	private XNumberEditPanel dfEdit;
	
	private boolean showingCorrectAnswer = false;
	
	protected void addTypeDelimiters() {
		addType("mean2", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("mean2"))
			return new NumValue(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("mean2")) {		//	assumes mean1, sd1, n1, sd2 and n2 already set
			
			Random rand = new Random(nextSeed());
			double r = rand.nextDouble();
			double pMin = (r < 0.25) ? 0    : (r < 0.5) ? 0.01 : (r < 0.75) ? 0.05 : 0.1;
			double pMax = (r < 0.25) ? 0.01 : (r < 0.5) ? 0.05 : (r < 0.75) ? 0.1  : 1.0;
			
			double pValue = pMin + rand.nextDouble() * (pMax - pMin);
			
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
			
			double mean1 = getMean1().toDouble();
			double seDiff = getSeDiff();
			double mean2 = mean1 - seDiff * getQuantile(cumulative);
			int decimals = getMean1().decimals;
			double factor = 1.0;
			for (int i=0 ; i<decimals ; i++) {
				mean2 *= 10;
				factor *= 10;
			}
			mean2 = Math.rint(mean2) / factor;
			
			return new NumValue(mean2, decimals);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("mean1", "const");
		registerParameter("sd1", "const");
		registerParameter("n1", "int");
		registerParameter("mean2", "mean2");
		registerParameter("sd2", "const");
		registerParameter("n2", "int");
	}
	
	private NumValue getMean1() {
		return getNumValueParam("mean1");
	}
	
	private NumValue getSd1() {
		return getNumValueParam("sd1");
	}
	
	private int getN1() {
		return getIntParam("n1");
	}
	
	private NumValue getMean2() {
		return getNumValueParam("mean2");
	}
	
	private NumValue getSd2() {
		return getNumValueParam("sd2");
	}
	
	private int getN2() {
		return getIntParam("n2");
	}
	
	private double getSeDiff() {
		int n1 = getN1();
		double s1 = getSd1().toDouble();
		int n2 = getN2();
		double s2 = getSd2().toDouble();
		return Math.sqrt(s1*s1/n1 + s2*s2/n2);
	}
	
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
		return "#mu##sub1# - #mu##sub2#";
	}
	
	protected String getShortDistnName() {
		return  "t(" + getCorrectDf() + ")";
	}
	
	protected String getSeString() {
		return  "se(" + getDiffMeanString() + ")";
	}
	
	protected String getDiffMeanString() {
		return  "x#bar##sub1# - x#bar##sub2#";
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
	
	protected int getCorrectDf() {
		return Math.min(getN1(), getN2()) - 1;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			TDistnVariable distnVar = new TDistnVariable("T distribution", kDefaultDf);	//	default T(20 df)
		data.addVariable("distn", distnVar);
		
		return data;
	}
	
	protected double getQuantile(double cumulative) {		//	used for random generation of observed mean
		return TTable.quantile(cumulative, getCorrectDf());
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("North", statisticPanel());
		thePanel.add("Center", tDistnPanel(data));
		
		return thePanel;
	}
	
	protected XPanel statisticPanel() {
		XPanel thePanel = new XPanel();
		
			XPanel dfPanel = new XPanel();
			dfPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				dfEdit = new XNumberEditPanel("df =", String.valueOf(kDefaultDf), 3, this);
				dfEdit.setIntegerType(1, Integer.MAX_VALUE);
				registerStatusItem("df", dfEdit);
				
			dfPanel.add(dfEdit);
			
			XPanel tPanel = hasOption(USE_T_TEMPLATE_OPTION) ? tTemplatePanel() : tExpressionPanel();
			
		if (hasOption(USE_T_TEMPLATE_OPTION)) {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			thePanel.add(dfPanel);
			thePanel.add(tPanel);
		}
		else {
			thePanel.setLayout(new BorderLayout(20, 0));
			thePanel.add("West", dfPanel);
			thePanel.add("Center", tPanel);
		}
		
		return thePanel;
	}
	
	protected XPanel tExpressionPanel() {
		testStatExpression = new ExpressionResultPanel(null, 2, 50, "t =", 6,
																								ExpressionResultPanel.HORIZONTAL, this);
		testStatExpression.setResultDecimals(kZDecimals);
		registerStatusItem("testStatistic", testStatExpression);
		return testStatExpression;
	}
	
	protected XPanel tTemplatePanel() {
		FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		testStatTemplate = new DiffTTemplatePanel("t =", kMaxTValue, stdContext);
		registerStatusItem("testStatistic", testStatTemplate);
		return testStatTemplate;
	}
	
	private XPanel tDistnPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(70, 0);
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis zAxis = new HorizAxis(this);
			zAxis.readNumLabels(kZAxisInfo);
			zAxis.setAxisName("T distribution");
		thePanel.add("Bottom", zAxis);
		
			ContinDistnLookupView zView = new ContinDistnLookupView(data, this, zAxis, "distn", true);
			zView.lockBackground(Color.white);
			zView.setDragEnabled(false);
		thePanel.add("Center", zView);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		if (testStatTemplate == null) {
			NumValue one = new NumValue(1, kZDecimals);
			testStatExpression.showAnswer(one, null);
		}
		else
			testStatTemplate.setValues(kOneValue, kOneValue, kOneValue, kOneValue, kOneValue, kOneValue);
		
		data.variableChanged("distn");		//	to get the distribution lookup panel to reset the heights of the pdfs
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		data.setSelection("distn", -1.0, 1.0);
	}
		
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double mean2) {
		NumValue mean1 = getMean1();
		int df = getCorrectDf();
		double seDiff = getSeDiff();
		
		return TTable.cumulative((mean1.toDouble() - mean2) / seDiff, df);
	}
	
	protected void showCorrectWorking() {
		int df = getCorrectDf();
		TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
		distnVar.setDF(df);
		data.variableChanged("distn");
		
		dfEdit.setIntegerValue(df);
		
		int n1 = getN1();
		int n2 = getN2();
		NumValue mean1 = getMean1();
		NumValue mean2 = getMean2();
		NumValue sd1 = getSd1();
		NumValue sd2 = getSd2();
			
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		if (testStatTemplate == null) {
			String expressionString = "(" + mean1 + " - " + mean2 + ") / sqrt(" + sd1 + "^2 / " + n1 + " + " + sd2 + "^2 / " + n2 + ")";
			testStatExpression.showAnswer(null, expressionString);
		}
		else
			testStatTemplate.setValues(mean1, sd1, new NumValue(n1, 0), mean2, sd2, new NumValue(n2, 0));
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
	protected double getCorrectPValue() {
		double mean2 = getMean2().toDouble();
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return cumulativeProbability(mean2);
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return 1 - cumulativeProbability(mean2);
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability(mean2);
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
				double pLower = cumulativeProbability(getMean2().toDouble());
				return (pLower <= 0.5);
		}
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Type the degrees of freedom for the test.\n");
		insertPValueInstructions(messagePanel);
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify your conclusion from the test.");
	}
	
	protected void insertPValueInstructions(MessagePanel messagePanel) {
		if (testStatTemplate == null) {
			messagePanel.insertText("#bullet#  Type an expression for the t-value for the difference between the means, click ");
			messagePanel.insertBoldText("Calculate");
			messagePanel.insertText(", then use this test statistic to find the p-value for the test. (Use the function sqrt() to find a square root.)\n");
		}
		else
			messagePanel.insertText("#bullet#  Use the template at the top to calculate the t-value for the difference between the means.\n");
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
		double zCorrect = (getMean1().toDouble() - getMean2().toDouble()) / seDiff;
		
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
		messagePanel.insertText("The test statistic is t = (" + getDiffMeanString()
									+ ") / " + getSeString() + ". If H#sub0# is true, t"
									+ " has a " + getShortDistnName() + " distribution. ");
		
		int tail = getTail();
		switch (tail) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("less than or equal to");
				messagePanel.insertText(" this t.");
				break;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("greater than or equal to");
				messagePanel.insertText(" this t.");
				break;
			case TAIL_BOTH:
				messagePanel.insertText("The p-value is the probability that this distribution is ");
				messagePanel.insertBoldText("at least as extreme as");
				messagePanel.insertText(" this t. It is ");
				messagePanel.insertBoldText("twice");
				messagePanel.insertText(" the probability of being ");
				messagePanel.insertText((cumulativeProbability(getMean2().toDouble()) < 0.5) ? "#le# " : "#ge# t.");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	protected NumValue getTestStat() {
		if (testStatTemplate == null)
			return testStatExpression.getAttempt();
		else
			return testStatTemplate.getResult();
	}
	
	private boolean localAction(Object target) {
		if (target == dfEdit) {
			int dfAttempt = dfEdit.getIntValue();
			TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
			if (dfAttempt != distnVar.getDF()) {
				distnVar.setDF(dfAttempt);
				data.variableChanged("distn");
			}
			
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