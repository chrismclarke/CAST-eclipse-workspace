package models;

import dataView.*;

public class ResponseVariable extends NumFunctionVariable {
	private DataSet data;
	protected String xKey[];
	protected String errorKey, modelKey, catKey;
	private int displayDecimals;
	private double catEffect;
	private boolean roundValues = false;
	
	private Value tempValue[];
	
	static private String[] makeArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	
	public ResponseVariable(String theName, DataSet data, String xKey, String errorKey,
																								String modelKey, int decimals) {
		this(theName, data, makeArray(xKey), errorKey, modelKey, null, 0.0, decimals);
	}
	
	public ResponseVariable(String theName, DataSet data, String[] xKey, String errorKey,
																								String modelKey, int decimals) {
		this(theName, data, xKey, errorKey, modelKey, null, 0.0, decimals);
	}
	
	public ResponseVariable(String theName, DataSet data, String[] xKey, String errorKey,
															String modelKey, String catKey, double catEffect, int decimals) {
		super(theName);
		this.data = data;
		this.xKey = xKey;
		this.errorKey = errorKey;
		this.modelKey = modelKey;
		this.catKey = catKey;
		this.catEffect = catEffect;
		displayDecimals = decimals;
		tempValue = new Value[xKey.length];
	}
	
	public ResponseVariable(String theName, DataSet data, String xKey, String errorKey,
															String modelKey, String catKey, double catEffect, int decimals) {
		this(theName, data, makeArray(xKey), errorKey, modelKey, catKey, catEffect, decimals);
	}
	
	public void setRoundValues(boolean roundValues) {
		this.roundValues = roundValues;
	}
	
	public void setXKey(String newXKey) {
		xKey[0] = newXKey;
	}
	
	public void setXKey(String[] newXKey) {
		xKey = newXKey;
	}
	
	public void setDecimals(int decimals) {
		displayDecimals = decimals;
	}
	
	public void setEffect(double catEffect) {		// only for TreatLurkingApplet
		this.catEffect = catEffect;
	}
	
	protected Variable getVariable(String key) {
		return (Variable)data.getVariable(key);
	}
	
	public boolean noteVariableChange(String key) {
		boolean changed = false;
		for (int i=0 ; i<xKey.length  ; i++)
			if (xKey[i].equals(key))
				changed = true;
		if (catKey != null && catKey.equals(key))
			changed = true;
		
		if (errorKey.equals(key) || modelKey.equals(key))
			changed = true;
		
		if (changed)
			clearSortedValues();
		return changed;
	}

//--------------------------------------------------------
	
	public int getMaxDecimals() {
		return displayDecimals;
	}
	
	public int noOfValues() {
		return Math.max(getVariable(xKey[0]).noOfValues(), getVariable(errorKey).noOfValues());
	}
	
	public Value valueAt(int index) {
		CoreModelVariable model = (CoreModelVariable)data.getVariable(modelKey);
		
		for (int i=0 ; i<xKey.length ; i++)
			tempValue[i] = getVariable(xKey[i]).valueAt(index);
		double yMean = model.evaluateMean(tempValue);
		
		NumValue error = (NumValue)getVariable(errorKey).valueAt(index);
		NumValue result = new NumValue(yMean + error.toDouble() * model.evaluateSD(tempValue).toDouble(), displayDecimals);
		
		if (catKey != null) {
			int cat = ((CatVariable)data.getVariable(catKey)).getItemCategory(index);
			if (cat == 1)
				result.setValue(result.toDouble() + catEffect);
		}
		
		if (roundValues)
			round(result);
		
		return result;
	}
	
	private void round(NumValue v) {
		double temp = v.toDouble();
		double factor = 1.0;
		for (int i=0 ; i<v.decimals ; i++) {
			temp *= 10.0;
			factor *= 0.1;
		}
		v.setValue(Math.round(temp) * factor);
	}
}
