package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import distn.*;

import transform.*;


public class PopnPercentileApplet extends XApplet {
	static final private String POPN_NAME_PARAM = "popnName";
	static final private String SAMPLE_NAME_PARAM = "sampleName";
	static final private String POPN_VAR_NAME_PARAM = "popnVarName";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String PROB_AXIS_INFO_PARAM = "probAxis";
	static final private String DISCRETE_DISTN_PARAM = "discreteDistn";
	
	private boolean hasLabels = false;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, popnPanel(data));
		
		add(ProportionLayout.BOTTOM, dataPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		EmpDiscDistnVariable distn = new EmpDiscDistnVariable(getParameter(POPN_VAR_NAME_PARAM));
		distn.setParams(getParameter(DISCRETE_DISTN_PARAM));
		data.addVariable("distn", distn);
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		PercentileVariable percentile = new PercentileVariable(translate("Percentile"), data, "y", distn);
		data.addVariable("percentile", percentile);
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null) {
			hasLabels = true;
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
		}
		
		return data;
	}
	
	private XPanel popnPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XLabel popLabel = new XLabel(getParameter(POPN_NAME_PARAM), XLabel.LEFT, this);
			popLabel.setFont(getBigBoldFont());
		thePanel.add("North", popLabel);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new AxisLayout());
				
				HorizAxis horizAxis = getHorizAxis(data, "distn");
			dataPanel.add("Bottom", horizAxis);
			
				VertAxis pAxis = new VertAxis(this);
				pAxis.readNumLabels(getParameter(PROB_AXIS_INFO_PARAM));
			dataPanel.add("Left", pAxis);
			
				DiscreteBarChartView dataView = new DiscreteBarChartView(data, this, "distn", horizAxis, pAxis,
																																				DiscreteBarChartView.NO_DRAG);
				dataView.setLastBarShade(DiscreteBarChartView.HALF_LAST_BAR);
				dataView.lockBackground(Color.white);
			dataPanel.add("Center", dataView);
		
		thePanel.add("Center", dataPanel);
		
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
			valuePanel.add(new OneValueView(data, "y", this));
			valuePanel.add(new OneValueView(data, "percentile", this));
		
		thePanel.add("South", valuePanel);
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XLabel sampleLabel = new XLabel(getParameter(SAMPLE_NAME_PARAM), XLabel.LEFT, this);
			sampleLabel.setFont(getBigBoldFont());
		thePanel.add("North", sampleLabel);
		
			XPanel dataPanel = new XPanel();
			dataPanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.VERTICAL));
			
			dataPanel.add(ProportionLayout.TOP, rawPanel(data));
			dataPanel.add(ProportionLayout.BOTTOM, percentilePanel(data));
		
		thePanel.add("Center", dataPanel);
		
			XPanel namePanel = new XPanel();
			namePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
			if (hasLabels)
				namePanel.add(new OneValueView(data, "label", this));
		
		thePanel.add("South", namePanel);
		
		return thePanel;
	}
	
	private XPanel rawPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getHorizAxis(data, "y");
		thePanel.add("Bottom", horizAxis);
		
			LinkedStackedView dataView = new LinkedStackedView(data, this, horizAxis, "y", "y", "distn");
			dataView.setRetainLastSelection(true);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel percentilePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getPercentileAxis(data, "distn");
		thePanel.add("Bottom", horizAxis);
		
			LinkedStackedView dataView = new LinkedStackedView(data, this, horizAxis, "percentile", "y", "distn");
			dataView.setRetainLastSelection(true);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private HorizAxis getHorizAxis(DataSet data, String varKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(data.getVariable(varKey).name);
		return theHorizAxis;
	}
	
	private HorizAxis getPercentileAxis(DataSet data, String distnKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels("0 100 0 10");
		theHorizAxis.setAxisName(translate("Percentile"));
		return theHorizAxis;
	}
}