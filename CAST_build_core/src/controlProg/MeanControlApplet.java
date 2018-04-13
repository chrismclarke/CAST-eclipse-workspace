package controlProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;
import axis.*;
import control.*;


public class MeanControlApplet extends ControlChartApplet {
	static final protected String GROUP_INFO_PARAM = "groupInfo";
	static final private String LIMIT_NAMES_PARAM = "limitNames";
	static final private String LIMIT_TYPE_PARAM = "limitType";
	
	protected DataSet rawData;
	protected int noInGroup, noOfTrainingSamples;
	
	private XCheckbox showMeansCheck;
	protected OneValueView valueView;
	
//	public void setupApplet() {
//		ScrollImages.loadScroll(this);
//		super.setupApplet();
//	}
	
	protected DataSet createData() {
		rawData = super.createData();
		
		int decimals = rawData.getNumVariable().getMaxDecimals() + 1;
		String groupInfoString = getParameter(GROUP_INFO_PARAM);
		StringTokenizer groupInfo = new StringTokenizer(groupInfoString);
		noInGroup = Integer.parseInt(groupInfo.nextToken());
		noOfTrainingSamples = Integer.parseInt(groupInfo.nextToken());
		
		DataSet meanData = new DataSet();
		meanData.addVariable("mean", new GroupMeanVariable("mean", rawData.getNumVariable(),
																							noInGroup, decimals));
		return meanData;
	}
	
	protected XPanel createProblemView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		return thePanel;
	}
	
	protected ControlView createControlView(DataSet data, IndexTimeAxis timeAxis,
																ControlLimitAxis limitAxis) {
		MeanControlView theView = new MeanControlView(data, this, timeAxis, limitAxis, ControlledEnumeration.ONE_OUTLIER,
																																			rawData, noOfTrainingSamples);
		String limitTypeString = getParameter(LIMIT_TYPE_PARAM);
		if (limitTypeString != null && limitTypeString.equals("rBar"))
			theView.setLimitType(MeanControlView.R_BAR_LIMITS);
		theView.setInitialFrame(noOfTrainingSamples);
		return theView;
	}
	
	protected void setControlAxisLimits(ControlLimitAxis limitAxis) {
											//		values will be set when MeanControlView is initialised
		limitAxis.setControlLimitNames(getParameter(LIMIT_NAMES_PARAM));
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(20, 2));
		
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		valueView = new OneValueView(data, "mean", this);
		valueView.show(false);
		valuePanel.add(valueView);
		controlPanel.add("West", valuePanel);
		
		animateSlider = new XNoValueSlider(translate("training"), translate("production"),
											null, noOfTrainingSamples, noOfValuesToDisplay(), noOfTrainingSamples, this);
		controlPanel.add("Center", animateSlider);
		
		showMeansCheck = new XCheckbox(translate("Show means"), this);
		controlPanel.add("East", showMeansCheck);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == showMeansCheck) {
			((MeanControlView)theView).setShowSummaries(showMeansCheck.getState());
			valueView.show(showMeansCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}