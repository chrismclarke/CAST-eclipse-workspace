package ssq;

import java.util.*;

import dataView.*;
import models.*;


public class VariableXRangeDataSet extends SimpleRegnDataSet {
	//		parameter X_RANDOM_PARAM = "xRandom" holds the sample size and min and max
	
	public VariableXRangeDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addExplanVariables(XApplet applet) {
		StringTokenizer st = new StringTokenizer(xRandomDistn[0]);
		int nValues = Integer.parseInt(st.nextToken());
		NumValue xMin = new NumValue(st.nextToken());
		NumValue xMax = new NumValue(st.nextToken());
		
		addVariable("x", new VariableXRangeVariable(getXVarName(), nValues, xMin, xMax));
	}
	
	public void setXRangePropn(double rangePropn) {
		VariableXRangeVariable xVar = (VariableXRangeVariable)getVariable("x");
		xVar.setRangePropn(rangePropn);
	}

}