package simulationProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import valueList.*;
import random.*;
import distn.*;
import coreSummaries.*;

//import randomStat.*;
import simulation.*;


public class RandomNormCumApplet extends XApplet {
	static final private String DISTN_NAME_PARAM = "distnName";
	static final private String SIM_RECT_NAME_PARAM = "simRectName";
	static final private String SIM_VALUE_NAME_PARAM = "simValueName";
	static final private String HORIZ_AXIS_NAME_PARAM = "horizAxisName";
	static final private String VALUE_AXIS_INFO_PARAM = "valueAxis";
	static final private String CUM_AXIS_INFO_PARAM = "cumAxis";
	static final private String NORMAL_DISTN_PARAM = "normalDistn";
	static final private String MAX_VALUE_PARAM = "maxValue";
	
	static final private NumValue maxPseudo = new NumValue(1.0, 4);
	
	private XCheckbox accumulateCheck;
	private RepeatingButton nextButton;
	
	protected DataSet data;
	private SummaryDataSet summaryData;
	
	private NumValue maxValue;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 0));
			
			leftPanel.add("Center", displayPanel(data, summaryData));
			leftPanel.add("South", valuePanel(data, summaryData));
		
		add("Center", leftPanel);
		
		add("East", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(NORMAL_DISTN_PARAM));
		double mean = Double.parseDouble(st.nextToken());
		double sd = Double.parseDouble(st.nextToken());
		
		NormalDistnVariable distn = getDistnVariable(getParameter(DISTN_NAME_PARAM));
		distn.setMean(mean);
		distn.setSD(sd);
		data.addVariable("distn", distn);
		
		RandomRectangular generator = new RandomRectangular(1, 0.001, 0.999);
		NumSampleVariable pseudo = new NumSampleVariable("Pseudo", generator, maxPseudo.decimals);
		data.addVariable("pseudo", pseudo);
		
		maxValue = new NumValue(getParameter(MAX_VALUE_PARAM));
		DistnFunctionVariable randomVal = new DistnFunctionVariable("Value", data,
																		"pseudo", "distn", maxValue.decimals);
		data.addVariable("randomVal", randomVal);
		
		return data;
	}
	
	protected NormalDistnVariable getDistnVariable(String name) {
		return new NormalDistnVariable(name);
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "pseudo");
		
		NumVariable pseudo = (NumVariable)data.getVariable("pseudo");
		MeanVariable pseudoSum = new MeanVariable(getParameter(SIM_RECT_NAME_PARAM),
																		"pseudo", pseudo.getMaxDecimals());
												//		this is only ever the mean of 1 value!!
		summaryData.addVariable("pseudoSum", pseudoSum);
		
		NumVariable value = (NumVariable)data.getVariable("randomVal");
		MeanVariable valueSum = new MeanVariable(getParameter(SIM_VALUE_NAME_PARAM),
																		"randomVal", value.getMaxDecimals());
												//		this is only ever the mean of 1 value!!
		summaryData.addVariable("valueSum", valueSum);
		
		summaryData.takeSample();
		
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			String cumAxisName = "P(value \u2264 x)";
		thePanel.add("North", new XLabel(cumAxisName, XLabel.LEFT, this));
		thePanel.add("Center", dataDisplayPanel(data, summaryData));
		return thePanel;
	}
	
	private XPanel dataDisplayPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis xAxis = new HorizAxis(this);
			xAxis.readNumLabels(getParameter(VALUE_AXIS_INFO_PARAM));
			xAxis.setAxisName(getParameter(HORIZ_AXIS_NAME_PARAM));
		thePanel.add("Bottom", xAxis);
		
			VertAxis probAxis = new VertAxis(this);
			probAxis.readNumLabels(getParameter(CUM_AXIS_INFO_PARAM));
		thePanel.add("Left", probAxis);
			
			CumulativePlotView theView = new CumulativePlotView(data, this, xAxis, probAxis, "distn",
																												"pseudo", "randomVal");
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		DotPlotView randomRectPlot = new DotPlotView(summaryData, this, probAxis, 1.0);
		randomRectPlot.setActiveNumVariable("pseudoSum");
		thePanel.add("LeftMargin", randomRectPlot);
		
		DotPlotView randomNormPlot = new DotPlotView(summaryData, this, xAxis, 1.0);
		randomNormPlot.setActiveNumVariable("valueSum");
		thePanel.add("BottomMargin", randomNormPlot);
		
		return thePanel;
	}
	
	private XPanel valuePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		thePanel.add(new OneValueView(summaryData, "pseudoSum", this, maxPseudo));
		
		thePanel.add(new OneValueView(summaryData, "valueSum", this, maxValue));
		
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 20));
		
			nextButton = new RepeatingButton(translate("Next value"), this);
		thePanel.add(nextButton);
		
			accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}