package estimation;

import dataView.*;
import distn.*;


public class GeometricLikelihoodFinder extends CoreLikelihoodFinder {
	
	protected int[] x;
	
	
	public GeometricLikelihoodFinder(DataSet data, String distnKey, int x[]) {
		super(data, distnKey, x.length);
		this.x = x;
	}
	
	
	protected double getParam() {
		GeometricDistnVariable distn = (GeometricDistnVariable)data.getVariable(distnKey);
		return distn.getPSuccess();
	}
	
	protected double getLogDerivative(int index, double pi) {
		return 1 / pi - (x[index] - 1) / (1 - pi);
	}
	
	protected double get2ndLogDerivative(int index, double pi) {
		return -1 / Math.pow(pi, 2) - (x[index] - 1) / Math.pow(1 - pi, 2);
	}
	
	protected double getProb(int index, double pi) {
		return pi * Math.pow(1 - pi, x[index] - 1);
	}
}