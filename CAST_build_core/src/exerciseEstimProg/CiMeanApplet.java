package exerciseEstimProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import random.*;
import coreGraphics.*;
import exercise2.*;
import formula.*;
import imageGroups.*;


import sampling.*;
import exerciseEstim.*;


public class CiMeanApplet extends ExerciseApplet {
//	static final private String SIGMA_PARAM = "sigma";
	
	static final private double kExactFactor = 0.001;
	static final private double kApproxFactor = 0.01;
	
	static final private NumValue kOneValue = new NumValue(1, 0);
	
	private RandomNormal generator;
//	private boolean alwaysKnownSigma;
	private boolean knownSigma;
	
	private HorizAxis theAxis;
	private StackedDotPlotView theView;
	private SummaryView meanView, sdView;
	
	private TLookupPanel tLookupPanel;
	
	private TSeTemplatePanel tseTemplate;
	
	private CiResultPanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
//		String sigmaParam = getParameter(SIGMA_PARAM);
//		alwaysKnownSigma = (sigmaParam != null) && sigmaParam.equals("alwaysKnown");
		
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				XPanel ansPanel = new XPanel();
				ansPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					
					XPanel insetPanel = new InsetPanel(10, 2);
					insetPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						resultPanel = new CiResultPanel(this, "Interval is", "", 6);
						resultPanel.setFont(getBigFont());
						registerStatusItem("ci", resultPanel);
					insetPanel.add(resultPanel);
					
//					insetPanel.lockBackground(kAnswerBackground);
				ansPanel.add(insetPanel);
				
