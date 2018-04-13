package exerciseTestProg;

import java.util.*;

import dataView.*;
import distn.*;


abstract public class CoreNormalApplet extends CoreTestApplet {
//	static final private NumValue kZero = new NumValue(0, 0);
	
	protected void addTypeDelimiters() {
		addType("mean", "*");
	}
	
	protected Object createConstObject(String baseType, String valueString) {
		if (baseType.equals("mean"))
			return new NumValue(valueString);
		else
			return super.createConstObject(baseType, valueString);
	}
	
	protected double getQuantile(double cumulative) {
		return NormalTable.quantile(cumulative);
	}
	
	protected Object createRandomObject(String baseType, String paramString, Object oldParam) {
		if (baseType.equals("mean")) {		//	assumes sampleSize, nullMean and sigma already set
			int n = getSampleSize();
			double nullMean = getNullMean().toDouble();
			double sigma = getSd().toDouble();
			
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
			
			double z = nullMean + sigma / Math.sqrt(n) * getQuantile(cumulative);
			int decimals = getMeanDecimals();
			double factor = 1.0;
			for (int i=0 ; i<decimals ; i++) {
				z *= 10;
				factor *= 10;
			}
			z = Math.rint(z) / factor;
			
			return new NumValue(z, decimals);
		}
		else
			return super.createRandomObject(baseType, paramString, oldParam);
	}
	
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("sampleSize", "int");
//		registerParameter("meanIndex", "int");
		registerParameter("nullMean", "const");
		registerParameter("sd", "const");
		registerParameter("meanDecimals", "int");
		registerParameter("observedMean", "mean");
		registerParameter("valueName", "string");
	}
	
	protected int getSampleSize() {
		return getIntParam("sampleSize");
	}
	
	protected NumValue getNullMean() {
		return getNumValueParam("nullMean");
	}
	
	protected NumValue getSd() {
		return getNumValueParam("sd");
	}
	
	protected int getMeanDecimals() {
		return getIntParam("meanDecimals");
	}
	
	protected NumValue getObservedMean() {
		return getNumValueParam("observedMean");
	}
	
	protected String getValueName() {
		return getStringParam("valueName");
	}
	
	public String getVarName() {		//	displayed on axis of normal density
		return "mean " + getValueName();
	}
	
	public NumValue getMaxValue() {			//	only needed for CoreTestApplet
		return null;
	}
	
//-----------------------------------------------------------
	
	protected String parameterName() {
		return "#mu#";
	}
	
	protected NumValue nullParamValue() {
		return getNullMean();
	}
	
	protected String parameterLongName() {
		return "the mean " + getValueName();
	}
	
//-----------------------------------------------------------
	
	public String getAxisInfo() {
		return null;
	}
	
//-----------------------------------------------------------
	
	
	abstract protected double cumulativeProbability(double mean);
	
	protected double getCorrectPValue() {
		double observedMean = getObservedMean().toDouble();
		switch (getTail()) {
			case TAIL_LOW:
			case TAIL_LOW_EQ:
				return cumulativeProbability(observedMean);
			case TAIL_HIGH:
			case TAIL_HIGH_EQ:
				return 1 - cumulativeProbability(observedMean);
			case TAIL_BOTH:
			default:
				double pLower = cumulativeProbability(observedMean);
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
				double pLower = cumulativeProbability(getObservedMean().toDouble());
				return (pLower <= 0.5);
		}
	}
}