package matrixProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import coreVariables.*;
import models.*;

import matrix.*;


public class GenericLinearPartApplet extends XApplet {
	static final protected String Y_NAME_PARAM = "yName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final protected String NX_COLUMNS_PARAM = "nXColumns";
	static final private String X_ROWS_PARAM = "xRows";
	static final protected String X_FULL_NAME_PARAM = "fullNameX";
	static final private String X_SHORT_NAME_PARAM = "shortNameX";
	static final private String X_POWER_PARAM = "powerX";
	static final private String X_VALUES_PARAM = "valuesX";
	static final private String X_LABELS_PARAM = "labelsX";
	static final private String FONT_SIZE_PARAM = "matrixFont";
	
	static final private String LS_DECIMALS_PARAM = "lsDecimals";
	
	static final private String ONE_FACTOR_PARAM = "oneFactor";		//	parameterisation for 1-factor model
	static final public String SEPARATE_MEANS = "separate means";
	static final public String BASELINE = "baseline";
	static final public String TWO_GROUP = "twoGroup";
	static final public String LINEAR = "linear";
	
	static final protected Color kConstColor = new Color(0x0066FF);
	static final private Color kX1Color = new Color(0xFF6600);		//	orange
	static final private Color kX2Color = new Color(0x009900);		//	dark green
	static final private Color kX3Color = new Color(0x993399);		//	purple
	static final private Color kX4Color = new Color(0x990000);		//	dark red
	
	static final protected Color kXColors[] = {kX1Color, kX2Color, kX3Color, kX4Color};
	
	protected DataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		ModelTerm xTerms[] = findXTerms(data, ModelTerm.BETA);
		
		add(createLinearEquation(data, xTerms));
	}
	
	protected Font getMatrixFont() {
		String fontString = getParameter(FONT_SIZE_PARAM);
		return (fontString == null || fontString.equals("big")) ? getBigFont() : getStandardFont();
	}
	
	protected int getRowGap() {
		return CoreMatrixView.kRowGap;		//	default gap
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		String yValuesString = getParameter(Y_VALUES_PARAM);
		if (yValuesString != null) {
				String fullName = getParameter(Y_NAME_PARAM);
				NumVariable yVar = new NumVariable(fullName);
				yVar.readValues(yValuesString);
				data.addVariable("y", yVar);
		}
		
		int nX = Integer.parseInt(getParameter(NX_COLUMNS_PARAM));
		boolean foundValues = false;
		
		for (int i=0 ; i<nX ; i++) {
			String valuesString = getParameter(X_VALUES_PARAM + (i+1));
			if (valuesString != null) {
				String fullName = getParameter(X_FULL_NAME_PARAM + (i+1));
				String labelsString = getParameter(X_LABELS_PARAM + (i+1));
				String powerString = getParameter(X_POWER_PARAM + (i+1));
				Variable xVar;
				if (labelsString != null) {
					xVar = new CatVariable(fullName);
					((CatVariable)xVar).readLabels(labelsString);
					xVar.readValues(valuesString);
				}
				else if (powerString == null) {
					xVar = new NumVariable(fullName);
					xVar.readValues(valuesString);
				}
				else {
					StringTokenizer st = new StringTokenizer(powerString);
					String baseKey = st.nextToken();
					int power = Integer.parseInt(st.nextToken());
					
					NumVariable baseVar = (NumVariable)data.getVariable(baseKey);
					xVar = new PowerVariable(fullName, baseVar, power, 9);
				}
				data.addVariable("x" + (i+1), xVar);
				
				foundValues = true;
			}
		}
		
		if (!foundValues) {
			int noOfXRows = Integer.parseInt(getParameter(X_ROWS_PARAM));
			
			IndexVariable dummyVar = new IndexVariable("dummy", noOfXRows);
			data.addVariable("dummy", dummyVar);
		}
		
		return data;
	}
	
	protected String[] getXKeys() {
		int nX = Integer.parseInt(getParameter(NX_COLUMNS_PARAM));
		String keys[] = new String[nX];
		for (int i=0 ; i<nX ; i++)
			keys[i] = "x" + (i+1);
		return keys;
	}
	
	protected void addLsModel(DataSet data) {
		String keys[] = getXKeys();
		int nCoeff = 1;
		for (int i=0 ; i<keys.length ; i++) {
			CoreVariable xVar = data.getVariable(keys[i]);
			if (xVar instanceof CatVariable)
				nCoeff += ((CatVariable)xVar).noOfCategories() - 1;
			else
				nCoeff ++;
		}
			
		NumValue b[] = new NumValue[nCoeff];
		
		String lsDecimalString = getParameter(LS_DECIMALS_PARAM);
		StringTokenizer st = new StringTokenizer(lsDecimalString);
		for (int i=0 ; i<nCoeff ; i++)
			b[i] = new NumValue(0.0, Integer.parseInt(st.nextToken()));
		
		NumValue s = new NumValue(0.0, 9);
		
		MultipleRegnModel ls = new MultipleRegnModel("ls", data, keys, b, s);
		ls.updateLSParams("y");
		
		data.addVariable("ls", ls);
	}

