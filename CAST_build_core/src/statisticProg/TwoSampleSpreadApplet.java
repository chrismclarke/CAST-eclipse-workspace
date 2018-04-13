package statisticProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.*;
import coreVariables.*;
import coreGraphics.*;

import statistic.*;


public class TwoSampleSpreadApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "dataAxis";
	static final private String GROUP_NAMES_PARAM = "groupNames";
	static final private String SUMMARY_DECIMALS_PARAM = "summaryDecimals";
	static final private String SUMMARY_AXIS_PARAM = "summaryAxis";
	static final private String SUMMARY_NAME_PARAM = "summaryName";
	
	private RepeatingButton takeSampleButton, resetButton;
//	private XCheckbox accumulateCheck;
	
	private RandomNormal generator1, generator2;
	private DataSet data;
	private SummaryDataSet summaryData;
	
//	private HorizAxis axis;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
//		setLayout(new BorderLayout(0, 8));
//		
//		add("Center", dataPanel(data));
//		add("South", controlPanel());

		setLayout(new ProportionLayout(0.45, 8, ProportionLayout.VERTICAL));
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(10, 0));
			
			topPanel.add("Center", dataPanel(data));
			topPanel.add("East", controlPanel());
		
		add(ProportionLayout.TOP, topPanel);
		
		add(ProportionLayout.BOTTOM, summaryPanel(summaryData));
		
		summaryData.takeSample();
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		StringTokenizer st = new StringTokenizer(getParameter(GROUP_NAMES_PARAM), "#");
		
		generator1 = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM + 1));
		NumVariable y1 = new NumSampleVariable(st.nextToken(), generator1, 9);
		data.addVariable("y1", y1);
		
		generator2 = new RandomNormal(getParameter(RANDOM_NORMAL_PARAM + 2));
		NumVariable y2 = new NumSampleVariable(st.nextToken(), generator2, 9);
		data.addVariable("y2", y2);
		
		data.addVariable("y", new BiSampleVariable(data, "y1", "y2"));
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		summaryData.setAccumulate(true);
		
		int summaryDecimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
		for (int i=1 ; i<=2 ; i++) {
			summaryData.addVariable("sd" + i, new SpreadVariable("sd" + i, "y" + i,
																						SpreadVariable.SD_SUMMARY, summaryDecimals));
			summaryData.addVariable("iqr" + i, new SpreadVariable("iqr" + i, "y" + i,
																						SpreadVariable.IQR_SUMMARY, summaryDecimals));
			summaryData.addVariable("range" + i, new SpreadVariable("range" + i, "y" + i,
																					SpreadVariable.RANGE_SUMMARY, summaryDecimals));
		}
		
		summaryData.addVariable("sdRatio", new RatioVariable("sd ratio", summaryData,
																											"sd1", "sd2", summaryDecimals));
		summaryData.addVariable("iqrRatio", new RatioVariable("irq ratio", summaryData,
																											"iqr1", "iqr2", summaryDecimals));
		summaryData.addVariable("rangeRatio", new RatioVariable("range ratio", summaryData,
																									"range1", "range2", summaryDecimals));
		
		return summaryData;
	}
	
/*
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			takeSampleButton = new RepeatingButton(translate("Another data set"), this);
		thePanel.add(takeSampleButton);
		
			resetButton = new RepeatingButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}
*/
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 30));
		
			takeSampleButton = new RepeatingButton("More data", this);
		thePanel.add(takeSampleButton);
		
			resetButton = new RepeatingButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			axis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", axis);
		
			int summaryDecimals = Integer.parseInt(getParameter(SUMMARY_DECIMALS_PARAM));
			
			OneSampleSpreadView topView = new OneSampleSpreadView(data, this, axis,
																																	"y1", summaryDecimals);
			OneSampleSpreadView bottomView = new OneSampleSpreadView(data, this, axis,
																																	"y2", summaryDecimals);
			
			MultipleDataView dataView = new MultipleDataView(data, this, topView, bottomView);
//			TwoSampleSpreadView dataView = new TwoSampleSpreadView(data, this, axis, "y1", "y2",
//																																	summaryDecimals);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet summmaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis axis = new HorizAxis(this);
			axis.readNumLabels(getParameter(SUMMARY_AXIS_PARAM));
			axis.setAxisName(getParameter(SUMMARY_NAME_PARAM));
		thePanel.add("Bottom", axis);
			
			DotPlotView sdView = new StackedDotPlotView(summmaryData, this, axis);
			sdView.setActiveNumVariable("sdRatio");
			sdView.setForeground(OneSampleSpreadView.kSdColor);
			
			DotPlotView iqrView = new StackedDotPlotView(summmaryData, this, axis);
			iqrView.setActiveNumVariable("iqrRatio");
			iqrView.setForeground(OneSampleSpreadView.kIqrColor);
			
			DotPlotView rangeView = new StackedDotPlotView(summmaryData, this, axis);
			rangeView.setActiveNumVariable("rangeRatio");
			rangeView.setForeground(OneSampleSpreadView.kRangeColor);
			
			MultipleDataView dataView = new MultipleDataView(data, this, sdView, iqrView, rangeView);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == resetButton) {
			summaryData.setSingleSummaryFromData();
//			summaryData.setAccumulate(false);
//			summaryData.setAccumulate(true);
			return true;
		}
//		else if (target == accumulateCheck) {
//			summaryData.setAccumulate(accumulateCheck.getState());
//			return true;
//		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}