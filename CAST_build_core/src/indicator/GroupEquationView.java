package indicator;

import dataView.*;
import models.*;

import regn.*;


public class GroupEquationView extends LinearEquationView {
	
	private int group;
	
	public GroupEquationView(DataSet theData, XApplet applet, String modelKey,
							String yName, String xName, NumValue minIntercept, NumValue maxIntercept,
							NumValue minSlope, NumValue maxSlope, int group) {
		super(theData, applet, modelKey, yName, xName, minIntercept, maxIntercept, minSlope, maxSlope);
		this.group = group;
	}
	
	protected NumValue getParamValue(int paramIndex) {
		MultipleRegnModel lsModel = (MultipleRegnModel)getVariable(modelKey);
		
		double b[] = new double[4];		//	intercept, slope, groupEffect, interaction
		for (int i=0 ; i<4 ; i++)
			b[i] = lsModel.getParameter(i).toDouble();
		
		if (paramIndex == 0) {
			double intercept = b[0];
			if (group == 1)
				intercept += b[2];
			return new NumValue(intercept, maxIntercept.decimals);
		}
		else {
			double slope = b[1];
			if (group == 1)
				slope += b[3];
			return new NumValue(slope, maxSlope.decimals);
		}
	}
}
