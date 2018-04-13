package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import ssq.*;


public class PooledVarApplet extends XApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MAX_MSSQ_PARAM = "maxMssq";
	static final private String MAX_DF_PARAM = "maxDF";
	
	protected GroupsDataSet data;
	protected SummaryDataSet summaryData;
	
	protected NumValue maxSsq, maxMssq, maxDF;
	
	private DataWithComponentsPanel scatterPanel;
	
	private XButton sampleButton;
	private XChoice dataSetChoice;
	
	public void setupApplet() {
		data = getData();
		
			maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
			maxMssq = new NumValue(getParameter(MAX_MSSQ_PARAM));
			maxDF = new NumValue(getParameter(MAX_DF_PARAM));
			
		summaryData = getSummaryData(data);
		doTakeSample();
		
		setLayout(new BorderLayout(20, 5));
		
			scatterPanel = new DataWithGroupResidPanel(this);
			scatterPanel.setupPanel(data, "x", "y", "ls", "model", this);
		add("Center", scatterPanel);
			
		add("East", rightPanel(summaryData));
		
		add("South", bottomPanel(summaryData));
	}
	
	private GroupsDataSet getData() {
		GroupsDataSet	data = new GroupsDataSet(this);
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nCats = xVar.noOfCategories();
		
		for (int i=0 ; i<nCats ; i++) {
			String name = "groupResid" + i;
			data.addVariable(name, new GroupResidComponent(name, data, "x", "y",
																					"ls", i, data.getResponseDecimals()));
		}
		
		data.addVariable("residual", new BasicComponentVariable(translate("Residual"), data, "x", "y",
								"ls", BasicComponentVariable.RESIDUAL, data.getResponseDecimals()));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(GroupsDataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "error");
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nCats = xVar.noOfCategories();
		
		for (int i=0 ; i<nCats ; i++) {
			String componentKey = "groupResid" + i;
			String ssqKey = "ssq" + i;
			String dfKey = "df" + i;
			String mssqKey = "mssq" + i;
			
			summaryData.addVariable(ssqKey, new SsqVariable(ssqKey, data, componentKey,
																							maxSsq.decimals, SsqVariable.SSQ));
			summaryData.addVariable(dfKey, new SsqVariable(dfKey, data, componentKey,
																							maxDF.decimals, SsqVariable.DF));
			summaryData.addVariable(mssqKey, new SsqVariable(mssqKey, data, componentKey,
																							maxMssq.decimals, SsqVariable.MEAN_SSQ));
		}
		
		summaryData.addVariable("residSsq", new SsqVariable("residSsq", data, "residual",
																							maxSsq.decimals, SsqVariable.SSQ));
		summaryData.addVariable("residDf", new SsqVariable("residDf", data, "residual",
																							maxDF.decimals, SsqVariable.DF));
		summaryData.addVariable("residMssq", new SsqVariable("residMssq", data, "residual",
																							maxMssq.decimals, SsqVariable.MEAN_SSQ));
		return summaryData;
	}
	
	protected XPanel rightPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 10));
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nCats = xVar.noOfCategories();
		
		for (int i=0 ; i<nCats ; i++) {
			String ssqKey = "ssq" + i;
			String dfKey = "df" + i;
			String mssqKey = "mssq" + i;
			
			FormulaContext compContext = new FormulaContext(GroupResidComponent.kGroupColor[i], getStandardFont(), this);
			thePanel.add(new MeanSsqPanel(summaryData, ssqKey, dfKey, mssqKey,
									maxSsq, maxDF, maxMssq, i, compContext));
		}
		
		return thePanel;
	}
	
	private XPanel pooledPanel(SummaryDataSet summaryData) {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nCats = xVar.noOfCategories();
		
		String ssqKey[] = new String[nCats];
		String dfKey[] = new String[nCats];
		FormulaContext context[] = new FormulaContext[nCats];
		
		for (int i=0 ; i<nCats ; i++) {
			ssqKey[i] = "ssq" + i;
			dfKey[i] = "df" + i;
			context[i] = new FormulaContext(GroupResidComponent.kGroupColor[i], getStandardFont(), this);
		}
		FormulaContext stdContext = new FormulaContext(Color.black, getStandardFont(), this);
		return new MeanSsqPanel(summaryData, ssqKey, dfKey, "residSsq", "residDf", "residMssq",
																							maxSsq, maxDF, maxMssq, context, stdContext);
	}
	
	protected XPanel bottomPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
		thePanel.add(pooledPanel(summaryData));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
			
				dataSetChoice = data.dataSetChoice(this);
			if (dataSetChoice != null)
				buttonPanel.add(dataSetChoice);
			
				sampleButton = new XButton(translate("Another sample"), this);
			buttonPanel.add(sampleButton);
		
		thePanel.add(buttonPanel);
		
		return thePanel;
	}
	
	private void doTakeSample() {
		summaryData.takeSample();
		data.updateForNewSample();
		data.variableChanged("ls");
		summaryData.redoLastSummary();
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (data.changeDataSet(dataSetChoice.getSelectedIndex())) {
				data.variableChanged("y");
				summaryData.redoLastSummary();
			}
			return true;
		}
		else if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}