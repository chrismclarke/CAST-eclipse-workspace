package percentile;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import formula.*;



public class GeomMean3FormulaPanel extends TextFormulaSequence {
	static final private Color kDarkGreen = new Color(0x009900);
	
	private Binary inequality;
	private PropnRangeView meanView;
	
	public GeomMean3FormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(context);
		
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
		StringTokenizer st = new StringTokenizer(context.getApplet().translate("i.e. the return period for*was*years"), "*");
		String returnString = st.nextToken();
		String wasString = st.nextToken();
		String yearsString = st.nextToken();
		
		addItem(returnString);
		addItem(inequalityPanel(data, yKey, refKey, refData, maxY, varInequality, context));
		addItem(wasString);
			meanView = new PropnRangeView(data, context.getApplet(), yKey, refKey,
											refData, varInequality, PropnRangeView.GEOM_MEAN);
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