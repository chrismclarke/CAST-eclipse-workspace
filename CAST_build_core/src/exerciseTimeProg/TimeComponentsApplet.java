package exerciseTimeProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import random.*;
import coreVariables.*;
import models.*;
import exercise2.*;

import time.*;


public class TimeComponentsApplet extends ExerciseApplet {

	static final private String kQuarterLabels = "1 2 3 4";
	static final private String kMonthLabels = "J F M A M J J A S O N D";
	
	static final private String[] kTrendKeys = {"trend", "quarter", "month"};
	static final private String[] kErrorModelKeys = {"trend", "lagError", "quarter", "month"};
	static final private String[] kModelKeys = {"trend", "cyclic", "quarter", "month"};
	
	static final private int[] kTrendDecimals = {9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9};
	static final private int[] kModelDecimals = {9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9};
	
	static final private double[] kYearlyCyclicConstraints = {Double.NaN, Double.NaN, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	static final private double[] kQuarterlyCyclicConstraints = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	static final private double[] kMonthlyCyclicConstraints = {Double.NaN, Double.NaN, 0, 0, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
	
	static final private double[] kYearlyErrorConstraints = {Double.NaN, Double.NaN, Double.NaN, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	static final private double[] kQuarterlyErrorConstraints = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	static final private double[] kMonthlyErrorConstraints = {Double.NaN, Double.NaN, Double.NaN, 0, 0, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
	
	static final private String[] kPatternChoices = {"increasing trend", "decreasing trend", "seasonal (year-end peak)", "seasonal (mid-year peak)", "cyclical pattern", "irregular"};
	static final private String[] kPatternAnswer = {
													"The values tend to increase from left to right.",
													"The values tend to decrease from left to right.",
													"There is a pattern that repeats each year with a peak at the end of the year and dip in the middle of the year. This is a seasonal pattern.",
													"There is a pattern that repeats each year with a peak in the middle of the year and dip at the end of the year. This is a seasonal pattern.",
													"There are irregular 'waves' to the time series -- a cyclical pattern.",
													"A considerable amount of value-to-value variation cannot be explained by trend, seasonal or cyclical patterns."};
	
	static final private String[] kPatternError = {
													"There is no tendency for values to increase from left to right.",
													"There is no tendency for values to decrease from left to right.",
													"The values are not higher at the end of each year than in the middle of the year.",
													"The values are not higher in the middle of each year than at the end of the year.",
													"There is no tendency for values to be similar to the values before and after (other than possibly seasonal patterns or linear trend). Since there are no irregular 'waves', there is no cyclical pattern.",
													"There are other patterns that are more prominent than random irregular variation."};
	
	
	private XPanel timePanel;
	private CardLayout timePanelLayout;
	
	private SeasonTimeAxis seasonTimeAxis;
	private IndexTimeAxis indexTimeAxis;
	private VertAxis ySeasonAxis, yIndexAxis;
	
	private RandomNormal errorGenerator, autoCorrelGenerator;
	private Random patternTypeGenerator;
	
	private int[] patterns;
	
	private XChoice[] componentChoice;
	
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
//		registerParameter("index", "int");					//	always registered
		
		registerParameter("count", "int");
		registerParameter("year0", "int");
		registerParameter("autoCorrel", "const");
		registerParameter("yAxis", "string");
		registerParameter("seasonEffects", "string");
	}
	
	private int getCount() {
		return getIntParam("count");
	}
	
	private String getSeasonalLabelInfo() {
		if (getCount() < 60)
			return "0 1 " + getIntParam("year0") + " 1";		//	start with season 1 and label every season and year
		else
			return "0 0 " + getIntParam("year0") + " 1";		//	2nd value = 0 for no labels on seasons
	}
	
	private String getYearLabelInfo() {
		int nVals = getCount();
		int year0 = getIntParam("year0");
		int labelPeriod = (nVals > 50) ? 10 : (nVals > 20) ? 5 : (nVals > 12) ? 2 : 1;
		int labelStep = labelPeriod;
		int firstValLabel = ((year0 + labelStep - 1) / labelStep) * labelStep;		//	round up
		int labelOneIndex = firstValLabel - year0;
		
		return labelOneIndex + " " + labelPeriod + " " + firstValLabel + " " + labelStep;
	}
	
	private String getYAxisInfo() {
		return getStringParam("yAxis");
	}
	
	private double getAutoCorrel() {
		return getDoubleParam("autoCorrel");
	}
	
	private double[] getSeasonEffects() {
		StringTokenizer st = new StringTokenizer(getStringParam("seasonEffects"));
		int nSeasons = st.countTokens();
		double seasonEffect[] = new double[nSeasons];
		for (int i=0 ; i<nSeasons ; i++)
			seasonEffect[i] = Double.parseDouble(st.nextToken());
		
		return seasonEffect;
	}
	
	private int getNoOfSeasons() {
		StringTokenizer st = new StringTokenizer(getStringParam("seasonEffects"));
		return st.countTokens();
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", timeSeriesPanel(data));
		thePanel.add("South", choicePanel(data));
		
		return thePanel;
	}
	
	private XPanel timeSeriesPanel(DataSet data) {
		timePanel = new XPanel();
		timePanelLayout = new CardLayout();
		timePanel.setLayout(timePanelLayout);
		
		timePanel.add("index", indexPanel(data));
		timePanel.add("seasonal", seasonalPanel(data));
		
		return timePanel;
	}
	
	private XPanel indexPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			indexTimeAxis = new IndexTimeAxis(this, 99);		//		no of values will be set later
			indexTimeAxis.setTimeScale(0, 1, 1900, 1);
		thePanel.add("Bottom", indexTimeAxis);
		
			yIndexAxis = new VertAxis(this);
		thePanel.add("Left", yIndexAxis);
		
			TimeView theView = new TimeView(data, this, indexTimeAxis, yIndexAxis);
			theView.setActiveNumVariable("y");
			theView.setSmoothedVariable("y");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel seasonalPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			seasonTimeAxis = new SeasonTimeAxis(this, 99);		//		no of values will be set later
			seasonTimeAxis.setTimeScale("4 " + kQuarterLabels, "0 1 1900 1");
		thePanel.add("Bottom", seasonTimeAxis);
		
			ySeasonAxis = new VertAxis(this);
		thePanel.add("Left", ySeasonAxis);
		
			TimeView theView = new TimeView(data, this, seasonTimeAxis, ySeasonAxis);
			theView.setActiveNumVariable("y");
			theView.setSmoothedVariable("y");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel choicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER));
		
			XLabel componentLabel = new XLabel("Main patterns in time series", XLabel.LEFT, this);
			componentLabel.setFont(getStandardBoldFont());
		thePanel.add(componentLabel);
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new EqualSpacingLayout());
		
