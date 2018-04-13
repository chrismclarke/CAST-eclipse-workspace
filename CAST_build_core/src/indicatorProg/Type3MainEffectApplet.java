package indicatorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;

import indicator.*;


public class Type3MainEffectApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	static final private String kXLabelsEnd = "Labels";
	
	static final private String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String MAX_PLUS_MINUS_PARAM = "maxPlusMinus";
	static final private String TERMS_PER_COL_PARAM = "termsPerColumn";
	
	private String xKeys[];
	private NumValue maxParam[];
	private NumValue maxPlusMinus[];
//	private int paramDecimals[];
	
	private DataSet data;
	
	private TermEstimatesView paramView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
			
		add("Center", coeffPanel(data));
		add("South", tablePanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		StringTokenizer stX = new StringTokenizer(getParameter(X_VARIABLES_PARAM));
		int nx = stX.countTokens();
		xKeys = new String[nx];
		
		StringTokenizer stMax = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
		maxParam = new NumValue[nx + 1];		//	first for intercept
		maxParam[0] = new NumValue(stMax.nextToken());
		
		String maxPlusMinString = getParameter(MAX_PLUS_MINUS_PARAM);
		StringTokenizer stMaxPlusMin = null;
		if (maxPlusMinString != null) {
			stMaxPlusMin = new StringTokenizer(maxPlusMinString);
			maxPlusMinus = new NumValue[nx + 1];		//	first for intercept
			maxPlusMinus[0] = new NumValue(stMaxPlusMin.nextToken());
		}
		
		int coeffInX[] = new int[nx];
		int totalCoeff = 1;			//	for intercept
		
		for (int i=0 ; i<nx ; i++) {
			xKeys[i] = stX.nextToken();
			String varName = getParameter(xKeys[i] + kXNameEnd);
			String values = getParameter(xKeys[i] + kXValuesEnd);
			String labels = getParameter(xKeys[i] + kXLabelsEnd);
			if (labels == null) {
				data.addNumVariable(xKeys[i], varName, values);
				coeffInX[i] = 1;
				totalCoeff ++;
			}
			else {
				CatVariable xVar = new CatVariable(varName);
				xVar.readLabels(labels);
				xVar.readValues(values);
				data.addVariable(xKeys[i], xVar);
				coeffInX[i] = xVar.noOfCategories() - 1;
				totalCoeff += coeffInX[i];
			}
			maxParam[i+1] = new NumValue(stMax.nextToken());
			if (stMaxPlusMin != null)
				maxPlusMinus[i+1] = new NumValue(stMaxPlusMin.nextToken());
		}
		
		
			int[] paramDecimals = new int[totalCoeff];
			paramDecimals[0] = maxParam[0].decimals;
			int paramIndex = 1;
			for (int i=0 ; i<nx ; i++)
				for (int j=0 ; j<coeffInX[i] ; j++)
					paramDecimals[paramIndex ++] = maxParam[i + 1].decimals;
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	protected XPanel coeffPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(TERMS_PER_COL_PARAM));
		int[] termsPerColumn = new int[st.countTokens()];
		for (int i=0 ; i<termsPerColumn.length ; i++)
			termsPerColumn[i] = Integer.parseInt(st.nextToken());
		
		paramView = new TermEstimatesView(data, this, xKeys, "ls", maxParam, maxPlusMinus, termsPerColumn);
		paramView.setFont(getBigFont());
		thePanel.add(paramView);
		
		return thePanel;
	}
	
	protected XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
			NumValue maxType3Ssq = new NumValue(st.nextToken());
			NumValue maxDf = new NumValue(st.nextToken());
			NumValue maxMeanSsq = new NumValue(st.nextToken());
			NumValue maxF = new NumValue(st.nextToken());
			Type3SsqTableView tableView = new Type3SsqTableView(data, this,
																"ls", "y", maxType3Ssq, maxDf, maxMeanSsq, maxF);
			tableView.setFont(getBigFont());
		thePanel.add("Center", tableView);
		
			paramView.setLinkedSsqTable(tableView);
		
		return thePanel;
	}
}