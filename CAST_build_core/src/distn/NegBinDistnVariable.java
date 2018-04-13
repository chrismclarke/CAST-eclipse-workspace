package distn;

import java.util.*;

import dataView.*;


public class NegBinDistnVariable extends DiscreteDistnVariable {
	
	private int meanDecimals = 0;
	private int sdDecimals = 0;
	
	private int previousX = -999;
	private double previousProb = 1.0;
	
	private NumValue pSuccess = new NumValue(0.5, 1);
	private NumValue kValue = new NumValue(1, 0);
	private PoissonDistnVariable limitingPoisson = null;
	
	private boolean startAtK = true;
	
	public NegBinDistnVariable(String theName) {
		super(theName);
	}
	
	public NegBinDistnVariable(String theName, boolean startAtK) {
		super(theName);
		this.startAtK = startAtK;
	}
	
	public void setParams(String s) {
		StringTokenizer st = new StringTokenizer(s);
		try {
			pSuccess = new NumValue(st.nextToken());
			if (pSuccess.toDouble() <= 0.0 || pSuccess.toDouble() > 1.0)
				throw new Exception();
			
			kValue = new NumValue(st.nextToken());
			if (kValue.toDouble() < 1)
				throw new Exception();
		} catch (Exception e) {
			System.err.println("Bad parameters for Negative binomial distn");
			kValue.setValue(1);
			pSuccess = new NumValue(0.5, 1);
		}
		double meanVal = getMean().toDouble();
		setMinSelection(meanVal);
		setMaxSelection(meanVal);
	}
	
	public void setMeanSdDecimals(int meanDecimals, int sdDecimals) {
		this.meanDecimals = meanDecimals;
		this.sdDecimals = sdDecimals;
	}
	
	public NumValue getMean() {
		if (limitingPoisson != null)
			return limitingPoisson.getMean();
		
		double p = pSuccess.toDouble();
		double k = kValue.toDouble();
		double mean = k / p;
		if (startAtK)
			mean += k;
		return new NumValue(mean, meanDecimals);
	}
	
	public NumValue getSD() {
		if (limitingPoisson != null)
			return limitingPoisson.getSD();
		double p = pSuccess.toDouble();
		double k = kValue.toDouble();
		return new NumValue(Math.sqrt(k * (1 - p)) / p, sdDecimals);
	}
	
	public void setPSuccess(NumValue pSuccess) {
		this.pSuccess = pSuccess;
		limitingPoisson = null;
	}
	
	public void setPSuccess(double pSuccessVal) {
		pSuccess.setValue(pSuccessVal);
		limitingPoisson = null;
	}
	
	public void setK(NumValue k) {
		kValue = k;
		limitingPoisson = null;
	}
	
	public void setK(double k) {
		kValue.setValue(k);
		limitingPoisson = null;
	}
	
	public void setPoisson(NumValue lambda) {		//	limiting case of neg binom 
		limitingPoisson = new PoissonDistnVariable("");
		limitingPoisson.setLambda(lambda);
	}
	
	public double getProbFactor() {
		if (limitingPoisson != null) 
			return limitingPoisson.getProbFactor();
		else
			return 1.0;
	}
	
	public double getMaxScaledProb() {
		if (limitingPoisson != null) 
			return limitingPoisson.getMaxScaledProb();
		else {
			double p = pSuccess.toDouble();
			double k = kValue.toDouble();
			double mode = (k * (1 - p) - 1) / p;
			if (mode < 0)
				return getScaledProb(startAtK ? (int)Math.round(k) : 0);
			else {
				double offset = startAtK ? k : 0;
				int lowM = (int)Math.floor(mode + offset);
				return Math.max(getScaledProb(lowM), getScaledProb(lowM + 1));
			}
		}
	}
	
	public double getScaledProb(int x) {
		if (limitingPoisson != null) 
			return limitingPoisson.getScaledProb(x);
		else {
			double k = kValue.toDouble();
			if (startAtK)
				x -= (int)Math.round(k);		//	calculations done with zero-based version of distn
			
			if (x < 0)
				return 0;
			double p = pSuccess.toDouble();
			
			if (x == previousX)
				return previousProb;
			else if (x == 0) {
				previousX = 0;
				previousProb = Math.pow(p, k);
				return previousProb;
			}
			else if (x == previousX + 1 && previousProb > 0.0) {
				previousX ++;
				previousProb *= (x + k - 1) * (1.0 - p) / x;
				return previousProb;
			}
			else {
				previousX = x;
				previousProb = Math.pow(p, k);
				for (int i=1 ; i<=x ; i++)
					previousProb *= (i + k - 1) * (1.0 - p) / i;
				return previousProb;
			}
		}
	}
	
	public double getCumulativeProb(double xDouble) {
		if (limitingPoisson != null) 
			return limitingPoisson.getCumulativeProb(xDouble);
		else {
			double p = pSuccess.toDouble();
			double k = kValue.toDouble();
			int x = (int)Math.floor(xDouble);
			if (startAtK) {
				int kRounded = (int)Math.round(k);		//	it should be an integer
				if (x < kRounded)
					return 0;
				else {				//		adds k binomial probs
					double term = Math.pow(1 - p, x - 1);
					double result = term;
					for (int v=1 ; v<kRounded ; v++) {
						term *= (x - v) * p / (1 - p) / v;
						result += term;
					}
					return result;
				}
			}
			else {
				if (x < 0)
					return 0;
				else {
					double result = 0;
					for (int y=0 ; y<=x ; y++)
						result += getScaledProb(y);
					return result;
				}
			}
		}
	}
	
}
