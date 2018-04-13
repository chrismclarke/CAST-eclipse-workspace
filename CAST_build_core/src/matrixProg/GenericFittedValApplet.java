package matrixProg;

import java.awt.*;

import dataView.*;
import utils.*;

import matrix.*;


public class GenericFittedValApplet extends GenericModelEqnApplet {
	static final private String FIT_NAME_PARAM = "fitName";

	public void setupApplet() {
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		ModelTerm xTerms[] = findXTerms(data, "b");
		ModelTerm leftFitTerm = findGenericFittedTerm(data);
		
		add(createFittedValEquation(data, leftFitTerm, xTerms, null));
	}

//--------------------------------------------------------------------------------
	
	protected ModelTerm findGenericFittedTerm(DataSet data) {
		String fitName = getParameter(FIT_NAME_PARAM);
		return new SymbolTerm("y" + ModelTerm.HAT, 99, fitName, null, 0, Color.black);
	}

//--------------------------------------------------------------------------------
	
	protected MatrixFormulaValue createLeftFitView(DataSet data, ModelTerm leftFitTerm) {
		return new MatrixFormulaValue(createVectorView(data, leftFitTerm), this);
	}
	
	private MatrixFormulaValue createRightFitView(DataSet data, ModelTerm[] xTerms,
																															ModelTerm rightFitTerm) {
		if (rightFitTerm ==  null)
			return new MatrixFormulaValue(createLinearMatrixView(data, xTerms, null), this);
		else
			return new MatrixFormulaValue(createVectorView(data, rightFitTerm), this);
	}
	
	protected MatrixFormulaPanel createFittedValEquation(DataSet data, ModelTerm leftFitTerm,
																							ModelTerm[] xTerms, ModelTerm rightFitTerm) {
		MatrixFormulaPanel xBeta = createLinearFunction(data, xTerms);
		
		MatrixFormulaValue fitVal = createLeftFitView(data, leftFitTerm);
		
		MatrixBinary leftPart = new MatrixBinary(MatrixBinary.EQUALS, fitVal, xBeta, this);
		
		MatrixFormulaValue rightFitVector = createRightFitView(data, xTerms, rightFitTerm);
		
		MatrixBinary model = new MatrixBinary(MatrixBinary.EQUALS, leftPart, rightFitVector, this);
		
		return model;
	}
}