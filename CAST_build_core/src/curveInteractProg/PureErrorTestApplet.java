package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import graphics3D.*;

import ssq.*;
import multivarProg.*;
import curveInteract.*;


public class PureErrorTestApplet extends RotateApplet {
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	static final private String COMP_NAME_PARAM = "compName";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String ANOVA_TYPE_PARAM = "anovaType";
	
	static final private String X_LIST_PARAM = "xList";
	static final private String Z_LIST_PARAM = "zList";
	
	static final protected String kXKeys[] = {"x", "z", "xx", "zz", "xz"};
	
	static final protected String kLSLinLinKeys[] = {"lsMean", "lsLin"};
	static final protected String kLSQuadInteractKeys[] = {"lsMean", "lsLin", "lsQuad", "lsInteract"};
	static final protected int kModelParams[] = {1, 3, 5, 6};
	
	static final private double kMeanConstraints[] = {Double.NaN, 0.0, 0.0, 0.0, 0.0, 0.0};
	static final private double kLinConstraints[] = {Double.NaN, Double.NaN, Double.NaN, 0.0, 0.0, 0.0};
	static final private double kQuadConstraints[] = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.0};
	
	static final protected Color kTableBackground = new Color(0xDDDDEE);
	
	protected DataSet data;
	private SummaryDataSet summaryData;
	
	private String compName[];
	private NumValue maxSsq, maxMsq, maxF;
	
	protected NumValue[] maxParam = new NumValue[6];
	protected int paramDecimals[] = new int[6];
	
