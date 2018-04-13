package exerciseGroupsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import distn.*;
import random.*;
import exercise2.*;
import formula.*;

import linMod.*;
import exerciseEstim.*;


abstract public class CoreCiApplet extends ExerciseApplet {
	static final protected double kExactFactor = 0.001;
	static final protected double kApproxFactor = 0.01;
	
	static final protected Color kGreenColor = new Color(0x006600);
	static final protected Color kWorkingBackground = new Color(0xE9E9FF);
	static final protected Color kDataBackground = new Color(0xFFEAF6);
	
	static final protected NumValue kOneValue = new NumValue(1, 0);
	
	static final protected String LOW_HIGH_BOUNDS_OPTION = "lowHigh";
	static final protected String ONLY_95_LEVEL_OPTION = "only95";
	
	protected TLookupPanel tLookupPanel;
	protected PlusMinusCalcPanel tseTemplate;
	
	protected CiResultPanel resultPanel;
	
	protected boolean showingCorrectAnswer = false;
	
//================================================
	
	protected void createDisplay() {
		
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
						resultPanel = new CiResultPanel(this, "Interval is", "", 6, CiResultPanel.HORIZONTAL,
																		hasOption(LOW_HIGH_BOUNDS_OPTION) ? CiResultPanel.LOW_HIGH_BOUNDS : CiResultPanel.MEAN_PLUS_MINUS);
						resultPanel.setFont(getBigFont());
						registerStatusItem("ci", resultPanel);
					insetPanel.add(resultPanel);
					
				ansPanel.add(insetPanel);
				
			bottomPanel.add(ansPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(120, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("ciLevel", "ciLevel");
		registerParameter("units", "string");
		registerParameter("maxSe", "const");
		registerParameter("maxPlusMinus", "const");
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
			if (hasOption(ONLY_95_LEVEL_OPTION))
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
	
//	--------------------------------------------------
	
	protected NumValue getMaxSe() {
		return getNumValueParam("maxSe");
	}
	
	protected NumValue getMaxPlusMinus() {
		return getNumValueParam("maxPlusMinus");
	}
	
	protected NumValue getCiPercent() {
		return getNumValueParam("ciLevel");
	}
	
	protected String getUnits() {
		String units = getStringParam("units");
		return (units == null) ? null : units;
	}
	
	
//-----------------------------------------------------------

	abstract protected DataSet getData();
	
	protected void setDisplayForQuestion() {
		if (tseTemplate != null) {
			tseTemplate.changeMaxValue(getMaxSe(), getMaxPlusMinus());
			tseTemplate.setValues(kOneValue, kOneValue);
		}
		
		tLookupPanel.reset();
		
		String joinerString = hasOption(LOW_HIGH_BOUNDS_OPTION) ? "to" : MText.expandText("#plusMinus#");
		resultPanel.changeLabel(ciLabelString(), joinerString);
		resultPanel.changeUnits(getUnits());
		resultPanel.clear();
	}
	
	abstract protected String ciLabelString();
	
	abstract protected void setDataForQuestion();
	
	
//-----------------------------------------------------------
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	
	abstract protected int getMessageHeight();
	
//-----------------------------------------------------------
	
	protected boolean effectivelySame(double x1, double x2, int decimals) {
		for (int i=0 ; i<decimals ; i++) {
			x1 *= 10;
			x2 *= 10;
		}
		return Math.rint(x1) == Math.rint(x2);
	}
	
	protected boolean effectivelySame(double x1, double x2) {
		double slop = Math.max(Math.abs(x1), Math.abs(x2)) * kExactFactor;
		return Math.abs(x1 - x2) < slop;
	}
	
	protected double getLowAttempt() {
		return resultPanel.getLowAttempt().toDouble();
	}
	
	protected double getHighAttempt() {
		return resultPanel.getHighAttempt().toDouble();
	}
	
	abstract protected double evaluateCorrectSe();
	
	protected double evaluatePlusMinus() {
		double se = evaluateCorrectSe();
		double t = getCorrectT();
		
		return t * se;
	}
	
	abstract protected double evaluateEstimate();
	
	abstract protected int getDf();
	
	protected double getQuantile(double lowTailProb) {
		int df = getDf();
		if (df > 0)
			return TTable.quantile(lowTailProb, df);
		else
			return NormalTable.quantile(lowTailProb);
	}
	
	protected double getCorrectT() {
		double lowTailProb = (1 + getCiPercent().toDouble() / 100) / 2;
		double t = getQuantile(lowTailProb);
		return Math.rint(t * 1000) / 1000;	//	round to 3 decimals
	}
	
	protected double evaluateLowCorrect(double plusMinus) {
		return evaluateEstimate() - plusMinus;
	}
	
	protected double evaluateHighCorrect(double plusMinus) {
		return evaluateEstimate() + plusMinus;
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
		int assessment = checkAttempt(lowAttempt, highAttempt, plusMinus);
		
		return assessment;
	}
	
	protected int checkAttempt(double lowAttempt, double highAttempt, double plusMinus) {
		double lowCorrect = evaluateLowCorrect(plusMinus);
		double highCorrect = evaluateHighCorrect(plusMinus);
		
		double lowAbsError = Math.abs(lowAttempt - lowCorrect);
		double highAbsError = Math.abs(highAttempt - highCorrect);
		
		double exactSlop = (Math.abs(lowCorrect) + Math.abs(highCorrect)) / 2 * kExactFactor;
		double approxSlop = (Math.abs(lowCorrect) + Math.abs(highCorrect)) / 2 * kApproxFactor;
		
		return (lowAbsError < exactSlop && highAbsError < exactSlop) ? ANS_CORRECT
										: (lowAbsError < approxSlop && highAbsError < approxSlop) ? ANS_CLOSE
										: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	
//-----------------------------------------------------------

	
	protected void showCorrectWorking() {
		int df = getDf();
		if (df > 0)
			tLookupPanel.setTDistnDf(df);
		double ciLevel = getCiPercent().toDouble() / 100;
		int ciDecimals = getCiPercent().decimals + 2;
		tLookupPanel.setConfidenceLevel(new NumValue(ciLevel, ciDecimals));
		
		double se = evaluateCorrectSe();
		
		NumValue t = new NumValue(getCorrectT(), 3);
		
		tseTemplate.setValues(t, new NumValue(se, getMaxSe().decimals));
		
		double plusMinus = evaluatePlusMinus();
		int decimals = getMaxPlusMinus().decimals;
		if (hasOption(LOW_HIGH_BOUNDS_OPTION)) {
			double lowCorrect = evaluateLowCorrect(plusMinus);
			double highCorrect = evaluateHighCorrect(plusMinus);
			resultPanel.showAnswer(new NumValue(lowCorrect, decimals), new NumValue(highCorrect, decimals));
		}
		else {
			double estimate = evaluateEstimate();
			resultPanel.showAnswer(new NumValue(estimate, decimals), new NumValue(plusMinus, decimals));
		}
	}
	
	protected double getMark() {
		int result = assessAnswer();
		
		return (result == ANS_CORRECT) ? 1.0 : (result == ANS_CLOSE) ? 0.5 : 0;
	}
	
	public boolean noteChangedWorking() {
		if (showingCorrectAnswer)						//	don't reset answering button highlight when showing correct answer
			return false;
		else
			return super.noteChangedWorking();
	}
}