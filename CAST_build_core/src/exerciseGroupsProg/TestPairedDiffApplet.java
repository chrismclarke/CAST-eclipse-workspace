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
import valueList.*;
import coreVariables.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;
import exerciseGroups.*;
import exerciseTestProg.*;


public class TestPairedDiffApplet extends CoreTestApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	static final private NumValue kOneValue = new NumValue(1, 0);
	static final private double kMaxTError = 0.00001;
	static final private int kDefaultDf = 20;
	static final private String kZAxisInfo = "-4 4 -4 1";
	
	static final private String[] kSummaryVarKeys = {"y1", "y2", "diff"};
	
	
	private ScrollValueList theList;
	private VariablesSummaryPanel variablesSummaries;
	private ZTemplatePanel testStatTemplate = null;
	
	private ExpressionResultPanel seExpression = null;
	private XNumberEditPanel dfEdit;
	
	private boolean showingCorrectAnswer = false;
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("mean1", "const");
		registerParameter("sd", "const");
		registerParameter("corr", "const");
		registerParameter("n", "int");
		registerParameter("dataDecimals", "int");
		registerParameter("seDecimals", "int");
		registerParameter("var1Name", "string");
		registerParameter("var2Name", "string");
		registerParameter("maxSampleMean", "const");
		registerParameter("maxSampleSd", "const");
	}
	
	private double getMean1() {
		return getDoubleParam("mean1");
	}
	
	private double getSd() {
		return getDoubleParam("sd");
	}
	
	private double getCorr() {
		return getDoubleParam("corr");
	}
	
	private int getN() {
		return getIntParam("n");
	}
	
	private int getDataDecimals() {
		return getIntParam("dataDecimals");
	}
	
	private int getSeDecimals() {
		return getIntParam("seDecimals");
	}
	
	private String getVar1Name() {
		return getStringParam("var1Name");
	}
	
	private String getVar2Name() {
		return getStringParam("var2Name");
	}
	
	private LabelValue getMaxVarName() {
		String v1Name = getVar1Name();
		String v2Name = getVar2Name();
		return new LabelValue(v1Name.length() > v2Name.length() ? v1Name : v2Name);			// not necessarily longest when displayed
	}
	
	public NumValue getMaxValue() {			//	only needed for CoreTestApplet
		return null;
	}
	
	private NumValue getMaxSampleMean() {
		return getNumValueParam("maxSampleMean");
	}
	
	private NumValue getMaxSampleSd() {
		return getNumValueParam("maxSampleSd");
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
		return  "d#bar#";
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
		return getN() - 1;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			String randomParams = "1 0.0 1.0 " + nextSeed() + " 3.0";
			RandomNormal generator1 = new RandomNormal(randomParams);
			NumSampleVariable z1 = new NumSampleVariable("Z1", generator1, 10);
			z1.generateNextSample();
		data.addVariable("z1", z1);
		
			String randomParams2 = "1 0.0 1.0 " + nextSeed() + " 3.0";
			RandomNormal generator2 = new RandomNormal(randomParams2);
			NumSampleVariable z2 = new NumSampleVariable("Z2", generator2, 10);
			z2.generateNextSample();
		data.addVariable("z2", z2);
		
			String randomParams3 = "1 0.0 1.0 " + nextSeed() + " 3.0";
			RandomNormal generator3 = new RandomNormal(randomParams3);
			NumSampleVariable z3 = new NumSampleVariable("Z3", generator3, 10);
			z3.generateNextSample();
		data.addVariable("z3", z3);
		
			BaseCorrelatedVariable y1 = new BaseCorrelatedVariable("Y1", data, "z1", "z3");
		data.addVariable("y1", y1);
		
			BaseCorrelatedVariable x2 = new BaseCorrelatedVariable("X2", data, "z2", "z3");
		data.addVariable("x2", x2);
		
			ScaledVariable y2 = new ScaledVariable("Y2", x2, "x2", 0.0, 1.0, 0);
		data.addVariable("y2", y2);
			
			SumDiffVariable diff = new SumDiffVariable("Diff", data, "y1", "y2", SumDiffVariable.DIFF);
		data.addVariable("diff", diff);
		
			TDistnVariable distnVar = new TDistnVariable("T distribution", kDefaultDf);	//	default T(20 df)
		data.addVariable("distn", distnVar);
		
		return data;
	}
	
	protected double getQuantile(double cumulative) {		//	used for random generation of scaling for Y2
		return TTable.quantile(cumulative, getCorrectDf());
	}
		
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
		thePanel.add("West", dataDisplayPanel(data));
		thePanel.add("Center", statisticPanel());
		
		return thePanel;
	}
	
	private XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			theList.addVariableToList("y1", ScrollValueList.RAW_VALUE);
			theList.addVariableToList("y2", ScrollValueList.RAW_VALUE);
			theList.addVariableToList("diff", ScrollValueList.RAW_VALUE);
		thePanel.add("Center", theList);
		
			variablesSummaries = new VariablesSummaryPanel(data, kSummaryVarKeys, this);
		thePanel.add("South", variablesSummaries);
		
		return thePanel;
	}
	
	protected XPanel statisticPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 3));
			
				seExpression = new ExpressionResultPanel(null, 2, 50, "se =", 6,
																										ExpressionResultPanel.HORIZONTAL, this);
				seExpression.setResultDecimals(1);			//	need to delete line and set for new data set
				registerStatusItem("se", seExpression);
			topPanel.add(seExpression);
			
				XPanel tDfPanel = new XPanel();
				tDfPanel.setLayout(new BorderLayout(20, 0));
			
					FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
					testStatTemplate = new ZTemplatePanel("t =", 4, stdContext);
					registerStatusItem("testStatistic", testStatTemplate);
				tDfPanel.add("Center", testStatTemplate);
				
					dfEdit = new XNumberEditPanel("df =", String.valueOf(kDefaultDf), 3, this);
					dfEdit.setIntegerType(1, Integer.MAX_VALUE);
					registerStatusItem("df", dfEdit);
				tDfPanel.add("East", dfEdit);
			
			topPanel.add(tDfPanel);
		
		thePanel.add("North", topPanel);
		
		thePanel.add("Center", tDistnPanel(data));
		
		return thePanel;
	}
	
	private XPanel tDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis zAxis = new HorizAxis(this);
			zAxis.readNumLabels(kZAxisInfo);
			zAxis.setAxisName("T distribution");
		thePanel.add("Bottom", zAxis);
		
			ContinDistnLookupView tView = new ContinDistnLookupView(data, this, zAxis, "distn", true);
			tView.lockBackground(Color.white);
			tView.setDragEnabled(false);
		thePanel.add("Center", tView);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		variablesSummaries.updateForNewData(getMaxVarName(), getMaxSampleMean(), getMaxSampleSd());
		
		seExpression.showAnswer(kOneValue, null);
		seExpression.setResultDecimals(getSeDecimals());
		testStatTemplate.setValues(kOneValue, kZeroValue, kOneValue);
		
		data.variableChanged("z3");
		data.variableChanged("distn");		//	to get the distribution lookup panel to reset the heights of the pdfs
		
		theList.resetVariables();
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		int n = getN();
		NumSampleVariable z1Var = (NumSampleVariable)data.getVariable("z1");
		z1Var.setSampleSize(n);
		z1Var.generateNextSample();
		
		NumSampleVariable z2Var = (NumSampleVariable)data.getVariable("z2");
		z2Var.setSampleSize(n);
		z2Var.generateNextSample();
		
		NumSampleVariable z3Var = (NumSampleVariable)data.getVariable("z3");
		z3Var.setSampleSize(n);
		z3Var.generateNextSample();
		
		double var1Mean = getMean1();
		double varSd = getSd();
		double varCorr = getCorr();
		int dataDecimals = getDataDecimals();
		BaseCorrelatedVariable y1Var = (BaseCorrelatedVariable)data.getVariable("y1");
		y1Var.setScaling(var1Mean, varSd, varCorr, dataDecimals);
		y1Var.name = getVar1Name();
		
		BaseCorrelatedVariable x2Var = (BaseCorrelatedVariable)data.getVariable("x2");
		x2Var.setScaling(0, 1, varCorr, 0);
		
		ScaledVariable y2Var = (ScaledVariable)data.getVariable("y2");
		y2Var.setScale(0, varSd, dataDecimals);	//	try initially with offset of zero
		y2Var.setScale(getY2Shift(y1Var, y2Var), varSd, dataDecimals);
		y2Var.name = getVar2Name();
		
		NumVariable diffVar = (NumVariable)data.getVariable("diff");
		diffVar.setDecimals(dataDecimals);
		
		data.setSelection("distn", -1.0, 1.0);
	}
	
	private double getY2Shift(NumVariable y1Var, NumVariable y2Var) {
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
			double t = getQuantile(cumulative);
			
			int n = Math.min(y1Var.noOfValues(), y2Var.noOfValues());
			double sd = 0.0, sdd = 0.0;
			for (int i=0 ; i<n ; i++) {
				double y1 = y1Var.doubleValueAt(i);
				double y2 = y2Var.doubleValueAt(i);
				double d = y1 - y2;
				sd += d;
				sdd += d * d;
			}
			double dMean = sd / n;
			double diffSe = Math.sqrt((sdd - sd * dMean) / (n * (n - 1)));
			
			return dMean - t * diffSe;
	}
		
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double dMean) {
		double dSd = variablesSummaries.getSd(2).toDouble();
		int df = getCorrectDf();
		double seDiff = dSd / Math.sqrt(getN());
		
		return TTable.cumulative(dMean / seDiff, df);
	}
	
	protected void showCorrectWorking() {
		int df = getCorrectDf();
		TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
		distnVar.setDF(df);
		data.variableChanged("distn");
		
		dfEdit.setIntegerValue(df);
		
		NumValue dSd = variablesSummaries.getSd(2);
		int n = getN();
			
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		
		String expressionString = dSd.toString() + " / sqrt(" + n + ")";
		seExpression.showAnswer(null, expressionString);
		
		NumValue x1Mean = variablesSummaries.getMean(0); // assumes x1 is 1st variable
		NumValue x2Mean = variablesSummaries.getMean(1); // assumes x2 is 2nd variable
		NumValue se = new NumValue(dSd.toDouble() / Math.sqrt(n), getSeDecimals());
		testStatTemplate.setValues(x1Mean, x2Mean, se);
		noteChangedWorking();
		
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
	protected double getCorrectPValue() {
		double dMean = variablesSummaries.getMean(2).toDouble(); // assumes diff is 3rd variable
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return cumulativeProbability(dMean);
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return 1 - cumulativeProbability(dMean);
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability(dMean);
				return 2 * Math.min(pLower, 1 - pLower);
		}
	}
	
	protected boolean lowTailHighlight() {
		double dMean = variablesSummaries.getMean(2).toDouble(); // assumes diff is 3rd variable
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return true;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return false;
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability(dMean);
				return (pLower <= 0.5);
		}
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all of the following steps:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Type a formula for the standard error of the difference between the means.\n");
		messagePanel.insertText("#bullet#  Use the template to calculate the t-value for the test.\n");
		messagePanel.insertText("#bullet#  Enter the degrees of freedom for the test then type the p-value.\n");
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
		
		double dMean = variablesSummaries.getMean(2).toDouble(); // assumes diff is 3rd variable
		double dSd = variablesSummaries.getSd(2).toDouble();
		double seDiff = dSd / Math.sqrt(getN());
		double tCorrect = dMean / seDiff;
		
		if (Math.abs(tCorrect - tAttempt.toDouble()) < kMaxTError) {
			messagePanel.insertText("(You have correctly specified the hypotheses and found the t test statistic.)\n");
			messagePanel.insertText("The p-value is found from a tail probability of the " + getShortDistnName() + " distribution but you have not evaluated it correctly.");
		}
		else {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The t test statistic has not been correcty evaluated.");
		}
	}
	
	protected void insertPvalueMessage(MessagePanel messagePanel) {
		messagePanel.insertText("The test statistic is t = " + getDiffMeanString()
									+ " / " + getSeString() + ". If H#sub0# is true, t"
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
				double dMean = variablesSummaries.getMean(2).toDouble(); // assumes diff is 3rd variable
				messagePanel.insertText((cumulativeProbability(dMean) < 0.5) ? "#le# " : "#ge# t.");
				break;
		}
	}
	
//-----------------------------------------------------------
	
	protected NumValue getTestStat() {
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