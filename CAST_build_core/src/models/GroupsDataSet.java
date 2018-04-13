package models;

import dataView.*;


public class GroupsDataSet extends CoreModelDataSet {
	static final private String ANOVA_MODEL_PARAM = "anovaModel";
	
	public GroupsDataSet(XApplet applet) {
		super(applet);
	}
	
	protected void addResponseModel(String yKey, XApplet applet) {
		addErrorVariable(applet);
		
			GroupsModelVariable yDistn = new GroupsModelVariable(getYVarName(), this, "x");
			yDistn.setParameters(applet.getParameter(ANOVA_MODEL_PARAM));
		addVariable("model", yDistn);
			
			ResponseVariable yData = new ResponseVariable(getYVarName(), this, "x", "error",
																													"model", getResponseDecimals());
		addVariable(yKey, yData);
	}
	
	protected void addLeastSquaresFit(XApplet applet) {
		GroupsModelVariable lsFit = new GroupsModelVariable("least squares", this, "x");
		addVariable("ls", lsFit);
	}
	
	protected void addAdjustedVariable(String newYKey, double initialR2, String rawYKey,
																							XApplet applet) {
		NumVariable rawY = (NumVariable)getVariable(rawYKey);
		AdjustedSsqVariable adjY = new GroupsAdjustedSsqVariable(rawY.name, this, "x", rawYKey,
																														rawY.getMaxDecimals());
		adjY.setR2(initialR2);
		addVariable(newYKey, adjY);
	}
	
	public boolean changeDataSet(int dataIndex) {
		if (super.changeDataSet(dataIndex)) {
			resetLSEstimates();
			return true;
		}
		else
			return false;
	}
//-------------------------------------------------------------------------
	
	public synchronized void variableChanged(String key) {
		initialised = false;
		super.variableChanged(key);
	}

//-------------------------------------------------------------------------
	
	private boolean initialised = false;
	
	private double mean[];
	private double sd[];
	private int n[];
	
	public void resetLSEstimates() {
		initialised = false;
	}
	
	private void initialise() {
		if (initialised)
			return;
		initialised = true;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable xVar = (CatVariable)getVariable("x");
		int noOfXCats = xVar.noOfCategories();
		mean = new double[noOfXCats];
		sd = new double[noOfXCats];
		n = new int[noOfXCats];
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			int group = xVar.labelIndex(xe.nextValue());
			mean[group] += y;
			sd[group] += y * y;
			n[group] ++;
		}
		
		for (int i=0 ; i<noOfXCats ; i++) {
			sd[i] = (n[i] > 1) ? Math.sqrt((sd[i] - mean[i] * mean[i] / n[i]) / (n[i] - 1)) : Double.NaN;
			mean[i] /= n[i];
		}
	}
	
	public double getMean(int group) {
		initialise();
		return mean[group];
	}
	
	public double getSD(int group) {
		initialise();
		return sd[group];
	}
	
	public int getN(int group) {
		initialise();
		return n[group];
	}

}