package exper;

import dataView.*;
import models.*;

public class MultiRegnParamVariable extends NumSummaryVariable {
	private int paramIndex;
	private String lsKey;
	
	public MultiRegnParamVariable(String theName, String lsKey, int paramIndex) {
		super(theName);
		this.lsKey = lsKey;
		this.paramIndex = paramIndex;
	}
	
	protected NumValue evaluateSummary(DataSet sourceData) {
		MultipleRegnModel ls = (MultipleRegnModel)sourceData.getVariable(lsKey);
		return ls.getParameter(paramIndex);
	}
}
