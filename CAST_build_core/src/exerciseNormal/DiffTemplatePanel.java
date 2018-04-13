package exerciseNormal;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class DiffTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kMaxDiffValue = new NumValue("9.9999");
	static final private NumValue kZeroValue = new NumValue(0, 0);
	
	static final public boolean BASIC = false;
	static final public boolean ADVANCED = true;
	
	private boolean advanced;
	private Edit x1Edit, x2Edit, x3Edit;
	
	public DiffTemplatePanel(boolean advanced, ExerciseApplet applet) {
		this(advanced, new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public DiffTemplatePanel(boolean advanced, FormulaContext context) {
		super(null, kMaxDiffValue, context);
		this.advanced = advanced;
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		x1Edit = new Edit("0", 5, context);
		x2Edit = new Edit("0", 5, context);
		
		if (advanced) {
			x3Edit = new Edit("0", 5, context);
			Binary subDiff = new Binary(Binary.MINUS, x2Edit, x3Edit, context);
			Bracket bracketed = new Bracket(subDiff, context);
			return new Binary(Binary.MINUS, x1Edit, bracketed, context);
		}
		else
			return new Binary(Binary.MINUS, x1Edit, x2Edit, context);
	}
	
	public void setValues(NumValue x1, NumValue x2) {
		x1Edit.setValue(x1);
		x2Edit.setValue(x2);
		if (x3Edit != null)
			x3Edit.setValue(kZeroValue);
		super.displayResult();
	}
	
	public void setValues(NumValue x1, NumValue x2, NumValue x3) {
		x1Edit.setValue(x1);
		x2Edit.setValue(x2);
		x3Edit.setValue(x3);
		super.displayResult();
	}
	
	public String getStatus() {
		String status = x1Edit.getValue() + " " + x2Edit.getValue();
		if (x3Edit != null)
			status += (" " + x3Edit.getValue());
		return status;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue x1Val = new NumValue(st.nextToken());
		NumValue x2Val = new NumValue(st.nextToken());
		if (st.hasMoreTokens()) {
			NumValue x3Val = new NumValue(st.nextToken());
			setValues(x1Val, x2Val, x3Val);
		}
		else
			setValues(x1Val, x2Val);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}