//--------------------------------------------------------------------------------
	
	protected ModelTerm[] findXTerms(DataSet data, String paramSymbol) {
		int nX = Integer.parseInt(getParameter(NX_COLUMNS_PARAM));
		
		ModelTerm[] xTerms = new ModelTerm[nX + 1];
		xTerms[0] = new ConstantTerm(paramSymbol, 0, kConstColor);
		int startSubscript = 1;
		
		for (int i=0 ; i<nX ; i++) {
			String fullName = getParameter(X_FULL_NAME_PARAM + (i+1));
			String shortName = getParameter(X_SHORT_NAME_PARAM + (i+1));
			
			String powerString = getParameter(X_POWER_PARAM + (i+1));
			String baseXKey;
			int power;
			if (powerString == null) {
				baseXKey = "x" + (i + 1);
				power = 1;
			}
			else {
				StringTokenizer st = new StringTokenizer(powerString);
				baseXKey = st.nextToken();
				power = Integer.parseInt(st.nextToken());
			}
			
			CoreVariable xVar = data.getVariable(baseXKey);
			if (xVar == null) {
				xTerms[i + 1] = new SymbolTerm(shortName, 99, fullName, paramSymbol, startSubscript,
																																							kXColors[i]);
				startSubscript ++;
			}
			else if (xVar instanceof CatVariable) {
				xTerms[i + 1] = new FactorTerm(data, baseXKey, 0, fullName != null,
																					paramSymbol, startSubscript - 1, kXColors[i], this);
				startSubscript += ((CatVariable)xVar).noOfCategories() - 1;
			}
			else {
				xTerms[i + 1] = new VariateTerm(data, baseXKey, fullName != null,
																				paramSymbol, startSubscript, shortName, kXColors[i]);
				startSubscript ++;
			}
			if (power > 1)
				xTerms[i + 1].setPower(power);
		}
		return xTerms;
	}
	
	protected ModelTerm[] findOneFactorXTerms(DataSet data, String paramSymbol) {
													//		needed here because it is used by several Factor-type
													//		sub-classes via different inheritances
		String fullName = getParameter(X_FULL_NAME_PARAM + 1);
		
		String oneFactorString = getParameter(ONE_FACTOR_PARAM);
		if (oneFactorString.equals(SEPARATE_MEANS)) {
			ModelTerm[] xTerms = new ModelTerm[1];
			xTerms[0] = new FactorTerm(data, "x1", -1, fullName != null,
																											ModelTerm.MU, 1, kConstColor, this);
			return xTerms;
		}
		else {
			StringTokenizer st = new StringTokenizer(oneFactorString);
			String command = st.nextToken();
			if (command.equals(BASELINE)) {
				int baselineCat = Integer.parseInt(st.nextToken());
				ModelTerm[] xTerms = new ModelTerm[2];
				xTerms[0] = new ConstantTerm(ModelTerm.MU, baselineCat + 1, kConstColor);
				xTerms[1] = new FactorTerm(data, "x1", baselineCat, fullName != null,
																												ModelTerm.DELTA, 1, kXColors[0], this);
				return xTerms;
			}
			else if (command.equals(TWO_GROUP)) {
				ModelTerm[] xTerms = new ModelTerm[3];
				xTerms[0] = new ConstantTerm(ModelTerm.MU, 1, kConstColor);
				
				int noOfCats = ((CatVariable)data.getVariable("x1")).noOfCategories();
				int contrastValue[] = new int[noOfCats];
				int group1Count = Integer.parseInt(st.nextToken());
				for (int i=0 ; i<noOfCats ; i++)
					contrastValue[i] = (i < group1Count) ? 0 : 1;
				xTerms[1] = new ContrastTerm(data, "x1", contrastValue, translate("Contrast"),
																										ModelTerm.BETA, group1Count + 1, kXColors[0]);
				
				boolean allowedTerm[] = new boolean[noOfCats];
				for (int i=0 ; i<noOfCats ; i++)
					allowedTerm[i] = (i != 0) && (i != group1Count);
				xTerms[2] = new FactorTerm(data, "x1", allowedTerm, fullName != null,
																												ModelTerm.DELTA, 1, kXColors[1], this);
				return xTerms;
			}
			else if (command.equals(LINEAR)) {
				ModelTerm[] xTerms = new ModelTerm[3];
				xTerms[0] = new ConstantTerm(ModelTerm.ALPHA, -1, kConstColor);
				
				int noOfCats = ((CatVariable)data.getVariable("x1")).noOfCategories();
				int contrastValue[] = new int[noOfCats];
				for (int i=0 ; i<noOfCats ; i++)
					contrastValue[i] = i;
				xTerms[1] = new ContrastTerm(data, "x1", contrastValue, translate("Linear"),
																										ModelTerm.BETA, -1, kXColors[0]);
				
				boolean allowedTerm[] = new boolean[noOfCats];
				for (int i=0 ; i<noOfCats ; i++)
					allowedTerm[i] = (i >= 2);
				xTerms[2] = new FactorTerm(data, "x1", allowedTerm, fullName != null,
																												ModelTerm.DELTA, 1, kXColors[1], this);
				return xTerms;
			}
			else
				return null;
		}
	}

