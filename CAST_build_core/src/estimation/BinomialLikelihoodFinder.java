package estimation;

import dataView.*;
import distn.*;


public class BinomialLikelihoodFinder extends CoreLikelihoodFinder {
	
	protected int n = 0;
	protected int[] x;

	
	public BinomialLikelihoodFinder(DataSet data, String distnKey, int n, int x) {
		super(data, distnKey, 1);				//	for a single binomial value
		this.x = new int[1];
		this.x[0] = x;
		this.n = n;
	}
	
	public BinomialLikelihoodFinder(DataSet theData, String distnKey, int n, int x[]) {
		super(theData, distnKey, x.length);
		this.x = x;
		this.n = n;
	}
	
	protected double getParam() {
		BinomialDistnVariable distn = (BinomialDistnVariable)data.getVariable(distnKey);
		return distn.getProb();
	}
	
	protected double getProb(int index, double pi) {
		double prob;
		double pq = pi * (1.0 - pi);
		if (x[index] < n - x[index])
			prob = Math.pow(1.0 - pi, n - 2 * x[index]);
		else
			prob = Math.pow(pi, 2 * x[index] - n);
		
		int x1 = Math.min(x[index], n - x[index]);
		for (int i=0 ; i<x1 ; i++)
			prob *= ((double)(n-i)) / (i+1) * pq;
		return prob;
	}
	
	protected double getLogDerivative(int index, double pi) {
		return x[index] / pi - (n - x[index]) / (1 - pi);
	}
	
	protected double get2ndLogDerivative(int index, double pi) {
		return - x[index] / Math.pow(pi, 2) - (n - x[index]) / Math.pow(1 - pi, 2);
	}
}