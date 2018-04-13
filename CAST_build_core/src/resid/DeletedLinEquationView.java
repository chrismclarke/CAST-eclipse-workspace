package resid;

import java.awt.*;

import dataView.*;
import models.*;

import regn.*;


public class DeletedLinEquationView extends LinearEquationView {
//	static public final String DELETED_LINEAR_MODEL = "deletedLinearEquation";
	
	private String yKey;
	private NumValue tempIntercept, tempSlope;
	
	public DeletedLinEquationView(DataSet theData, XApplet applet, String modelKey, String yKey,
															String yName, String xName, NumValue maxIntercept, NumValue maxSlope) {
		super(theData, applet, modelKey, yName, xName, maxIntercept, maxIntercept,
																													maxSlope, maxSlope, null, null);
		this.yKey = yKey;
		tempIntercept = new NumValue(maxIntercept);
		tempSlope = new NumValue(maxSlope);
	}
	
	protected NumValue getParamValue(int paramIndex) {
		LinearModel lm = (LinearModel)getVariable(modelKey);
		if (paramIndex == 0) {
			tempIntercept.setValue(lm.getIntercept().toDouble());		// overrides decimals in model
			return tempIntercept;
		}
		else  {
			tempSlope.setValue(lm.getSlope().toDouble());		// overrides decimals in model
			return tempSlope;
		}
	}

	public int paintModel(Graphics g) {
		int deletedIndex = getSelection().findSingleSetFlag();
		if (deletedIndex >= 0) {
			LinearModel deletedLS = (LinearModel)getVariable(modelKey);
			if (deletedLS.setDeletedIndex(deletedIndex))
				deletedLS.updateLSParams(yKey);
			
			return super.paintModel(g);
		}
		else
			return kLeftRightBorder;
	}
	
}
