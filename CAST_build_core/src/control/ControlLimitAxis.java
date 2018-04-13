package control;

import java.util.*;

import axis.*;
import dataView.*;


public class ControlLimitAxis extends VertAxis {
	private double centre, lcl, ucl;
	
	public ControlLimitAxis(XApplet applet) {
		super(applet);
		setFont(applet.getSmallFont());
	}
	
	private double coreAddLabel(String name, NumValue value) {
		LabelValue label = new LabelValue(name + " = " + value.toString());
		addAxisLabel(label, (value.toDouble() - minOnAxis) / (maxOnAxis - minOnAxis));
		return value.toDouble();
	}
	
	private double addLabel(LabelEnumeration controlLabels) throws AxisException {
		if (controlLabels.hasMoreElements()) {
			String name = (String)controlLabels.nextElement();
			if (controlLabels.hasMoreElements()) {
				String valueString = (String)controlLabels.nextElement();
				NumValue value = new NumValue(valueString);
				return coreAddLabel(name, value);
			}
		}
		throw new AxisException(AxisException.FORMAT_ERROR);
	}
	
	public void readExtremes(String numLabelInfo) {
		StringTokenizer numLabels = new StringTokenizer(numLabelInfo);
		String labelExtremes = "";
		try {
			labelExtremes = numLabels.nextToken() + " " + numLabels.nextToken();
		} catch (Exception e) {
		}
		readNumLabels(labelExtremes);
	}
	
	public void readControlLabels(String controlLabelInfo) {
		if (getPowerIndex() != kOnePowerIndex)
			throw new RuntimeException("Cannot handle transformed control axis");
		LabelEnumeration controlLabels = new LabelEnumeration(controlLabelInfo);
		try {
			lcl = addLabel(controlLabels);
			centre = addLabel(controlLabels);
			ucl = addLabel(controlLabels);
		} catch (AxisException e) {
			throw new RuntimeException("Centre & control limits not specified");
		}
		
		repaint();
	}
	
	private String lclName, centreName, uclName;
	
	public void setControlLimitNames(String limitStrings) {
		LabelEnumeration controlLabels = new LabelEnumeration(limitStrings);
		try {
			lclName = (String)controlLabels.nextElement();
		} catch (Exception e) {
			lclName = "";
		}
		try {
			centreName = (String)controlLabels.nextElement();
		} catch (Exception e) {
			centreName = "";
		}
		try {
			uclName = (String)controlLabels.nextElement();
		} catch (Exception e) {
			uclName = "";
		}
	}
	
	public void setControlLimits(NumValue lclValue, NumValue centreValue, NumValue uclValue) {
		if (getPowerIndex() != kOnePowerIndex)
			throw new RuntimeException("Cannot handle transformed control axis");
		labels.removeAllElements();
		lcl = coreAddLabel(lclName, lclValue);
		centre = coreAddLabel(centreName, centreValue);
		ucl = coreAddLabel(uclName, uclValue);
		repaint();
	}
	
	public double getCentre() {
		return centre;
	}
	
	public double getLowerLimit() {
		return lcl;
	}
	
	public double getUpperLimit() {
		return ucl;
	}
	
	public double getLowerABLimit() {
		return centre - (centre - lcl) / 1.5;
	}
	
	public double getUpperABLimit() {
		return centre + (ucl - centre) / 1.5;
	}
	
	public double getLowerBCLimit() {
		return centre - (centre - lcl) / 3.0;
	}
	
	public double getUpperBCLimit() {
		return centre + (ucl - centre) / 3.0;
	}
}