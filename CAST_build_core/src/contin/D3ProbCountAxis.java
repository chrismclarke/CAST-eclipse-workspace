package contin;

import java.util.*;

import dataView.*;
import graphics3D.*;


public class D3ProbCountAxis extends D3Axis {
	static final public int COUNTS = 0;
	static final public int PROPN = 1;
	
	private XApplet applet;
	private Vector countLabels;
	private int displayType = COUNTS;
	
	public D3ProbCountAxis(int orientation, int tickAxisOrientation, XApplet applet) {
		super(applet.translate("Frequency"), orientation, tickAxisOrientation, applet);
		this.applet = applet;
	}
	
	public void setNumScale(String probLabelInfo, String countLabelInfo) {
		super.setNumScale(countLabelInfo);
		countLabels = labels;
		labels = new Vector(10);
		super.setNumScale(probLabelInfo);
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		setLabelName((displayType == COUNTS) ? applet.translate("Frequency") : applet.translate("Proportion"));
	}
	
	public Enumeration getLabelEnumeration() {
		if (displayType == COUNTS)
			return countLabels.elements();
		else
			return super.getLabelEnumeration();
	}
}