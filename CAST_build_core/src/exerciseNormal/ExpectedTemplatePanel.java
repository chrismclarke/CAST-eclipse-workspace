package exerciseNormal;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class ExpectedTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kMaxZValue = new NumValue("-99.999");
	
	private Edit nEdit, pEdit;
	
	public ExpectedTemplatePanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public ExpectedTemplatePanel(FormulaContext context) {
		super(null, kMaxZValue, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		nEdit = new Edit("0", 3, context);
		pEdit = new Edit("0.0", 5, context);
		
		return new Binary(Binary.TIMES, nEdit, pEdit, context);
	}
	
	public void setValues(NumValue n, NumValue p) {
		nEdit.setValue(n);
		pEdit.setValue(p);
		super.displayResult();
	}
	
	public String getStatus() {
		return nEdit.getValue() + " " + pEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue n = new NumValue(st.nextToken());
		NumValue p = new NumValue(st.nextToken());
		setValues(n, p);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}