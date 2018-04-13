package qnUtils;

import dataView.*;
import axis.*;
import random.*;


public class AxisGenerator {
	static final protected String AXIS_INFO_PARAM = "axis";
	
	private String axisInfo[];
	private RandomUniform randomAxisGenerator;
	
	public AxisGenerator(XApplet applet) {
		int noOfAxes = 0;
		while (true) {
			String theAxisInfo = applet.getParameter(AXIS_INFO_PARAM + (noOfAxes+1));
			if (theAxisInfo == null)
				break;
			noOfAxes++;
		}
		axisInfo = new String[noOfAxes];
		for (int i=0 ; i<noOfAxes ; i++)
			axisInfo[i] = applet.getParameter(AXIS_INFO_PARAM + (i+1));
		
		randomAxisGenerator = new RandomUniform(1, 0, noOfAxes - 1);
	}
	
	public void changeRandomAxis(AxisChoice axis, NumVariable variable, XApplet applet) {
		if (axis.axis == null) {
			if (axis.horizNotVert)
				axis.axis = new HorizAxis(applet);
			else
				axis.axis = new VertAxis(applet);
			axis.axis.readNumLabels(axisInfo[axis.axisVersion]);
			if (variable != null)
				axis.axis.setAxisName(variable.name);
		}
		else {
			int newHorizVersion = randomAxisGenerator.generateOne();
			if (newHorizVersion != axis.axisVersion) {
				axis.axisVersion = newHorizVersion;
				axis.axis.readNumLabels(axisInfo[axis.axisVersion]);
			}
		}
	}
}