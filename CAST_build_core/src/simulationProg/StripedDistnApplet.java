package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import random.*;
import valueList.*;
import coreGraphics.*;
import simulation.*;

public class StripedDistnApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String NORMAL_INFO_PARAM = "normalInfo";
	static final private String DATA_DECIMALS_PARAM = "decimals";
	static final private String SAMPLE_NAME_PARAM = "sampleName";
	static final private String COVERAGE_AXIS_INFO_PARAM = "coverageAxis";
	static final private String COVERAGE_NAMES_PARAM = "coverageNames";
	static final private String COVERAGE_AXIS_NAME_PARAM = "coverageAxisName";
	
	private RepeatingButton takeSampleButton;
	private XCheckbox accumulateCheck;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = createData();
		summaryData = createSummaryData(data);
		summaryData.takeSample();
		
		setLayout(new ProportionLayout(0.5, 8, ProportionLayout.VERTICAL));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(12, 0));
			topPanel.add("Center", coveragePanel(data));
			topPanel.add("East", controlPanel());
			
		add(ProportionLayout.TOP, topPanel);
		add(ProportionLayout.BOTTOM, summaryPanel(data, summaryData));
	}
	
	private DataSet createData() {
		DataSet data = new DataSet();
		String normalParamString = getParameter(NORMAL_INFO_PARAM);
		int meanStart = normalParamString.indexOf(' ');
		String normalDistnString = normalParamString.substring(meanStart);
		
		NormalDistnVariable popnVar = new NormalDistnVariable(translate("Population"));
		popnVar.setParams(normalDistnString);
		data.addVariable("distn", popnVar);
		
		RandomNormal generator = new RandomNormal(normalParamString);
		int decimals = Integer.parseInt(getParameter(DATA_DECIMALS_PARAM));
		NumSampleVariable yVar = new NumSampleVariable(getParameter(SAMPLE_NAME_PARAM),
																							generator, decimals);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private SummaryDataSet createSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
		NumSampleVariable yVar = (NumSampleVariable)sourceData.getVariable("y");
		int noOfValues = yVar.getGenerator().getSampleSize();
		int noOfIntervals = noOfValues + 1;
		
		for (int i=0 ; i<noOfIntervals ; i++) {
			CoverageSummaryVariable coverageI = new CoverageSummaryVariable("Coverage " + i,
																						"distn", "y", i);
			summaryData.addVariable("coverage" + i, coverageI);
		}
		
		return summaryData;
	}
	
	private XPanel coveragePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis horizAxis = new HorizAxis(this);
		horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		horizAxis.setAxisName(getParameter(SAMPLE_NAME_PARAM));
		thePanel.add("Bottom", horizAxis);
		
		StripedDensityView theView = new StripedDensityView(data, this, horizAxis, "distn", "y");
		
		thePanel.add("Center", theView);
		
		
		DotPlotView dataPlot = new DotPlotView(data, this, horizAxis, 1.0);
		dataPlot.setActiveNumVariable("y");
		thePanel.add("BottomMargin", dataPlot);
		
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(getParameter(COVERAGE_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
		
		NumSampleVariable yVar = (NumSampleVariable)data.getVariable("y");
		int noOfIntervals = yVar.noOfValues() + 1;
		String[] dataKey = new String[noOfIntervals];
		for (int i=0 ; i<noOfIntervals ; i++)
			dataKey[i] = "coverage" + i;
		
		CatVariable tempGroupVar = new CatVariable("");
		String labelString = getParameter(COVERAGE_NAMES_PARAM);
		tempGroupVar.readLabels(labelString);
		HorizAxis groupAxis = new HorizAxis(this);
		groupAxis.setCatLabels(tempGroupVar);
		groupAxis.setAxisName(getParameter(COVERAGE_AXIS_NAME_PARAM));
		thePanel.add("Bottom", groupAxis);
		
		CoveragesPlotView coverageDotPlot = new CoveragesPlotView(summaryData, this, vertAxis, groupAxis, dataKey);
		thePanel.add("Center", coverageDotPlot);
		
		coverageDotPlot.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
		takeSampleButton = new RepeatingButton(translate("Take sample"), this);
		thePanel.add(takeSampleButton);
		
		accumulateCheck = new XCheckbox(translate("Accumulate"), this);
		thePanel.add(accumulateCheck);
		
		ValueCountView theCount = new ValueCountView(summaryData, this);
		theCount.setLabel(translate("No of samples") + " =");
		thePanel.add(theCount);
		
		return thePanel;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else if (evt.target == takeSampleButton) {
			summaryData.takeSample();
			return true;
		}
		else if (evt.target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		return false;
	}
}