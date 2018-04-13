package exerciseBivar;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class InterceptTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kDefaultMaxValue = new NumValue("-99.999");
	
	private Edit yEdit, xEdit, slopeEdit;
	
	public InterceptTemplatePanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public InterceptTemplatePanel(FormulaContext context) {
		super(null, null, kDefaultMaxValue, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		yEdit = new Edit("0.0", 4, context);
		xEdit = new Edit("0.0", 4, context);
		slopeEdit = new Edit("0.0", 4, context);
		
		FormulaPanel product = new Binary(Binary.TIMES, slopeEdit, xEdit, context);
		return new Binary(Binary.MINUS, yEdit, product, context);
	}
	
	public void setValues(NumValue y, NumValue slope, NumValue x) {
		yEdit.setValue(y);
		slopeEdit.setValue(slope);
		xEdit.setValue(x);
		super.displayResult();
	}
	
	public String getStatus() {
		return yEdit.getValue() + " " + slopeEdit.getValue() + " " + xEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue y = new NumValue(st.nextToken());
		NumValue slope = new NumValue(st.nextToken());
		NumValue x = new NumValue(st.nextToken());
		setValues(y, slope, x);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}