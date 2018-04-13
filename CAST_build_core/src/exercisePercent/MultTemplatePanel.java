package exercisePercent;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class MultTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kMaxZValue = new NumValue("-99.999");
	
	private Edit numerEdit, denomEdit, factorEdit;
	
	public MultTemplatePanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public MultTemplatePanel(FormulaContext context) {
		super(null, null, kMaxZValue, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		numerEdit = new Edit("1.0", 4, context);
		denomEdit = new Edit("1.0", 4, context);
		factorEdit = new Edit("1.0", 5, context);
		
		FormulaPanel left = new Ratio(numerEdit, denomEdit, context);
		return new Binary(Binary.TIMES, left, factorEdit, context);
	}
	
	public void setValues(NumValue numer, NumValue denom, NumValue factor) {
		numerEdit.setValue(numer);
		denomEdit.setValue(denom);
		factorEdit.setValue(factor);
		super.displayResult();
	}
	
	public String getStatus() {
		return numerEdit.getValue() + " " + denomEdit.getValue() + " " + factorEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue numer = new NumValue(st.nextToken());
		NumValue denom = new NumValue(st.nextToken());
		NumValue factor = new NumValue(st.nextToken());
		setValues(numer, denom, factor);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}