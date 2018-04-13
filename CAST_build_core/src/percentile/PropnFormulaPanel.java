package percentile;

import java.awt.*;

import dataView.*;
import formula.*;



public class PropnFormulaPanel extends SimplePropnFormulaPanel {
	private PropnRangeView countView;
	
	public PropnFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnConstants
		super(data, yKey, refKey, refData, maxY, varInequality, context);
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnConstants
		super.setInequality(varInequality);
		countView.setComparison(varInequality);
	}
	
	private FormulaPanel dataRatioPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		FormulaContext redContext = context.getRecoloredContext(Color.red);
		
		countView = new PropnRangeView(data, context.getApplet(), yKey, refKey, refData,
																												varInequality, PropnConstants.COUNT);
		countView.setCenterValue(true);
		SummaryValue count = new SummaryValue(countView, redContext);
		
		SampleSize sampSize = new SampleSize(data, yKey, blackContext);
		
		return new Ratio(count, sampSize, blackContext);
	}
	
	protected FormulaPanel rightPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		
		FormulaPanel formula = dataRatioPanel(data, yKey, refKey, refData, maxY, varInequality, context);
		
		FormulaPanel propn = super.rightPanel(data, yKey, refKey, refData,
																											maxY, varInequality, context);
		
		return new Binary(Binary.EQUALS, formula, propn, blackContext);
	}
}