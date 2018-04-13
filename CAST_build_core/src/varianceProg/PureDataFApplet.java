package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;
import utils.*;

import ssq.*;
import variance.*;


public class PureDataFApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String LEFT_PROPN_PARAM = "leftProportion";
	
	protected CoreModelDataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMsq, maxF, maxRSquared;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		
		setLayout(new BorderLayout(20, 5));
			
			XPanel topPanel = new XPanel();
				double topLeftPropn = Double.parseDouble(getParameter(LEFT_PROPN_PARAM));
			topPanel.setLayout(new ProportionLayout(topLeftPropn, 0, ProportionLayout.HORIZONTAL,
																											ProportionLayout.TOTAL));
		
				PureComponentsPanel scatterPanel = new PureComponentsPanel(this);
				scatterPanel.setupPanel(data, "xNum", "x", "y", PureRegnDataSet.kLinLsKey,
																		PureRegnDataSet.kFactorLsKey, null,
																		DataWithComponentView.NO_COMPONENT_DISPLAY, this);
			topPanel.add(ProportionLayout.LEFT, scatterPanel);
			topPanel.add(ProportionLayout.RIGHT, new XPanel());
		
		add("Center", topPanel);
		
		add("South", anovaPanel(summaryData));
	}
	
	private CoreModelDataSet getData() {
		PureRegnDataSet data = new PureRegnDataSet(this);
		
		PureComponentVariable.addComponentsToData(data, "xNum", "y", "x", PureRegnDataSet.kLinLsKey,
																														PureRegnDataSet.kFactorLsKey);
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		return new AnovaSummaryData(data, "y", PureComponentVariable.kComponentKey,
																									maxSsq.decimals, maxRSquared.decimals);
	}
	
	private XPanel anovaPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
															PureComponentVariable.kComponentKey, maxSsq, maxMsq, maxF,
															AnovaTableView.SSQ_F_PVALUE);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			int nNames = st.countTokens();
			String componentName[] = new String[nNames];
			for (int i=0 ; i<nNames ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(PureComponentVariable.kComponentColor);
		thePanel.add("Center", table);
		
		return thePanel;
	}
}