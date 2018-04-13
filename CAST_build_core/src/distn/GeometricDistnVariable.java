package distn;

import dataView.*;


public class GeometricDistnVariable extends DiscreteDistnVariable {
	
	private int meanDecimals = 0;
	private int sdDecimals = 0;
	
	private NumValue pSuccess = new NumValue(0.5, 1);
	
	public GeometricDistnVariable(String theName) {
		super(theName);
	}
	
	public void setParams(String s) {
		try {
			pSuccess = new NumValue(s);
			if (pSuccess.toDouble() <= 0.0 || pSuccess.toDouble() > 1.0)
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
		double p = pSuccess.toDouble();
		return new NumValue(1.0 / p, meanDecimals);
	}
	
	public NumValue getSD() {
		double p = pSuccess.toDouble();
		return new NumValue(Math.sqrt(1 - p) / p, sdDecimals);
	}
	
	public void setPSuccess(NumValue pSuccess) {
		this.pSuccess = pSuccess;
	}
	
	public double getPSuccess() {
		return pSuccess.toDouble();
	}
	
	public double getProbFactor() {
		return 1.0;
	}
	
	public double getMaxScaledProb() {
		return pSuccess.toDouble();
	}
	
	public double getScaledProb(int x) {
		if (x <= 0)
			return 0;
		double p = pSuccess.toDouble();
		return Math.pow(1.0 - p, x - 1) * p;
	}
	
	public double getCumulativeProb(double v) {
		double vFloor = Math.floor(v);
		double p = pSuccess.toDouble();
		return 1 - Math.pow(1 - p, vFloor);
	}
	
}
