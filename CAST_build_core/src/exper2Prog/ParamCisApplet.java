package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import indicator.*;


public class ParamCisApplet extends XApplet {
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VARIABLES_PARAM = "xVariables";
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	static final private String kXLabelsEnd = "Labels";
	
	
	private String xKeys[];
	private NumValue maxParam;
	private DataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 30));
		
		add("Center", parametersPanel(data));
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
		
			maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
			MultipleRegnModel lsModel = new MultipleRegnModel("Least sqrs", data, xKeys);
			lsModel.setParameterDecimals(maxParam.decimals);			//	to create NumValue array b[]
			lsModel.updateLSParams("y");
		
		data.addVariable("ls", lsModel);
		
		return data;
	}
	
	private XPanel parametersPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 5));
		
			XLabel mainEffectLabel = new XLabel(translate("Parameter estimates (& 95% CIs)"), XLabel.CENTER, this);
			mainEffectLabel.setFont(getBigBoldFont());
		thePanel.add(mainEffectLabel);
		
			int nParamGroups = xKeys.length + 1;
			NumValue maxMainEffects[] = new NumValue[nParamGroups];
			for (int i=0 ; i<nParamGroups ; i++)
				maxMainEffects[i] = maxParam;
			int termsPerColumn[] = new int[nParamGroups / 2];
			for (int i=0 ; i<termsPerColumn.length ; i++)
				termsPerColumn[i] = 2;
			
			TermEstimatesView mainEffects = new TermEstimatesView(data, this, xKeys, "ls",
																					maxMainEffects, maxMainEffects, termsPerColumn);
		
		thePanel.add(mainEffects);
		
		return thePanel;
	}
}