package linMod;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import exercise2.*;
import utils.*;



public class PlusMinusCalcPanel extends MainFormulaPanel implements StatusInterface {
	private Edit tEdit, seEdit;
	
	static private Image leftImage(FormulaContext context) {
		RatioImages.loadRatio(context.getApplet());
		return RatioImages.plusMinus;
	}
	
	public PlusMinusCalcPanel(NumValue maxResultVal, FormulaContext context) {
		super(leftImage(context), RatioImages.kWidth, RatioImages.kAscent,
									RatioImages.kDescent, null, maxResultVal, context);
		
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		tEdit = new Edit("1.0", 4, context);
		seEdit = new Edit("1.0", 3, context);
		
		return new Binary(Binary.TIMES, tEdit, seEdit, context);
	}
	
	public void changeMaxValue(NumValue maxSe, NumValue maxPlusMinus) {
		seEdit.setColumns(maxSe.toString().length() - 1);
		reinitialise();
		super.changeMaxValue(maxPlusMinus);
	}
	
	public String getStatus() {
		String s = tEdit.getValue() + " " + seEdit.getValue();
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue t = new NumValue(st.nextToken());
		NumValue se = new NumValue(st.nextToken());
		setValues(t, se);
	}
	
	public NumValue getT() {
		return tEdit.getValue();
	}
	
	public NumValue getSe() {
		return seEdit.getValue();
	}
	
	public void setValues(NumValue t, NumValue se) {
		tEdit.setValue(t);
		seEdit.setValue(se);
		seEdit.revalidate();
		reinitialise();
		revalidate();
		super.displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		XApplet applet = context.getApplet();
		if (applet instanceof ExerciseApplet)
			((ExerciseApplet)applet).noteChangedWorking();
	}
}