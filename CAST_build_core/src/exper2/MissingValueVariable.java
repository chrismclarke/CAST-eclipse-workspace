package exper2;

import dataView.*;

import cat.CatSelection;

public class MissingValueVariable extends NumFunctionVariable {
	static final private NumValue kNanValue = new NumValue("?");
	
	private NumVariable baseVar;
	private CatSelection missingSelection;
	
	public MissingValueVariable(String theName, NumVariable baseVar) {
		super(theName);
		this.baseVar = baseVar;
		missingSelection = new CatSelection(baseVar.noOfValues());
	}
	
	public int getMaxDecimals() {
		return baseVar.getMaxDecimals();
	}
	
	public int noOfValues() {
		return baseVar.noOfValues();
	}
	
	public Value valueAt(int index) {
		if (isMissing(index))
			return kNanValue;
		else
			return baseVar.valueAt(index);
	}
	
	public void clearMissingValues() {
		missingSelection.resetList();
	}
	
	public boolean isMissing(int index) {
		return missingSelection.valueClicked[index];
	}
	
	public void changeMissing(int index) {
		missingSelection.valueClicked[index] = !missingSelection.valueClicked[index];
	}
	
	public boolean anyMissing() {
		return missingSelection.numberCompleted() != 0;
	}
	
	public NumValue baseValueAt(int index) {
		return (NumValue)baseVar.valueAt(index);
	}
}
