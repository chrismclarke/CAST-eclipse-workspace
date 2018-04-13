package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;
import coreVariables.*;

import transform.*;


public class LogTransformApplet extends DotLabelApplet {
	static final private String AXIS_EXTREME_PARAM = "axisExtremes";
	static final private String LOG_NAME_PARAM = "logName";
	
	private XChoice logRawChoice;
	private LogAxis axis;
	
	protected DataSet createData() {
		DataSet data = super.createData();
		
		NumVariable logVariable = new LogVariable(getParameter(LOG_NAME_PARAM), data, "y", 3);
		data.addVariable("log", logVariable);
		
		return data;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		addStackJitterChoice(thePanel);
		
		logRawChoice = new XChoice(this);
		logRawChoice.addItem("Show Logs");
		logRawChoice.addItem("Show Raw Data");
		logRawChoice.select(0);
		thePanel.add(logRawChoice);
		
		return thePanel;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 2));
		thePanel.add(new OneValueView(data, "label", this));
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 2));
		valuePanel.add(new OneValueView(data, "y", this));
		valuePanel.add(new OneValueView(data, "log", this));
		thePanel.add(valuePanel);
		return thePanel;
	}
	
	protected XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		axis = new LogAxis(this);
		axis.readExtremes(getParameter(AXIS_EXTREME_PARAM));
		thePanel.add("Bottom", axis);
		
		DataView theView = coreView(data, axis);
		theView.setActiveNumVariable("y");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == logRawChoice) {
			axis.setTransValueDisplay(logRawChoice.getSelectedIndex() == 1);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}