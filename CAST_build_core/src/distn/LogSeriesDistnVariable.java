package distn;

import dataView.*;


public class LogSeriesDistnVariable extends DiscreteDistnVariable {
	
	private int meanDecimals = 0;
	private int sdDecimals = 0;
	
	private NumValue theta = new NumValue(0.5, 1);
	
	public LogSeriesDistnVariable(String theName) {
		super(theName);
	}
	
	public void setParams(String s) {
		try {
			theta = new NumValue(s);
			if (theta.toDouble() <= 0.0 || theta.toDouble() > 1.0)
				throw new Exception();
		} catch (Exception e) {
			System.err.println("Bad parameters for Geometric distn");
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
		double t = theta.toDouble();
		double logVal = Math.log(1 - t);
		return new NumValue(-t / (logVal * (1 - t)), meanDecimals);
	}
	
	public NumValue getSD() {
		double t = theta.toDouble();
		double logVal = Math.log(1 - t);
		return new NumValue(Math.sqrt(-t * (t + logVal)) / ((1 - t) * Math.abs(logVal)), sdDecimals);
	}
	
	public void setTheta(NumValue theta) {
		this.theta = theta;
	}
	
	public double getTheta() {
		return theta.toDouble();
	}
	
	public double getProbFactor() {
		return 1.0;
	}
	
	public double getMaxScaledProb() {
		return theta.toDouble();
	}
	
	public double getScaledProb(int x) {
		if (x <= 0)
			return 0;
		double t = theta.toDouble();
		double logVal = Math.log(1 - t);
		return -Math.pow(t, x) / x / logVal;
	}
	
	public double getCumulativeProb(double v) {
		int vFloor = (int)Math.floor(v);
		double t = theta.toDouble();
		double logVal = Math.log(1 - t);
		double result = 0.0;
		for (int i=1 ; i<vFloor ; i++)
			result += Math.pow(t, i) / i;
		return -result / logVal;
	}
	
}
