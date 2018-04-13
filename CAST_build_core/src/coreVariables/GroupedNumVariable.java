package coreVariables;

import dataView.*;

public class GroupedNumVariable extends CatFunctionVariable {
	private String sourceKey;
	private NumVariable sourceVar;
	private double[] boundary;
	
	public GroupedNumVariable(String theName, DataSet data, String sourceKey, double[] boundary) {
																									//	the no of boundaries must be 1 less than the no of labels
		super(theName);
		this.sourceKey = sourceKey;
		this.boundary = boundary;
		sourceVar = (NumVariable)data.getVariable(sourceKey);
	}
	
	public int noOfValues() {
		return sourceVar.noOfValues();
	}
	
	public Value valueAt(int index) {
		double sourceVal = sourceVar.doubleValueAt(index);
		for (int i=0 ; i<boundary.length ; i++)
			if (sourceVal < boundary[i])
				return getLabel(i);
		return getLabel(boundary.length);
	}
	
	public boolean noteVariableChange(String key) {
		return sourceKey.equals(key);
	}
}
