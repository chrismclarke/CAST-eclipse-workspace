package distribution;

import dataView.*;
import distn.*;


public class RectMaxDistnVariable extends ContinDistnVariable {
	
	private int meanDecimals = 0;
	private int sdDecimals = 0;
	
	private int n = 10;
	
	public RectMaxDistnVariable(String theName) {
		super(theName);
	}
	
	public void setParams(String s) {
		n = Integer.parseInt(s);
		setMinSelection(Double.NEGATIVE_INFINITY);
		setMaxSelection(Double.NEGATIVE_INFINITY);
	}
	
	public void setMeanSdDecimals(int meanDecimals, int sdDecimals) {
		this.meanDecimals = meanDecimals;
		this.sdDecimals = sdDecimals;
	}
	
	public NumValue getMean() {
		return new NumValue(n  / (n + 1.0), meanDecimals);
	}
	
	public NumValue getSD() {
		return new NumValue(Math.sqrt(n / ((n + 1.0) * (n + 1.0) * (n + 2.0))), sdDecimals);
	}
	
	public void setN(int n) {
		this.n = n;
	}
	
	public double getDensityFactor() {
		return 1.0;
	}
	
	public double getMaxScaledDensity() {
			return n;
	}
	
	public double getScaledDensity(double x) {
		return (x < 0 |x > 1) ? 0 : Math.pow(x, n-1) * n;
	}
	
	public double getCumulativeProb(double v) {
		return (v < 0) ? 0 : (v > 1) ? 1 : Math.pow(v, n);
	}
	
	public double getQuantile(double prob) {
		return Math.pow(prob, 1.0 / n);
	}
	
	public DistnInfo getDistnInfo() {
		return null;		//	not implemented yet
	}
	
	public double xToZ(double x) {
		return x;
	}
	
	public double zToX(double z) {
		return z;
	}
	
}
