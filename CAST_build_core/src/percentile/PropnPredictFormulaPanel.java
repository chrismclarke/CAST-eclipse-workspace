package percentile;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class PropnPredictFormulaPanel extends SimplePropnFormulaPanel {
	static final private Color kDarkGreen = new Color(0x009900);
	
	private PropnRangeView predictView;
	
	public PropnPredictFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, String yearsKey,
								NumValue maxYears, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(data, yKey, refKey, refData, maxY, varInequality, yearsKey, maxYears, context);
	}
	
	private FormulaPanel predictRatioPanel(DataSet data, String yKey, String refKey,
									DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		XApplet applet = context.getApplet();
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
			predictView = new PropnRangeView(data, applet, yKey, refKey,
											refData, varInequality, yearsKey, maxYears);
			predictView.setCenterValue(true);
		SummaryValue predict = new SummaryValue(predictView, greenContext);
		
			OneValueView yearsValueView = new OneValueView(refData, yearsKey, applet,
																									new NumValue(maxYears.toDouble(), 0));
			yearsValueView.unboxValue();
			yearsValueView.setCenterValue(true);
			yearsValueView.setNameDraw(false);
			yearsValueView.setHighlightSelection(false);
		SummaryValue yearsValue = new SummaryValue(yearsValueView, context);
		
		return new Ratio(predict, yearsValue, context);
	}
	
	protected FormulaPanel rightPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaPanel propn = super.rightPanel(data, yKey, refKey, refData,
																												maxY, varInequality, context);
		propnView.unboxValue();
		
		FormulaPanel ratioPanel = predictRatioPanel(data, yKey, refKey, refData,
																																maxY, varInequality, context);
		
		return new Binary(Binary.EQUALS, propn, ratioPanel, context);
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnRangeView
		super.setInequality(varInequality);
		predictView.setComparison(varInequality);
	}
}