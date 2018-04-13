package coreVariables;

import dataView.*;

public class WeightedMeanVariable extends NumFunctionVariable {
	private DataSet data;
	private String sourceKey1, sourceKey2;
	private int displayDecimals;
	private double wt0, wt1;
	
	public WeightedMeanVariable(String theName, DataSet data, String sourceKey1, String sourceKey2,
																				double wt0, double wt1) {
		super(theName);
		this.data = data;
		this.sourceKey1 = sourceKey1;
		this.sourceKey2 = sourceKey2;
		displayDecimals = Math.max(getVariable(sourceKey1).getMaxDecimals(),
																	getVariable(sourceKey2).getMaxDecimals());
		this.wt0 = wt0;
		this.wt1 = wt1;
	}
	
	public WeightedMeanVariable(String theName, DataSet data, String sourceKey1, String sourceKey2,
																				double wt0) {
		this(theName, data, sourceKey1, sourceKey2, wt0, 1 - wt0);
	}
	
	public void setWeight(double wt0) {
		if (wt0 >= 0.0 && wt0 <= 1.0) {
			this.wt0 = wt0;
			this.wt1 = 1.0 - wt0;
			clearData();
		}
	}
	
	public void setWeights(double wt0, double wt1) {
		this.wt0 = wt0;
		this.wt1 = wt1;
		clearData();
	}
	
	private NumVariable getVariable(String key) {
		return (NumVariable)data.getVariable(key);
	}
	
	public boolean noteVariableChange(String key) {
		return sourceKey1.equals(key) || sourceKey2.equals(key);
	}

//--------------------------------------------------------
	
	public int getMaxDecimals() {
		return displayDecimals;
	}
	
	public int noOfValues() {
		return Math.min(getVariable(sourceKey1).noOfValues(), getVariable(sourceKey2).noOfValues());
	}
	
	public Value valueAt(int index) {
		NumValue baseVal1 = (NumValue)getVariable(sourceKey1).valueAt(index);
		NumValue baseVal2 = (NumValue)getVariable(sourceKey2).valueAt(index);
		NumValue result = new NumValue(wt0 * baseVal1.toDouble()
											+ wt1 * baseVal2.toDouble(), displayDecimals);
		return result;
	}
}
