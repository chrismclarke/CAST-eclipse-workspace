package percentile;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import formula.*;



public class PropnPredict2FormulaPanel extends TextFormulaSequence {
	static final private Color kDarkGreen = new Color(0x009900);
	
	private Binary inequality;
	private PropnRangeView predictView;
	
	public PropnPredict2FormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, String yearsKey,
								NumValue maxYears, Object units, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(context);
		
		XApplet applet = context.getApplet();
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
		addItem(inequalityPanel(data, yKey, refKey, refData, maxY, varInequality, context));
		
		StringTokenizer st = new StringTokenizer(context.getApplet().translate("occurred with rate*out of"), "*");
		String occurredString = st.nextToken();
		String outOfString = st.nextToken();
		
		addItem(occurredString);
			predictView = new PropnRangeView(data, applet, yKey, refKey,
											refData, varInequality, yearsKey, maxYears);
			predictView.setCenterValue(true);
			SummaryValue predict = new SummaryValue(predictView, greenContext);
		addItem(predict);
		addItem(units);
		addItem(outOfString);
			OneValueView yearsValueView = new OneValueView(refData, yearsKey, applet,
																					new NumValue(maxYears.toDouble(), 0));
			yearsValueView.unboxValue();
			yearsValueView.setNameDraw(false);
			yearsValueView.setHighlightSelection(false);
			SummaryValue yearsValue = new SummaryValue(yearsValueView, context);
		addItem(yearsValue);
	}
	
	private FormulaPanel inequalityPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		VariableName yName = new VariableName(data, yKey, context);
		
		OneValueView refValueView = new OneValueView(refData, refKey, context.getApplet(), maxY);
		refValueView.unboxValue();
		refValueView.setNameDraw(false);
		refValueView.setHighlightSelection(false);
		SummaryValue refValue = new SummaryValue(refValueView, context);
		
		int binaryInequality = SimplePropnFormulaPanel.translateInequality(varInequality);
		inequality = new Binary(binaryInequality, yName, refValue, context);
		
		return inequality;
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnRangeView
		inequality.changeOperator(SimplePropnFormulaPanel.translateInequality(varInequality));
		predictView.setComparison(varInequality);
	}
}