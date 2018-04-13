package exerciseGroups;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class DiffTTemplatePanel extends MainFormulaPanel implements StatusInterface {
	private Edit mean1Edit, sd1Edit, n1Edit, mean2Edit, sd2Edit, n2Edit;
	
	public DiffTTemplatePanel(String label, NumValue maxResult, FormulaContext context) {
		super(label, null, maxResult, context);
		setFormula(createFormula(context));
	}
	
	public String getStatus() {
		String s = mean1Edit.getValue() + " " + sd1Edit.getValue() + " " + n1Edit.getValue() + " ";
		s += mean2Edit.getValue() + " " + sd2Edit.getValue() + " " + n2Edit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue mean1 = new NumValue(st.nextToken());
		NumValue sd1 = new NumValue(st.nextToken());
		NumValue n1 = new NumValue(st.nextToken());
		NumValue mean2 = new NumValue(st.nextToken());
		NumValue sd2 = new NumValue(st.nextToken());
		NumValue n2 = new NumValue(st.nextToken());
		setValues(mean1, sd1, n1, mean2, sd2, n2);
	}
	
	protected FormulaPanel createFormula(FormulaContext context) {
		mean1Edit = new Edit("0", 4, context);
		sd1Edit = new Edit("0", 4, context);
		n1Edit = new Edit("0", 4, context);
		mean2Edit = new Edit("0", 4, context);
		sd2Edit = new Edit("0", 4, context);
		n2Edit = new Edit("0", 4, context);
		
		FormulaPanel diffMeanPanel = new Binary(Binary.MINUS, mean1Edit, mean2Edit, context);
		
		FormulaPanel diffVarPanel = new Binary(Binary.PLUS, varPanel(sd1Edit, n1Edit, context),
																								varPanel(sd2Edit, n2Edit, context), context);
		FormulaPanel sePanel = new Root(diffVarPanel, context);
		
		return new Ratio(diffMeanPanel, sePanel, context);
	}
	
	private FormulaPanel varPanel(FormulaPanel sdEdit, FormulaPanel nEdit, FormulaContext context) {
		return new Ratio(new Square(sdEdit, context), nEdit, context);
	}
	
	public NumValue getMean1() {
		return mean1Edit.getValue();
	}
	
	public NumValue getSd1() {
		return sd1Edit.getValue();
	}
	
	public NumValue getN1() {
		return n1Edit.getValue();
	}
	
	public NumValue getMean2() {
		return mean2Edit.getValue();
	}
	
	public NumValue getSd2() {
		return sd2Edit.getValue();
	}
	
	public NumValue getN2() {
		return n2Edit.getValue();
	}
	
	public void setValues(NumValue mean1, NumValue sd1, NumValue n1, NumValue mean2, NumValue sd2, NumValue n2) {
		mean1Edit.setValue(mean1);
		sd1Edit.setValue(sd1);
		n1Edit.setValue(n1);
		mean2Edit.setValue(mean2);
		sd2Edit.setValue(sd2);
		n2Edit.setValue(n2);
		displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}