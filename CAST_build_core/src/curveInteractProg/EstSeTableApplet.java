package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import imageUtils.*;

import curveInteract.*;


public class EstSeTableApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final protected String MAX_TABLE_ENTRIES_PARAM = "maxTableEntries";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	
	static final private String kXStart = "x";
	static final private String kXNameEnd = "VarName";
	static final private String kXValuesEnd = "Values";
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	protected String xKeys[];
	
	protected String paramName[];
	protected NumValue maxParam[];
	private int paramDecimals[];
	
	protected ParamTestsView testTable;
	
	protected NumValue maxSsq, maxMsq, maxRootMsq;
	protected int maxDF;
	
	private XCheckbox showSeCheck;
	
	public void setupApplet() {
		data = readData();
		summaryData = createSummaryData(data);
		summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			mainPanel.add(tablePanel(data, summaryData));
			
		add("Center", mainPanel);
		add("East", rightPanel(data, summaryData));
		add("South", bottomPanel(summaryData));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
		
		int nx = 0;
		while (getParameter(kXStart + nx + kXNameEnd) != null)
			nx ++;
		xKeys = new String[nx];
		
		paramName = new String[nx + 1];
		paramName[0] = translate("intercept");
		StringTokenizer st = new StringTokenizer(getParameter(MAX_PARAM_PARAM));
		maxParam = new NumValue[nx + 1];
		paramDecimals = new int[nx + 1];
		maxParam[0] = new NumValue(st.nextToken());
		paramDecimals[0] = maxParam[0].decimals;
		
		for (int i=0 ; i<nx ; i++) {
			paramName[i+1] = getParameter(kXStart + i + kXNameEnd);
			String values = getParameter(kXStart + i + kXValuesEnd);
			xKeys[i] = kXStart + i;
			data.addNumVariable(xKeys[i], paramName[i+1], values);
			maxParam[i+1] = new NumValue(st.nextToken());
			paramDecimals[i+1] = maxParam[i+1].decimals;
		}
		
			MultipleRegnModel lsModel = new MultipleRegnModel("ls", data, xKeys);
			lsModel.setLSParams("y", paramDecimals, 9);
		data.addVariable("ls", lsModel);
		
		BasicComponentVariable resids = new BasicComponentVariable("Resid", data, xKeys, "y",
																								"ls", BasicComponentVariable.RESIDUAL, 9);
		data.addVariable("resid", resids);
		
		return data;
	}
	
	protected SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxRootMsq = new NumValue(st.nextToken());
		
		summaryData.addVariable("rss", new SsqVariable("Resid ssq", sourceData, "resid",
																												maxSsq.decimals, SsqVariable.SSQ));
		summaryData.addVariable("df", new SsqVariable("Resid df", sourceData, "resid",
																																		0, SsqVariable.DF));
		SsqVariable meanRssVar = new SsqVariable("Resid msq", sourceData, "resid",
																										maxMsq.decimals, SsqVariable.MEAN_SSQ);
		summaryData.addVariable("rmss", meanRssVar);
		
			PowerVariable errorSdEst = new PowerVariable("errorSdEst", meanRssVar, 0.5,
																																		maxRootMsq.decimals);
		summaryData.addVariable("errorSdEst", errorSdEst);
		
		return summaryData;
	}
	
	protected XPanel tablePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new InsetPanel(20, 7);
		thePanel.setLayout(new BorderLayout(0, 0));
			StringTokenizer st = new StringTokenizer(getParameter(MAX_TABLE_ENTRIES_PARAM));
			NumValue maxParam = new NumValue(st.nextToken());
			NumValue maxSE = new NumValue(st.nextToken());
			NumValue maxT = new NumValue(st.nextToken());
//			NumValue maxVIF = new NumValue(st.nextToken());
			testTable = new ParamTestsView(data, this, "ls", "y", paramName, null, maxParam, maxSE, maxT);
			testTable.setShowT(false);
			testTable.setShowPValue(false);
			testTable.setDrawSE(false);
		thePanel.add("Center", testTable);
		
		return thePanel;
	}
	
	protected XPanel rightPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 24));
		
		thePanel.add(ssqPanel(summaryData));
		
			showSeCheck = new XCheckbox(translate("Show std errors"), this);
		thePanel.add(showSeCheck);
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	protected XPanel ssqPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 7));
		
			OneValueImageView ssqView = new OneValueImageView(summaryData, "rss", this,
																									"xEquals/residualSsqBlack.png", 13, maxSsq);
			ssqView.setHighlightSelection(false);
		thePanel.add(ssqView);
		
			OneValueImageView dfView = new OneValueImageView(summaryData, "df", this,
																								"xEquals/dfBlack.png", 13, new NumValue(maxDF, 0));
			dfView.setHighlightSelection(false);
		thePanel.add(dfView);
		
			OneValueImageView sEstView = new OneValueImageView(summaryData, "errorSdEst", this,
																												"xEquals/sigmaHatFromRss.png", 26, maxRootMsq);
		thePanel.add(sEstView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showSeCheck) {
			testTable.setDrawSE(showSeCheck.getState());
			testTable.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}