//--------------------------------------------------------------------------------
	
	protected MatrixFormulaPanel createLinearFunction(DataSet data, ModelTerm[] xTerms) {
		MatrixFormulaValue x = new MatrixFormulaValue(createXMatrixView(data, xTerms), this);
		
		MatrixFormulaValue beta = new MatrixFormulaValue(createBetaMatrixView(data, xTerms), this);
		
		MatrixBinary xBeta = new MatrixBinary(MatrixBinary.TIMES, x, beta, this);
		
		return xBeta;
	}
	
	protected MatrixFormulaPanel createLinearEquation(DataSet data, ModelTerm[] xTerms) {
		MatrixFormulaPanel xBeta = createLinearFunction(data, xTerms);
		
		MatrixFormulaValue linearVector = new MatrixFormulaValue(createLinearMatrixView(data,
																								xTerms, translate("Mean")), this);
		
		MatrixBinary eqn = new MatrixBinary(MatrixBinary.EQUALS, xBeta, linearVector, this);
		
		return eqn;
	}

//--------------------------------------------------------------------------------
	
	protected CoreMatrixView createXMatrixView(DataSet data, ModelTerm[] xTerms) {
		DataMatrixView matrix = new DataMatrixView(data, this);
		matrix.setMatrixColumns(xTerms);
		
		matrix.setAllowRowSelection(true);
//		matrix.setRetainLastSelection(true);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
	
	protected CoreMatrixView createBetaMatrixView(DataSet data, ModelTerm[] xTerms) {
		CoreVariable lsVar = data.getVariable("ls");
		CoreMatrixView  matrix;
		
		if (lsVar == null)
			matrix = new CoeffMatrixView(data, this);
		else
			matrix = new LsCoeffMatrixView(data, this, "ls");
		
		matrix.setMatrixColumns(xTerms);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
	
	protected CoreMatrixView createLinearMatrixView(DataSet data, ModelTerm[] xTerms,
																												String heading) {
		LinearPartMatrixView matrix = new LinearPartMatrixView(data, this, heading);
		matrix.setMatrixColumns(xTerms);
		
		matrix.setAllowRowSelection(true);
//		matrix.setRetainLastSelection(true);
		matrix.setFont(getMatrixFont());
		matrix.setRowGap(getRowGap());
		
		return matrix;
	}
}