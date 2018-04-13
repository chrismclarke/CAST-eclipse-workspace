package varianceProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import ssq.*;
import variance.*;

public class QuadAdjustSsqApplet extends XApplet {
	
	static final private String RESID_AXIS_PARAM = "residAxis";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String R2_LABELS_PARAM = "r2Labels";
	static final private String INITIAL_R2_PARAM = "initialR2";
	static final private String QUADR2_LABELS_PARAM = "quadR2Labels";
	static final private String INITIAL_QUADR2_PARAM = "quadR2";
	
	static final private int kR2Decimals = 3;
	
	private NumValue maxSsq;
	private ComponentEqnPanel ssqEquation;
	
	private CoreModelDataSet data;
	private SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(10, 0));
		
			double initialR2 = Double.parseDouble(getParameter(INITIAL_R2_PARAM));
			double initialQuadR2 = Double.parseDouble(getParameter(INITIAL_QUADR2_PARAM));
		add("South", controlPanel(data, summaryData, initialR2, initialQuadR2));
		
		add("Center", dataDisplayPanel(data, ssqEquation, ComponentPlotPanel.SHOW_SD));
	}
	
	protected CoreModelDataSet getData() {
		QuadRegnDataSet data = new QuadRegnDataSet(this);
		
		QuadComponentVariable.addComponentsToData(data, "x", "y",
																				QuadRegnDataSet.kLinLsKey, QuadRegnDataSet.kQuadLsKey);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		AnovaSummaryData summaryData = new AnovaSummaryData(data, "error",
																				componentKeys(), maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		return summaryData;
	}
	
	protected String[] componentKeys() {
		return QuadComponentVariable.kComponentKey;
	}
	
	protected Color[] componentColors() {
		return QuadComponentVariable.kComponentColor;
	}
	
	protected int[] componentTypes() {
		return QuadComponentVariable.kComponentType;
	}
	
	private XPanel dataDisplayPanel(DataSet data, ComponentEqnPanel equationPanel,
																																	boolean showSD) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																ProportionLayout.TOTAL));
			
			DataWithComponentsPanel theScatterPanel = scatterPanel(data);
		thePanel.add(ProportionLayout.LEFT, theScatterPanel);
		
			String residAxisInfo = getParameter(RESID_AXIS_PARAM);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[4];
			for (int i=0 ; i<4 ; i++)
				componentName[i] = st.nextToken();
			ComponentsPanel componentPanel = new ComponentsPanel(data, residAxisInfo,
										componentName, componentKeys(), componentColors(), componentTypes(),
										0, theScatterPanel.getView(), equationPanel, showSD, this);
			
		thePanel.add(ProportionLayout.RIGHT, componentPanel);
			
		return thePanel;
	}
	
	protected DataWithComponentsPanel scatterPanel(DataSet data) {
		QuadraticComponentsPanel thePanel = new QuadraticComponentsPanel(this);
		thePanel.setupPanel(data, "x", "y", QuadRegnDataSet.kLinLsKey,
									QuadRegnDataSet.kQuadLsKey, null, BasicComponentVariable.TOTAL, this);
		return thePanel;
	}
	
	private XPanel controlPanel(CoreModelDataSet data, SummaryDataSet summaryData,
																								double initialR2, double initialQuadR2) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new BorderLayout(10, 0));
		
		thePanel.add("Center", new QuadR2Panel(this, data, "y", summaryData,
																						getParameter(R2_LABELS_PARAM), initialR2,
																						getParameter(QUADR2_LABELS_PARAM), initialQuadR2));
		
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			ssqEquation = new ComponentEqnPanel(summaryData, componentKeys(), 
							maxSsq, ssqImages(), componentColors(), AnovaImages.kQuadSsq2Width,
							AnovaImages.kQuadSsq2Height, bigContext);
		thePanel.add("East", ssqEquation);
		
		return thePanel;
	}
	
	protected Image[] ssqImages() {
		AnovaImages.loadQuadImages(this);
		return AnovaImages.quadSsqs2;
	}
}