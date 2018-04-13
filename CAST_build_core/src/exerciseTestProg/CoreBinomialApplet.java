package exerciseTestProg;

import java.util.*;

import dataView.*;
import distn.*;


abstract public class CoreBinomialApplet extends CoreTestApplet {
	
	protected void addTypeDelimiters() {
		addType("nSuccess", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("nSuccess"))
			return Integer.valueOf(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("nSuccess")) {		//	assumes nTrials, pSuccess and tail already set
			int nTrials = getNTrials();
			double pSuccess = getPSuccess().toDouble();
			double mean = nTrials * pSuccess;
			double sd = Math.sqrt(mean * (1 - pSuccess));
			
			Random rand = new Random(nextSeed());
			double r = rand.nextDouble();
			double pMin = (r < 0.25) ? 0    : (r < 0.5) ? 0.01 : (r < 0.75) ? 0.05 : 0.1;
			double pMax = (r < 0.25) ? 0.01 : (r < 0.5) ? 0.05 : (r < 0.75) ? 0.1  : 1.0;
			
			double approxPValue = pMin + rand.nextDouble() * (pMax - pMin);
			
			double cumulative = approxPValue;
			switch (getTail()) {
				case TAIL_LOW:
				case TAIL_LOW_EQ:
					break;
				case TAIL_HIGH:
				case TAIL_HIGH_EQ:
					cumulative = 1 - approxPValue;
					break;
				case TAIL_BOTH:
					cumulative = approxPValue / 2;
					if (new Random(nextSeed()).nextDouble() > 0.5)
						cumulative = 1 - cumulative;
					break;
			}
			
			double z = mean + sd * NormalTable.quantile(cumulative);
			
			return Integer.valueOf(Math.max(0, Math.min(nTrials, (int)Math.round(z))));
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("nTrials", "int");
//		registerParameter("probIndex", "int");
		registerParameter("pSuccess", "const");
		registerParameter("nSuccess", "nSuccess");
		registerParameter("trialsName", "string");
		registerParameter("successName", "string");
		registerParameter("successesName", "string");
		registerParameter("maxValue", "const");
	}
	
	protected int getNTrials() {
		return getIntParam("nTrials");
	}
	
	protected NumValue getPSuccess() {
		return getNumValueParam("pSuccess");
	}
	
	protected int getNSuccess() {
		return getIntParam("nSuccess");
	}
	
	protected String getTrialsName() {
		return getStringParam("trialsName");
	}
	
	protected String getSuccessName() {
		return getStringParam("successName");
	}
	
	protected String getSuccessesName() {
		return getStringParam("successesName");
	}
	
	public String getVarName() {		//	displayed on axis of bar chart
		return "number of " + getSuccessesName();
	}
	
	public NumValue getMaxValue() {					//		for min and max limits above bar charts
		return getNumValueParam("maxValue");
	}
	
//-----------------------------------------------------------
	
	protected String parameterName() {
		return "#pi#";
	}
	
	protected NumValue nullParamValue() {
		return getPSuccess();
	}
	
	protected String parameterLongName() {
		return "the probability of " + getSuccessName();
	}
	
//-----------------------------------------------------------
	
	
	abstract protected double evaluateProbability(int lowCount, int highCount);		//	including both ends
	
	protected double getCorrectPValue() {
		int nTrials = getNTrials();
		int observedCount = getNSuccess();
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return evaluateProbability(0, observedCount);
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return evaluateProbability(observedCount, nTrials);
			case TAIL_BOTH:
			default:
				double pLower = evaluateProbability(0, observedCount);
				double pHigher = evaluateProbability(observedCount, nTrials);
				if (pLower < pHigher)
					return Math.min(1.0, 2 * pLower);
				else if (pLower >= pHigher)
					return Math.min(1.0, 2 * pHigher);
				else
					return Double.NaN;
		}
	}
	
	protected boolean lowTailHighlight() {
		int nTrials = getNTrials();
		int observedCount = getNSuccess();
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return true;
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return false;
			case TAIL_BOTH:
			default:
				double pLower = evaluateProbability(0, observedCount);
				double pHigher = evaluateProbability(observedCount, nTrials);
				return (pLower < pHigher);
		}
	}
}