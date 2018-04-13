package estimation;

import dataView.*;
import distn.*;


public class LogSeriesLikelihoodFinder extends CoreLikelihoodFinder {
	
	protected int[] x;
	
	
	public LogSeriesLikelihoodFinder(DataSet data, String distnKey, int x[]) {
		super(data, distnKey, x.length);
		this.x = x;
	}
	
	
	protected double getParam() {
		LogSeriesDistnVariable distn = (LogSeriesDistnVariable)data.getVariable(distnKey);
		return distn.getTheta();
	}
	
	protected double getLogDerivative(int index, double theta) {
		double logVal = Math.log(1 - theta);
		return x[index] / theta + 1 / (1 - theta) / logVal;
	}
	
	protected double get2ndLogDerivative(int index, double theta) {
		double logVal = Math.log(1 - theta);
		return -x[index] / Math.pow(theta, 2) + (1 + logVal) / Math.pow((1 - theta) * logVal, 2);
	}
	
	protected double getProb(int index, double theta) {
		double logVal = Math.log(1 - theta);
		return -1 / logVal * Math.pow(theta, x[index]) / x[index];
	}
}