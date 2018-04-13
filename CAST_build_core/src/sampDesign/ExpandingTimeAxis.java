package sampDesign;

import java.awt.*;

import dataView.*;
import axis.*;


public class ExpandingTimeAxis extends TimeAxis {
	static final private int labelInt[] = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
//	static final private int minToPrint[] = {60, 110, 220, 600, 1100, 2200, 6000, 11000, 22000, 60000, 110000, 220000, 600000};
	static final private NumValue labelForInt[] = new NumValue[labelInt.length];
	
	static {
		for (int i=0 ; i<labelInt.length ; i++)
			labelForInt[i] = new NumValue(labelInt[i], 0);
	}
	
	private int minIndex;
	
	public ExpandingTimeAxis(XApplet applet, int minIndex, int maxIndex) {
//		super(applet, BUFFERED, minIndex + 1);
		super(applet, minIndex);
		this.minIndex = minIndex;
	}
	
	public void setNoOfValues(int noOfVals) {
//		this.noOfVals = Math.max(minIndex + 1, noOfVals);
		this.noOfVals = Math.max(minIndex, noOfVals);
		repaint();
	}
	
	public void findAxisWidth() {					//		must be called AFTER findLengthInfo()
		super.findAxisWidth();
		axisWidth += (kLongTickLength - kShortTickLength) + fontHeight;
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		Graphics g = getGraphics();
		setFontInfo(g);
		
		lowBorder = minLowBorder;
		highBorder = minHighBorder;
		
		NumValue firstLabel = labelForInt[0];
		NumValue lastLabel = labelForInt[labelForInt.length - 1];
		boolean needsCheck = true;
		while (needsCheck) {
			axisLength = availableLength - lowBorder - highBorder;
			if (axisLength < kMinHorizAxisLength)
				break;
				
			lowBorderUsed = firstLabel.stringWidth(g) / 2;
			highBorderUsed = lastLabel.stringWidth(g) / 2;
				
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
		drawAxis(g);
		int noOfLabels = 0;
		for (int i=0 ; i<labelInt.length ; i++) {
			if (labelInt[i] > noOfVals)
				break;
			noOfLabels ++;
		}
		int startLabel = Math.max(0, noOfLabels - 5);
		for (int i=startLabel ; i<labelInt.length ; i++) {
			if (labelInt[i] > noOfVals)
				break;
			drawLabelledTick(g, labelInt[i] - 1, labelForInt[i]);
		}
		drawName(g);
	}
}