			componentChoice = new XChoice[2];
			for (int i=0 ; i<2 ; i++) {
				componentChoice[i] = new XChoice(this);
				for (int j=0 ; j<kPatternChoices.length ; j++)
					componentChoice[i].addItem(kPatternChoices[j]);
				
				controlPanel.add(componentChoice[i]);
				registerStatusItem("pattern" + i, componentChoice[i]);
			}
		
		thePanel.add(controlPanel);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		int nVals = getCount();
		int nSeasons = getNoOfSeasons();
		
		if (nSeasons == 1) {
			indexTimeAxis.setNoOfVals(nVals);
			indexTimeAxis.setTimeScale(getYearLabelInfo());
			indexTimeAxis.invalidate();
			
			yIndexAxis.readNumLabels(getYAxisInfo());
			yIndexAxis.invalidate();
			
			timePanelLayout.show(timePanel, "index");
		}
		else {
			seasonTimeAxis.setNoOfVals(nVals);
				String seasonNames = (getNoOfSeasons() == 12) ? ("12 " + kMonthLabels) : ("4 " + kQuarterLabels);
			seasonTimeAxis.setTimeScale(seasonNames, getSeasonalLabelInfo());
			seasonTimeAxis.invalidate();
			
			ySeasonAxis.readNumLabels(getYAxisInfo());
			ySeasonAxis.invalidate();
			
			timePanelLayout.show(timePanel, "seasonal");
		}
		
		for (int i=0 ; i<2 ; i++)
			componentChoice[i].select(0);
		
		validate();
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			
			errorGenerator = new RandomNormal(1, 0.0, 1.0, 2.5);
			errorGenerator.setSeed(nextSeed());
			autoCorrelGenerator = new RandomNormal(1, 0.0, 1.0, 2.5);
			autoCorrelGenerator.setSeed(nextSeed());
			
			patternTypeGenerator = new Random(nextSeed());
		
			NumSampleVariable acErrorVar = new NumSampleVariable("AcError", autoCorrelGenerator, 9);
			acErrorVar.generateNextSample();
		data.addVariable("acError", acErrorVar);
		
			ARVariable acVar = new ARVariable("AutoCorr", data, "acError");
		data.addVariable("ac", acVar);
		
//			NumVariable acVar = new NumVariable("AutoCorr");
//		data.addVariable("ac", acVar);
		
