package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import time.*;
import transform.*;


public class Log10TimeApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final protected String SEASON_PARAM = "seasonName";
	static final private String SEASON_INFO_PARAM = "seasonInfo";
//	static final private String TRANSFORM_PARAM = "transform";
	static final protected String TIME_NAME_PARAM = "timeAxisName";
	static final protected String TIME_SEQUENCE_PARAM = "timeSequence";
	static final private String LOG_AXIS_INFO_PARAM = "logAxis";
	static final private String USE_SMALL_FONT_PARAM = "smallAxisFont";
	
	private DataSet data;
	private TimeView theView;
	
	protected VertAxis rawVertAxis;
	protected Log10VertAxis logVertAxis;
	
	private XNoValueSlider transformSlider;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		String timeName = getParameter(TIME_NAME_PARAM);
		String timeSequence = getParameter(TIME_SEQUENCE_PARAM);
		if (timeName != null && timeSequence != null) {
			NumVariable timeVar = new NumVariable(timeName);
			timeVar.readSequence(timeSequence);
			data.addVariable("time", timeVar);
		}
		
		return data;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		return new TimeView(data, this, theHorizAxis, theVertAxis);
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		String varName = data.getVariable("y").name;
		thePanel.add("West", new XLabel(translate("Raw") + " " + varName, XLabel.LEFT, this));
		thePanel.add("Center", new XPanel());
		thePanel.add("East", new XLabel(translate("Log") + "(" + varName + ")", XLabel.RIGHT, this));
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		rawVertAxis = new VertAxis(this);
		rawVertAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		thePanel.add("Left", rawVertAxis);
		
		logVertAxis = new Log10VertAxis(this, Log10VertAxis.SHOW_LOGS);
		String labelInfo = getParameter(LOG_AXIS_INFO_PARAM);
		logVertAxis.readLogLabels(labelInfo);
		thePanel.add("Right", logVertAxis);
		
		TimeAxis theHorizAxis = horizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = createTimeView(theHorizAxis, rawVertAxis);
		theView.setActiveNumVariable("y");
		theView.setSmoothedVariable("y");
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		TimeAxis valueAxis;
		String timeParam = getParameter(TIME_INFO_PARAM);
		if (timeParam != null) {
			IndexTimeAxis theHorizAxis = new IndexTimeAxis(this, data.getNumVariable().noOfValues());
			theHorizAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
			valueAxis = theHorizAxis;
		}
		else {
			SeasonTimeAxis theHorizAxis = new SeasonTimeAxis(this, data.getNumVariable().noOfValues());
			theHorizAxis.setTimeScale(getParameter(SEASON_PARAM), getParameter(SEASON_INFO_PARAM));
			valueAxis = theHorizAxis;
		}
		
		String smallFontString = getParameter(USE_SMALL_FONT_PARAM);
		boolean smallFont = (smallFontString != null) && smallFontString.equals("true");
		if (smallFont)
			valueAxis.setFont(getSmallFont());
		return valueAxis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.8, 0, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			leftPanel.add("Left", new XPanel());
		
				transformSlider = new XNoValueSlider(translate("Original scale"), translate("Log scale"),
																																	null, 0, 100, 0, this);
			leftPanel.add("Right", transformSlider);
		
		thePanel.add("Left", leftPanel);
		thePanel.add("Right", new XPanel());
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == transformSlider) {
			int newTransformIndex = 300 - transformSlider.getValue();
			rawVertAxis.setPowerIndex(newTransformIndex);
			logVertAxis.setPowerIndex(newTransformIndex);
			data.transformedAxis(rawVertAxis);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}