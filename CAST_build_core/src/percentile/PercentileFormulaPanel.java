package percentile;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import formula.*;



public class PercentileFormulaPanel extends TextFormulaSequence {
	static final private Color kDarkGreen = new Color(0x009900);
	static final private NumValue kMaxPercentile = new NumValue(100, 0);
	
	private PropnRangeView percentileView;
	private TextLabel unitsLabel;
	
	public PercentileFormulaPanel(DataSet data, String yKey, String percentKey,
								String propnKey, DataSet refData, NumValue maxY, String units, FormulaContext context) {
		super(context);
		
		XApplet applet = context.getApplet();
		FormulaContext redContext = context.getRecoloredContext(Color.red);
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
		StringTokenizer st = new StringTokenizer(context.getApplet().translate("The*'th percentile is"), "*");
		String theWord = st.nextToken();
		String percentileWord = st.nextToken();
		
		addItem(theWord);
		
			OneValueView percentValueView = new OneValueView(refData, percentKey, applet, kMaxPercentile);
			percentValueView.unboxValue();
			percentValueView.setNameDraw(false);
			percentValueView.setHighlightSelection(false);
			percentValueView.setCenterValue(true);
			SummaryValue percentage = new SummaryValue(percentValueView, redContext);
		addItem(percentage);
			
		addItem(percentileWord);
			percentileView = new PropnRangeView(data, applet, yKey, propnKey, refData,
																PropnRangeView.LESS_EQUAL, PropnRangeView.PERCENTILE, maxY);
			percentileView.setCenterValue(true);
			SummaryValue percentile = new SummaryValue(percentileView, greenContext);
		addItem(percentile);
		
		if (units != null) {
			unitsLabel = new TextLabel(units, context);
			addItem(unitsLabel);
		}
//			addItem(units);
	}
	
	public void setCumEvaluateType(int cumEvaluateType) {
		percentileView.setCumEvaluateType(cumEvaluateType);
	}
	
	public void changeData(NumValue maxValue, String units) {
		percentileView.setMaxValue(maxValue);
		if (unitsLabel != null && units != null)
			unitsLabel.changeText(units);
		reinitialise();
	}
}