			IndexVariable trendVar = new IndexVariable("Trend", 1);
		data.addVariable("trend", trendVar);
		
			CatVariable quarterVar = new CatVariable("Quarter");
			quarterVar.readLabels(kQuarterLabels);
		data.addVariable("quarter", quarterVar);
		
			CatVariable monthVar = new CatVariable("Month");
			monthVar.readLabels(kMonthLabels);
		data.addVariable("month", monthVar);
		
			MultipleRegnModel detrendCyclicModel = new MultipleRegnModel("DetrendCyclic", data, kTrendKeys);
		data.addVariable("detrendCyclicModel", detrendCyclicModel);
		
			ResidValueVariable cyclicVar = new ResidValueVariable("Cyclic", data, kTrendKeys, "ac", "detrendCyclicModel", 9);
		data.addVariable("cyclic", cyclicVar);
		
		
			
			NumSampleVariable rawErrorVar = new NumSampleVariable("RawError", errorGenerator, 9);
			rawErrorVar.generateNextSample();
		data.addVariable("rawError", rawErrorVar);
		
			LaggedVariable lagErrorVar = new LaggedVariable("LaggedError", rawErrorVar, 1);
			lagErrorVar.setInitialValues(new NumValue(0.0));
		data.addVariable("lagError", lagErrorVar);
		
			MultipleRegnModel detrendErrorModel = new MultipleRegnModel("DetrendError", data, kErrorModelKeys);
		data.addVariable("detrendErrorModel", detrendErrorModel);
		
			ResidValueVariable errorVar = new ResidValueVariable(translate("Error"), data, kModelKeys, "rawError", "detrendErrorModel", 9);
		data.addVariable("error", errorVar);
		
		
		
			MultipleRegnModel yModel = new MultipleRegnModel("YModel", data, kModelKeys);
		data.addVariable("yModel", yModel);
			
			ResponseVariable yVar = new ResponseVariable("Y", data, kModelKeys, "error", "yModel", 9);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	
//-----------------------------------------------------------

	
	private double getComponentRange() {
		StringTokenizer st = new StringTokenizer(getYAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max - min) * 2 / 5;
	}
	
	private double getMean() {
		StringTokenizer st = new StringTokenizer(getYAxisInfo());
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		
		return (max + min) / 2;
	}
	
	private double getRange(String key) {
		NumVariable v = (NumVariable)data.getVariable(key);
		double min = v.doubleValueAt(0);
		double max = min;
		for (int i=1 ; i<v.noOfValues() ; i++) {
			double x = v.doubleValueAt(i);
			if (x < min)
				min = x;
			if (x > max)
				max = x;
		}
		return max - min;
	}
	
	private boolean hasPattern(int patternIndex) {
		for (int i=0 ; i<patterns.length ; i++)
			if (patterns[i] == patternIndex)
				return true;
		return false;
	}
	
