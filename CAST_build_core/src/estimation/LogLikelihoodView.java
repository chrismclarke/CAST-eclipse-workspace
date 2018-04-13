package estimation;

import dataView.*;
import axis.*;


public class LogLikelihoodView extends LikelihoodView {
	
	public LogLikelihoodView(DataSet theData, XApplet applet, String distnKey,
													 CoreLikelihoodFinder likelihoodFinder,
													 HorizAxis paramAxis, VertAxis likelihoodAxis) {
		super(theData, applet, distnKey, likelihoodFinder, paramAxis, likelihoodAxis);
	}
	
	
	protected double getLikelihood(double param) {
		return likelihoodFinder.getLogL(param);
	}
	
	protected double getDerivative(double param) {
		return likelihoodFinder.getLogLDeriv(param);
	}
	
	protected double get2ndDerivative(double param) {
		return likelihoodFinder.getLogLDeriv2(param);
	}
}