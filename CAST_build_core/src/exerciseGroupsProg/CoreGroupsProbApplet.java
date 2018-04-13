package exerciseGroupsProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import distn.*;
import random.*;
import exercise2.*;
import formula.*;


import exerciseNormal.*;
import exerciseNormal.JdistnAreaLookup.*;
import exerciseNormalProg.*;
import exerciseGroups.*;


abstract public class CoreGroupsProbApplet extends CoreLookupApplet implements GroupsIntervalConstants {
	static final private int kZDecimals = 3;
	static final protected double kEps = 0.0002;
	
	static final protected NumValue kOneValue = new NumValue(1.0, 0);
	static final protected NumValue kZeroValue = new NumValue(0.0, 0);
	
	static private AlmostRandomInteger generator;
	
//---------------------------------------------------------------------
	
	private NormalLookupPanel zLookupPanel;
	private ZTemplatePanel zTemplate;
	protected boolean swapGroups, sumNotDiff;
	
	protected ResultValuePanel resultPanel;
	
	
//-----------------------------------------------------------
//	Abstract methods in CoreLookupApplet must be defined but are not used
	
	public String getAxisInfo() { return null;}
	public String getVarName() { return null;}
	
	
//-----------------------------------------------------------
	
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
						
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		registerParameter("maxMeanParam", "const");
		registerParameter("maxSdParam", "const");
		registerParameter("group-cut-offs", "group-cut-offs");
		registerParameter("mean1", "const");
		registerParameter("sd1", "const");
		registerParameter("mean2", "const");
		registerParameter("sd2", "const");
	}
	
	protected void addTypeDelimiters() {
		addType("group-cut-offs", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("group-cut-offs")) {
			StringTokenizer pst = new StringTokenizer(valueString, "*");
			NumValue cutoffVal = new NumValue(pst.nextToken());
			int questionType = Integer.parseInt(pst.nextToken());
			String groupAString = pst.nextToken();
			String groupBString = pst.nextToken();
			String unitsString = pst.hasMoreTokens() ? pst.nextToken() : null;
			String totalString = pst.hasMoreTokens() ? pst.nextToken() : null;
			return new GroupsIntervalLimits(cutoffVal, questionType, groupAString, groupBString, unitsString, totalString);
		}
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("group-cut-offs")) {
			
			StringTokenizer pst = new StringTokenizer(paramString, ":");
			double z = Double.parseDouble(pst.nextToken());			//	will be generated random between -z and z
			int decimals = Integer.parseInt(pst.nextToken());
			String groupAString = pst.nextToken();
			String groupBString = pst.nextToken();
			String unitsString = pst.hasMoreTokens() ? pst.nextToken() : null;
			if (unitsString.equals("null"))		//	null must be possible if there is a total but no units
				unitsString = null;
			String totalString = pst.hasMoreTokens() ? pst.nextToken() : null;
			int noOfQuestionTypes = totalString == null ? 3 : 5;
			if (generator == null || generator.getMax() != noOfQuestionTypes)
				generator = new AlmostRandomInteger(0, noOfQuestionTypes, nextSeed());
			int questionType = generator.generateOne();
			
			sumNotDiff = questionType == TOTAL_MORE || questionType == TOTAL_LESS;
			swapGroups = false;
			double mean = getVarMean().toDouble();
			double sd = getVarSd().toDouble();
			
			Random generator = new Random(nextSeed());
			double factor = 1.0;
			for (int i=0 ; i<decimals ; i++)
				factor *= 10.0;
			for (int i=0 ; i<-decimals ; i++)
				factor *= 0.1;
			double limit = Math.rint(factor * (mean + z * sd * (1 - 2 * generator.nextDouble()))) / factor;
			
			if (questionType <= 1) {
				swapGroups = limit < 0;
				if (swapGroups) {
					String temp = groupAString;
					groupAString = groupBString;
					groupBString = temp;
					limit = -limit;
					questionType = 1 - questionType;
				}
			}
			else {
				double limit2 = Math.rint(factor * (mean - z * sd * generator.nextDouble())) / factor;
				limit = Math.min(Math.abs(limit), Math.abs(limit2));
			}
			NumValue cutoff = new NumValue(limit, decimals);
			
			return new GroupsIntervalLimits(cutoff, questionType, groupAString, groupBString, unitsString, totalString);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	public NumValue getMaxValue() {			//	needed for normal lookup template
		return new NumValue(-9, kZDecimals);
	}
	
	public NumValue getMaxMeanParam() {
		return getNumValueParam("maxMeanParam");
	}
	
	public NumValue getMaxSdParam() {
		return getNumValueParam("maxSdParam");
	}
	
	abstract protected NumValue getVarMean();		//	returns mean for difference. Used when generating cutoffs
	
	abstract protected NumValue getVarSd();		//	returns sd for difference. Used when generating cutoffs
	
	protected NumValue getMean1() {
		return getNumValueParam("mean1");
	}
	
	protected NumValue getSD1() {
		return getNumValueParam("sd1");
	}
	
	protected NumValue getMean2() {
		NumValue mean2 = getNumValueParam("mean2");
		if (mean2 != null)
			return mean2;
		else
			return getMean1();
	}
	
	protected NumValue getSD2() {
		NumValue sd2 = getNumValueParam("sd2");
		if (sd2 != null)
			return sd2;
		else
			return getSD1();
	}
	
	protected GroupsIntervalLimits getGroupLimits() {
		return (GroupsIntervalLimits)getObjectParam("group-cut-offs");
	}
	
//-----------------------------------------------------------
	
	protected NormalLookupPanel createZLookupPanel() {
		zLookupPanel = new NormalLookupPanel(data, "z", this, NormalLookupPanel.HIGH_AND_LOW,
																																	NormalLookupPanel.SINGLE_DENSITY);
		registerStatusItem("zLookup", zLookupPanel);
		return zLookupPanel;
	}
	
	protected ZTemplatePanel createZTemplate() {
		FormulaContext templateContext = new FormulaContext(kTemplateColor, getStandardFont(), this);
		zTemplate = new ZTemplatePanel("z =", 5, templateContext);
		registerStatusItem("zTemplate", zTemplate);
		return zTemplate;
	}
	
	protected void setDataForQuestion() {
		NumValue mean = getVarMean();
		NumValue sd = getVarSd();
		
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		normalDistn.setMean(mean);
		normalDistn.setSD(sd);
		
		NormalDistnVariable zDistn = (NormalDistnVariable)data.getVariable("z");
		zDistn.setMinSelection(-1.0);
		zDistn.setMaxSelection(1.0);
	}
	
//-----------------------------------------------------------
	
	
	protected void addDensityComment(MessagePanel messagePanel) {
		int intervalType = getGroupLimits().questionType;
		switch (intervalType) {
			case DIFF_LESS:
			case TOTAL_LESS:
				messagePanel.insertText("\nThe probabiity is the area under the normal density to the left of this z-score.");
				break;
			case DIFF_MORE:
			case TOTAL_MORE:
				messagePanel.insertText("\nThe probabiity is the area under the normal density to the right of this z-score.");
				break;
			case WITHIN_OF_ZERO:
				messagePanel.insertText("\nThe probabiity is the area under the normal density between these two z-scores.");
				break;
			case OUTSIDE_OF_ZERO:
				messagePanel.insertText("\nThe probabiity is the sum of the areas under the normal density beyond these two z-scores.");
				break;
			default:
		}
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
			NormalDistnVariable normalDistn = new NormalDistnVariable("Distn");
		data.addVariable("distn", normalDistn);
		
			NormalDistnVariable zDistn = new NormalDistnVariable("Z");
			zDistn.setMean(0.0);
			zDistn.setSD(1.0);
		data.addVariable("z", zDistn);
		
		return data;
	}
	
	protected void resetZ() {
		zLookupPanel.resetPanel();
		zTemplate.setValues(kOneValue, kOneValue, kOneValue);
		
		data.variableChanged("z");
	}
	
//-----------------------------------------------------------
	
	
	protected void showCorrectZ() {
		NormalDistnVariable normalDistn = (NormalDistnVariable)data.getVariable("distn");
		NumValue meanVal = normalDistn.getMean();
		NumValue sdVal = normalDistn.getSD();
		
		GroupsIntervalLimits groupLimits = getGroupLimits();
		NumValue cutoff = groupLimits.cutoff;
		NumValue z = new NumValue((cutoff.toDouble() - meanVal.toDouble()) / sdVal.toDouble(), kZDecimals);
		
		if (groupLimits.questionType == WITHIN_OF_ZERO || groupLimits.questionType == OUTSIDE_OF_ZERO) {
			NumValue negativeZ = new NumValue((-cutoff.toDouble() - meanVal.toDouble()) / sdVal.toDouble(), kZDecimals);
			zLookupPanel.showAnswer(negativeZ, z);
		}
		else
			zLookupPanel.showAnswer(null, z);
		
		zTemplate.setValues(cutoff, meanVal, sdVal);
	}
	
	protected double maxExactError() {
		return kEps;
	}
	
	protected double maxCloseError(NumValue startZ, NumValue endZ) {
		return zLookupPanel.getPixError(startZ, endZ);
	}
	
	protected double evaluateProbability() {
		DistnVariable distn = (DistnVariable)data.getVariable("distn");
		GroupsIntervalLimits groupLimits = getGroupLimits();
		
		double start = groupLimits.getStartX();
		double end = groupLimits.getEndX();
		double prob = distn.getCumulativeProb(end) - distn.getCumulativeProb(start);
		if (prob < 0)	//	for OUTSIDE_OF_ZERO
			prob = 1 + prob;
		
		return prob;
	}
	
	
	protected double getAttempt() {
		return resultPanel.getAttempt().toDouble();
	}
	
	protected int assessAnswer() {
		double attempt = getAttempt();
		
		if (resultPanel.isClear())
			return ANS_INCOMPLETE;
		else if(attempt < 0.0 || attempt > 1.0)
			return ANS_INVALID;
		else {
			GroupsIntervalLimits groupLimits = getGroupLimits();
			double correct = evaluateProbability();
			
			double mean = getVarMean().toDouble();
			double sd = getVarSd().toDouble();
			NumValue startZ = Double.isInfinite(groupLimits.getStartX()) ? null : new NumValue((groupLimits.getStartX() - mean) / sd);
			NumValue endZ = Double.isInfinite(groupLimits.getEndX()) ? null : new NumValue((groupLimits.getEndX() - mean) / sd);
			
			if (Math.abs(correct - attempt) <= maxExactError())
				return ANS_CORRECT;
			else {
				double maxCloseError = maxCloseError(startZ, endZ);
				
				return (Math.abs(correct - attempt) <= maxCloseError) ? ANS_CLOSE : ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
	}
}