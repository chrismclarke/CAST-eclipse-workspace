package estimation;

import dataView.*;
import distn.*;


public class NormalLikelihoodFinder extends CoreLikelihoodFinder {
	static final private double kInv2RootPi = 1.0 / Math.sqrt(2 * Math.PI);
	
	private double[] x;
	private double sigma;
	
	
	public NormalLikelihoodFinder(DataSet data, String distnKey, double x[]) {
		super(data, distnKey, x.length);
		this.x = x;
		NormalDistnVariable distn = (NormalDistnVariable)data.getVariable(distnKey);
		sigma = distn.getSD().toDouble();
	}
	
	
	protected double getParam() {
		NormalDistnVariable distn = (NormalDistnVariable)data.getVariable(distnKey);
		return distn.getMean().toDouble();
	}
	
	protected double getLogDerivative(int index, double mu) {
		double z = (x[index] - mu) / sigma;
		return z / sigma;
	}
	
	protected double get2ndLogDerivative(int index, double mu) {
		return -1 / (sigma * sigma);
	}
	
	protected double getProb(int index, double mu) {
		double z = (x[index] - mu) / sigma;
		return kInv2RootPi / sigma * Math.exp(-z * z / 2);
	}
}