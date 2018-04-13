package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import glmAnova.*;


public class AnovaTableReorderApplet extends XApplet {
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MAX_BAR_SSQ_PARAM = "maxBarSsq";
	static final private String X_GROUPS_PARAM = "xGroups";
	
	static final private Color kBarColors[] = {Color.blue, new Color(0x660066), new Color(0x006600), new Color(0x993300), new Color(0x6633CC)};
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	static final private String kXLabelsEnd = "Labels";
	
//	static final private int kMaxR2Decimals = 4;
	
//	static final private LabelValue kExplainedLabel = new LabelValue("Explained");
//	static final private LabelValue kUnexplainedLabel = new LabelValue("Unexplained");
//	static final private LabelValue kTotalLabel = new LabelValue("Total");
	
	protected DataSet data;
	
	private String shortVarName[];
	private String xKeys[];
	protected String lsKeys[];
	private int nxInComponent[] = null;
	private double type3Ssq[] = null;
	
	private String componentKeys[];
	private Color componentColor[];
	
	private String explainedComponentName[];
	
	private NumValue maxSsq, maxMsq, maxF;
	@SuppressWarnings("unused")
	private int maxDF;
	private boolean showTests;
	
	public void setupApplet() {
		readMaxSsq();
		
		data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
			AnovaReorderTableView table = createTable(data);
				
		add(table);
	}
	
	protected void readMaxSsq() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		showTests = st.hasMoreTokens();
		if (showTests) {
			maxMsq = new NumValue(st.nextToken());
			maxF = new NumValue(st.nextToken());
		}
	}
	
	protected AnovaReorderTableView createTable(DataSet data) {
		double maxType3Ssq = Double.parseDouble(getParameter(MAX_BAR_SSQ_PARAM));
		AnovaReorderTableView table = new AnovaReorderTableView(data, this, shortVarName,
									componentKeys, lsKeys, maxSsq, componentColor, explainedComponentName,
									maxType3Ssq, nxInComponent, type3Ssq);
		table.setFont(getBigFont());
		if (showTests)
			table.setShowTests(true, maxMsq, maxF);
		
		return table;
	}
	
	private void createLsAndFit(int nComponents, String[] xKeys, DataSet data) {
		lsKeys = new String[nComponents];
		for (int i=0 ; i<nComponents ; i++) {
			lsKeys[i] = "ls" + (i + 1);
			MultipleRegnModel lsModel = new MultipleRegnModel(lsKeys[i], data, xKeys);
			lsModel.setParameterDecimals(0);			//	to create NumValue array b[]
			data.addVariable(lsKeys[i], lsModel);
			
			String fitKey = "fit" + (i + 1);
			FittedValueVariable fitVar = new FittedValueVariable(fitKey, data, xKeys,
																																lsKeys[i], 9);
			data.addVariable(fitKey, fitVar);
		}
	}
	
	protected String[] getXKeys() {
		StringTokenizer st = new StringTokenizer(getParameter(X_VARIABLES_PARAM));
		int nx = st.countTokens();
		String xKeys[] = new String[nx];
		for (int i=0 ; i<nx ; i++)
			xKeys[i] = st.nextToken();
		return xKeys;
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
		yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
		
		xKeys = getXKeys();
		int nx = xKeys.length;
		String variableName[] = new String[nx];
		for (int i=0 ; i<nx ; i++) {
			variableName[i] = getParameter(xKeys[i] + kXNameEnd);
			String xLabels = getParameter(xKeys[i] + kXLabelsEnd);
			String xValues = getParameter(xKeys[i] + kXValuesEnd);
			if (xLabels == null)
				data.addNumVariable(xKeys[i], variableName[i], xValues);
			else
				data.addCatVariable(xKeys[i], variableName[i], xValues, xLabels);
		}
		
		String xGroupsString = getParameter(X_GROUPS_PARAM);
		int nExplainedComponents;
		if (xGroupsString == null) {
			nExplainedComponents = nx;
			explainedComponentName = variableName;
			shortVarName = xKeys;
		}
		else {
			StringTokenizer st1 = new StringTokenizer(xGroupsString, "#");
			nExplainedComponents = st1.countTokens();
			nxInComponent = new int[nExplainedComponents];
			explainedComponentName = new String[nExplainedComponents];
			shortVarName = new String[nExplainedComponents];
			for (int i=0 ; i<nExplainedComponents ; i++) {
				StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ":");
				nxInComponent[i] = Integer.parseInt(st2.nextToken());
				shortVarName[i] = st2.nextToken();
				explainedComponentName[i] = st2.nextToken();
			}
		}
		
		createLsAndFit(nExplainedComponents, xKeys, data);
		
		componentKeys = new String[nExplainedComponents + 2];
		componentColor = new Color[nExplainedComponents + 2];
		
		componentKeys[0] = "Total";
		componentColor[0] = Color.black;
		data.addVariable(componentKeys[0], new BasicComponentVariable(componentKeys[0], data,
									xKeys, "y", "ls" + nExplainedComponents, BasicComponentVariable.TOTAL, 9));
		
		componentKeys[nExplainedComponents + 1] = "Residual";
		componentColor[nExplainedComponents + 1] = Color.red;
		data.addVariable(componentKeys[nExplainedComponents + 1],
										new BasicComponentVariable(componentKeys[nExplainedComponents + 1],
										data, xKeys, "y", "ls" + nExplainedComponents,
										BasicComponentVariable.RESIDUAL, 9));
		
		componentKeys[1] = "Explained" + 1;
		componentColor[1] = kBarColors[0];
		data.addVariable(componentKeys[1], new BasicComponentVariable(componentKeys[1], data,
													xKeys, "y", "ls" + 1, BasicComponentVariable.EXPLAINED, 9));
		
		for (int i=1 ; i<nExplainedComponents ; i++) {
			componentKeys[i + 1] = "Explained" + (i + 1);
			componentColor[i + 1] = kBarColors[i % kBarColors.length];
			data.addVariable(componentKeys[i + 1], new SeqComponentVariable(componentKeys[i + 1],
														data, "fit" + (i + 1), "fit" + i, 9));
		}
		
		if (nxInComponent != null)
			type3Ssq = findType3Ssqs(data, "Residual", "ls" + nExplainedComponents, nx);
		
		return data;
	}
	
	protected double[] findType3Ssqs(DataSet data, String residKey, String lsKey, int nx) {
		MultipleRegnModel lsModel = (MultipleRegnModel)data.getVariable(lsKey);
		lsModel.updateLSParams("y");
		CoreComponentVariable residComp = (CoreComponentVariable)data.getVariable(residKey);
		double bestRss = residComp.getSsq();
		
		double localType3Ssq[] = new double[nx];
		
		double constraints[] = new double[nx + 1];
		int startIndex = 1;
		for (int i=0 ; i<nxInComponent.length ; i++) {
			for (int j=0 ; j<constraints.length ; j++)
				constraints[j] = Double.NaN;
			for (int j=0 ; j<nxInComponent[i] ; j++)
				constraints[startIndex + j] = 0.0;
			
			lsModel.updateLSParams("y", constraints);
			localType3Ssq[i] = residComp.getSsq() - bestRss;
			
			startIndex += nxInComponent[i];
		}
		return localType3Ssq;
	}
}