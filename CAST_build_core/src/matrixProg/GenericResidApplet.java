package matrixProg;

import dataView.*;
import utils.*;

import matrix.*;


public class GenericResidApplet extends GenericModelEqnApplet {
	public void setupApplet() {
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		ModelTerm xTerms[] = findXTerms(data, "b");
		ModelTerm yTerm = findResponseTerm(data, "y");
		ModelTerm symbolResidTerm = findErrorTerm(data, "e");
		ModelTerm actualResidTerm = findResidTerm(data);
		
		add(createResidEquation(data, yTerm, xTerms, symbolResidTerm, actualResidTerm));
	}

//--------------------------------------------------------------------------------
	
	protected ModelTerm findResidTerm(DataSet data) {
		return null;
	}

//--------------------------------------------------------------------------------
	
	protected MatrixFormulaPanel createResidEquation(DataSet data, ModelTerm yTerm,
									ModelTerm[] xTerms, ModelTerm symbolResidTerm, ModelTerm actualResidTerm) {
		MatrixFormulaPanel xBeta = createLinearFunction(data, xTerms);
		
		MatrixFormulaValue yVal = new MatrixFormulaValue(createVectorView(data, yTerm), this);
		
		MatrixBinary residPart = new MatrixBinary(MatrixBinary.MINUS, yVal, xBeta, this);
		
		MatrixFormulaValue residVector = new MatrixFormulaValue(
																										createVectorView(data, symbolResidTerm), this);
		
		MatrixBinary leftEqn = new MatrixBinary(MatrixBinary.EQUALS, residVector, residPart, this);
		
		MatrixFormulaValue residEqnVector = new MatrixFormulaValue(
													createResidMatrixView(data, xTerms, yTerm, actualResidTerm, null), this);
		
		MatrixBinary eqn = new MatrixBinary(MatrixBinary.EQUALS, leftEqn, residEqnVector, this);
		
		return eqn;
	}
	
	protected CoreMatrixView createResidMatrixView(DataSet data, ModelTerm[] xTerms,
															ModelTerm yTerm, ModelTerm actualResidTerm, String heading) {
		CoreMatrixView matrix;
		if (actualResidTerm == null) {
			ResidualMatrixView localMatrix = new ResidualMatrixView(data, this, heading);
			localMatrix.setMatrixColumns(xTerms);
			localMatrix.setResponseColumn(yTerm);
			matrix = localMatrix;
		}
		else
			matrix = createVectorView(data, actualResidTerm);
		
		matrix.setAllowRowSelection(true);
//		matrix.setRetainLastSelection(true);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
}