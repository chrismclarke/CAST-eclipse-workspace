package estimation;

import dataView.*;


abstract public class CoreLikelihoodFinder {
	
	protected DataSet data;
	protected String distnKey;
	
	private int noOfValues;
	
	public CoreLikelihoodFinder(DataSet data, String distnKey, int noOfValues) {
		this.data = data;
		this.distnKey = distnKey;
		this.noOfValues = noOfValues;
	}
	
	protected double getLikelihood(double param) {
		double result = 1.0;
		for (int i=0 ; i<noOfValues ; i++)
			result *= getProb(i, param);
		return result;
	}
	
	protected double getDerivative(double param) {
		double logDerivative = 0.0;
		for (int i=0 ; i<noOfValues ; i++)
			logDerivative += getLogDerivative(i, param);
		return getLikelihood(param) * logDerivative;
	}
	
	protected double get2ndDerivative(double param) {
		double log2Derivative = 0.0;
		for (int i=0 ; i<noOfValues ; i++)
			log2Derivative += get2ndLogDerivative(i, param);
		
		double likelihood = getLikelihood(param);
		return likelihood * log2Derivative + Math.pow(getDerivative(param), 2) / likelihood;
	}
	
	protected double getLogL(double param) {
		double logL = 0.0;
		for (int i=0 ; i<noOfValues ; i++)
			logL += Math.log(getProb(i, param));
		return logL;
	}
	
	protected double getLogLDeriv(double param) {
		double deriv = 0.0;
		for (int i=0 ; i<noOfValues ; i++)
			deriv += getLogDerivative(i, param);
		return deriv;
	}
	
	protected double getLogLDeriv2(double param) {
		double deriv2 = 0.0;
		for (int i=0 ; i<noOfValues ; i++)
			deriv2 += get2ndLogDerivative(i, param);
		return deriv2;
	}
	
	abstract protected double getParam();
	abstract protected double getProb(int index, double param);
	abstract protected double getLogDerivative(int index, double param);
	abstract protected double get2ndLogDerivative(int index, double param);
}