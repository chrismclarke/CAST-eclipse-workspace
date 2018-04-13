package exerciseBivar;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class SlopeTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final private NumValue kDefaultMaxValue = new NumValue("-99.999");
	
	private Edit x0Edit, x1Edit, y0Edit, y1Edit;
	
	public SlopeTemplatePanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public SlopeTemplatePanel(FormulaContext context) {
		super(null, null, kDefaultMaxValue, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		x0Edit = new Edit("0.0", 4, context);
		x1Edit = new Edit("0.0", 4, context);
		y0Edit = new Edit("0.0", 4, context);
		y1Edit = new Edit("0.0", 4, context);
		
		FormulaPanel numer = new Binary(Binary.MINUS, y1Edit, y0Edit, context);
		FormulaPanel denom = new Binary(Binary.MINUS, x1Edit, x0Edit, context);
		return new Ratio(numer, denom, context);
	}
	
	public void setValues(NumValue x0, NumValue y0, NumValue x1, NumValue y1) {
		x0Edit.setValue(x0);
		x1Edit.setValue(x1);
		y0Edit.setValue(y0);
		y1Edit.setValue(y1);
		super.displayResult();
	}
	
	public String getStatus() {
		return x0Edit.getValue() + " " + x1Edit.getValue() + " " + y0Edit.getValue() + " " + y1Edit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue x0 = new NumValue(st.nextToken());
		NumValue x1 = new NumValue(st.nextToken());
		NumValue y0 = new NumValue(st.nextToken());
		NumValue y1 = new NumValue(st.nextToken());
		setValues(x0, y0, x1, y1);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}