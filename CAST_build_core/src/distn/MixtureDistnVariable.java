package distn;

import dataView.*;


public class MixtureDistnVariable extends ContinDistnVariable {
	static final private double kEps = 1E-7;

	private ContinDistnVariable distn1, distn2;
	private double p1;
	
	public MixtureDistnVariable(String theName, ContinDistnVariable distn1, ContinDistnVariable distn2) {
		super(theName);
		this.distn1 = distn1;
		this.distn2 = distn2;
	}
	
	public void setParams(String s) {
		try {
			p1 = Double.parseDouble(s);
			if (p1 < 0 || p1 > 1)
				throw new Exception();
		} catch (Exception e) {
			System.err.println("Bad parameters for mixture: propn = " + p1);
		}
	}
	
	public NumValue getMean() {
		NumValue mean1 = distn1.getMean();
		NumValue mean2 = distn2.getMean();
		
		return new NumValue(mean1.toDouble() * p1 + mean2.toDouble() * (1 - p1),
																						Math.max(mean1.decimals, mean1.decimals));
	}
	
	public NumValue getSD() {
		double mean1 = distn1.getMean().toDouble();
		double mean2 = distn2.getMean().toDouble();
		
		NumValue sd1Val = distn1.getSD();
		NumValue sd2Val = distn2.getSD();
		
		double s1 = sd1Val.toDouble();
		double s2 = sd2Val.toDouble();
		
		double eX2 = p1 * (s1 * s1 + mean1 * mean1) + (1 - p1) * (s2 * s2 + mean2 * mean2);
		
		double mean = p1 * mean1 + (1 - p1) * mean2;
		
		return new NumValue(Math.sqrt(eX2 - mean * mean),
																					Math.max(sd1Val.decimals, sd2Val.decimals));
	}
	
	public void setPropn(double p1) {
		this.p1 = p1;
	}
	
	public double getDensityFactor() {
		return 1.0;
	}
	
	public double getMaxScaledDensity() {
		return 1.0;		//			
	}
	
	public double getScaledDensity(double x) {
		double d1 = distn1.getScaledDensity(x) * distn1.getDensityFactor();
		double d2 = distn2.getScaledDensity(x) * distn2.getDensityFactor();
		return d1 * p1 + d2 * (1 - p1);
	}
	
	public double getCumulativeProb(double v) {
		double cum1 = distn1.getCumulativeProb(v);
		double cum2 = distn2.getCumulativeProb(v);
		return cum1 * p1 + cum2 * (1 - p1);
	}
	
	public double getQuantile(double prob) {
		double q1 = distn1.getQuantile(prob);
		double q2 = distn2.getQuantile(prob);
		if (q2 < q1) {
			double temp = q1;
			q1 = q2;
			q2 = temp;
		}
		
		double cum1 = getCumulativeProb(q1);
		double cum2 = getCumulativeProb(q2);
		
		while (cum2 - cum1 > kEps && q1 != q2) {
			double qTest = (q1 + q2) / 2;
			double cumTest = getCumulativeProb(qTest);
			if (cumTest < prob) {
				q1 = qTest;
				cum1 = cumTest;
			}
			else {
				q2 = qTest;
				cum2 = cumTest;
			}
		}
		
		return (q1 + q2) / 2;
	}
	
	public DistnInfo getDistnInfo() {
		return new MixtureInfo(distn1, distn2, p1);
	}
	
	public double xToZ(double x) {
		return x;
	}
	
	public double zToX(double z) {
		return z;
	}
}
