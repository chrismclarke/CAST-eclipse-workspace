package models;

import dataView.*;


public class FittedValueVariable extends NumFunctionVariable {
	
	static private String[] makeArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	
	private DataSet data;
	private String lineKey;
	private String xKey[];
	private int decimals;
	
	private Value tempX[];
	
	public FittedValueVariable(String theName, DataSet data, String[] xKey,
																			String lineKey, int decimals) {
		super(theName);
		this.data = data;
		this.xKey = xKey;
		this.lineKey = lineKey;
		this.decimals = decimals;
		tempX = new Value[xKey.length];
	}
	
	public FittedValueVariable(String theName, DataSet data, String oneXKey,
																			String lineKey, int decimals) {
		this(theName, data, makeArray(oneXKey), lineKey, decimals);
	}
	
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	
	public boolean noteVariableChange(String key) {
		boolean changed = lineKey.equals(key);
		for (int i=0 ; i<xKey.length ; i++)
			changed = changed | xKey[i].equals(key);
		if (changed)
			clearSortedValues();
		return changed;
	}

//--------------------------------------------------------
	
	public int getMaxDecimals() {
		return decimals;
	}
	
	public int noOfValues() {
		return ((Variable)data.getVariable(xKey[0])).noOfValues();
	}
	
	public Value valueAt(int index) {
		CoreModelVariable theModel = (CoreModelVariable)data.getVariable(lineKey);
		for (int i=0 ; i<xKey.length ; i++)
			tempX[i] = ((Variable)data.getVariable(xKey[i])).valueAt(index);
		double fit = theModel.evaluateMean(tempX);
		
		NumValue fittedVal = new NumValue(fit, decimals);
		return fittedVal;
	}

//--------------------------------------------------------
	
	public int noOfParameters() {
		CoreModelVariable theModel = (CoreModelVariable)data.getVariable(lineKey);
		return theModel.noOfParameters() - theModel.noOfConstrainedParameters();
	}
}
