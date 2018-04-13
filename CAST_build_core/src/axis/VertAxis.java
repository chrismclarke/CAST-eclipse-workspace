package axis;

import java.awt.*;
import java.util.*;

import dataView.*;


public class VertAxis extends NumCatAxis {
	static final protected int kTopBottomGap = 5;
	static final protected int kTickLength = 4;
	static final protected int kLeftBorder = 4;
	static final protected int kTextTickGap = 2;
	static final protected int kMinVertAxisLength = 100;
	
	public VertAxis(XApplet applet) {
		super(applet);
	}
	
	public void findAxisWidth() {
		if (labels.isEmpty()) {
			axisWidth = showUnlabelledAxis ? (1 + kLeftBorder) : 0;
			return;
		}
		
		findLabelSizes();
		int maxLabelWidth = 0;
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			if (theLabel.labelWidth > maxLabelWidth)
				maxLabelWidth = theLabel.labelWidth;
		}
		axisWidth = kTickLength + 1 + maxLabelWidth + kLeftBorder;
	}
	
	protected int getTopExtraHeight() {				//	to allow transform to be shown
		return 0;
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		lowBorder = minLowBorder;
		highBorder = minHighBorder;
		
		if (labels.isEmpty()) {
			axisLength = availableLength - lowBorder - highBorder;
			return;
		}
				
		boolean needsCheck = true;
		while (needsCheck) {
			axisLength = availableLength - lowBorder - highBorder;
			if (axisLength < kMinVertAxisLength)
				break;
			
			lowBorderUsed = 0;
			highBorderUsed = 0;
			Enumeration e = labels.elements();
			int halfFontHeight = (ascent + descent) / 2;
			while (e.hasMoreElements()) {
				AxisLabel theLabel = (AxisLabel)e.nextElement();
				if (theLabel.position >= 0 && theLabel.position <= 1) {
					lowBorderUsed = Math.max(lowBorderUsed, halfFontHeight
															- (int)Math.round(axisLength * theLabel.position));
					highBorderUsed = Math.max(highBorderUsed, (int)Math.round(axisLength * theLabel.position)
															+ halfFontHeight - axisLength);
				}
			}
			highBorderUsed += getTopExtraHeight();
			
			needsCheck = false;
			if (lowBorderUsed + kTopBottomGap > lowBorder) {
				lowBorder = lowBorderUsed + kTopBottomGap;
				needsCheck = true;
			}
			if (highBorderUsed + kTopBottomGap > highBorder) {
				highBorder = highBorderUsed + kTopBottomGap;
				needsCheck = true;
			}
		}
	}
	
	static final private int kSmallestPos = -9999;			//	somewhere below the axis
	static final private int kMinSpaceBetweenLabels = 2;	//	labels are not drawn unless this far apart
	
	public void corePaint(Graphics g) {
		if (labels.isEmpty() && !showUnlabelledAxis)
			return;
		
		int axisStartPos = highBorderUsed;
		int axisEndPos = axisStartPos + axisLength - 1;
		int axisHorizPos = normalSide ? (axisWidth - 1) : 0;
		g.drawLine(axisHorizPos, axisStartPos, axisHorizPos, axisEndPos);
		
		int textBaselineOffset = (ascent - descent) / 2;
		
		int tickEndHorizPos = normalSide ? (axisWidth - kTickLength - 1) : kTickLength;
		int labelEndHorizPos = normalSide ? (tickEndHorizPos - kTextTickGap) : (tickEndHorizPos + kTextTickGap);
		int lastLabelTop = kSmallestPos;
		
		Enumeration e = labels.elements();
		while (e.hasMoreElements()) {
			AxisLabel theLabel = (AxisLabel)e.nextElement();
			try {
				int axisPos = axisPosition(theLabel.position);
				int vertPos = axisEndPos - axisPos;
				g.drawLine(axisHorizPos, vertPos, tickEndHorizPos, vertPos);
				if (axisPos - (ascent + descent) / 2 - kMinSpaceBetweenLabels > lastLabelTop) {
					if (normalSide)
						theLabel.label.drawLeft(g, labelEndHorizPos, vertPos + textBaselineOffset);
					else
						theLabel.label.drawRight(g, labelEndHorizPos, vertPos + textBaselineOffset);
					lastLabelTop = axisPos + (ascent + descent) / 2;
				}
			} catch (AxisException ex) {
			}
		}
	}
	
	protected int getAxisPosition(int xPos, int yPos) {
		return getSize().height - 1 - yPos;
	}
}