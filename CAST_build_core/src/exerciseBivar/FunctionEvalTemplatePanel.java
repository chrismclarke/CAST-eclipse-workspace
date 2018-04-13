package exerciseBivar;

import java.awt.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class FunctionEvalTemplatePanel extends MainFormulaPanel implements StatusInterface {
	private NumValue maxResultValue;
	
	private Edit xEdit;
	
	public FunctionEvalTemplatePanel(ExerciseApplet applet, int transformType, NumValue maxResultValue) {
		this(transformType, maxResultValue, new FormulaContext(Color.black, applet.getStandardFont(), applet));
		this.maxResultValue = maxResultValue;
	}
	
	public FunctionEvalTemplatePanel(int transformType, NumValue maxResultValue,
																																	FormulaContext context) {
		super(null, null, maxResultValue, context);
		
		setFormula(createFormula(transformType, context));
	}
	
	public void setMaxResultValue(NumValue newMaxResultValue) {
		maxResultValue.setValue(newMaxResultValue.toDouble());
		maxResultValue.decimals = newMaxResultValue.decimals;
	}
	
	private FormulaPanel createFormula(int transformType, FormulaContext context) {
		xEdit = new Edit("0.0", 5, context);
		
		return new Function(xEdit, transformType, context);
	}
	
	public void setXValue(NumValue x) {
		xEdit.setValue(x);
		super.displayResult();
	}
	
	public String getStatus() {
		return xEdit.getValue().toString();
	}
	
	public void setStatus(String statusString) {
		NumValue x = new NumValue(statusString);
		setXValue(x);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}