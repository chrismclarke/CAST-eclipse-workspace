package models;

import dataView.*;

abstract public class CoreComponentVariable extends NumFunctionVariable {
	protected DataSet data;
	protected String yKey, modelKey;
	protected int decimals;
	
	protected boolean foundYMean = false;
	protected double yMean;
	
	static protected String[] makeArray(String s) {
		String[] sArray = new String[1];
		sArray[0] = s;
		return sArray;
	}
	
	public CoreComponentVariable(String theName, DataSet data, String yKey,
																													String modelKey, int decimals) {
		super(theName);
		this.data = data;
		this.yKey = yKey;
		this.modelKey = modelKey;
		this.decimals = decimals;
	}
	
	public boolean noteVariableChange(String key) {
		boolean changed = yKey.equals(key);
		if (modelKey != null && modelKey.equals(key))
			changed = true;
		if (changed)
			foundYMean = false;
		return changed;
	}

//--------------------------------------------------------

	protected CoreVariable getVariable(String key) {
		return data.getVariable(key);
	}
	
	public int getMaxDecimals() {
		return decimals;
	}
	
	public int noOfValues() {
		return ((Variable)data.getVariable(yKey)).noOfValues();
	}
	
	public double getYMean() {
		if (!foundYMean) {
			NumVariable yVar = (NumVariable)getVariable(yKey);
			double sumY = 0.0;
			int n = 0;
			ValueEnumeration e = yVar.values();
			while (e.hasMoreValues()) {
				double y = e.nextDouble();
				if (!Double.isNaN(y)) {
					sumY += y;
					n ++;
				}
			}
			yMean = sumY / n;
			foundYMean = true;
		}
		return yMean;
	}
	
	public double getSsq() {
		int n = noOfValues();
		double ssq = 0.0;
		for (int i=0 ; i<n ; i++) {
			NumValue v = (NumValue)valueAt(i);
			if (v == null)
				continue;
			double comp = v.toDouble();
			if (!Double.isNaN(comp))
				ssq += comp * comp;
		}
		return ssq;
	}

//--------------------------------------------------------
	
	abstract public Value valueAt(int index);
	abstract public int getDF();
}
