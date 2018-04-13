package percentile;

import java.awt.*;

import dataView.*;
import valueList.*;
import formula.*;



public class SimplePropnFormulaPanel extends Binary {
	private LabelValue kPrLabel;
	
	static public int translateInequality(int varInequality) {
		return varInequality - PropnConstants.LESS_THAN + Binary.LESS_THAN;
	}
	
	private Binary inequality;
	protected PropnRangeView propnView;
	private VariableName yName;
	private OneValueView refValueView;
	protected String yearsKey;		//	used to pass key to constructor for PropnPredictFormulaPanel
	protected NumValue maxYears;
	
	public SimplePropnFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
																							//	inequality as defined in PropnConstants
		super(Binary.EQUALS, context);
		kPrLabel = new LabelValue(context.getApplet().translate("Pr"));
		
		createFormulae(data, yKey, refKey, refData, maxY, varInequality, context);
	}
	
	public SimplePropnFormulaPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, String yearsKey,
								NumValue maxYears, FormulaContext context) {
															//	inequality as defined in PropnConstants
															//	yearsKey & maxYears used to pass key to constructor for PropnPredictFormulaPanel
		super(Binary.EQUALS, context);
		kPrLabel = new LabelValue(context.getApplet().translate("Pr"));
		
		this.yearsKey = yearsKey;
		this.maxYears = maxYears;
		
		createFormulae(data, yKey, refKey, refData, maxY, varInequality, context);
	}
	
	public void changeDetails(NumValue maxCutoff) {
		reinitialise();
		yName.invalidate();
		refValueView.reset(maxCutoff);
	}
	
	private void createFormulae(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaPanel left = leftPanel(data, yKey, refKey, refData, maxY, varInequality, context);
		FormulaPanel right = rightPanel(data, yKey, refKey, refData, maxY, varInequality, context);
		addSubFormulae(left, right);
	}
	
	public void setInequality(int varInequality) {	//	inequality as defined in PropnConstants
		inequality.changeOperator(translateInequality(varInequality));
		propnView.setComparison(varInequality);
	}
	
	protected FormulaPanel rightPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		FormulaContext redContext = context.getRecoloredContext(Color.red);
		
		propnView = new PropnRangeView(data, context.getApplet(), yKey, refKey,
										refData, varInequality, PropnConstants.PROPORTION);
		return new SummaryValue(propnView, redContext);
	}
	
	private FormulaPanel leftPanel(DataSet data, String yKey, String refKey,
								DataSet refData, NumValue maxY, int varInequality, FormulaContext context) {
		XApplet applet = context.getApplet();
		FormulaContext blackContext = context.getRecoloredContext(Color.black);
		
		yName = new VariableName(data, yKey, context);
		
		refValueView = new OneValueView(refData, refKey, applet, maxY);
		refValueView.unboxValue();
		refValueView.setNameDraw(false);
		refValueView.setHighlightSelection(false);
		SummaryValue refValue = new SummaryValue(refValueView, blackContext);
		
		int binaryInequality = translateInequality(varInequality);
		inequality = new Binary(binaryInequality, yName, refValue, blackContext);
		
		Bracket formula = new Bracket(inequality, blackContext);
		formula.setLeftLabel(kPrLabel);
		
		return formula;
	}
}