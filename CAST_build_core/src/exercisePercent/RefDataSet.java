package exercisePercent;

import dataView.*;
import utils.*;


public class RefDataSet extends DataSet implements StatusInterface {
	private DataSet mainData;
	
	public RefDataSet(DataSet mainData) {
		this.mainData = mainData;
		addNumVariable("ref", "Reference", "0");		//	one value
		setSelection(0);
	}
	
	public void changedRefValue() {
		variableChanged("ref", 0);
	}
	
	public void setRefValue(double value, int decimals) {
		NumVariable refVar = (NumVariable)getVariable("ref");
		NumValue ref = (NumValue)refVar.valueAt(0);
		ref.decimals = decimals;
		ref.setValue(value);
	}
	
	public String getStatus() {
		NumVariable refVar = (NumVariable)getVariable("ref");
		NumValue refValue = (NumValue)refVar.valueAt(0);
		return String.valueOf(refValue.toDouble());
	}
	
	public void setStatus(String statusString) {
		double ref = Double.parseDouble(statusString);
		NumVariable refVar = (NumVariable)getVariable("ref");
		NumValue refValue = (NumValue)refVar.valueAt(0);
		refValue.setValue(ref);
		mainData.variableChanged("y");
		valueChanged(0);
	}
}