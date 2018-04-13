package axis;

import java.awt.*;
import java.util.*;

import dataView.*;


public class HorizAxis extends NumCatAxis {
	static final private int kExtraUnderName = 5;
	
	static final int kLeftRightGap = 5;
	static final int kMinLabelGap = 15;
	static final protected int kTickLength = 4;
	static final int kBottomBorder = 4;
	static final int kMinHorizAxisLength = 100;
	
	protected boolean staggered;
	protected boolean canStagger = true;
	protected boolean alwayDrawLabels = false;
	
	private boolean centerAxisName = false;
	
	public HorizAxis(XApplet applet) {
		super(applet);
	}
	
	public void setCenterAxisName(boolean centerAxisName) {
		this.centerAxisName = centerAxisName;
	}
	
	public void findAxisWidth() {					//		must be called AFTER findLengthInfo()
		if (labels.isEmpty()) {
			axisWidth = 1 + kBottomBorder;
			if (axisName != null)
				axisWidth += (ascent + descent + kExtraUnderName);
			return;
		}
		staggered = false;
		Enumeration e = labels.elements();
		AxisLabel label2 = (AxisLabel)e.nextElement();
		int label2Pos = (int)Math.round(axisLength * label2.position);
		int label2Width = label2.labelWidth;
		while (e.hasMoreElements()) {
			int label1Pos = label2Pos;
			int label1Width = label2Width;
			label2 = (AxisLabel)e.nextElement();
			label2Pos = (int)Math.round(axisLength * label2.position);
			label2Width = label2.labelWidth;
			
			if (canStagger && (label1Width + label2Width) / 2 + kMinLabelGap > label2Pos - label1Pos) {
				staggered = true;
				break;
			}
		}
		axisWidth = kTickLength + 1 + fontHeight + kBottomBorder;
		if (staggered)
			axisWidth += fontHeight;
		if (axisName != null)
			axisWidth += (ascent + descent + kExtraUnderName);
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		findLabelSizes();
		
		lowBorder = minLowBorder;
		highBorder = minHighBorder;
		staggered = false;
		
		if (labels.isEmpty()) {
			axisLength = availableLength - lowBorder - highBorder;
			return;
		}
		
		boolean needsCheck = true;
		while (needsCheck) {
			axisLength = availableLength - lowBorder - highBorder;
			if (axisLength < kMinHorizAxisLength)
				break;
			
			lowBorderUsed = 0;
			highBorderUsed = 0;
			Enumeration e = labels.elements();
			while (e.hasMoreElements()) {
				AxisLabel theLabel = (AxisLabel)e.nextElement();
				if (theLabel.position >= 0 && theLabel.position <= 1) {
					lowBorderUsed = Math.max(lowBorderUsed,
								theLabel.labelWidth / 2 - (int)Math.round(axisLength * theLabel.position));
					highBorderUsed = Math.max(highBorderUsed, (int)Math.round(axisLength
													* theLabel.position) + theLabel.labelWidth / 2 - axisLength);
				}
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
	
	static final private int kSmallestPos = -9999;			//	somewhere below the axis
	static final private int kMinSpaceBetweenLabels = 12;	//	labels are not drawn unless this far apart
	
	public void corePaint(Graphics g) {
		int underLabels = 0;
		if (!labels.isEmpty() || showUnlabelledAxis) {
			int axisStartPos = lowBorderUsed;
			int axisEndPos = axisStartPos + axisLength - 1;
			int axisVertPos = normalSide ? 0 : (axisWidth - 1);
			g.drawLine(axisStartPos, axisVertPos, axisEndPos, axisVertPos);
			
			if (!labels.isEmpty()) {
				int oddTickEndVertPos = normalSide ? kTickLength : (axisWidth - kTickLength - 1);
				int evenTickEndVertPos = oddTickEndVertPos + (staggered ? (normalSide ? fontHeight : (-fontHeight)) : 0);
				int oddLabelVertPos = normalSide ? (oddTickEndVertPos + ascent + 1) : (oddTickEndVertPos - descent);
				int evenLabelVertPos = oddLabelVertPos + (staggered ? (normalSide ? fontHeight : (-fontHeight)) : 0);
				
				boolean staggered = (oddLabelVertPos != evenLabelVertPos);
				int lastLabelEnd = kSmallestPos;
				
				boolean oddLabel = true;
				Enumeration e = labels.elements();
				while (e.hasMoreElements()) {
					AxisLabel theLabel = (AxisLabel)e.nextElement();
					try {
						int horizPos = lowBorderUsed + axisPosition(theLabel.position);
						if (oddLabel) {
							g.drawLine(horizPos, axisVertPos, horizPos, oddTickEndVertPos);
							if (alwayDrawLabels || staggered || horizPos - theLabel.labelWidth / 2 - kMinSpaceBetweenLabels > lastLabelEnd) {
								theLabel.label.drawCentred(g, horizPos, oddLabelVertPos);
								lastLabelEnd = horizPos + theLabel.labelWidth / 2;
							}
						}
						else {
							g.drawLine(horizPos, axisVertPos, horizPos, evenTickEndVertPos);
							if (alwayDrawLabels || staggered || horizPos - theLabel.labelWidth / 2 - kMinSpaceBetweenLabels > lastLabelEnd) {
								theLabel.label.drawCentred(g, horizPos, evenLabelVertPos);
								lastLabelEnd = horizPos + theLabel.labelWidth / 2;
							}
						}
					} catch (AxisException ex) {
					}
					oddLabel = !oddLabel;
				}
	//			underLabels = evenLabelVertPos + descent + kExtraUnderName;
				underLabels = normalSide ? (evenLabelVertPos + descent) : 0;
			}
			else
				underLabels = normalSide ? (kExtraUnderName + 1) : 0;
		}
		if (axisName != null) {
			if (centerAxisName)
				axisName.drawCentred(g, (getSize().width + lowBorderUsed) / 2,
																											underLabels + (fontHeight - descent));
			else
				axisName.drawLeft(g, getSize().width - highBorderUsed, underLabels + (fontHeight - descent));
		}
	}
	
	protected int getAxisPosition(int xPos, int yPos) {
		return xPos;
	}
}