package exerciseEstim;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class RatioTemplatePanel extends MainFormulaPanel implements StatusInterface {
	
	private Edit numerEdit, denomEdit;
	
	public RatioTemplatePanel(NumValue maxResult, FormulaContext context) {
		super(null, null, maxResult, context);
		
		setFormula(createSumMeanFormula(context));
	}
	
	public String getStatus() {
		String s = numerEdit.getValue() + " " + denomEdit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue numer = new NumValue(st.nextToken());
		NumValue denom = new NumValue(st.nextToken());
		setValues(numer, denom);
	}
	
	private FormulaPanel createSumMeanFormula(FormulaContext context) {
		numerEdit = new Edit("1", 5, context);
		denomEdit = new Edit("1", 3, context);
		
		return new Ratio(numerEdit, denomEdit, context);
	}
	
	public void setValues(NumValue numer, NumValue denom) {
		numerEdit.setValue(numer);
		denomEdit.setValue(denom);
		super.displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}