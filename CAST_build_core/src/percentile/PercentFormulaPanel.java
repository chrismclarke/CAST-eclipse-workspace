package percentile;

import java.awt.*;

import dataView.*;
import formula.*;



public class PercentFormulaPanel extends PropnFormulaPanel {
	private PropnRangeView percentView;
	
	public PercentFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(data, yKey, refKey, refData, maxY, varInequality, context);
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnRangeView
		super.setInequality(varInequality);
		percentView.setComparison(varInequality);
	}
	
	protected FormulaPanel rightPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		FormulaContext redContext = context.getRecoloredContext(Color.red);
		
			percentView = new PropnRangeView(data, context.getApplet(), yKey, refKey,
										refData, varInequality, PropnRangeView.PERCENTAGE);
			percentView.setUnitsString("%");
		SummaryValue percent = new SummaryValue(percentView, redContext);
		
		FormulaPanel propn = super.rightPanel(data, yKey, refKey, refData,
																										maxY, varInequality, context);
		
		return new Binary(Binary.EQUALS, propn, percent, blackContext);
	}
}