package distn;

import java.util.*;

import dataView.*;


public class BetaBinomDistnVariable extends DiscreteDistnVariable {
	private int meanDecimals = 0;
	private int sdDecimals = 0;
	
	private BinomialDistnVariable limitingBinomial = null;
	private NumValue piForZeroOne = null;
	
	public BetaBinomDistnVariable(String theName) {
		super(theName);
	}
	
	private int n;
	private NumValue alpha, beta;
	
	private int previousX = 0;
	private double previousProb = 1.0;
	
	public void setParams(String s) {
		if (s == null) {
			alpha = new NumValue(1.0, 0);
			beta = new NumValue(1.0, 0);
			n = 1;
		}
		else
			try {
				StringTokenizer st = new StringTokenizer(s);
				n = Integer.parseInt(st.nextToken());
				alpha = new NumValue(st.nextToken());
				if (alpha.toDouble() <= 0.0)
					throw new Exception();
				beta = new NumValue(st.nextToken());
				if (beta.toDouble() <= 0.0)
					throw new Exception();
			} catch (Exception e) {
				System.err.println("Bad parameters for Poisson distn");
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
		if (limitingBinomial != null)
			return limitingBinomial.getMean();
		else if (piForZeroOne != null)
			return new NumValue(n * piForZeroOne.toDouble(), meanDecimals);
		else {
			double a = alpha.toDouble();
			double b = beta.toDouble();
			double mean = n * a / (a + b);
			
			return new NumValue(mean, meanDecimals);
		}
	}
	
	public NumValue getSD() {
		if (limitingBinomial != null)
			return limitingBinomial.getSD();
		else if (piForZeroOne != null) {
			double pi = piForZeroOne.toDouble();
			return new NumValue(n * Math.sqrt(pi * (1 - pi)), sdDecimals);
		}
		else {
			double a = alpha.toDouble();
			double b = beta.toDouble();
			double var = n * a * b * (a + b + n) / (a + b) / (a + b) / (a + b + 1);
			
			return new NumValue(Math.sqrt(var), sdDecimals);
		}
	}
	
	public void setAlpha(NumValue alpha) {
		this.alpha = alpha;
		limitingBinomial = null;
		piForZeroOne = null;
		previousX = -999;
	}
	
	public void setAlpha(double a) {
		alpha.setValue(a);
		limitingBinomial = null;
		piForZeroOne = null;
		previousX = -999;
	}
	
	public void setBeta(NumValue beta) {
		this.beta = beta;
		limitingBinomial = null;
		piForZeroOne = null;
		previousX = -999;
	}
	
	public void setBeta(double b) {
		beta.setValue(b);
		limitingBinomial = null;
		piForZeroOne = null;
		previousX = -999;
	}
	
	public void setN(int n) {
		this.n = n;
		previousX = -999;
	}
	
	public int getN() {
		return n;
	}
	
	public void setBinomial(NumValue pi) {		//	limiting case of binom
		limitingBinomial = new BinomialDistnVariable("");
		limitingBinomial.setProb(pi.toDouble());
		limitingBinomial.setCount(n);
	}
	
	public void setZeroN(NumValue pi) {		//	limiting case of all 0 or n 
		piForZeroOne = pi;
	}

//-------------------------------------------------------------
	
	public double getProbFactor() {
		if (limitingBinomial != null)
			return limitingBinomial.getProbFactor();
		else
			return 1.0;
	}
	
	public double getMaxScaledProb() {
		if (limitingBinomial != null)
			return limitingBinomial.getMaxScaledProb();
		else if (piForZeroOne != null) {
			double pi = piForZeroOne.toDouble();
			return Math.max(pi, 1 - pi);
		}
		else {
			double a = alpha.toDouble();
			double b = beta.toDouble();
			double mode = (n * (a - 1) - (b - 1)) / (a + b - 2);
			double extremeMax = Math.max(getScaledProb(0), getScaledProb(n));
			if (mode < 0 || mode >= n)
				return extremeMax;
			else {
				int lowM = (int)Math.floor(mode);		//	 the "mode" could be a minimum if U-shaped
				double modeMax = Math.max(getScaledProb(lowM), getScaledProb(lowM + 1));
				return Math.max(modeMax, extremeMax);
			}
		}
	}
	
	public double getScaledProb(int x) {
		if (limitingBinomial != null)
			return limitingBinomial.getScaledProb(x);
		else if (piForZeroOne != null) {
			double pi = piForZeroOne.toDouble();
			return (x == 0) ? (1 - pi) : (x == n) ? pi : 0.0;
		}
		else {
			double a = alpha.toDouble();
			double b = beta.toDouble();
			
			if (x == previousX)
				return previousProb;
			else if (x == 0) {
				previousX = 0;
				previousProb = getP0(a, b);
				return previousProb;
			}
			else if (x == previousX + 1) {
				previousX ++;
				previousProb *= (x - 1 + a) * (n - x + 1) / x / (n - x + b);
				return previousProb;
			}
			else if (x < 0)
				return 0.0;
			else {
				previousX = x;
				previousProb = getP0(a, b);
				for (int i=1 ; i<=x ; i++)
					previousProb *= (i - 1 + a) * (n - i + 1) / i / (n - i + b);
				return previousProb;
			}
		}
	}
	
	private double getP0(double a, double b) {
		return Math.exp(GammaDistnVariable.aLoGam(a + b) + GammaDistnVariable.aLoGam(n + b)
										- GammaDistnVariable.aLoGam(b) - GammaDistnVariable.aLoGam(n + a + b));
	}
	
	public double getCumulativeProb(double xDouble) {
		if (limitingBinomial != null)
			return limitingBinomial.getCumulativeProb(xDouble);
		else if (piForZeroOne != null) {
			double pi = piForZeroOne.toDouble();
			return (xDouble < 0) ? 0 : (xDouble < n) ? (1 - pi) : 1.0;
		}
		else {
			int x = (int)Math.floor(xDouble);
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
