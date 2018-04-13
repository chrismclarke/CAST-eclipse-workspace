package exercise2;

import dataView.*;


abstract public class NumOptionInformation extends OptionInformation {
	private NumValue value;
	private String unitString;
	
	public NumOptionInformation(NumValue value, boolean correct) {
		super(correct);
		this.value = value;
	}
	
	public void setUnitString(String unitString) {
		this.unitString = unitString;
	}
	
	public boolean equals(OptionInformation a) {
		NumOptionInformation oa = (NumOptionInformation)a;
		return value.equals(oa.value);
	}
	
	public boolean lessThan(OptionInformation a) {
		NumOptionInformation oa = (NumOptionInformation)a;
		return value.toDouble() < oa.value.toDouble();
	}
	
	abstract public String getMessageString();
	
	public String getOptionString() {
		if (unitString == null)
			return value.toString();
		else
			return value.toString() + unitString;
	}
	
	public NumValue getOptionValue() {
		return value;
	}
}