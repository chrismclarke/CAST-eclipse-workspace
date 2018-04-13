package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;

import graphics.*;


public class BarSortApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "yAxis";
	
	private SortingBarView barChart;
	
	private XCheckbox sortCheck;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("North", controlPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addLabelVariable("label", getParameter(LABEL_NAME_PARAM), getParameter(LABELS_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel catLabel = new XLabel(data.getVariable("label").name, XLabel.LEFT, this);
			catLabel.setFont(getStandardBoldFont());
		thePanel.add("North", catLabel);
		
			barChart = new SortingBarView(data, this, "y", "label", getParameter(AXIS_INFO_PARAM));
		thePanel.add("Center", barChart);
		
			XLabel yLabel = new XLabel(data.getVariable("y").name, XLabel.RIGHT, this);
			yLabel.setFont(getStandardBoldFont());
		thePanel.add("South", yLabel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			sortCheck = new XCheckbox(translate("Sorted values"), this);
		thePanel.add(sortCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == sortCheck) {
			barChart.setSort(sortCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}