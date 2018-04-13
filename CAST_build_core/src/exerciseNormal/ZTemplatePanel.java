package exerciseNormal;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class ZTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kMaxZValue = new NumValue("-99.999");
	
	private Edit xEdit, meanEdit, sdEdit;
	
	public ZTemplatePanel(String leftString, int columns, FormulaContext context) {
		super(leftString, null, kMaxZValue, context);
		
		setFormula(createFormula(columns, context));
	}
	
	public ZTemplatePanel(FormulaContext context) {
		this("z =", 3, context);
	}
	
	private FormulaPanel createFormula(int columns, FormulaContext context) {
		xEdit = new Edit("0.0", columns, context);
		meanEdit = new Edit("0.0", columns, context);
		sdEdit = new Edit("1.0", columns, context);
		
		FormulaPanel numer = new Binary(Binary.MINUS, xEdit, meanEdit, context);
		return new Ratio(numer, sdEdit, context);
	}
	
	public void setValues(NumValue x, NumValue mean, NumValue sd) {
		xEdit.setValue(x);
		meanEdit.setValue(mean);
		sdEdit.setValue(sd);
		super.displayResult();
	}
	
	public String getStatus() {
		return xEdit.getValue() + " " + meanEdit.getValue() + " " + sdEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue x = new NumValue(st.nextToken());
		NumValue mean = new NumValue(st.nextToken());
		NumValue sd = new NumValue(st.nextToken());
		setValues(x, mean, sd);
	}
	
	public NumValue getX1() {
		return xEdit.getValue();
	}
	
	public NumValue getX2() {
		return meanEdit.getValue();
	}
	
	public NumValue getSd() {
		return sdEdit.getValue();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}