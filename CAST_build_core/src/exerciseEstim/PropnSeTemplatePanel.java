package exerciseEstim;

import java.util.*;


import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class PropnSeTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final public int SE_ONLY = 0;
	static final public int SE_PLUS_Z = 1;
	static final public int SE_PLUS_2 = 2;
	
	private int zType;
	private Edit zEdit, pEdit, qEdit, nEdit;
	
	public PropnSeTemplatePanel(NumValue maxResult, int zType, FormulaContext context) {
		this(null, maxResult, zType, context);
	}
	
	public PropnSeTemplatePanel(String label, NumValue maxResult, int zType, FormulaContext context) {
		super(label, null, maxResult, context);
		
		this.zType = zType;
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		pEdit = new Edit("1", 4, context);
		qEdit = new Edit("1", 4, context);
		nEdit = new Edit("1", 3, context);
		
		FormulaPanel varFormula = new Ratio(new Binary(Binary.TIMES, pEdit, qEdit, context),
																																					nEdit, context);
		FormulaPanel seFormula = new Root(varFormula, context);
		
		if (zType == 1) {
			zEdit = new Edit("1", 4, context);
			return new Binary(Binary.TIMES, zEdit, seFormula, context);
		}
		else if (zType == 2) {
			FormulaPanel twoConst = new Const(new NumValue(2, 0), context);
			return new Binary(Binary.TIMES, twoConst, seFormula, context);
		}
		else
			return seFormula;
	}
	
	public String getStatus() {
		String s = pEdit.getValue() + " " + qEdit.getValue()
																													+ " " + nEdit.getValue();
		if (zType == 1)
			s += " " + zEdit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue p = new NumValue(st.nextToken());
		NumValue q = new NumValue(st.nextToken());
		NumValue n = new NumValue(st.nextToken());
		if (zType == 1) {
			NumValue z = new NumValue(st.nextToken());
			setValues(p, q, n, z);
		}
		else
			setValues(p, q, n);
	}
	
	public NumValue getZ() {
		return zEdit.getValue();
	}
	
	public NumValue getP() {
		return pEdit.getValue();
	}
	
	public NumValue getQ() {
		return qEdit.getValue();
	}
	
	public NumValue getN() {
		return nEdit.getValue();
	}
	
	public void setValues(NumValue p, NumValue q, NumValue n, NumValue z) {
		zEdit.setValue(z);
		setValues(p, q, n);
	}
	
	public void setValues(NumValue p, NumValue q, NumValue n) {
		pEdit.setValue(p);
		qEdit.setValue(q);
		nEdit.setValue(n);
		super.displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}