//	private String yName;
//	private String paramName[] = new String[6];
	
	private String lsKeys[];
	
	protected DataSet readData() {
		data = new DataSet();
		
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
		
			NumVariable zVar = new NumVariable(getParameter(Z_VAR_NAME_PARAM));
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		
		data.addVariable("xx", new QuadraticVariable(xVar.name + "-sqr", xVar, 0.0, 0.0, 1.0, 9));
		data.addVariable("zz", new QuadraticVariable(zVar.name + "-sqr", zVar, 0.0, 0.0, 1.0, 9));
		
		data.addVariable("xz", new ProductVariable("interaction (x.z)", data, "x", "z"));
		
		data.addCatVariable("factor", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																									getParameter(CAT_LABELS_PARAM));
		
		if (getParameter(ANOVA_TYPE_PARAM).equals("linLin"))
			lsKeys = kLSLinLinKeys;
		else
			lsKeys = kLSQuadInteractKeys;
		
		addLeastSquares(data);
		
		int nComponents = lsKeys.length + 2;		//	one extra for factor model
		StringTokenizer st = new StringTokenizer(getParameter(COMP_NAME_PARAM), "#");
		compName = new String[nComponents];
		for (int i=0 ; i<nComponents ; i++)
			compName[i] = st.nextToken();
		
		addComponents(data, 9);
		summaryData = getSummaryData(data);
		
		return data;
	}
	
	private void addLeastSquares(DataSet data) {
			MultipleRegnModel lsInteract = new MultipleRegnModel("modelInteract", data, kXKeys);
			StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
			
			for (int i=0 ; i<6 ; i++) {
				maxParam[i] = new NumValue(st.nextToken());
				paramDecimals[i] = maxParam[i].decimals;
			}
			lsInteract.setLSParams("y", paramDecimals, 9);
		data.addVariable("lsInteract", lsInteract);
		
		MultipleRegnModel lsMean = new MultipleRegnModel("modelMean", data, kXKeys);
		lsMean.setLSParams("y", kMeanConstraints, paramDecimals, 9);
		data.addVariable("lsMean", lsMean);
		
		MultipleRegnModel lsLin = new MultipleRegnModel("modelLin", data, kXKeys);
		lsLin.setLSParams("y", kLinConstraints, paramDecimals, 9);
		data.addVariable("lsLin", lsLin);
		
		MultipleRegnModel lsQuad = new MultipleRegnModel("modelQuad", data, kXKeys);
		lsQuad.setLSParams("y", kQuadConstraints, paramDecimals, 9);
		data.addVariable("lsQuad", lsQuad);
		
		GroupsModelVariable factorModel = new GroupsModelVariable("modelFactor", data, "factor");
		factorModel.updateLSParams("y");
		data.addVariable("lsFactor", factorModel);
	}
	
	private void addComponents(DataSet data, int componentDecimals) {
		data.addVariable("total", new BasicComponentVariable(compName[0], data, "factor",
												"y", "lsFactor", BasicComponentVariable.TOTAL, componentDecimals));
		data.addVariable("resid", new BasicComponentVariable(compName[compName.length - 1],
														data, "factor", "y", "lsFactor", BasicComponentVariable.RESIDUAL,
														componentDecimals));
		for (int i=0 ; i<lsKeys.length-1 ; i++) {
			SsqDiffComponentVariable comp = new SsqDiffComponentVariable(compName[i+1], data,
																kXKeys, lsKeys[i], lsKeys[i+1], kModelParams[i+1] - kModelParams[i],
																componentDecimals);
			data.addVariable("comp" + i, comp);
		}
		
		String factorKey[] = new String[1];
		factorKey[0] = "factor";
		GroupsModelVariable factorModel = (GroupsModelVariable)data.getVariable("lsFactor");
		SsqDiffComponentVariable pureErrorComp = new SsqDiffComponentVariable(compName[lsKeys.length + 1],
														data, kXKeys, lsKeys[lsKeys.length - 1], factorKey, "lsFactor",
														factorModel.noOfParameters() - kModelParams[lsKeys.length - 1],
															componentDecimals);
		data.addVariable("comp" + (lsKeys.length - 1), pureErrorComp);
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		int nComponents = lsKeys.length + 2;
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
								SsqDiffComponentVariable.getComponentKeys(nComponents), maxSsq.decimals, 4);
		summaryData.setSingleSummaryFromData();
		
		return summaryData;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			D3Axis xAxis = new D3Axis(data.getVariable("x").name, D3Axis.X_AXIS, D3Axis.Z_AXIS, this);
			xAxis.setNumScale(getParameter(X_AXIS_INFO_PARAM));
			D3Axis yAxis = new D3Axis(data.getVariable("y").name, D3Axis.Y_AXIS, D3Axis.X_Z_AXIS, this);
			yAxis.setNumScale(getParameter(Y_AXIS_INFO_PARAM));
			D3Axis zAxis = new D3Axis(data.getVariable("z").name, D3Axis.Z_AXIS, D3Axis.X_AXIS, this);
			zAxis.setNumScale(getParameter(Z_AXIS_INFO_PARAM));
			
				double xValues[] = readValueList(X_LIST_PARAM);
				double zValues[] = readValueList(Z_LIST_PARAM);
			theView = new PureErrorView(data, this, xAxis, yAxis, zAxis, lsKeys[lsKeys.length - 1],
																											"x", "y", "z", "lsFactor", xValues, zValues);
			theView.lockBackground(Color.white);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	double[] readValueList(String param) {
		StringTokenizer st = new StringTokenizer(getParameter(param));
		double val[] = new double[st.countTokens()];
		for (int i=0 ; i<val.length ; i++)
			val[i] = Double.parseDouble(st.nextToken());
		return val;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
		rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 8, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(20, 7);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				int nComponents = lsKeys.length + 2;
				AnovaTableView anovaTable = new AnovaTableView(summaryData, this,
												SsqDiffComponentVariable.getComponentKeys(nComponents), maxSsq,
												maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
				anovaTable.setComponentNames(compName);
				anovaTable.setComponentColors(SsqDiffComponentVariable.getComponentColors(nComponents));
			innerPanel.add(anovaTable);
		
			innerPanel.lockBackground(kTableBackground);
		thePanel.add(innerPanel);
		
		return thePanel;
	}
}