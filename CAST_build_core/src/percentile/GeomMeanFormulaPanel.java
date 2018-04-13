package percentile;

import java.awt.*;

import dataView.*;
import formula.*;



public class GeomMeanFormulaPanel extends SimplePropnFormulaPanel {
	static final private Color kDarkGreen = new Color(0x009900);
	static final private NumValue kOne = new NumValue(1, 0);
	
	private PropnRangeView meanView;
	
	public GeomMeanFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnRangeView
		super(data, yKey, refKey, refData, maxY, varInequality, context);
	}
	
	private FormulaPanel predictRatioPanel(DataSet data, String yKey, String refKey,
									DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		FormulaContext greenContext = context.getRecoloredContext(kDarkGreen);
		
			meanView = new PropnRangeView(data, context.getApplet(), yKey, refKey, refData,
																								varInequality, PropnRangeView.GEOM_MEAN);
			meanView.setCenterValue(true);
		SummaryValue mean = new SummaryValue(meanView, greenContext);
		
		Const one = new Const(kOne, blackContext);
		
		return new Ratio(one, mean, blackContext);
	}
	
	protected FormulaPanel rightPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		
		FormulaPanel propn = super.rightPanel(data, yKey, refKey, refData,
																							maxY, varInequality, context);
		propnView.unboxValue();
		
		FormulaPanel ratioPanel = predictRatioPanel(data, yKey, refKey, refData,
																							maxY, varInequality, context);
		
		return new Binary(Binary.EQUALS, propn, ratioPanel, blackContext);
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnRangeView
		super.setInequality(varInequality);
		meanView.setComparison(varInequality);
	}
}