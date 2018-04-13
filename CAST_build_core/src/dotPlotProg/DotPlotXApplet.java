package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;
import dotPlot.DotPlotXView;


public class DotPlotXApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String AXIS_LABEL_PARAM = "axisLabel";
	
	private DotPlotXView theDotPlot;
	private XChoice plotTypeChoice;
	
	protected XPanel dataViewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		String label = getParameter(AXIS_LABEL_PARAM);
		if (label != null)
			theHorizAxis.setAxisName(label);
		thePanel.add("Bottom", theHorizAxis);
		
		theDotPlot = new DotPlotXView(data, this, theHorizAxis);
		thePanel.add("Center", theDotPlot);
		theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataSet readLabelledData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		return data;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		plotTypeChoice = new XChoice(this);
		plotTypeChoice.addItem(translate("Unjittered"));
		plotTypeChoice.addItem(translate("Jittered"));
		plotTypeChoice.addItem(translate("Stacked"));
		plotTypeChoice.select(0);
		thePanel.add(plotTypeChoice);
		
		if (data.getVariable("label") != null)
			thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
	
	public void setupApplet() {
		DataSet data = readLabelledData();
		
		setLayout(new BorderLayout());
		
		add("Center", dataViewPanel(data));
		add("South", controlPanel(data));
	}
	
	private boolean localAction(Object target) {
		if (target == plotTypeChoice) {
			theDotPlot.animateTo(plotTypeChoice.getSelectedIndex());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}