	private void setEffectCoeffs(MultipleRegnModel yModel) {
		double coeffs[] = new double[17];
		
		double cRange = getComponentRange();
		
		coeffs[0] = getMean();
		
		if (hasPattern(0) || hasPattern(1)) {
			int n = getCount();
			double slope = cRange / n;
			if (hasPattern(1))
				slope = -slope;
			coeffs[0] -= slope * n / 2;
			coeffs[1] = slope;
		}
		
		if (hasPattern(4)) {
			double cyclicRange = getRange("cyclic");
			
			double cyclicScale = cRange / cyclicRange;
//			coeffs[2] = cyclicScale * 1.5;		//	factor to make autocorrelation more obvious
			coeffs[2] = cyclicScale;
		}
		
		if (hasPattern(2) || hasPattern(3)) {			//	assumes no of seasons > 1
			double seasonEffect[] = getSeasonEffects();
			int nSeasons = seasonEffect.length;
			
			if (hasPattern(3))
				for (int i=0 ; i<nSeasons ; i++)
					seasonEffect[i] = -seasonEffect[i];
			
			double minEffect = seasonEffect[0];
			double maxEffect = minEffect;
			for (int i=1 ; i<nSeasons ; i++) {
				minEffect = Math.min(minEffect, seasonEffect[i]);
				maxEffect = Math.max(maxEffect, seasonEffect[i]);
			}
			
			double midEffect = (maxEffect + minEffect) / 2;
			double effectScale = cRange / (maxEffect - minEffect);
			
			coeffs[0] += (seasonEffect[0] - midEffect) * effectScale;
			int baseIndex = (nSeasons == 4) ? 2 : 5;
			for (int i=1 ; i<nSeasons ; i++)
				coeffs[baseIndex + i] = (seasonEffect[i] - seasonEffect[0]) * effectScale;
		}
		
		NumValue effectCoeffs[] = new NumValue[coeffs.length];
		for (int i=0 ; i<coeffs.length ; i++)
			effectCoeffs[i] = new NumValue(coeffs[i], 9);
		
		yModel.setParameters(effectCoeffs);
		
		double errorRange = getRange("error");
		double errorScale = cRange / errorRange;
		if (hasPattern(5))
			yModel.setSD(errorScale);
		else
			yModel.setSD(errorScale * 0.2);
	}
	
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		errorGenerator.setSeed(nextSeed());
		autoCorrelGenerator.setSeed(nextSeed());
		patternTypeGenerator.setSeed(nextSeed());
	}
	
	private int generateOnePattern() {
		double r = patternTypeGenerator.nextDouble();
		int nSeasons = getNoOfSeasons();
		if (nSeasons == 1)
			return r < 0.167 ? 0
						: r < 0.333 ? 1
						: r < 0.667 ? 4 : 5;
		else
			return r < 0.125 ? 0
						: r < 0.25 ? 1
						: r < 0.375 ? 2
						: r < 0.5 ? 3
						: r < 0.75 ? 4 : 5;
	}
	
	private int[] generateTwoPatterns() {
		int[] p = new int[2];
		p[0] = generateOnePattern();
		p[1] = generateOnePattern();
		while (true) {
			boolean error = (p[0] == p[1]) || (p[0] < 2 && p[1] < 2);		//	both equal or both linear
			if (p[0] >= 2 && p[0] < 5 && p[1] >= 2 && p[1] < 5)	//	both seasonal or both cyclic
				error = true;
			if (!error)
				break;
			p[1] = generateOnePattern();
		}
		if (p[0] > p[1]) {
			int temp = p[0];
			p[0] = p[1];
			p[1] = temp;
		}
		return p;
	}
	
	private double getAutoCorr(String key) {
		NumVariable xVar = (NumVariable)data.getVariable(key);
		int n = xVar.noOfValues();
		double xLag = xVar.doubleValueAt(0);
		double sx = 0.0;
		double sxx = 0.0;
		double sxLag = 0.0;
		for (int i=1 ; i<n ; i++) {
			double x = xVar.doubleValueAt(i);
			sx += x;
			sxx += x * x;
			sxLag += x * xLag;
			xLag = x;
		}
		double xMean = sx / (n - 1);
		return (sxLag - sx * xMean) / (sxx - sx * xMean);
	}
	
	protected void setDataForQuestion() {
		int n = getCount();
		patterns = generateTwoPatterns();
		
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable("rawError");
		errorVar.setSampleSize(n);
		errorVar.generateNextSample();
		
		IndexVariable trendVar = (IndexVariable)data.getVariable("trend");
		trendVar.setNoOfValues(n);
		
		CatVariable quarterVar = (CatVariable)data.getVariable("quarter");
		int values[] = new int[n];
		for (int i=0 ; i<n ; i++)
			values[i] = i % 4;
		quarterVar.setValues(values);
		
		CatVariable monthVar = (CatVariable)data.getVariable("month");
		for (int i=0 ; i<n ; i++)
			values[i] = i % 12;
		monthVar.setValues(values);
		
		
		ARVariable acVar = (ARVariable)data.getVariable("ac");
		acVar.setSerialCorr(getAutoCorrel());
		
		NumSampleVariable acErrorVar = (NumSampleVariable)data.getVariable("acError");
		acErrorVar.setSampleSize(n);
		Object cyclicConstraints = (getNoOfSeasons() == 12) ? kMonthlyCyclicConstraints
															: (getNoOfSeasons() == 4) ? kQuarterlyCyclicConstraints : kYearlyCyclicConstraints;
		MultipleRegnModel detrendCyclicModel = (MultipleRegnModel)data.getVariable("detrendCyclicModel");
		
		double minAutoCorr = 1 - 1.5 * (1 - getAutoCorrel());
		int iterations = 0;
		while(iterations < 40) {
			iterations ++;
			acErrorVar.generateNextSample();
			acVar.noteVariableChange("acError");
			detrendCyclicModel.setLSParams("ac", cyclicConstraints, kTrendDecimals, 9);
			double autoCorr = getAutoCorr("cyclic");
			if (autoCorr > minAutoCorr)
				break;
		}
//		System.out.println("Cyclic iterations = " + iterations);
		
		data.variableChanged("acError");
		
		
		MultipleRegnModel detrendErrorModel = (MultipleRegnModel)data.getVariable("detrendErrorModel");
		Object errorConstraints = (getNoOfSeasons() == 12) ? kMonthlyErrorConstraints
														: (getNoOfSeasons() == 4) ? kQuarterlyErrorConstraints : kYearlyErrorConstraints;
		detrendErrorModel.setLSParams("rawError", errorConstraints, kModelDecimals, 9);
		
		MultipleRegnModel yModel = (MultipleRegnModel)data.getVariable("yModel");
		setEffectCoeffs(yModel);
		
		data.variableChanged("rawError");
		
//		NumVariable cyclic = (NumVariable)data.getVariable("cyclic");
//		NumVariable error = (NumVariable)data.getVariable("error");
//		ResponseVariable yVar = (ResponseVariable)data.getVariable("y");
//		for (int i=0 ; i<trendVar.noOfValues() ; i++) {
//			System.out.println(trendVar.valueAt(i) + ": " + cyclic.valueAt(i) + " " + error.valueAt(i) + " " + yVar.valueAt(i));
//		}
	}
	
	
