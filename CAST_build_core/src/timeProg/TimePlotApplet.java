package timeProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.*;

import time.*;


public class TimePlotApplet extends BasicTimeApplet {
	private TimeAxis timeAxis;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		String dataString = getParameter(VALUES_PARAM);
		String moreDataString = getParameter(VALUES_PARAM + "2");
		if (moreDataString != null)
			dataString += moreDataString;		//		Netscape 4.5 can't seem to read very long PARAM
														//		strings, so they must be split into two.
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), dataString);
		
		String timeName = getParameter(TIME_NAME_PARAM);
		String timeSequence = getParameter(TIME_SEQUENCE_PARAM);
		if (timeName != null && timeSequence != null) {
			NumVariable timeVar = new NumVariable(timeName);
			timeVar.readSequence(timeSequence);
			data.addVariable("time", timeVar);
		}
		
		return data;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"y"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return true;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		timeAxis = super.horizAxis(data);
		return timeAxis;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
			
			if (timeAxis instanceof SeasonTimeAxis) {
				String timeName = getParameter(TIME_NAME_PARAM);
				if (timeName != null)
					thePanel.add(new SeasonYearValueView(data, this, (SeasonTimeAxis)timeAxis, timeName));
			}
			else {
				NumVariable timeVar = (NumVariable)data.getVariable("time");
				if (timeVar != null)
					thePanel.add(new OneValueView(data, "time", this));
			}
			
			OneValueView actual = new OneValueView(data, getCrossKey(), this);
			actual.setForeground(kActualColor);
		thePanel.add(actual);
		
		return thePanel;
	}
}