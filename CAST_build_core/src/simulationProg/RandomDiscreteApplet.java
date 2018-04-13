package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import valueList.*;
import coreVariables.*;

import simulation.*;


public class RandomDiscreteApplet extends XApplet {
	static final private String INDEX_NAME_PARAM = "indexName";
	static final private String SOURCE_NAME_PARAM = "sourceName";
	static final private String SIM_VAR_NAME_PARAM = "simVarName";
	static final private String SIM_INDEX_NAME_PARAM = "simIndexName";
	static final private String VALUE_AXIS_INFO_PARAM = "valueAxis";
	static final private String INDEX_AXIS_INFO_PARAM = "indexAxis";
	static final private String DISCRETE_STEP_PARAM = "discreteStep";
	
	private XButton resetButton;
	private RepeatingButton nextButton;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new ProportionLayout(0.5, 12, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.VERTICAL,
																					ProportionLayout.TOTAL));
			leftPanel.add(ProportionLayout.TOP, topLeftPanel(data, summaryData));
			leftPanel.add(ProportionLayout.BOTTOM, bottomLeftPanel(data, summaryData));
		
		add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.VERTICAL,
																					ProportionLayout.TOTAL));
			rightPanel.add(ProportionLayout.TOP, topRightPanel(data, summaryData));
			rightPanel.add(ProportionLayout.BOTTOM, bottomRightPanel(data, summaryData));
			
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	private double getDiscreteStep() {
		return Double.parseDouble(getParameter(DISCRETE_STEP_PARAM));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable distn = new NumVariable(getParameter(SOURCE_NAME_PARAM), Variable.USES_REPEATS);
		distn.readValues(getParameter(VALUES_PARAM));
		data.addVariable("distn", distn);
		
		IndexVariable index = new IndexVariable(getParameter(INDEX_NAME_PARAM),
																								distn.noOfValues());
		data.addVariable("index", index);
		
		OneRandomVariable pseudo = new OneRandomVariable("Pseudo", data, "distn");
		data.addVariable("pseudo", pseudo);
		
		return data;
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "pseudo");
		
		PseudoDiscreteVariable index = new PseudoDiscreteVariable(getParameter(SIM_INDEX_NAME_PARAM),
																		"pseudo", PseudoDiscreteVariable.INDEX);
		summaryData.addVariable("index", index);
		
		PseudoDiscreteVariable value = new PseudoDiscreteVariable(getParameter(SIM_VAR_NAME_PARAM),
																		"pseudo", PseudoDiscreteVariable.VALUE);
		summaryData.addVariable("value", value);
		
		summaryData.setAccumulate(true);
		
		return summaryData;
	}
	
	private XPanel topLeftPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(VALUE_AXIS_INFO_PARAM));
			horizAxis.setAxisName(translate("Distribution"));
		thePanel.add("Bottom", horizAxis);
		
			BoxedDotPlotView theDotPlot = new BoxedDotPlotView(data, this, horizAxis, "distn", getDiscreteStep());
			theDotPlot.setRetainLastSelection(true);
//			theDotPlot.lockBackground(Color.white);
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
	
	private XPanel topRightPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 4));
		
		thePanel.add(new OneValueView(data, "index", this));
		
		thePanel.add(new OneValueView(data, "distn", this));
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data, SummaryDataSet summaryData, String summaryKey,
																		String axisInfo, double step) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
			
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(axisInfo);
			horizAxis.setAxisName(summaryData.getVariable(summaryKey).name);
		thePanel.add("Bottom", horizAxis);
		
			StackedDiscreteView theDotPlot = new StackedDiscreteView(summaryData, this,
																									horizAxis, summaryKey, getDiscreteStep());
			theDotPlot.lockBackground(Color.white);
		thePanel.add("Center", theDotPlot);
		
		return thePanel;
	}
	
	private XPanel bottomRightPanel(DataSet data, SummaryDataSet summaryData) {
		return summaryPanel(data, summaryData, "value", getParameter(VALUE_AXIS_INFO_PARAM),
																							getDiscreteStep());
	}
	
	private XPanel bottomLeftPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 7));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
				nextButton = new RepeatingButton(translate("Next value"), this);
			topPanel.add(nextButton);
			
				resetButton = new XButton(translate("Reset"), this);
			topPanel.add(resetButton);
		
		thePanel.add("North", topPanel);
		
		thePanel.add("Center", summaryPanel(data, summaryData, "index",
																	getParameter(INDEX_AXIS_INFO_PARAM), 1.0));
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
			summaryData.takeSample();
			return true;
		}
		else if (target == resetButton) {
			summaryData.clearData();
			summaryData.variableChanged("value");
			summaryData.variableChanged("index");
			data.clearSelection();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}