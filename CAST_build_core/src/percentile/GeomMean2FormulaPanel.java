package percentile;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import formula.*;



public class GeomMean2FormulaPanel extends TextFormulaSequence {
	static final private Color kDarkGreen = new Color(0x009900);
	
	private Binary inequality;
	private PropnRangeView meanView;
	
	public GeomMean2FormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(context);
		
		XApplet applet = context.getApplet();
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
		addItem(inequalityPanel(data, yKey, refKey, refData, maxY, varInequality, context));
		
		StringTokenizer st = new StringTokenizer(context.getApplet().translate("occurred on average once in*years"), "*");
		String occurredString = st.nextToken();
		String yearsString = st.nextToken();
		
		addItem(occurredString);
			meanView = new PropnRangeView(data, applet, yKey, refKey, refData, varInequality, PropnRangeView.GEOM_MEAN);
			meanView.setCenterValue(true);
			SummaryValue mean = new SummaryValue(meanView, greenContext);
		addItem(mean);
		addItem(yearsString);
	}
	
	private FormulaPanel inequalityPanel(DataSet data, String yKey, String refKey,
									DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		LabelValue yNameLabel = new LabelValue(data.getVariable(yKey).name);
		TextLabel yName = new TextLabel(yNameLabel, context);
		
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
		meanView.setComparison(varInequality);
	}
}