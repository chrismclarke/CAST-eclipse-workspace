package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class PropnTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kMaxProbValue = new NumValue("99.9999");
	
	private Edit numerEdit, denomEdit;
	
	public PropnTemplatePanel(String leftString, ExerciseApplet applet) {
		this(leftString, new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public PropnTemplatePanel(String leftString, FormulaContext context) {
		this(leftString, 3, context);	}
	
	public PropnTemplatePanel(String leftString, int editSize, FormulaContext context) {
		super(leftString, null, kMaxProbValue, context);
		
		setFormula(createFormula(editSize, context));
	}
	
	private FormulaPanel createFormula(int editSize, FormulaContext context) {
		numerEdit = new Edit("1", editSize, context);
		denomEdit = new Edit("1", editSize, context);
		
		return new Ratio(numerEdit, denomEdit, context);
	}
	
	public void setValues(NumValue numer, NumValue denom) {
		numerEdit.setValue(numer);
		denomEdit.setValue(denom);
		super.displayResult();
	}
	
	public String getStatus() {
		return numerEdit.getValue() + " " + denomEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue numer = new NumValue(st.nextToken());
		NumValue denom = new NumValue(st.nextToken());
		setValues(numer, denom);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}