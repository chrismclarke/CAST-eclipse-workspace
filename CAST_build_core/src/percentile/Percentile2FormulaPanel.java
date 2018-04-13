package percentile;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class Percentile2FormulaPanel extends TextFormulaSequence {
	static final private Color kDarkGreen = new Color(0x009900);
	static final private NumValue kMaxPropn = new NumValue(1.0, 2);
	
	private LabelValue kPrLabel;
	
	private PropnRangeView percentileView;
	
	public Percentile2FormulaPanel(DataSet data, String yKey, String percentKey, String propnKey,
																DataSet refData, NumValue maxY, FormulaContext context) {
		super(context);
		
		kPrLabel = new LabelValue(context.getApplet().translate("Pr"));
		
		FormulaContext redContext = context.getRecoloredContext(Color.red);
		
		addItem(probPanel(data, yKey, propnKey, refData, maxY, context));
			
		addItem(context.getApplet().translate("is approximately"));
		
			OneValueView propnValueView = new OneValueView(refData, propnKey, context.getApplet(), kMaxPropn);
			propnValueView.unboxValue();
			propnValueView.setNameDraw(false);
			propnValueView.setHighlightSelection(false);
			propnValueView.setCenterValue(true);
			SummaryValue propn = new SummaryValue(propnValueView, redContext);
		addItem(propn);
	}
	
	private FormulaPanel probPanel(DataSet data, String yKey, String propnKey,
																	DataSet refData, NumValue maxY, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
		LabelValue yNameLabel = new LabelValue(data.getVariable(yKey).name);
		TextLabel yName = new TextLabel(yNameLabel, blackContext);
		
		percentileView = new PropnRangeView(data, context.getApplet(), yKey, propnKey, refData,
																	PropnRangeView.LESS_THAN, PropnRangeView.PERCENTILE, maxY);
		percentileView.setCenterValue(true);
		SummaryValue percentile = new SummaryValue(percentileView, greenContext);
		
		Binary inequality = new Binary(Binary.LESS_THAN, yName, percentile, blackContext);
		
		Bracket formula = new Bracket(inequality, blackContext);
		formula.setLeftLabel(kPrLabel);
		
		return formula;
	}
	
	public void setCumEvaluateType(int cumEvaluateType) {
		percentileView.setCumEvaluateType(cumEvaluateType);
	}
}