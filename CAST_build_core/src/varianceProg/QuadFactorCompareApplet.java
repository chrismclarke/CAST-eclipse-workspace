package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import ssq.*;
import variance.*;


public class QuadFactorCompareApplet extends XApplet {
	static final private String MAX_SUMMARIES_PARAM = "maxSummaries";
	static final private String FACTOR_COMPONENT_PARAM = "factorName";
	static final private String QUAD_COMPONENT_PARAM = "quadName";
	
	static final private Color kQuadAnovaColor = new Color(0x660000);
	static final private Color kPureAnovaColor = new Color(0x000099);
	
	protected CoreModelDataSet data;
	protected AnovaSummaryData summaryData;
	protected NumValue maxSsq, maxMsq, maxF, maxRSquared;
	
	private XButton sampleButton;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(30, 0));
		
		add("Center", scatterPanel(data));
		add("East", controlPanel());
		
			XPanel tablePanel = new XPanel();
			tablePanel.setLayout(new BorderLayout(0, 10));
			tablePanel.add("North", anovaTablePanel(summaryData, translate("Quadratic"), getParameter(QUAD_COMPONENT_PARAM),
									QuadComponentVariable.kComponentKey, QuadComponentVariable.kComponentColor,
									kQuadAnovaColor));
			tablePanel.add("Center", anovaTablePanel(summaryData, translate("Factor"), getParameter(FACTOR_COMPONENT_PARAM),
									PureComponentVariable.kComponentKey, PureComponentVariable.kComponentColor,
									kPureAnovaColor));
		add("South", tablePanel);
	}
	
	private CoreModelDataSet getData() {
		PureRegnDataSet data = new PureRegnDataSet(this);
		
		PureComponentVariable.addComponentsToData(data, "xNum", "y", "x", PureRegnDataSet.kLinLsKey,
																														PureRegnDataSet.kFactorLsKey);
		
		QuadComponentVariable.addComponentsToData(data, "xNum", "y", PureRegnDataSet.kLinLsKey,
																																	PureRegnDataSet.kQuadLsKey);
		
		return data;
	}
	
	protected AnovaSummaryData getSummaryData(CoreModelDataSet sourceData) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SUMMARIES_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
										QuadComponentVariable.kComponentKey, maxSsq.decimals, maxRSquared.decimals);
		
		summaryData.addSsqs(sourceData, PureComponentVariable.kComponentKey, "rSquared_F",
																									maxSsq.decimals, maxRSquared.decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	private XPanel anovaTablePanel(SummaryDataSet summaryData, String analysisName,
								String componentNameString, String[] componentKeys, Color[] componentColors,
								Color tableLabelColor) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel text = new XLabel(analysisName, XLabel.LEFT, this);
				text.setFont(getStandardBoldFont());
				text.setForeground(tableLabelColor);
			labelPanel.add(text);
				text = new XLabel(translate("Model"), XLabel.LEFT, this);
				text.setFont(getStandardBoldFont());
				text.setForeground(tableLabelColor);
			labelPanel.add(text);
			
		thePanel.add("West", labelPanel);
		
			AnovaTableView table = new AnovaTableView(summaryData, this, componentKeys, maxSsq,
																										maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
			StringTokenizer st = new StringTokenizer(componentNameString, "#");
			int nNames = st.countTokens();
			String componentName[] = new String[nNames];
			for (int i=0 ; i<nNames ; i++)
				componentName[i] = st.nextToken();
			table.setComponentNames(componentName);
			table.setComponentColors(componentColors);
			table.setForeground(tableLabelColor);
		thePanel.add("Center", table);
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			sampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
		
		return thePanel;
	}
	
	protected XPanel scatterPanel(DataSet data) {
		PureComponentsPanel thePanel = new PureComponentsPanel(this);
		thePanel.setupPanel(data, "xNum", "x", "y", PureRegnDataSet.kLinLsKey,
																		PureRegnDataSet.kFactorLsKey, "model",
																		DataWithComponentView.NO_COMPONENT_DISPLAY, this);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			summaryData.takeSample();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}