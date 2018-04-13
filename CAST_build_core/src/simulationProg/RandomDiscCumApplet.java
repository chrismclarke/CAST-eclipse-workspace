package simulationProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;
import coreVariables.*;

import simulation.*;


public class RandomDiscCumApplet extends XApplet {
	static final private String INDEX_NAME_PARAM = "indexName";
	static final private String INDEX_NAME2_PARAM = "indexName2";
	static final private String SOURCE_NAME_PARAM = "sourceName";
	static final private String HORIZ_AXIS_NAME_PARAM = "horizAxisName";
	static final private String VALUE_AXIS_INFO_PARAM = "valueAxis";
	static final private String CUM_AXIS_INFO_PARAM = "cumAxis";
	static final private String DISCRETE_STEP_PARAM = "discreteStep";
	
	private XButton nextButton;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		
		setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 0));
			
			leftPanel.add("Center", displayPanel(data));
			leftPanel.add("South", valuePanel(data));
		
		add("Center", leftPanel);
		
		add("East", controlPanel());
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
		return summaryData;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			String cumAxisName = getParameter(INDEX_NAME2_PARAM) + " \u2264 x)";
		thePanel.add("North", new XLabel(cumAxisName, XLabel.LEFT, this));
		thePanel.add("Center", dataDisplayPanel(data));
		return thePanel;
	}
	
	private XPanel dataDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(VALUE_AXIS_INFO_PARAM));
			horizAxis.setAxisName(getParameter(HORIZ_AXIS_NAME_PARAM));
		thePanel.add("Bottom", horizAxis);
		
			VertAxis vertAxis = new VertAxis(this);
			vertAxis.readNumLabels(getParameter(CUM_AXIS_INFO_PARAM));
		thePanel.add("Left", vertAxis);
			
			double discreteStep = Double.parseDouble(getParameter(DISCRETE_STEP_PARAM));
			CumBoxedView theView = new CumBoxedView(data, this, horizAxis, vertAxis, "distn", "pseudo", discreteStep);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		thePanel.add(new OneValueView(data, "index", this));
		
		thePanel.add(new OneValueView(data, "distn", this));
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			nextButton = new XButton(translate("Next value"), this);
		thePanel.add(nextButton);
			
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == nextButton) {
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