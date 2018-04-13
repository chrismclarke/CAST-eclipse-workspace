package coreSummaries;

import dataView.*;

public class DiffSummaryVariable extends NumSummaryVariable {
	private int decimals;
	private String numKey, catKey;
	
	private int plusIndex = 1;
	private int minusIndex = 0;
	
	public DiffSummaryVariable(String theName, String numKey, String catKey, int decimals) {
		super(theName);
		this.catKey = catKey;
		this.numKey = numKey;
		this.decimals = decimals;
	}
	
	public void setDiffIndices(int plusIndex, int minusIndex) {
		this.plusIndex = plusIndex;
		this.minusIndex = minusIndex;
	}
	
	protected NumValue evaluateSummary(DataSet sourceData) {
		NumVariable yVar = (NumVariable)sourceData.getVariable(numKey);
		CatVariable xVar = (CatVariable)sourceData.getVariable(catKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		
		int count[] = new int[xVar.noOfCategories()];
		double sum[] = new double[xVar.noOfCategories()];
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int x = xVar.labelIndex(xe.nextValue());
			count[x] ++;
			sum[x] += y;
		}
		
		return new NumValue(sum[plusIndex] / count[plusIndex]
														- sum[minusIndex] / count[minusIndex], decimals);
	}
}
