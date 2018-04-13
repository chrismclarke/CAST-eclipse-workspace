package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import time.*;


public class TimeJitterApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "vertAxis";
	static final private String TIME_INFO_PARAM = "timeAxis";
	static final private String SEASON_PARAM = "seasonName";
	static final private String SEASON_INFO_PARAM = "seasonInfo";
	static final private String TRANSFORM_PARAM = "transform";
	
	private DataSet data;
	private TimeJitterView theView;
	
	private XCheckbox showLinesCheck;
	private XButton animateButton;
	private XSlider animateSlider;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected DataSet getData() {
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		String transformString = getParameter(TRANSFORM_PARAM);
		VertAxis theVertAxis;
		if (transformString != null && transformString.equals("true"))
			theVertAxis = new TransformVertAxis(this);
		else
			theVertAxis = new VertAxis(this);
		theVertAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
		thePanel.add("Left", theVertAxis);
		
		TimeAxis theHorizAxis = horizAxis(data);
		theHorizAxis.setAxisName("Time");
		theHorizAxis.show(false);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = new TimeJitterView(data, this, theHorizAxis, theVertAxis);
		if (theVertAxis instanceof TransformVertAxis)
			theVertAxis.setLinkedData(data, true);
		
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		String indexTimeParam = getParameter(TIME_INFO_PARAM);
		String seasonTimeParam = getParameter(SEASON_PARAM);
		if (seasonTimeParam != null) {
			SeasonTimeAxis theHorizAxis = new SeasonTimeAxis(this, data.getNumVariable().noOfValues());
			theHorizAxis.setTimeScale(getParameter(SEASON_PARAM), getParameter(SEASON_INFO_PARAM));
			return theHorizAxis;
		}
		else {
			IndexTimeAxis theHorizAxis = new IndexTimeAxis(this, data.getNumVariable().noOfValues());
			if (indexTimeParam != null)
				theHorizAxis.setTimeScale(indexTimeParam);
			else
				theHorizAxis.setTimeScale(0, 1, 1, 1);
			return theHorizAxis;
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 4));
		
		XPanel buttonPanel = new XPanel();
		buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 5));
		animateButton = new XButton("Animate Ordering", this);
		buttonPanel.add(animateButton);
		
		showLinesCheck = new XCheckbox("Join crosses", this);
		showLinesCheck.setState(theView.getShowLines());
		buttonPanel.add(showLinesCheck);
		
		thePanel.add("East", buttonPanel);
		
		animateSlider = new XNoValueSlider("jittered", "time ordered", null, 0, TimeJitterView.kEndFrame,
																																										0, this);
		thePanel.add("Center", animateSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == showLinesCheck) {
			boolean newState = showLinesCheck.getState();
			if (newState != theView.getShowLines())
				theView.setShowLines(newState);
			return true;
		}
		if (target == animateSlider) {
			theView.setFrame(animateSlider.getValue());
			return true;
		}
		else if (target == animateButton) {
			theView.doOrderingAnimation(animateSlider);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}