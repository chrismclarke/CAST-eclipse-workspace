package axis;

import java.util.*;
import java.awt.*;
import dataView.*;


public class IndexTimeAxis extends TimeAxis {
	private int labelOneIndex, labelPeriod, firstValLabel, labelStep;
	
	public IndexTimeAxis(XApplet applet, int noOfVals) {
		super(applet, noOfVals);
	}
	
	public void setTimeScale(int labelOneIndex, int labelPeriod,
																	int firstValLabel, int labelStep) {
		this.labelOneIndex = labelOneIndex;
		this.labelPeriod = labelPeriod;
		this.firstValLabel = firstValLabel;
		this.labelStep = labelStep;
	}
	
	public void setTimeScale(String labelInfo) {
		try {
			StringTokenizer theLabels = new StringTokenizer(labelInfo);
		
			labelOneIndex = Integer.parseInt(theLabels.nextToken());
			labelPeriod = Integer.parseInt(theLabels.nextToken());
			firstValLabel = Integer.parseInt(theLabels.nextToken());
			labelStep = Integer.parseInt(theLabels.nextToken());
		} catch (Exception e) {
			System.err.println("Badly formatted time label specification: " + labelInfo);
			labelOneIndex = -1;
		}
		repaint();
	}
	
	public void findAxisWidth() {					//		must be called AFTER findLengthInfo()
		super.findAxisWidth();
		if (labelOneIndex >= 0 && labelOneIndex < noOfVals)
			axisWidth += (kLongTickLength - kShortTickLength) + fontHeight;
	}
	
	private int noOfLabels() {
		return (noOfVals - labelOneIndex - 1) / labelPeriod + 1;
	}
	
	private NumValue getLabel(int labelIndex) {
		return new NumValue(firstValLabel + labelIndex * labelStep, 0);
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		Graphics g = getGraphics();
		setFontInfo(g);
		
		lowBorder = minLowBorder;
		highBorder = minHighBorder;
		
		if (labelOneIndex < 0 || labelOneIndex >= noOfVals) {
			axisLength = availableLength - lowBorder - highBorder;
			return;
		}
		
		int firstIndex = labelOneIndex;
		int labelCount = noOfLabels();
		int lastIndex = firstIndex + (labelCount - 1) * labelPeriod;
		NumValue firstLabel = getLabel(0);
		NumValue lastLabel = getLabel(labelCount - 1);
		
		boolean needsCheck = true;
		while (needsCheck) {
			axisLength = availableLength - lowBorder - highBorder;
			if (axisLength < kMinHorizAxisLength)
				break;
			try {
				lowBorderUsed = Math.max(0, firstLabel.stringWidth(g) / 2 - timePosition(firstIndex));
				highBorderUsed = Math.max(0, timePosition(lastIndex) + lastLabel.stringWidth(g) / 2 - axisLength);
			} catch (AxisException e) {
			}
			
			needsCheck = false;
			if (lowBorderUsed + kLeftRightGap > lowBorder) {
				lowBorder = lowBorderUsed + kLeftRightGap;
				needsCheck = true;
			}
			if (highBorderUsed + kLeftRightGap > highBorder) {
				highBorder = highBorderUsed + kLeftRightGap;
				needsCheck = true;
			}
		}
	}
	
	public void corePaint(Graphics g) {
		super.corePaint(g);
		
		if (labelOneIndex >= 0) {
			int labelCount = noOfLabels();
			int index = labelOneIndex;
			for (int i=0 ; i<labelCount ; i++) {
				drawLabelledTick(g, index, getLabel(i));
				index += labelPeriod;
			}
		}
	}
}