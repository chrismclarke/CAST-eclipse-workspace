package statistic;

import dataView.*;
import coreGraphics.*;
import coreSummaries.*;


public class SpreadVariable extends SDVariable {
	static final public int SD_SUMMARY = 0;
	static final public int IQR_SUMMARY = 1;
	static final public int RANGE_SUMMARY = 2;
	
	private int summaryType;
	
	public SpreadVariable(String theName, String sourceKey, int summaryType, int decimals) {
		super(theName, sourceKey, decimals);
		this.summaryType = summaryType;
	}
	
	protected NumValue evaluateSummary(DataSet sourceData) {
		if (summaryType == SD_SUMMARY)
			return super.evaluateSummary(sourceData);
		
		NumVariable yVar = (NumVariable)sourceData.getVariable(sourceKey);
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.initialiseBox(yVar.getSortedData(), false, null);
		double summary = (summaryType == IQR_SUMMARY)
										? boxInfo.boxVal[BoxInfo.HIGH_QUART] - boxInfo.boxVal[BoxInfo.LOW_QUART]
										: boxInfo.boxVal[BoxInfo.HIGH_EXT] - boxInfo.boxVal[BoxInfo.LOW_EXT];
		
		return new NumValue(summary, decimals);
	}
}