			bottomPanel.add(ansPanel);
			
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
		registerParameter("varName", "string");
		registerParameter("noOfValues", "int");
		registerParameter("axis", "string");
		registerParameter("mean", "const");
		registerParameter("sd", "const");
		registerParameter("maxPlusMinus", "const");
		registerParameter("units", "string");
		registerParameter("ciLevel", "ciLevel");
		registerParameter("popnSd", "const");
	}
	
	protected void addTypeDelimiters() {
		addType("ciLevel", ",");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("ciLevel"))
			return super.createConstObject("const", valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("ciLevel")) {				//		must have paramString == ":" to be recognised as random object
			int level;
			if (hasOption("only95"))
				level = 95;
			else {
				switch(new RandomInteger(0, 2, 1, nextSeed()).generateOne()) {
					case 0:
						level = 90;
						break;
					case 1:
						level = 99;
						break;
					case 2:
					default:
						level = 95;
						break;
				}
			}
			return new NumValue(level, 0);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	private String getVarName() {
		return getStringParam("varName");
	}
	
	private int getNoOfValues() {
		return getIntParam("noOfValues");
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	private NumValue getSd() {
		return getNumValueParam("sd");
	}
	
	private NumValue getMaxPlusMinus() {
		return getNumValueParam("maxPlusMinus");
	}
	
	private String getUnits() {
		return getStringParam("units");
	}
	
	private NumValue getCiPercent() {
		return getNumValueParam("ciLevel");
	}
	
	private int getMeanSdDecimals() {
		return Math.max(getMean().decimals, getSd().decimals);
	}
	
	private NumValue getSigma() {
		return getNumValueParam("popnSd");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 4));
		
			XPanel dataPanel = new InsetPanel(40, 0);
			dataPanel.setLayout(new BorderLayout(10, 0));
			
			dataPanel.add("Center", dotPlotPanel(data));
			dataPanel.add("East", summaryPanel(data));
			
		thePanel.add("North", dataPanel);
		
			XPanel bottomPanel = new InsetPanel(0, 7, 0, 0);
			bottomPanel.setLayout(new BorderLayout(10, 0));
				
				int distnType = hasOption("onlyNormal") ? TLookupPanel.NORMAL_ONLY : TLookupPanel.T_AND_NORMAL;
				tLookupPanel = new TLookupPanel(this, TLookupPanel.QUANTILE_LOOKUP, distnType);
			bottomPanel.add("Center", tLookupPanel);
				
				XPanel templatePanel = new InsetPanel(0, 20, 0, 0);
				templatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
				
					FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
					tseTemplate = new TSeTemplatePanel(new NumValue(0), stdContext);
					registerStatusItem("tseTemplate", tseTemplate);
				
				templatePanel.add(tseTemplate);
				
			bottomPanel.add("East", templatePanel);
			bottomPanel.lockBackground(kWorkingBackground);
				
		thePanel.add("Center", bottomPanel);
		
		return thePanel;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(50, 50));
				
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				theAxis = new HorizAxis(this);
			innerPanel.add("Bottom", theAxis);
			
				theView = new StackedDotPlotView(data, this, theAxis);
				theView.lockBackground(Color.white);
			innerPanel.add("Center", theView);
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data) {
		MeanSDImages.loadMeanSD(this);
		
		XPanel statisticPanel = new InsetPanel(20, 10, 0, 35);
		statisticPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 3));
		
		SummaryView countView = new SummaryView(data, this, "y", null, SummaryView.COUNT, 0, SummaryView.SAMPLE);
		statisticPanel.add(countView);
		
		meanView = new SummaryView(data, this, "y", null, SummaryView.MEAN, 9, SummaryView.SAMPLE);
		meanView.setForeground(Color.blue);
		statisticPanel.add(meanView);
		
		sdView = new SummaryView(data, this, "y", null, SummaryView.SD, 9, SummaryView.SAMPLE);
		sdView.setForeground(Color.red);
		statisticPanel.add(sdView);
		
		return statisticPanel;
	}
	
	protected void setDisplayForQuestion() {
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		int decimals = getMeanSdDecimals();
		meanView.setDecimals(decimals);
		sdView.setDecimals(decimals);
		
		NumValue maxTemplate = getMaxPlusMinus();
		tseTemplate.changeMaxValue(maxTemplate);
		tseTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		tLookupPanel.reset();
		
		resultPanel.changeUnits(getUnits());
		resultPanel.clear();
		
		data.variableChanged("y");
	}
	
	protected void setDataForQuestion() {
		int n = getNoOfValues();
		
		generator.setMean(getMean().toDouble());
		generator.setSD(getSd().toDouble());
		
		NumSampleVariable yVar = (NumSampleVariable)data.getVariable("y");
		yVar.setSampleSize(n);
		yVar.name = getVarName();
		yVar.generateNextSample();
		
		knownSigma = getSigma() != null;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				if (knownSigma)
					messagePanel.insertText("Use the standard normal distribution");
				else
					messagePanel.insertText("Use the t distribution (adjusting the degrees of freedom if necessary)");
				messagePanel.insertText(" and the template on its right to help find a " + getCiPercent() + "% CI for the mean. Finally type its end points into the boxes above.\n");
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
				if (knownSigma)
					messagePanel.insertText("The standard error of the mean should be found from the known population standard devn, #sigma#, not the sample standard devn, s, and is ");
				else
					messagePanel.insertText("The standard error of the mean is ");
				insertSeFormula(messagePanel);
				messagePanel.insertText(".\n" + getCiPercent() + "% of the ");
				if (knownSigma)
					messagePanel.insertText("standard normal distribution");
				else
					messagePanel.insertText("t distribution with (n-1) = " + (getNoOfValues() - 1) + " df");
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
				if (knownSigma)
					insertNormalHints(messagePanel);
				else
					insertTHints(messagePanel);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The end-points of your confidence interval are not close enough to the correct ones.\n");
				if (knownSigma)
					insertNormalHints(messagePanel);
				else
					insertTHints(messagePanel);
				break;
		}
	}
	
	private void insertSeFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		MFormula sd = new MText(knownSigma ? getSigma().toString() : sdView.getValueString(), context);
		MFormula rootN = new MRoot(new MText(String.valueOf(getNoOfValues()), context), context);
		MFormula ratio = new MRatio(sd, rootN, true, context);
		
		MFormula name = new MText("se(x#bar#)", context);
		
		messagePanel.insertFormula(new MBinary(MBinary.EQUALS, name, ratio, context));
	}
	
	private void insertIntervalFormula(MessagePanel messagePanel) {
		FormulaContext context = new FormulaContext(null, null, this);
		
		MFormula t = new MConst(new NumValue(getCorrectT(), 3), context);
		MFormula se = new MText("se(x#bar#)", context);
		MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
		
		MFormula xBar = new MText("x#bar#", context);
		
		MFormula ci1 = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
		
		MFormula mean = new MText(meanView.getValueString(), context);
		MFormula tseVal = new MConst(tseTemplate.getResult(), context);
		
		MFormula ci2 = new MBinary(MBinary.PLUS_MINUS, mean, tseVal, context);
		
		MFormula f = new MBinary(MBinary.EQUALS, ci1, ci2, context);
		
		messagePanel.insertFormula(f);
	}
	
	private void insertNormalHints(MessagePanel messagePanel) {
		if (!tLookupPanel.isCorrectDf(Double.POSITIVE_INFINITY))
			messagePanel.insertText("Since the population standard deviation, #sigma#, is known, you should use the normal distribution, not a t distribution.");
		else {
			if (!hasOption("onlyNormal"))
				messagePanel.insertText("You have correctly used the normal distribution, not a t distribution, since the population standard deviation, #sigma#, is known.\n");
				
			NumValue ciPercent = getCiPercent();
			NumValue ciLevel = new NumValue(ciPercent.toDouble() / 100, ciPercent.decimals + 2);
			if (!tLookupPanel.isCorrectConfidenceLevel(ciLevel))
				messagePanel.insertText("You should find the z-score for which " + ciPercent + "% of the area is between #plusMinus#z. (The red area in the centre of the distribution should be " + ciLevel + ".)");
			else {
				messagePanel.insertText("You have found the correct z-score for a " + ciPercent + "% confidence interval.");
				
				boolean copiedT = tseTemplate.getT().toDouble() == getCorrectT();
				boolean correctSd = tseTemplate.getSd().toDouble() == getSigma().toDouble();
				boolean correctN = tseTemplate.getN().toDouble() == getNoOfValues();
				if (!copiedT || !correctSd || !correctN) {
					if (!copiedT)
						messagePanel.insertText(" However you do not seem to have used this value in the template.");
					if (!correctSd)
						messagePanel.insertText("\nYou have not used the value of #sigma# given in the question in the template. (Make sure that you use #sigma#, not the sample standard deviation, s.)");
					if (!correctN)
						messagePanel.insertText("\nYou have not used the sample size, n=" + getNoOfValues() + ", in the template.");
				}
				else {
					messagePanel.insertText(" And you seem to have found the \"#plusMinus#\" value correctly in the template.\nThe confidence interval should be (x#bar# - this value) to(x#bar# + this value),\n");
					messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
					
					FormulaContext context = new FormulaContext(null, null, this);
					MFormula sd = new MText("#sigma#", context);
					MFormula rootN = new MRoot(new MText("n", context), context);
					MFormula se = new MRatio(sd, rootN, context);
					
					MFormula t = new MText("z", context);
					MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
					
					MFormula xBar = new MText("x#bar#", context);
					MFormula ci = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
					
					messagePanel.insertFormula(ci);
				}
			}
		}
	}
	
	private void insertTHints(MessagePanel messagePanel) {
		int df = getNoOfValues() - 1;
		if (!tLookupPanel.isCorrectDf(df))
			messagePanel.insertText("Firstly use a t distribution with (n-1) = "  + df + " df.");
		else {
			messagePanel.insertText("You have used the correct degrees of freedom for the t distribution.\n");
			
			NumValue ciPercent = getCiPercent();
			NumValue ciLevel = new NumValue(ciPercent.toDouble() / 100, ciPercent.decimals + 2);
			if (!tLookupPanel.isCorrectConfidenceLevel(ciLevel))
				messagePanel.insertText("Next find the t-value for which " + ciPercent + "% of the area is between #plusMinus#t. (The red centre area in the distribution should be " + ciLevel + ".)");
			else {
				messagePanel.insertText("You have found the correct t-value for a " + ciPercent + "% confidence interval.");
				
				boolean copiedT = tseTemplate.getT().toDouble() == getCorrectT();
				boolean correctSd = tseTemplate.getSd().toDouble() == Double.parseDouble(sdView.getValueString());
				boolean correctN = tseTemplate.getN().toDouble() == getNoOfValues();
				if (!copiedT || !correctSd || !correctN) {
					if (!copiedT)
						messagePanel.insertText(" However you do not seem to have used this value in the template.");
					if (!correctSd)
						messagePanel.insertText("\nYou have not used the correct value of the standard deviation, s, in the template.");
					if (!correctN)
						messagePanel.insertText("\nYou have not used the sample size, n=" + getNoOfValues() + ", in the template.");
				}
				else {
					messagePanel.insertText(" And you seem to have found the \"#plusMinus#\" value correctly in the template.\nThe confidence interval should be (x#bar# - this value) to (x#bar# + this value),\n");
					messagePanel.setAlignment(MessagePanel.CENTER_ALIGN);
					
					FormulaContext context = new FormulaContext(null, null, this);
					MFormula sd = new MText("s", context);
					MFormula rootN = new MRoot(new MText("n", context), context);
					MFormula se = new MRatio(sd, rootN, context);
					
					MFormula t = new MText("t", context);
					MFormula tse = new MBinary(MBinary.TIMES, t, se, context);
					
					MFormula xBar = new MText("x#bar#", context);
					MFormula ci = new MBinary(MBinary.PLUS_MINUS, xBar, tse, context);
					
					messagePanel.insertFormula(ci);
				}
			}
		}
	}
	
	protected int getMessageHeight() {
		return 130;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			generator = new RandomNormal(10, 0.0, 1.0, 2.0);
			generator.setSeed(nextSeed());
			NumSampleVariable yVar = new NumSampleVariable("Y", generator, 9);
			yVar.generateNextSample();
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private double getLowAttempt() {
		return resultPanel.getLowAttempt().toDouble();
	}
	
	private double getHighAttempt() {
		return resultPanel.getHighAttempt().toDouble();
	}
	
	private double evaluatePlusMinus() {
		double s = knownSigma ? getSigma().toDouble()
									: Double.parseDouble(sdView.getValueString());
		int n = getNoOfValues();
		double se = s / Math.sqrt(n);
		
		double t = getCorrectT();
		
		return t * se;
	}
	
	private double getCorrectT() {
		double lowTailProb = (1 + getCiPercent().toDouble() / 100) / 2;
		double t;
		if (knownSigma)
			t = NormalTable.quantile(lowTailProb);
		else {
			int df = getNoOfValues() - 1;
			t = TTable.quantile(lowTailProb, df);
		}
		return Math.rint(t * 1000) / 1000;	//	round to 3 decimals
	}
	
	private double evaluateLowCorrect(double plusMinus) {
		double mean = Double.parseDouble(meanView.getValueString());
		return mean - plusMinus;
	}
	
	private double evaluateHighCorrect(double plusMinus) {
		double mean = Double.parseDouble(meanView.getValueString());
		return mean + plusMinus;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		if (resultPanel.isIncomplete())
			return ANS_INCOMPLETE;
			
		double lowAttempt = getLowAttempt();
		double highAttempt = getHighAttempt();
		if (lowAttempt >= highAttempt)
			return ANS_INVALID;
		
		double plusMinus = evaluatePlusMinus();
		double lowCorrect = evaluateLowCorrect(plusMinus);
		double highCorrect = evaluateHighCorrect(plusMinus);
		
		double lowAbsError = Math.abs(lowAttempt - lowCorrect);
		double highAbsError = Math.abs(highAttempt - highCorrect);
		
		double exactSlop = (lowCorrect + highCorrect) / 2 * kExactFactor;
		double approxSlop = (lowCorrect + highCorrect) / 2 * kApproxFactor;
		
		return (lowAbsError < exactSlop && highAbsError < exactSlop) ? ANS_CORRECT
										: (lowAbsError < approxSlop && highAbsError < approxSlop) ? ANS_CLOSE
										: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		int n = getNoOfValues();
		int df = n - 1;
		
		if (knownSigma)
			tLookupPanel.setNormalDistn();
		else
			tLookupPanel.setTDistnDf(df);
		double ciLevel = getCiPercent().toDouble() / 100;
		int ciDecimals = getCiPercent().decimals + 2;
		tLookupPanel.setConfidenceLevel(new NumValue(ciLevel, ciDecimals));
		
		NumValue sd = knownSigma ? getSigma() : new NumValue(sdView.getValueString());
		NumValue nValue = new NumValue(n, 0);
		NumValue t = new NumValue(getCorrectT(), 3);
		
		tseTemplate.setValues(t, sd, nValue);
		
		double plusMinus = evaluatePlusMinus();
		double lowCorrect = evaluateLowCorrect(plusMinus);
		double highCorrect = evaluateHighCorrect(plusMinus);
		int decimals = getMaxPlusMinus().decimals;
		resultPanel.showAnswer(new NumValue(lowCorrect, decimals), new NumValue(highCorrect, decimals));
	}
	
	protected double getMark() {
		int result = assessAnswer();
		
		return (result == ANS_CORRECT) ? 1.0 : (result == ANS_CLOSE) ? 0.5 : 0;
	}
}