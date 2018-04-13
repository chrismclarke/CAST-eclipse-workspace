package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class ScaleXYTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final public int X_TO_Y = 0;
	static final public int Y_TO_X = 1;
	
	private NumValue maxResultValue;
	
	private Edit xEdit, aEdit, bEdit, yEdit;
	
	public ScaleXYTemplatePanel(ExerciseApplet applet, int transformType, NumValue maxResultValue) {
		this(transformType, maxResultValue, new FormulaContext(Color.black, applet.getStandardFont(), applet));
		this.maxResultValue = maxResultValue;
	}
	
	public ScaleXYTemplatePanel(int transformType, NumValue maxResultValue, FormulaContext context) {
		super(null, null, maxResultValue, context);
		
		setFormula(transformType == X_TO_Y ? createXYFormula(context)
																								: createYXFormula(context));
	}
	
	public void setMaxResultValue(NumValue newMaxResultValue) {
		maxResultValue.setValue(newMaxResultValue.toDouble());
		maxResultValue.decimals = newMaxResultValue.decimals;
	}
	
	private FormulaPanel createXYFormula(FormulaContext context) {
		xEdit = new Edit("0.0", 4, context);
		aEdit = new Edit("0.0", 2, context);
		bEdit = new Edit("1.0", 3, context);
		
		FormulaPanel product = new Binary(Binary.TIMES, bEdit, xEdit, context);
		return new Binary(Binary.PLUS, aEdit, product, context);
	}
	
	private FormulaPanel createYXFormula(FormulaContext context) {
		yEdit = new Edit("0.0", 4, context);
		aEdit = new Edit("0.0", 2, context);
		bEdit = new Edit("1.0", 3, context);
		
		FormulaPanel numer = new Binary(Binary.MINUS, yEdit, aEdit, context);
		return new Ratio(numer, bEdit, context);
	}
	
	public void setXYValues(NumValue x, NumValue a, NumValue b) {
		xEdit.setValue(x);
		aEdit.setValue(a);
		bEdit.setValue(b);
		super.displayResult();
	}
	
	public void setYXValues(NumValue y, NumValue a, NumValue b) {
		yEdit.setValue(y);
		aEdit.setValue(a);
		bEdit.setValue(b);
		super.displayResult();
	}
	
	public String getStatus() {
		String s = aEdit.getValue() + " " + bEdit.getValue();
		if (xEdit != null)
			s += " " + xEdit.getValue();
		else
			s += " " + yEdit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue a = new NumValue(st.nextToken());
		NumValue b = new NumValue(st.nextToken());
		NumValue xy = new NumValue(st.nextToken());
		if (xEdit != null)
			setXYValues(xy, a, b);
		else
			setYXValues(xy, a, b);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}