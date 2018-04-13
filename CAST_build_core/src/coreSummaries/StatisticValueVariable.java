package coreSummaries;

import dataView.*;

public class StatisticValueVariable extends NumSummaryVariable {
	private CoreHypothesisTest test;
	private int decimals;
	
	public StatisticValueVariable(String theName, CoreHypothesisTest test, int decimals) {
		super(theName);
		this.test = test;
		this.decimals = decimals;
	}
	
	protected NumValue evaluateSummary(DataSet sourceData) {
		double pValue = test.evaluateStatistic();
		return new NumValue(pValue, decimals);
	}
	
	public void setTest(CoreHypothesisTest test) {
		this.test = test;
	}
}
