package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import distn.*;

import transform.*;


public class EmpDiscreteApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String PROB_AXIS_INFO_PARAM = "probAxis";
	static final private String DISCRETE_DISTN_PARAM = "discreteDistn";
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		
		add("North", axisLabelPanel());
		add("Center", dataViewPanel(data, "y"));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		EmpDiscDistnVariable y = new EmpDiscDistnVariable(getParameter(VAR_NAME_PARAM));
		y.setParams(getParameter(DISCRETE_DISTN_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	private XPanel axisLabelPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("West", new XLabel(translate("Count"), XLabel.LEFT, this));
		thePanel.add("East", new XLabel(translate("Proportion"), XLabel.RIGHT, this));
		
		return thePanel;
	}
	
	private XPanel dataViewPanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis horizAxis = getHorizAxis(data, variableKey);
		thePanel.add("Bottom", horizAxis);
		
		VertAxis nAxis = new VertAxis(this);
		nAxis.readNumLabels(getParameter(COUNT_AXIS_INFO_PARAM));
		thePanel.add("Left", nAxis);
		
		VertAxis pAxis = new VertAxis(this);
		pAxis.readNumLabels(getParameter(PROB_AXIS_INFO_PARAM));
		thePanel.add("Right", pAxis);
		
		DiscreteBarChartView dataView = new DiscreteBarChartView(data, this, "y", horizAxis, pAxis,
																												DiscreteBarChartView.DRAG_CUMULATIVE);
		thePanel.add("Center", dataView);
		dataView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private HorizAxis getHorizAxis(DataSet data, String variableKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(data.getVariable(variableKey).name);
		return theHorizAxis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		thePanel.add(new ProportionView(data, "y", this));
		
		return thePanel;
	}
}