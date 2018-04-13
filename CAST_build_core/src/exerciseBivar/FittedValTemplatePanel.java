package exerciseBivar;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class FittedValTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kDefaultMaxValue = new NumValue("-99.999");
	
	private ExerciseApplet exerciseApplet;
	private Edit interceptEdit, slopeEdit, xEdit;
	
	public FittedValTemplatePanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public FittedValTemplatePanel(FormulaContext context) {
		super(null, null, kDefaultMaxValue, context);
		
		setFormula(createFormula(context));
		exerciseApplet = (ExerciseApplet)context.getApplet();
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		interceptEdit = new Edit("0.0", 4, context);
		slopeEdit = new Edit("0.0", 5, context);
		xEdit = new Edit("0.0", 4, context);
		
		FormulaPanel product = new Binary(Binary.TIMES, slopeEdit, xEdit, context);
		return new Binary(Binary.PLUS, interceptEdit, product, context);
	}
	
	public void setValues(NumValue intercept, NumValue slope, NumValue x) {
		interceptEdit.setValue(intercept);
		slopeEdit.setValue(slope);
		xEdit.setValue(x);
		super.displayResult();
	}
	
	public String getStatus() {
		return interceptEdit.getValue() + " " + slopeEdit.getValue() + " " + xEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue intercept = new NumValue(st.nextToken());
		NumValue slope = new NumValue(st.nextToken());
		NumValue x = new NumValue(st.nextToken());
		setValues(intercept, slope, x);
	}
	
	public void displayResult() {
		super.displayResult();
		exerciseApplet.noteChangedWorking();
	}
}