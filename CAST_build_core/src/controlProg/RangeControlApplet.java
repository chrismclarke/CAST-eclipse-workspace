package controlProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.OneValueView;
import axis.*;
import control.*;


public class RangeControlApplet extends MeanControlApplet {
	static final private String RAW_AXIS_PARAM = "rawAxis";
	
	protected DataSet createData() {
		rawData = new DataSet();
		rawData.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		int decimals = rawData.getNumVariable().getMaxDecimals();
		String groupInfoString = getParameter(GROUP_INFO_PARAM);
		StringTokenizer groupInfo = new StringTokenizer(groupInfoString);
		noInGroup = Integer.parseInt(groupInfo.nextToken());
		noOfTrainingSamples = Integer.parseInt(groupInfo.nextToken());
		
		DataSet rangeData = new DataSet();
		rangeData.addVariable("range", new GroupRangeVariable(translate("range"), rawData.getNumVariable(),
																							noInGroup, decimals));
		return rangeData;
	}
	
	protected XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		VertAxis theVertAxis = new VertAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theVertAxis);
		
		VertAxis rawVertAxis = new VertAxis(this);
		labelInfo = getParameter(RAW_AXIS_PARAM);
		rawVertAxis.readNumLabels(labelInfo);
		rawVertAxis.setForeground(Color.gray);
		thePanel.add("Left", rawVertAxis);
		
		limitAxis = new ControlLimitAxis(this);
		limitAxis.readExtremes(getParameter(AXIS_INFO_PARAM));
		setControlAxisLimits(limitAxis);
		thePanel.add("Right", limitAxis);
		
		IndexTimeAxis timeAxis = new IndexTimeAxis(this, noOfValuesToDisplay());
		timeAxis.setTimeScale(getParameter(TIME_INFO_PARAM));
		thePanel.add("Bottom", timeAxis);
		
		theView = new RangeControlView(data, this, timeAxis, limitAxis, ControlledEnumeration.ONE_OUTLIER,
																										rawData, noOfTrainingSamples, rawVertAxis);
		theView.setInitialFrame(noOfTrainingSamples);
		thePanel.add("Center", theView);
		
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new BorderLayout(5, 2));
		
		XPanel valuePanel = new XPanel();
		valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 3));
		
		valueView = new OneValueView(data, "range", this);
		valuePanel.add(valueView);
		controlPanel.add("West", valuePanel);
		
		animateSlider = new XNoValueSlider(translate("training"), translate("production"),
							null, noOfTrainingSamples, noOfValuesToDisplay(), noOfTrainingSamples, this);
		controlPanel.add("Center", animateSlider);
		
		return controlPanel;
	}
}