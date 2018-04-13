package glmAnova;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;


public class SequentialDataSet extends DataSet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	static final private String COMPONENT_NAMES_PARAM = "componentNames";
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	static final private String kXLabelsEnd = "Labels";
	
	private String xKeys[];
	private String fitKeys[];
	private String componentKeys[];
	
	private String componentName[];
	private String variableName[];
	private Color componentColor[];
	
	public SequentialDataSet(XApplet applet) {
		NumVariable yVar = new NumVariable(applet.getParameter(Y_VAR_NAME_PARAM));
		yVar.readValues(applet.getParameter(Y_VALUES_PARAM));
		addVariable("y", yVar);
		
		StringTokenizer st = new StringTokenizer(applet.getParameter(X_VARIABLES_PARAM));
		int nx = st.countTokens();
		xKeys = new String[nx];
		variableName = new String[nx];
		int noOfParams = 1;
		int nParamsInX[] = new int[nx];
		for (int i=0 ; i<nx ; i++) {
			xKeys[i] = st.nextToken();
			variableName[i] = applet.getParameter(xKeys[i] + kXNameEnd);
			String labels = applet.getParameter(xKeys[i] + kXLabelsEnd);
			if (labels == null) {
				addNumVariable(xKeys[i], variableName[i], applet.getParameter(xKeys[i] + kXValuesEnd));
				nParamsInX[i] = 1;
				noOfParams ++;
			}
			else {
				CatVariable xVar = new CatVariable(variableName[i]);
				xVar.readLabels(labels);
				xVar.readValues(applet.getParameter(xKeys[i] + kXValuesEnd));
				addVariable(xKeys[i], xVar);
				nParamsInX[i] = xVar.noOfCategories() - 1;
				noOfParams += nParamsInX[i];
			}
		}
		
		fitKeys = new String[nx];
		String xSequence = "";
		int paramDecimals[] = new int[noOfParams];		//		all can be zero
		double constraints[] = new double[noOfParams];
		constraints[0] = Double.NaN;
		
		int startParam = 1;
		for (int i=0 ; i<nx ; i++) {
			xSequence += xKeys[i];
			for (int j=0 ; j<nParamsInX[i] ; j++)
				constraints[startParam + j] = Double.NaN;
			
			String lsKey = "ls" + xSequence;
			MultipleRegnModel lsModel = new MultipleRegnModel(lsKey, this, xKeys);
			lsModel.setLSParams("y", constraints, paramDecimals, 9);
			addVariable(lsKey, lsModel);
			
//			System.out.print("\nModel " + lsKey + ":");
//			for (int j=0 ; j<noOfParams ; j++)
//				System.out.print(lsModel.getParameter(j).toDouble() + " ");
//			System.out.print("\n");
			
			String fitKey = "fit" + xSequence;
			fitKeys[i] = fitKey;
			FittedValueVariable fitVar = new FittedValueVariable(fitKey, this, xKeys,
																																lsKey, 9);
			addVariable(fitKey, fitVar);
			startParam += nParamsInX[i];
		}
		
		componentKeys = new String[nx + 2];
		componentName = new String[nx + 2];
		componentColor = new Color[nx + 2];
		
		String componentNamesString = applet.getParameter(COMPONENT_NAMES_PARAM);
		st = (componentNamesString == null) ? null : new StringTokenizer(componentNamesString, "#");
		
		componentKeys[0] = "Total";
		componentName[0] = applet.translate("Total");
		componentColor[0] = Color.black;
		addVariable(componentKeys[0], new BasicComponentVariable(componentName[0], this,
														xKeys, "y", "ls" + xSequence, BasicComponentVariable.TOTAL, 9));
		
		componentKeys[nx + 1] = "Residual";
		componentName[nx + 1] = applet.translate("Residual");
		componentColor[nx + 1] = Color.red;
		addVariable(componentKeys[nx + 1], new BasicComponentVariable(componentName[nx + 1], this,
														xKeys, "y", "ls" + xSequence, BasicComponentVariable.RESIDUAL, 9));
		
		componentKeys[1] = "comp" + xKeys[0];
		componentName[1] = (st == null) ? xKeys[0] : st.nextToken();
		componentColor[1] = Color.blue;
		addVariable(componentKeys[1], new BasicComponentVariable(componentName[1], this,
														xKeys, "y", "ls" + xKeys[0], BasicComponentVariable.EXPLAINED, 9));
		
		String afterXSequence = "";
		for (int i=1 ; i<xKeys.length ; i++) {
			afterXSequence += xKeys[i - 1];
			componentKeys[i + 1] = "comp" + xKeys[i];
			componentName[i + 1] = (st == null) ? (xKeys[i] + "  after  " + afterXSequence)
																														: st.nextToken();
			componentColor[i + 1] = Color.blue;
			addVariable(componentKeys[i + 1], new SeqComponentVariable(componentName[i + 1], this,
																"fit" + afterXSequence + xKeys[i], "fit" + afterXSequence, 9));
		}
	}
	
	public String[] getXKeys() {
		return xKeys;
	}
	
	public String[] getFitKeys() {
		return fitKeys;
	}
	
	public String[] getComponentKeys() {
		return componentKeys;
	}
	
	public String[] getComponentNames() {
		return componentName;
	}
	
	public String[] getVariableNames() {
		return variableName;
	}
	
	public Color[] getComponentColors() {
		return componentColor;
	}

}