package timeProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.*;

import time.*;


public class MultiTimePlotApplet extends BasicTimeApplet {
	static final private String NO_OF_GROUPS_PARAM = "noOfGroups";
	static final private String GROUP_NAME_PARAM = "groupName";
	static final private String GROUP_VALUES_PARAM = "groupValues";
	
	static final private Color[] groupColor = {Color.black, Color.blue, Color.red, new Color(0x00CC00)};
	
	private String groupKeys[];
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		int noOfGroups = Integer.parseInt(getParameter(NO_OF_GROUPS_PARAM));
		groupKeys = new String[noOfGroups];
		for (int i=0 ; i<noOfGroups ; i++) {
			groupKeys[i] = "g" + i;
			data.addNumVariable(groupKeys[i], getParameter(GROUP_NAME_PARAM + i),
																										getParameter(GROUP_VALUES_PARAM + i));
		}
		
		return data;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		return groupKeys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		TimeView theView = super.createTimeView(theHorizAxis, theVertAxis);
		theView.setLineColors(groupColor);
		return theView;
	}
	
	private XPanel halfValuesPanel(DataSet data, int startIndex) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel groupValPanel = new XPanel();
			groupValPanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 4));
			
			for (int i=startIndex ; i<groupKeys.length ; i+=2) {
				OneValueView groupVal = new OneValueView(data, groupKeys[i], this);
				groupVal.setForeground(groupColor[i]);
				groupValPanel.add(groupVal);
			}
			
		thePanel.add(groupValPanel);
		
		return thePanel;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 10);
		thePanel.setLayout(new ProportionLayout(0.5, 0));
		
		thePanel.add("Left", halfValuesPanel(data, 0));
		thePanel.add("Right", halfValuesPanel(data, 1));
		
		return thePanel;
	}
}