package exerciseRegnProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import exercise2.*;
import formula.*;

import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;
import exerciseTestProg.*;
import exerciseRegn.*;


public class TestSlopeApplet extends CoreTestApplet {
	static final private NumValue kZeroValue = new NumValue(0, 0);
	static final private NumValue kOneValue = new NumValue(1, 0);
//	static final private int kZDecimals = 4;
	static final private double kMaxTError = 0.00001;
	static final private int kDefaultDf = 20;
	static final private String kZAxisInfo = "-4 4 -4 1";
	
	private ZTemplatePanel testStatTemplate = null;
	private XNumberEditPanel dfEdit;
	private ParameterSummaryPanel parameterSummaries;
	
	private boolean showingCorrectAnswer = false;
	
	protected void addTypeDelimiters() {
		addType("slope", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("slope"))
			return new NumValue(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("slope")) {		//	assumes count, slopeSe and slopeDecimals already set
			
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
			
			double slope = getQuantile(cumulative) * getSlopeSe().toDouble();
			int decimals = getSlopeDecimals();
			double factor = 1.0;
			for (int i=0 ; i<decimals ; i++) {
				slope *= 10.0;
				factor /= 10.0;
			}
			
			return new NumValue(Math.rint(slope) * factor, decimals);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("xMean", "const");
		registerParameter("yMean", "const");
		registerParameter("slope", "slope");
		registerParameter("seSlope", "const");
		registerParameter("seIntercept", "const");
		registerParameter("slopeDecimals", "int");
		registerParameter("interceptDecimals", "int");
		registerParameter("count", "int");
		registerParameter("interceptName", "string");
		registerParameter("slopeName", "string");
	}
	
	private double getXMean() {
		return getDoubleParam("xMean");
	}
	
	private double getYMean() {
		return getDoubleParam("yMean");
	}
	
	private NumValue getSlopeSe() {
		return getNumValueParam("seSlope");
	}
	
	private NumValue getInterceptSe() {
		return getNumValueParam("seIntercept");
	}
	
	private int getSlopeDecimals() {
		return getIntParam("slopeDecimals");
	}
	
	private int getInterceptDecimals() {
		return getIntParam("interceptDecimals");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private NumValue getSlope() {
		return getNumValueParam("slope");
	}
	
	private NumValue getIntercept() {
		return new NumValue(getYMean() - getSlope().toDouble() * getXMean(), getInterceptDecimals());
	}
	
	private String getInterceptName() {
		String s = getStringParam("interceptName");
		return (s == null) ? "Intercept" : s;
	}
	
	private String getSlopeName() {
		String s = getStringParam("slopeName");
		return (s == null) ? "Slope" : s;
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
		return "#beta##sub1#";
	}
	
	protected String getShortDistnName() {
		return  "t(" + getCorrectDf() + ")";
	}
	
	protected String getSeString() {
		return  "se(" + getDiffMeanString() + ")";
	}
	
	protected String getDiffMeanString() {
		return  "b#sub1#";
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
		return getCount() - 2;
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
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 10));
			
			topPanel.add(ProportionLayout.LEFT, summaryPanel());
			topPanel.add(ProportionLayout.RIGHT, tTemplatePanel());
			
		thePanel.add("North", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(40, 0));
			
			bottomPanel.add("West", dfPanel());
			bottomPanel.add("Center", tDistnPanel(data));
		
		thePanel.add("Center", bottomPanel);
		
		return thePanel;
	}
	
	private XPanel summaryPanel() {
		parameterSummaries = new ParameterSummaryPanel(null, null, this);
		return parameterSummaries;
	}
	
	private XPanel dfPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			dfEdit = new XNumberEditPanel("df =", String.valueOf(kDefaultDf), 3, this);
			dfEdit.setIntegerType(1, Integer.MAX_VALUE);
			registerStatusItem("df", dfEdit);
			
		thePanel.add(dfEdit);
		return thePanel;
	}
	
	private XPanel tTemplatePanel() {
		FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		testStatTemplate = new ZTemplatePanel("t =", 4, stdContext);
		testStatTemplate.setFont(getBigFont());
		registerStatusItem("testStatistic", testStatTemplate);
		return testStatTemplate;
	}
	
	private XPanel tDistnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
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
		parameterSummaries.setValues(new LabelValue(getInterceptName()), new LabelValue(getSlopeName()),
							getIntercept(), getInterceptSe(), getSlope(), getSlopeSe());
		
		testStatTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		dfEdit.setIntegerValue(10);
		
		data.variableChanged("distn");		//	to get the distribution lookup panel to reset the heights of the pdfs
		
		resetAnswer();
	}
	
	protected void setDataForQuestion() {
		data.setSelection("distn", -1.0, 1.0);
	}
		
//-----------------------------------------------------------
	
	
	protected double cumulativeProbability(double slope) {
		int df = getCorrectDf();
		NumValue seSlope = getSlopeSe();
		
		return TTable.cumulative(slope / seSlope.toDouble(), df);
	}
	
	protected void showCorrectWorking() {
		int df = getCorrectDf();
		TDistnVariable distnVar = (TDistnVariable)data.getVariable("distn");
		distnVar.setDF(df);
		data.variableChanged("distn");
		
		dfEdit.setIntegerValue(df);
		
		showingCorrectAnswer = true;					//	don't reset answering button highlight when showing correct answer
		testStatTemplate.setValues(getSlope(), kZeroValue, getSlopeSe());
		noteChangedWorking();
		showingCorrectAnswer = false;
		
		showCorrectAnswer();
	}
	
	protected double getCorrectPValue() {
		double slope = getSlope().toDouble();
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return cumulativeProbability(slope);
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return 1 - cumulativeProbability(slope);
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability(slope);
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
				double pLower = cumulativeProbability(getSlope().toDouble());
				return (pLower <= 0.5);
		}
	}
	
//-----------------------------------------------------------
	
	protected void insertInstructions(MessagePanel messagePanel) {
		messagePanel.insertText("To answer the question, you must complete all steps below:\n");
		messagePanel.insertText("#bullet#  Use the pop-up menus to specify the hypotheses.\n");
		messagePanel.insertText("#bullet#  Type the degrees of freedom for the test.\n");
		messagePanel.insertText("#bullet#  Use the template at the top to calculate the t-value for the test.\n");
		messagePanel.insertText("#bullet#  Find the p-value and type it into the answer.\n");
		messagePanel.insertText("#bullet#  Use the second pop-up menu to specify your conclusion from the test.");
	}
	
	protected void insertWrongPValueMessage(MessagePanel messagePanel) {
		messagePanel.insertRedHeading("p-value is wrong!\n");
		
		NumValue tAttempt = getTestStat();
		if (tAttempt == null) {
			messagePanel.insertText("(You have correctly specified the hypotheses.)\n");
			messagePanel.insertText("The t test statistic has not been found and the p-value for the test is incorrect.");
			return;
		}
		
		double seSlope = getSlopeSe().toDouble();
		double zCorrect = getSlope().toDouble() / seSlope;
		
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
				messagePanel.insertText((cumulativeProbability(getSlope().toDouble()) < 0.5) ? "#le# " : "#ge# t.");
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