package estimation;

import dataView.*;
import distn.*;


public class PoissonLikelihoodFinder extends CoreLikelihoodFinder {
	
	protected int[] x;
	
	
	public PoissonLikelihoodFinder(DataSet data, String distnKey, int x[]) {
		super(data, distnKey, x.length);
		this.x = x;
	}
	
	
	protected double getParam() {
		PoissonDistnVariable distn = (PoissonDistnVariable)data.getVariable(distnKey);
		return distn.getMean().toDouble();
	}
	
	protected double getLogDerivative(int index, double lambda) {
		return x[index] / lambda - 1;
	}
	
	protected double get2ndLogDerivative(int index, double lambda) {
		return -x[index] / (lambda * lambda);
	}
	
	protected double getProb(int index, double lambda) {
		int xFact = 1;
		for (int i=1 ; i<x[index] ; i++)
			xFact *= i;
		return Math.pow(lambda, x[index]) * Math.exp(-lambda) / xFact;
	}
}