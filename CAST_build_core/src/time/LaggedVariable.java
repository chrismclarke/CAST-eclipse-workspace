package time;

import dataView.*;


public class LaggedVariable extends NumFunctionVariable {
	static final private NumValue kNaNValue = new NumValue("?");
	
	private NumVariable baseVariable;
	private int lag;
	
	private NumValue initialValue = kNaNValue;
	
	public LaggedVariable(String theName, NumVariable baseVariable, int lag) {
		super(theName);
		this.baseVariable = baseVariable;
		this.lag = lag;
	}
	
	public void setLag(int lag) {
		this.lag = lag;
	}
	
	public void setInitialValues(NumValue initialValue) {
		this.initialValue = initialValue;
	}
	
	public int getMaxDecimals() {
		return baseVariable.getMaxDecimals();
	}
	
	public int noOfValues() {
		return baseVariable.noOfValues();
	}
	
	public Value valueAt(int index) {
		return (index >= lag) ? baseVariable.valueAt(index - lag) : initialValue;
	}
}
