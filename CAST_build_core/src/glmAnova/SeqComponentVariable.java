package glmAnova;

import dataView.*;
import models.*;

public class SeqComponentVariable extends CoreComponentVariable {
	static final private NumValue kNanValue = new NumValue("?");
	
	private NumVariable yVar = null;		//	allows it to deal with missing values
	private FittedValueVariable fit1, fit2;
	private String fit1Key, fit2Key;		//	fit1 minus fit2 (fit1 usually has more variables)
	
	public SeqComponentVariable(String theName, DataSet data, String fit1Key, String fit2Key,
																														String yKey, int decimals) {
		super(theName, data, null, null, decimals);
		
		this.fit1Key = fit1Key;
		fit1 = (FittedValueVariable)data.getVariable(fit1Key);
		
		this.fit2Key = fit2Key;
		fit2 = (FittedValueVariable)data.getVariable(fit2Key);
		
		if (yKey != null)
			yVar = (NumVariable)data.getVariable(yKey);
	}
	
	public SeqComponentVariable(String theName, DataSet data, String fit1Key, String fit2Key,
																																						int decimals) {
		this(theName, data, fit1Key, fit2Key, null, decimals);
	}

//--------------------------------------------------------
	
	public int noOfValues() {
		return Math.min(fit1.noOfValues(), fit2.noOfValues());
	}
	
	public boolean noteVariableChange(String key) {
		return fit1Key.equals(key) || fit2Key.equals(key);
	}
	
	public Value valueAt(int index) {
		if (yVar != null && Double.isNaN(yVar.doubleValueAt(index)))
			return kNanValue;
		else {
			double value = fit1.doubleValueAt(index) - fit2.doubleValueAt(index);
			return new NumValue(value, decimals);
		}
	}
	
	public int getDF() {
		return fit1.noOfParameters() - fit2.noOfParameters();
	}
}
