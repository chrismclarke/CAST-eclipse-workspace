package exerciseCateg;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class ExpectedTemplatePanel extends MainFormulaPanel implements StatusInterface {
	
	private Edit rowTotalEdit, colTotalEdit, totalEdit;
	
	public ExpectedTemplatePanel(String leftString, int editSize, FormulaContext context, NumValue resultTemplate) {
		super(leftString, null, resultTemplate, context);
		
		setFormula(createFormula(editSize, context));
	}
	
	private FormulaPanel createFormula(int editSize, FormulaContext context) {
		rowTotalEdit = new Edit("1", editSize, context);
		rowTotalEdit.setIntegerType();
		colTotalEdit = new Edit("1", editSize, context);
		colTotalEdit.setIntegerType();
		Binary numer = new Binary(BinaryConstants.TIMES, rowTotalEdit, colTotalEdit, context);
		
		totalEdit = new Edit("1", editSize, context);
		totalEdit.setIntegerType();
		
		return new Ratio(numer, totalEdit, context);
	}
	
	public void setValues(int rowTotal, int colTotal, int total) {
		rowTotalEdit.setValue(rowTotal);
		colTotalEdit.setValue(colTotal);
		totalEdit.setValue(total);
		super.displayResult();
	}
	
	public String getStatus() {
		return rowTotalEdit.getValue() + " " + colTotalEdit.getValue() + " " + totalEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		int rowTotal = Integer.parseInt(st.nextToken());
		int colTotal = Integer.parseInt(st.nextToken());
		int total = Integer.parseInt(st.nextToken());
		setValues(rowTotal, colTotal, total);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}