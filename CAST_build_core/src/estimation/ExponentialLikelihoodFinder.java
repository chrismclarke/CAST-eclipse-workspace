package estimation;

import dataView.*;

import distn.*;


public class ExponentialLikelihoodFinder extends CoreLikelihoodFinder {
	private double[] x;
	
	
	public ExponentialLikelihoodFinder(DataSet data, String distnKey, double x[]) {
		super(data, distnKey, x.length);
		this.x = x;
	}
	
	
	protected double getParam() {
		ExponDistnVariable distn = (ExponDistnVariable)data.getVariable(distnKey);
		return distn.getLambda().toDouble();
	}
	
	protected double getLogDerivative(int index, double lambda) {
		return 1 / lambda - x[index];
	}
	
	protected double get2ndLogDerivative(int index, double lambda) {
		return -1 / (lambda * lambda);
	}
	
	protected double getProb(int index, double lambda) {
		return lambda * Math.exp(- x[index] * lambda);
	}
}