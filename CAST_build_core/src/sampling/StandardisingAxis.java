package sampling;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class StandardisingAxis extends HorizAxis {
	static final public String kStandardisedLabelInfo = "-3.0 3.0 -3 1";
	static final private int kMaxLabelsDisplayed = 10;
	
	private AxisLabel labelStorage[];
	private double baseValue;
	private NumValue stepSize[];
	
	public double minScaled, maxScaled;
	
	public StandardisingAxis(XApplet applet) {
		super(applet);
		labelStorage = new AxisLabel[kMaxLabelsDisplayed];
		for (int i=0 ; i<kMaxLabelsDisplayed ; i++)
			labelStorage[i] = new AxisLabel(null, 0.0);
		canStagger = false;
	}
	
	public void readNumLabels(String labelInfo) {
		super.readNumLabels(kStandardisedLabelInfo);
		
		StringTokenizer st = new StringTokenizer(labelInfo);
		
		int noOfResolutions = st.countTokens() - 1;
		stepSize = new NumValue[noOfResolutions];
		baseValue = Double.parseDouble(st.nextToken());
		
		for (int i=0 ; i<noOfResolutions ; i++)
			stepSize[i] = new NumValue(st.nextToken());
	}
	
	public void setInitialMinMax(double min, double max) {
		labels.removeAllElements();
		for (int i=0 ; i<stepSize.length ; i++) {
			int minStepIndex = (int)Math.round((min - baseValue) / stepSize[i].toDouble());
			int maxStepIndex = (int)Math.round((max - baseValue) / stepSize[i].toDouble());
			int noOfLabels = maxStepIndex - minStepIndex + 1;
			if (noOfLabels <= kMaxLabelsDisplayed) {
				for (int labelIndex=0 ; labelIndex<noOfLabels ; labelIndex++) {
					NumValue labelVal = new NumValue(baseValue + (minStepIndex + labelIndex)
																	* stepSize[i].toDouble(), stepSize[i].decimals);
					labelStorage[labelIndex].label = labelVal;
					labelStorage[labelIndex].position = (labelVal.toDouble() - min) / (max - min);
					labels.addElement(labelStorage[labelIndex]);
				}
				break;
			}	
		}
		minScaled = min;
		maxScaled = max;
	}
	
	public void setMinMax(double min, double max) {
		setInitialMinMax(min, max);
		Graphics g = getGraphics();
		
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			theLabel.labelWidth = theLabel.label.stringWidth(g);
		}
		repaint();
	}
}