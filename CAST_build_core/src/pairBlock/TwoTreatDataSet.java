package pairBlock;

import dataView.*;

import multiRegn.*;


public class TwoTreatDataSet extends MultiRegnDataSet {
	
	private double[] xOnlyConstraints, zOnlyConstraints;
	
	public TwoTreatDataSet(XApplet applet) {
		super(applet);
		
		CatVariable xVar = (CatVariable)getVariable("x");
		CatVariable zVar = (CatVariable)getVariable(getZDataKey());
		int nXParams = xVar.noOfCategories() - 1;
		int nZParams = zVar.noOfCategories() - 1;
		
		xOnlyConstraints = new double[nXParams + nZParams + 1];
		for (int i=0 ; i<xOnlyConstraints.length ; i++)
			xOnlyConstraints[i] = Double.NaN;
		
		for (int i=0 ; i<nZParams ; i++)
			xOnlyConstraints[nXParams + 1 + i] = 0.0;
		
		zOnlyConstraints = new double[nXParams + nZParams + 1];
		for (int i=0 ; i<zOnlyConstraints.length ; i++)
			zOnlyConstraints[i] = Double.NaN;
		
		for (int i=0 ; i<nXParams ; i++)
			zOnlyConstraints[i + 1] = 0.0;
	}
	
	protected int noOfParams() {
		CatVariable xVar = (CatVariable)getVariable("x");
		CatVariable zVar = (CatVariable)getVariable(getZDataKey());
		
		return xVar.noOfCategories() + zVar.noOfCategories() - 1;
	}
	
	public double[] getXOnlyConstraints() {
		return xOnlyConstraints;
	}
	
	public double[] getZOnlyConstraints() {
		return zOnlyConstraints;
	}
}