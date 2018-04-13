package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import models.*;
import utils.*;

import ssq.*;
import variance.*;


public class QuadDataFApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String LEFT_PROPN_PARAM = "leftProportion";
	
	static final protected NumValue kMaxRSquared = new NumValue(1.0, 3);
	
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
		
				QuadraticComponentsPanel scatterPanel = new QuadraticComponentsPanel(this);
				scatterPanel.setupPanel(data, "x", "y", QuadRegnDataSet.kLinLsKey,
																		QuadRegnDataSet.kQuadLsKey, null,
																		DataWithComponentView.NO_COMPONENT_DISPLAY, this);
			topPanel.add(ProportionLayout.LEFT, scatterPanel);
			topPanel.add(ProportionLayout.RIGHT, new XPanel());
		
		add("Center", topPanel);
		
		add("South", anovaPanel(summaryData));
	}
	
	private CoreModelDataSet getData() {
		QuadRegnDataSet data = new QuadRegnDataSet(this);
		
		QuadComponentVariable.addComponentsToData(data, "x", "y", QuadRegnDataSet.kLinLsKey,
																														QuadRegnDataSet.kQuadLsKey);
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		return new AnovaSummaryData(data, "y", QuadComponentVariable.kComponentKey,
																									maxSsq.decimals, kMaxRSquared.decimals);
	}
	
	private XPanel anovaPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			AnovaTableView table = new AnovaTableView(summaryData, this,
							QuadComponentVariable.kComponentKey, maxSsq, maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			int nNames = st.countTokens();
			String componentName[] = new String[nNames];
			for (int i=0 ; i<nNames ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(QuadComponentVariable.kComponentColor);
		thePanel.add("Center", table);
		
		return thePanel;
	}
}