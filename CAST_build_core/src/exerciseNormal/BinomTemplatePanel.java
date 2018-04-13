package exerciseNormal;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class BinomTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final public int X_MEAN = 0;
	static final public int X_SD = 1;
	static final public int P_SD = 2;
	
	@SuppressWarnings("unused")
	private int templateType;
	private Edit nEdit, pEdit, qEdit;
	
	public BinomTemplatePanel(String leftString, int templateType, NumValue maxResult, FormulaContext context) {
		super(leftString, null, maxResult, context);
		
		this.templateType = templateType;
		switch (templateType) {
			case X_MEAN:
				setFormula(createXMeanFormula(context));
				break;
			case X_SD:
				setFormula(createXSdFormula(context));
				break;
			case P_SD:
				setFormula(createPSdFormula(context));
				break;
		}
	}
	
	public BinomTemplatePanel(int templateType, NumValue maxResult, FormulaContext context) {
		this(null, templateType, maxResult, context);
	}
	
	private FormulaPanel createXMeanFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		pEdit = new Edit("0.5", 3, context);
		
		return new Binary(Binary.TIMES, nEdit, pEdit, context);
	}
	
	private FormulaPanel createXSdFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		pEdit = new Edit("0.5", 3, context);
		qEdit = new Edit("0.5", 3, context);
		
		FormulaPanel varFormula = new Binary(Binary.TIMES, nEdit, new Binary(Binary.TIMES, pEdit, qEdit, context), context);
		
		return new Root(varFormula, context);
	}
	
	private FormulaPanel createPSdFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		pEdit = new Edit("0.5", 3, context);
		qEdit = new Edit("0.5", 3, context);
		
		FormulaPanel varFormula = new Ratio(new Binary(Binary.TIMES, pEdit, qEdit, context), nEdit, context);
		
		return new Root(varFormula, context);
	}
	
	public void setValues(NumValue n, NumValue p, NumValue q) {
		nEdit.setValue(n);
		pEdit.setValue(p);
		if (qEdit !=  null)
			qEdit.setValue(q);
		super.displayResult();
	}
	
	public String getStatus() {
		String status = nEdit.getValue() + " " + pEdit.getValue();
		if (qEdit != null)
			status += " " + qEdit.getValue();
		return status;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue n = new NumValue(st.nextToken());
		NumValue p = new NumValue(st.nextToken());
		NumValue q = st.hasMoreTokens() ? new NumValue(st.nextToken()) : null;
		setValues(n, p, q);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}