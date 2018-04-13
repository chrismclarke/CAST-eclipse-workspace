package exerciseBivar;

import java.awt.*;
import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;


public class LinearEquationPanel extends Binary implements StatusInterface {
//	static final private NumValue kDefaultMaxValue = new NumValue("-99.999");
	
	private TextLabel yVarName, xVarName;
	private Edit interceptEdit, slopeEdit;
	
	public LinearEquationPanel(ExerciseApplet applet) {
		this(new FormulaContext(Color.black, applet.getStandardFont(), applet));
	}
	
	public LinearEquationPanel(FormulaContext context) {
		super(EQUALS, context);
		
		yVarName = new TextLabel("", context);
		addSubFormulae(yVarName, createLinearPart(context));
	}
	
	public String getStatus() {
		return interceptEdit.getValue() + " " + slopeEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		NumValue intercept = new NumValue(st.nextToken());
		NumValue slope = new NumValue(st.nextToken());
		interceptEdit.setValue(intercept);
		slopeEdit.setValue(slope);
	}
	
	private FormulaPanel createLinearPart(FormulaContext context) {
		interceptEdit = new Edit("0.0", 4, context);
		slopeEdit = new Edit("0.0", 5, context);
		xVarName = new TextLabel("", context);
		
//		FormulaPanel product = new Binary(Binary.TIMES, slopeEdit, xVarName, c, f);
		TextFormulaSequence product = new TextFormulaSequence(context);
		product.addItem(slopeEdit);
		product.addItem(xVarName);
		return new Binary(Binary.PLUS, interceptEdit, product, context);
	}
	
	public void setValues(NumValue intercept, NumValue slope) {
		interceptEdit.setValue(intercept);
		slopeEdit.setValue(slope);
	}
	
	public void setVarNames(String yNameString, String xNameString) {
		yVarName.changeText(yNameString);		//	does invalidate()
		xVarName.changeText(xNameString);
		reinitialise();
	}
	
	public NumValue getIntercept() {
		return interceptEdit.getValue();
	}
	
	public NumValue getSlope() {
		return slopeEdit.getValue();
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
		return true;
	}
}