//-----------------------------------------------------------
	
	private int[] getMenuChoices() {
		int choice[] = new int[2];
		choice[0] = Math.min(componentChoice[0].getSelectedIndex(), componentChoice[1].getSelectedIndex());
		choice[1] = Math.max(componentChoice[0].getSelectedIndex(), componentChoice[1].getSelectedIndex());
		return choice;
	}
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		int menuChoices[] = getMenuChoices();
		
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Use the two pop-up menus to pick the most important two patterns in the time series.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You must make different selections from the two pop-up menus.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				for (int i=0 ; i<2 ; i++)
					messagePanel.insertText("\n" + kPatternAnswer[patterns[i]]);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!");
				messagePanel.insertText("\nYes. " + kPatternAnswer[patterns[0]]);
				if (patterns[1] == menuChoices[1])
					messagePanel.insertText("\nYes. " + kPatternAnswer[patterns[1]]);
				else
					messagePanel.insertText("\nThe other component is actually random not cyclical, but it is very difficult to distinguish these if there is a seasonal pattern.");
				break;
			case ANS_CLOSE:
				messagePanel.insertRedHeading("Close!");
				messagePanel.insertText("\nYes. " + kPatternAnswer[patterns[0]]);
				messagePanel.insertRedText("\nThe other component is actually "
									+ ((patterns[1] == 4) ? "cyclical not random" : "random not cyclical")
									+ ", but it is often difficult to distinguish between random and cyclical components.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				for (int i=0 ; i<patterns.length ; i++)
					if (menuChoices[i] == patterns[0] || menuChoices[i] == patterns[1])
						messagePanel.insertText("\n" + kPatternAnswer[menuChoices[i]]);
					else
						messagePanel.insertRedText("\n" + kPatternError[menuChoices[i]]);
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 130;
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<componentChoice.length ; i++)
			if (target == componentChoice[i]) {
				noteChangedWorking();
				return true;
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		int correct0 = patterns[0];
		int correct1 = patterns[1];
		
		int menuChoices[] = getMenuChoices();
		int menuChoice0 = menuChoices[0];
		int menuChoice1 = menuChoices[1];
		
		if (menuChoice0 == menuChoice1)
			return ANS_INVALID;
		
		if (menuChoice0 == correct0 && menuChoice1 == correct1)
			return ANS_CORRECT;
		
		if (menuChoice0 == correct0 && menuChoice0 >= 2 && menuChoice0 <= 3 && menuChoice1 > 3)
			return ANS_CORRECT;
		
		if (menuChoice0 == correct0 && correct1 > 3 && menuChoice1 > 3)
			return ANS_CLOSE;
		
		return ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		componentChoice[0].select(patterns[0]);
		componentChoice[1].select(patterns[1]);
	}
	
	protected double getMark() {
		int correct0 = patterns[0];
		int correct1 = patterns[1];
		
		int menuChoices[] = getMenuChoices();
		int menuChoice0 = menuChoices[0];
		int menuChoice1 = menuChoices[1];
		
		if (menuChoice0 == correct0 && menuChoice1 == correct1)			//	both correct
			return 1.0;
		
		if (menuChoice0 == correct0 && menuChoice0 >= 2 && menuChoice0 <= 3 && menuChoice1 > 3)		//	seasonal with cyclic instead of random
			return 1.0;
		
		if (menuChoice0 == correct0 && menuChoice0 <= 1 && correct1 > 3 && menuChoice1 > 3)		//	linear with cyclic/random error
			return 0.8;
		
		if (menuChoice0 == correct0 || menuChoice1 == correct1)		//	one correct
			return 0.2;
			
		return 0.0;
	}
}
