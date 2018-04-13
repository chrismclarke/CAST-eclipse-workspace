package distn;

import dataView.*;


public class PoissonDistnVariable extends DiscreteDistnVariable {
	public PoissonDistnVariable(String theName) {
		super(theName);
	}

	private NumValue lambda;
	private double logLambda;
	private int previousX = 0;
	private double previousProb = 1.0;
	
	public void setParams(String s) {
		if (s == null) {
			lambda = new NumValue(1.0, 0);
			logLambda = 0.0;
		}
		else
			try {
				lambda = new NumValue(s);
				if (lambda.toDouble() <= 0.0)
					throw new Exception();
				logLambda = Math.log(lambda.toDouble());
			} catch (Exception e) {
				System.err.println("Bad parameters for Poisson distn");
			}
		double meanVal = getMean().toDouble();
		setMinSelection(meanVal);
		setMaxSelection(meanVal);
	}
	
	public NumValue getMean() {
		return lambda;
	}
	
	public NumValue getSD() {
		return new NumValue(Math.sqrt(lambda.toDouble()), lambda.decimals);
	}
	
	public void setLambda(NumValue lambda) {
		this.lambda = lambda;
		logLambda = Math.log(lambda.toDouble());
		previousX = -1;
	}
	
	public double getProbFactor() {
		return Math.exp(-lambda.toDouble());
	}
	
	public double getMaxScaledProb() {
		return Math.exp(lambda.toDouble() * Math.log(lambda.toDouble()) - GammaDistnVariable.aLoGam(lambda.toDouble() + 1.0));
	}
	
	public double getScaledProb(int x) {
		if (x == previousX)
			return previousProb;
		else if (x == 0) {
			previousX = 0;
			previousProb = 1.0;
			return previousProb;
		}
		else if (x == previousX + 1) {
			previousX ++;
			previousProb *= (lambda.toDouble() / previousX);
			return previousProb;
		}
		else if (x < 0)
			return 0.0;
		else {
			previousX = x;
			previousProb = Math.exp(x * logLambda - GammaDistnVariable.aLoGam(x + 1));
			return previousProb;
		}
	}
	
	public double getCumulativeProb(double v) {
																				//	P(v'th event in Poisson(1) process occurs after time 1/lambda)
		double vFloor = Math.floor(v);
		return 1 - GammaDistnVariable.gammaProb(1 / lambda.toDouble(), vFloor);
	}
	
}
