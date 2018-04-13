package models;

import dataView.*;
import random.*;


public class ContinTableDataSet extends CoreModelDataSet {
	static final private String CONTIN_PROB_PARAM = "continProbs";
	
	public ContinTableDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
			CatDistnVariable yDistn = new CatDistnVariable(getYVarName());
			yDistn.readLabels(yLabelString[currentDataSetIndex]);
			yDistn.setParams(applet.getParameter(CONTIN_PROB_PARAM));
		addVariable("model", yDistn);
			
			CatVariable x = (CatVariable)getVariable("x");
			RandomProductMulti generator = new RandomProductMulti(x.getCounts(), yDistn.getProbs());
			
			CatSampleVariable y = new CatSampleVariable(getYVarName(), generator, CatVariable.USES_REPEATS);
			y.readLabels(yLabelString[currentDataSetIndex]);
		addVariable(yKey, y);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
	}

//-------------------------------------------------------------------------
	
	public synchronized void variableChanged(String key) {
		initialised = false;
		super.variableChanged(key);
	}

//-------------------------------------------------------------------------
	
	private boolean initialised = false;
	
	private int[][] counts;
	private int[] xCounts;
	
	private void initialise() {
		if (initialised)
			return;
		initialised = true;
		
		CatVariable yVar = (CatVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable("x");
		counts = xVar.getCounts(yVar);
		xCounts = xVar.getCounts();
	}
	
	public double getPropn(int group) {
		initialise();
		return counts[group][0] / (double)xCounts[group];
	}
	
	public int getN(int group) {
		initialise();
		return xCounts[group];
	}
}