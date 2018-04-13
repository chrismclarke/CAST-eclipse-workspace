package twoGroup;

import java.util.*;

import dataView.*;
import formula.*;
import imageGroups.*;
import utils.*;
import exercise2.*;



public class DiffSDCalcPanel extends MainFormulaPanel implements StatusInterface {
	static final public int MEANS = 0;
	static final public int PROPNS = 1;
	
	private Edit s1Edit, s2Edit;
	
	public DiffSDCalcPanel(NumValue maxResult, FormulaContext context) {
		this(null, maxResult, context);
	}
	
	public DiffSDCalcPanel(String title, NumValue maxResult, FormulaContext context) {
		super(title, null, maxResult, context);
		setFormula(createFormula(context));
	}
	
	public DiffSDCalcPanel(int imageType, NumValue maxResultVal, FormulaContext context) {
		super((imageType == MEANS) ? GroupsEqualsImages.sigmaDiffHat : GroupsEqualsImages.sigmaPDiffHat,
					GroupsEqualsImages.kSigmaDiffParamWidth, GroupsEqualsImages.kSigmaDiffParamAscent,
					GroupsEqualsImages.kSigmaDiffParamDescent,
					null, maxResultVal, context);
		setFormula(createFormula(context));
	}
	
	private FormulaPanel createFormula(FormulaContext context) {
		FormulaContext smallContext = context.getSmallerContext();
		
		s1Edit = new Edit("1.0", 3, context);
		s2Edit = new Edit("1.0", 3, context);
		FormulaPanel sum = new Binary(Binary.PLUS, new Square(s1Edit, smallContext), 
																		new Square(s2Edit, smallContext), context);
		return new Root(sum, context);
	}
	
	public void changeMaxValue(NumValue maxVal) {
		s1Edit.setColumns(maxVal.toString().length() - 2);
		s2Edit.setColumns(maxVal.toString().length() - 2);
		super.changeMaxValue(maxVal);
	}
	
	public String getStatus() {
		return s1Edit.getValue() + " " + s2Edit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue s1 = new NumValue(st.nextToken());
		NumValue s2 = new NumValue(st.nextToken());
		setValues(s1, s2);
	}
	
	public void setValues(NumValue s1, NumValue s2) {
		s1Edit.setValue(s1);
		s2Edit.setValue(s2);
		displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		XApplet applet = context.getApplet();
		if (applet instanceof ExerciseApplet)
			((ExerciseApplet)applet).noteChangedWorking();
	}
}