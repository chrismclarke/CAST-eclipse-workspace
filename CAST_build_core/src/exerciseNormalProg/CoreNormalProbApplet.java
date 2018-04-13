package exerciseNormalProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import exercise2.*;

import exerciseNormal.*;


abstract public class CoreNormalProbApplet extends CoreLookupApplet
																											implements IntervalConstants {
	static final protected double kEps = 0.0002;
	static final protected NumValue kNaNValue = new NumValue(Double.NaN, 0);
	static final protected NumValue kOneValue = new NumValue(1.0, 0);
	static final protected NumValue kZeroValue = new NumValue(0.0, 0);
	
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	static final private String kMenuTitle = "Try:";
	static final private String[] kQuestionMenuOptions = {"Mixture of question types", "Only questions of form P(lower)",
																									"Only questions of form P(higher)", "Only questions of form P(between)", "Only questions of form P(outside)"};
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this, "Answer =", 6);
				registerStatusItem("answer", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
			if (hasOption("hasTypeChoice") && isPracticeMode()) {
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					XPanel qnTypePanel = createQuestionTypeMenu(kMenuTitle, kQuestionMenuOptions, OTHER_MENU);
					qnTypePanel.lockBackground(getBackground());
				choicePanel.add(qnTypePanel);
				bottomPanel.add(choicePanel);
			}
			
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
		registerParameter("mean", "const");
		registerParameter("sd", "const");
		registerParameter("intervalType", "choice");
		registerParameter("cut-offs", "cut-offs");
		registerParameter("questionType", "choice");			//	0 = prob, 1 = expected count, 2 = quantile
		registerParameter("count", "int");
		registerParameter("percent", "const");
		registerParameter("axis", "string");
		registerParameter("varName", "string");
		registerParameter("maxValue", "const");
	}
	
	protected void addTypeDelimiters() {
		addType("cut-offs", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("cut-offs")) {
			int questionType = tailType();
			StringTokenizer pst = new StringTokenizer(valueString, ",");
			NumValue lowLimit = new NumValue(pst.nextToken());
			NumValue highLimit = new NumValue(pst.nextToken());
			return new IntervalLimits(lowLimit.toDouble(), highLimit.toDouble(),
															Math.max(lowLimit.decimals, highLimit.decimals), questionType);
		}
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("cut-offs")) {
			StringTokenizer pst = new StringTokenizer(paramString, ":");
			int questionType = tailType();
			
			double mean = 0;
			double sd = 1;
			if (getMean() != null)
				mean = getMean().toDouble();
			if (getSD() != null)
				sd = getSD().toDouble();
			
			double zMin = Double.parseDouble(pst.nextToken());
			double zMax = Double.parseDouble(pst.nextToken());
			int decimals = Integer.parseInt(pst.nextToken());
			
			Random generator = new Random(nextSeed());
			double factor = 1.0;
			for (int i=0 ; i<decimals ; i++)
				factor *= 10.0;
			for (int i=0 ; i<-decimals ; i++)
				factor *= 0.1;
			double limit1 = Math.rint(factor * (mean + zMin * sd + (zMax - zMin) * sd * generator.nextDouble())) / factor;
			double limit2;
			do {
				limit2 = Math.rint(factor * (mean + zMin * sd + (zMax - zMin) * sd * generator.nextDouble())) / factor;
			} while (Math.abs(limit1 - limit2) < sd * (zMax - zMin) * 0.1);
			double lowerLimit = Math.min(limit1, limit2);
			double higherLimit = Math.max(limit1, limit2);
			
			IntervalLimits limits = null;
			decimals = Math.max(decimals, 0);
			switch (questionType) {
				case LESS_THAN:
				case LESS_THAN_SIMPLE:
					limits = new IntervalLimits(Double.NEGATIVE_INFINITY, limit1, decimals, questionType);
					break;
				case GREATER_THAN:
				case GREATER_THAN_SIMPLE:
					limits = new IntervalLimits(limit1, Double.POSITIVE_INFINITY, decimals, questionType);
					break;
				case BETWEEN:
					limits = new IntervalLimits(lowerLimit, higherLimit, decimals, questionType);
					break;
				case OUTSIDE:
					limits = new IntervalLimits(higherLimit, lowerLimit, decimals, questionType);
					break;
				case WITHIN_ZERO:
					limit2 = Math.rint(factor * (mean + zMin * sd - (zMax - zMin) * sd * generator.nextDouble())) / factor;
					limits = new IntervalLimits(Double.NEGATIVE_INFINITY, Math.min(Math.abs(limit1), Math.abs(limit2)), decimals, questionType);
					break;
				case OUTSIDE_ZERO:
					limit2 = Math.rint(factor * (mean + zMin * sd - (zMax - zMin) * sd * generator.nextDouble())) / factor;
					limits = new IntervalLimits(Double.NEGATIVE_INFINITY, Math.min(Math.abs(limit1), Math.abs(limit2)), decimals, questionType);
					break;
			}
			return limits;
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	protected NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	protected NumValue getSD() {
		return getNumValueParam("sd");
	}
	
	protected IntervalLimits getLimits() {
		return (IntervalLimits)getObjectParam("cut-offs");
	}
	
	protected int getQuestionType() {
		if (getObjectParam("questionType") == null)
			return 0;
		else
			return getIntParam("questionType");
	}
	
	protected boolean isCountQuestionType() {
		return getQuestionType() == 1 && getObjectParam("count") != null;
	}
	
	protected int getN() {
		return getIntParam("count");
	}
	
	protected NumValue getPercent() {
		return getNumValueParam("percent");
	}
	
	protected int tailType() {
		if (otherChoice != null && otherChoice.getSelectedIndex() > 0)
			return otherChoice.getSelectedIndex() - 1;
		else
			return getIntParam("intervalType");
	}
	
	public String getAxisInfo() {
		return getStringParam("axis");
	}
	
	public String getVarName() {
		return getStringParam("varName");
	}
	
	public NumValue getMaxValue() {
		return getNumValueParam("maxValue");
	}
	
//-----------------------------------------------------------
	
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NormalDistnVariable normalDistn = new NormalDistnVariable("Distn");
		data.addVariable("distn", normalDistn);
		
		return data;
	}
	
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected double evaluateProbability(IntervalLimits limits) {
		DistnVariable distn = (DistnVariable)data.getVariable("distn");
		NumValue start = limits.startVal;
		NumValue end = limits.endVal;
		double prob;
		
		if (limits.questionType == WITHIN_ZERO)
			prob = distn.getCumulativeProb(end.toDouble()) - distn.getCumulativeProb(-end.toDouble());
		else if (limits.questionType == OUTSIDE_ZERO)
			prob = 1 - distn.getCumulativeProb(end.toDouble()) + distn.getCumulativeProb(-end.toDouble());
		else if (start == null)
			prob = distn.getCumulativeProb(end.toDouble());
		else if (end == null)
			prob = 1 - distn.getCumulativeProb(start.toDouble());
		else if (start.toDouble() <= end.toDouble())
			prob = distn.getCumulativeProb(end.toDouble()) - distn.getCumulativeProb(start.toDouble());
		else
			prob = distn.getCumulativeProb(end.toDouble()) + 1 - distn.getCumulativeProb(start.toDouble());
		return prob;
	}
	
	protected double evaluatePercentile(NumValue percent, String distnKey) {
		ContinDistnVariable distn = (ContinDistnVariable)data.getVariable(distnKey);
		double cumulativeProb = percent.toDouble() * 0.01;
		if (tailType() == GREATER_THAN)
			cumulativeProb = 1.0 - cumulativeProb;
		return distn.getQuantile(cumulativeProb);
	}
	
//-------------------------------------------------------------------------
//															all already declared abstract in ExerciseApplet
	
	abstract protected XPanel getWorkingPanels(DataSet data);
	abstract protected void setDataForQuestion();
	abstract protected void setDisplayForQuestion();
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	abstract protected int getMessageHeight();
	
	abstract protected int assessAnswer();
	abstract protected void giveFeedback();
	abstract protected void showCorrectWorking();
	
	protected double getMark() {
		int markType = assessAnswer();
		return (markType == ANS_CORRECT) ? 1 : (markType == ANS_CLOSE) ? 0.8 : 0;
	}
}