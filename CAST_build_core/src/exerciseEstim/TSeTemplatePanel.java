package exerciseEstim;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class TSeTemplatePanel extends MainFormulaPanel implements StatusInterface {
	
	private Edit tEdit, sdEdit, nEdit;
	
	public TSeTemplatePanel(NumValue maxResult, FormulaContext context) {
		super(null, null, maxResult, context);
		
		setFormula(createTSeFormula(context));
	}
	
	public String getStatus() {
		String s = tEdit.getValue() + " " + sdEdit.getValue() + " " + nEdit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue t = new NumValue(st.nextToken());
		NumValue sd = new NumValue(st.nextToken());
		NumValue n = new NumValue(st.nextToken());
		setValues(t, sd, n);
	}
	
	public NumValue getSd() {
		return sdEdit.getValue();
	}
	
	public NumValue getT() {
		return tEdit.getValue();
	}
	
	public NumValue getN() {
		return nEdit.getValue();
	}
	
	private FormulaPanel createTSeFormula(FormulaContext context) {
		tEdit = new Edit("1", 4, context);
		sdEdit = new Edit("1", 4, context);
		nEdit = new Edit("1", 3, context);
		
		FormulaPanel seFormula = new Ratio(sdEdit, new Root(nEdit, context), context);
		
		return new Binary(Binary.TIMES, tEdit, seFormula, context);
	}
	
	public void setValues(NumValue t, NumValue sd, NumValue n) {
		tEdit.setValue(t);
		sdEdit.setValue(sd);
		nEdit.setValue(n);
		super.displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}