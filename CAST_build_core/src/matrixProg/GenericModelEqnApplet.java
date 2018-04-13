package matrixProg;

import java.awt.*;

import dataView.*;
import utils.*;

import matrix.*;


public class GenericModelEqnApplet extends GenericLinearPartApplet {
	static final private String ERROR_NAME_PARAM = "errorName";
	static final private String GROUP_KEY_INDEX_PARAM = "groupKeyIndex";		//	1 for 1st x-variable, etc.

/*	
	public void init() {
		if (initialisationFailed()) return;
		
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new XPanel();		//	We need nested panels because when its components are too tall,
																				//	VerticalLayout can only shrink a single component to the available ht
			innerPanel.setLayout(new BorderLayout(0, 10));
		
				ModelTerm xTerms[] = findXTerms(data, ModelTerm.BETA);
				ModelTerm yTerm = findResponseTerm(data, "Y");
				ModelTerm errorTerm = findErrorTerm(data, ModelTerm.EPSILON);
			
			innerPanel.add("Center", createModelEquation(data, yTerm, xTerms, errorTerm));
			
				XPanel oneModelPanel = new XPanel();
				oneModelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				oneModelPanel.add(createSelectedModelView(data, yTerm, xTerms, errorTerm));
			
			innerPanel.add("South", oneModelPanel);
		
		add(innerPanel);
		
		addBottomPanel();
		
		repaint();
	}
*/	
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
				
				ModelTerm xTerms[] = findXTerms(data, ModelTerm.BETA);
				ModelTerm yTerm = findResponseTerm(data, "Y");
				ModelTerm errorTerm = findErrorTerm(data, ModelTerm.EPSILON);
			
		add("Center", createModelEquation(data, yTerm, xTerms, errorTerm));
			
				XPanel lowerPanel = new XPanel();
				lowerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
				lowerPanel.add(createSelectedModelView(data, yTerm, xTerms, errorTerm));
			
				addBottomPanel(lowerPanel);
		
		add("South", lowerPanel);
	}
	
	protected void addBottomPanel(XPanel lowerPanel) {
	}

//--------------------------------------------------------------------------------
	
	protected ModelTerm findResponseTerm(DataSet data, String symbol) {
		String responseName = getParameter(Y_NAME_PARAM);
		NumVariable yVar = (NumVariable)data.getVariable("y");
		if (yVar == null)
			return new SymbolTerm(symbol, 99, responseName, null, 0, Color.black);
		else
			return new VariateTerm(data, "y", responseName != null,
																										null, 0, symbol, Color.black);
	}
	
	protected ModelTerm findErrorTerm(DataSet data, String symbol) {
		String errorName = getParameter(ERROR_NAME_PARAM);
		return new SymbolTerm(symbol, 99, errorName, null, 0, Color.black);
	}

//--------------------------------------------------------------------------------
	
	protected MatrixFormulaValue createResponseView(DataSet data, ModelTerm yTerm) {
		MatrixFormulaValue yVector = new MatrixFormulaValue(createVectorView(data, yTerm), this);
		
		String groupKeyIndex = getParameter(GROUP_KEY_INDEX_PARAM);
		if (groupKeyIndex != null)
			yVector.setGroupKey("x" + groupKeyIndex);
		
		return yVector;
	}
	
	private MatrixFormulaPanel createModelEquation(DataSet data, ModelTerm yTerm,
																		ModelTerm[] xTerms, ModelTerm errorTerm) {
		MatrixFormulaPanel xBeta = createLinearFunction(data, xTerms);
		
		MatrixFormulaValue error = new MatrixFormulaValue(createVectorView(data, errorTerm), this);
		
		MatrixBinary linearPart = new MatrixBinary(MatrixBinary.PLUS, xBeta, error, this);
		
		MatrixFormulaValue y = createResponseView(data, yTerm);
		
		MatrixBinary model = new MatrixBinary(MatrixBinary.EQUALS, y, linearPart, this);
		
		return model;
	}

//--------------------------------------------------------------------------------
	
	protected CoreMatrixView createVectorView(DataSet data, ModelTerm yTerm) {
		DataMatrixView matrix = new DataMatrixView(data, this);
		
		matrix.addMatrixColumn(yTerm);
		matrix.setAllowRowSelection(true);
//		matrix.setRetainLastSelection(true);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
	
	private ModelValueView createSelectedModelView(DataSet data, ModelTerm yTerm,
																		ModelTerm[] xTerms, ModelTerm errorTerm) {
		ModelValueView view = new ModelValueView(data, this, yTerm, xTerms, errorTerm, true);
		view.setFont(getMatrixFont());
		
		return view;
	}
}