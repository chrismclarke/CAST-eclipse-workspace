package exerciseGroups;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class SumDiffTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final public int SUM_OPTION = 0;
	static final public int DIFF_OPTION = 1;
	static final public int BASIC_OPTION = 0;
	static final public int SIMPLE_VAR_OPTION = 1;
	static final public int VAR_N_OPTION = 2;
	
	private boolean sumNotDiff;
	private int formulaType;
	private Edit x1Edit, x2Edit, n1Edit, n2Edit;
	
	public SumDiffTemplatePanel(int sumDiffType, int formulaType, NumValue maxResult, FormulaContext context) {
		super(null, null, maxResult, context);
		
		sumNotDiff = sumDiffType == SUM_OPTION;
		this.formulaType = formulaType;
		
		setFormula(createFormula(context));
	}
	
	public String getStatus() {
		String s = x1Edit.getValue() + " ";
		s += x2Edit.getValue() + " ";
		if (formulaType == VAR_N_OPTION) {
			s += n1Edit.getValue() + " ";
			s += n2Edit.getValue() + " ";
		}
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue x1 = new NumValue(st.nextToken());
		NumValue x2 = new NumValue(st.nextToken());
		NumValue n1 = null;
		NumValue n2 = null;
		if (formulaType == VAR_N_OPTION) {
			n1 = new NumValue(st.nextToken());
			n2 = new NumValue(st.nextToken());
		}
		setValues(x1, x2, n1, n2);
	}
	
	protected FormulaPanel createFormula(FormulaContext context) {
		x1Edit = new Edit("0", 4, context);
		x2Edit = new Edit("0", 4, context);
		
		FormulaPanel panel1, panel2;
		switch (formulaType) {
			case BASIC_OPTION:
				panel1 = x1Edit;
				panel2 = x2Edit;
				break;
			case SIMPLE_VAR_OPTION:
			case VAR_N_OPTION:
			default:
				panel1 = new Square(x1Edit, context);
				panel2 = new Square(x2Edit, context);
				if (formulaType == VAR_N_OPTION) {
					n1Edit = new Edit("0", 4, context);
					n2Edit = new Edit("0", 4, context);
					panel1 = new Ratio(panel1, n1Edit, context);
					panel2 = new Ratio(panel2, n2Edit, context);
				}
				break;
		}
		FormulaPanel sumDiff = new Binary(sumNotDiff ? Binary.PLUS : Binary.MINUS, panel1, panel2, context);
		
		return (formulaType == BASIC_OPTION) ? sumDiff : new Root(sumDiff, context);
	}
	
	public NumValue getX1() {
		return x1Edit.getValue();
	}
	
	public NumValue getX2() {
		return x2Edit.getValue();
	}
	
	public NumValue getN1() {
		return n1Edit.getValue();
	}
	
	public NumValue getN2() {
		return n2Edit.getValue();
	}
	
	public void setValues(NumValue x1, NumValue x2, NumValue n1, NumValue n2) {
		x1Edit.setValue(x1);
		x2Edit.setValue(x2);
		if (n1 != null)
			n1Edit.setValue(n1);
		if (n2 != null)
			n2Edit.setValue(n2);
		super.displayResult();
	}
	
	public void setValues(NumValue x1, NumValue x2) {
		setValues(x1, x2, null, null);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}