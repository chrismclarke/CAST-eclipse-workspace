package exerciseNormal;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class ZInverseTemplatePanel extends MainFormulaPanel implements StatusInterface {
	private Edit zEdit, meanEdit, sdEdit;
	
	public ZInverseTemplatePanel(NumValue maxX, ExerciseApplet applet) {
		this(maxX, new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public ZInverseTemplatePanel(NumValue maxX, FormulaContext context) {
		super("x =", null, maxX, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		zEdit = new Edit("0.0", 5, context);
		meanEdit = new Edit("0.0", 3, context);
		sdEdit = new Edit("1.0", 3, context);
		
		FormulaPanel scaledZ = new Binary(Binary.TIMES, sdEdit, zEdit, context);
		return new Binary(Binary.PLUS, meanEdit, scaledZ, context);
	}
	
	public void setValues(NumValue z, NumValue mean, NumValue sd) {
		zEdit.setValue(z);
		meanEdit.setValue(mean);
		sdEdit.setValue(sd);
		super.displayResult();
	}
	
	public String getStatus() {
		return zEdit.getValue() + " " + meanEdit.getValue() + " " + sdEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue zVal = new NumValue(st.nextToken());
		NumValue meanVal = new NumValue(st.nextToken());
		NumValue sdVal = new NumValue(st.nextToken());
		setValues(zVal, meanVal, sdVal);
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}