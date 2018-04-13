package controlProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;
import axis.*;
import control.*;


public class ControlChartApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final protected String TIME_INFO_PARAM = "timeAxis";
	
	static final private String CONTROL_LIMIT_PARAM = "controlLimits";
	static final private String TRIGGER_PARAM = "trigger";
	
	protected DataSet data;
	
	protected XSlider animateSlider;
	protected ControlView theView;
	protected ControlLimitAxis limitAxis;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		data = createData();
		
		setLayout(new BorderLayout());
		add("Center", createView(data));
		add("North", createProblemView(data));
		add("South", createControls(data));
	}
	
	protected DataSet createData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected XPanel createProblemView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		ControlProblemView problem = new ControlProblemView(data, "y", this,
												limitAxis, theView.getProblemFlags());
		thePanel.add(problem);
		return thePanel;
	}
	
	protected int noOfValuesToDisplay() {
		return data.getNumVariable().noOfValues();
	}
	
	protected XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis theVertAxis = new VertAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theVertAxis);
		
		limitAxis = new ControlLimitAxis(this);
		limitAxis.readExtremes(labelInfo);
		setControlAxisLimits(limitAxis);
		thePanel.add("Right", limitAxis);
		
		IndexTimeAxis timeAxis = new IndexTimeAxis(this, noOfValuesToDisplay());
		timeAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		thePanel.add("Bottom", timeAxis);
		
		theView = createControlView(data, timeAxis, limitAxis);
		thePanel.add("Center", theView);
		
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected ControlView createControlView(DataSet data, IndexTimeAxis timeAxis,
																					ControlLimitAxis limitAxis) {
		String triggerParam = getParameter(TRIGGER_PARAM);
		int triggerFlags = (triggerParam == null || !triggerParam.equals("all"))
							? ControlledEnumeration.ONE_OUTLIER : ControlledEnumeration.ALL_PROBLEMS;
		ControlView theView = new ControlView(data, this, timeAxis, limitAxis, triggerFlags);
		if (triggerFlags != ControlledEnumeration.ONE_OUTLIER)
			theView.setZoneDisplay(true);
		return theView;
	}
	
	protected void setControlAxisLimits(ControlLimitAxis limitAxis) {
		limitAxis.readControlLabels(getParameter(CONTROL_LIMIT_PARAM));
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(5, 2));
		
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		OneValueView valueView = new OneValueView(data, "y", this);
		valuePanel.add(valueView);
		controlPanel.add("West", valuePanel);
		
		animateSlider = new XNoValueSlider(translate("start"), translate("end"), null, 0, noOfValuesToDisplay(), 0, this);
		controlPanel.add("Center", animateSlider);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theView.setFrame(animateSlider.getValue());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}