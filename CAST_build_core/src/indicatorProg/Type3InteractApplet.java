package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;
import coreVariables.*;

import indicator.*;


public class Type3InteractApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	static final private String kXLabelsEnd = "Labels";
	
	static final private String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	
	private String xKeys[];
	
	private String termName[];
	private int nxPerTerm[];
	
	private int[][] hierarchy;
	
	private DataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
			
		add("Center", tablePanel(data));
	}
	
	private int countInteractionVars(Variable[] xVar, int[] coeffInX) {
		int nVars = 0;
		for (int i=0 ; i<coeffInX.length-1 ; i++)
			for (int j=i+1 ; j<coeffInX.length ; j++)
				if ((xVar[i] instanceof NumVariable) == (xVar[j] instanceof NumVariable))
																														//	both num or both cat
					nVars ++;
				else
					nVars += Math.max(coeffInX[i], coeffInX[j]);	//	num * cat interaction
		return nVars;
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		StringTokenizer stX = new StringTokenizer(getParameter(X_VARIABLES_PARAM));
		int nx = stX.countTokens();
		String mainEffectKeys[] = new String[nx];
		Variable xVar[] = new Variable[nx];
		
		int mainCoeffInX[] = new int[nx];
		int totalCoeff = 1;			//	for intercept
		
		termName = new String[nx * (nx + 1) / 2];
		nxPerTerm = new int[nx * (nx + 1) / 2];
		
		for (int i=0 ; i<nx ; i++) {
			mainEffectKeys[i] = stX.nextToken();
			String varName = getParameter(mainEffectKeys[i] + kXNameEnd);
			String values = getParameter(mainEffectKeys[i] + kXValuesEnd);
			String labels = getParameter(mainEffectKeys[i] + kXLabelsEnd);
			if (labels == null) {
				xVar[i] = new NumVariable(varName);
				xVar[i].readValues(values);
				mainCoeffInX[i] = 1;
				totalCoeff ++;
			}
			else {
				CatVariable catVar = new CatVariable(varName);
				xVar[i] = catVar;
				catVar.readLabels(labels);
				catVar.readValues(values);
				mainCoeffInX[i] = catVar.noOfCategories() - 1;
				totalCoeff += mainCoeffInX[i];
			}
			data.addVariable(mainEffectKeys[i], xVar[i]);
			termName[i] = varName;
			nxPerTerm[i] = 1;
		}
		
		int nInteractVars = countInteractionVars(xVar, mainCoeffInX);
		xKeys = new String[nx + nInteractVars];
		System.arraycopy(mainEffectKeys, 0, xKeys, 0, mainEffectKeys.length);
		
		int coeffInX[] = new int[nx + nInteractVars];
		System.arraycopy(mainCoeffInX, 0, coeffInX, 0, mainEffectKeys.length);
		
		int xIndex = nx;
		int termIndex = nx;
		hierarchy = new int[nx * (nx + 1) / 2][];
		
		for (int i=0 ; i<nx-1 ; i++) {
			Variable x1Var = xVar[i];
			for (int j=i+1 ; j<nx ; j++) {
				Variable x2Var = xVar[j];
				termName[termIndex] = x1Var.name + "*" + x2Var.name;
				if (x1Var instanceof NumVariable && x2Var instanceof NumVariable) {
					String interactKey = mainEffectKeys[i] + mainEffectKeys[j];
					data.addVariable(interactKey, new ProductVariable(termName[termIndex],
																											data, mainEffectKeys[i], mainEffectKeys[j]));
					xKeys[xIndex] = interactKey;
					coeffInX[xIndex] = 1;
					xIndex ++;
					totalCoeff ++;
					nxPerTerm[termIndex] = 1;
				}
				else if (x1Var instanceof CatVariable && x2Var instanceof CatVariable) {
					String interactKey = mainEffectKeys[i] + mainEffectKeys[j];
					data.addVariable(interactKey, new CatCatInteractionVariable(termName[termIndex],
																											data, mainEffectKeys[i], mainEffectKeys[j]));
					xKeys[xIndex] = interactKey;
					coeffInX[xIndex] = mainCoeffInX[i] * mainCoeffInX[j];
					totalCoeff += coeffInX[xIndex];
					xIndex ++;
					nxPerTerm[termIndex] = 1;
				}
				else if (x1Var instanceof CatVariable) {			//		and x2Var is NumVariable
					CatVariable catVar = (CatVariable)x1Var;
					for (int k=1 ; k<catVar.noOfCategories() ; k++) {
						String interactKey = mainEffectKeys[i] + mainEffectKeys[j] + k;
						data.addVariable(interactKey, new CatNumInteractTermVariable(termName[termIndex],
																											data, mainEffectKeys[i], mainEffectKeys[j], k));
						xKeys[xIndex] = interactKey;
						coeffInX[xIndex] = 1;
						xIndex ++;
						totalCoeff ++;
					}
					nxPerTerm[termIndex] = catVar.noOfCategories() - 1;
				}
				else {			//		and x2Var is CatVariable and x1Var is NumVariable
					CatVariable catVar = (CatVariable)x2Var;
					for (int k=1 ; k<catVar.noOfCategories() ; k++) {
						String interactKey = mainEffectKeys[i] + mainEffectKeys[j] + k;
						data.addVariable(interactKey, new CatNumInteractTermVariable(termName[termIndex],
																											data, mainEffectKeys[j], mainEffectKeys[i], k));
						xKeys[xIndex] = interactKey;
						coeffInX[xIndex] = 1;
						xIndex ++;
						totalCoeff ++;
					}
					nxPerTerm[termIndex] = catVar.noOfCategories() - 1;
				}
				hierarchy[termIndex] = new int[2];
				hierarchy[termIndex][0] = i;
				hierarchy[termIndex][1] = j;
				termIndex ++;
			}
		}
		
		int paramDecimals[] = new int[totalCoeff];		//	all zero
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
			NumValue maxType3Ssq = new NumValue(st.nextToken());
			NumValue maxDf = new NumValue(st.nextToken());
			NumValue maxMeanSsq = new NumValue(st.nextToken());
			NumValue maxF = new NumValue(st.nextToken());
			Type3SsqTableView tableView = new Type3SsqTableView(data, this, "ls", "y", maxType3Ssq,
																				maxDf, maxMeanSsq, maxF, termName, nxPerTerm, hierarchy);
			tableView.setFont(getBigFont());
		thePanel.add("Center", tableView);
		
		return thePanel;
	}
}