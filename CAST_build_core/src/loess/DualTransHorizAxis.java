package loess;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class DualTransHorizAxis extends HorizAxis implements TransAxisInterface {
	static final private int kNameGap = 5;
	
	private int axisLabelOverlap;		//		offset before label can be drawn
	
	private TransAxisLabelInfo labelInfo;
	private NumValue maxHighVal, maxLowVal;
	
	public DualTransHorizAxis(XApplet applet, String transformStepString) {
		super(applet);
		canStagger = false;
		
		labelInfo = new TransAxisLabelInfo(transformStepString);
	}
	
	public void setMaxTransformed(String maxTransformedString) {
		StringTokenizer st = new StringTokenizer(maxTransformedString);
		maxLowVal = new NumValue(st.nextToken());
		maxHighVal = new NumValue(st.nextToken());
	}
	
	public void findAxisWidth() {
		LabelValue tempName = axisName;
		axisName = null;
		super.findAxisWidth();
		axisName = tempName;
		
		axisWidth *= 2;
	}
	
	public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder) {
		Graphics g = getGraphics();
		int maxTransformLength = maxNamePowerWidth(g);
		
		int maxLowOverlap = maxLowVal.stringWidth(g) / 2;
		int maxHighOverlap = maxHighVal.stringWidth(g) / 2;
		
		minHighBorder = Math.max(minHighBorder, maxHighOverlap + kNameGap + maxTransformLength);
		minLowBorder = Math.max(minLowBorder, maxLowOverlap);
		
		super.findLengthInfo(availableLength, minLowBorder, minHighBorder);
		axisLabelOverlap = Math.max(highBorderUsed, maxHighOverlap + kNameGap);
		lowBorderUsed = Math.max(lowBorderUsed, maxLowOverlap);
		highBorderUsed = Math.max(highBorderUsed, maxHighOverlap + kNameGap
																							+ maxTransformLength);
	}
	
	public void corePaint(Graphics g) {
		if (canDrag()) {
			int transPos = lowBorderUsed + findDragPos();
			if (selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(transPos - 2, 0, 5, getSize().height - valHeight);
			}
			g.setColor(Color.red);
			g.drawLine(transPos, 0, transPos, getSize().height - valHeight - 1);
			
			g.setColor(getForeground());
		}
		
		LabelValue tempName = axisName;
		axisName = null;
		super.corePaint(g);
		axisName = tempName;
		
		int labelStartHoriz = lowBorderUsed + axisLength + 1 + axisLabelOverlap;
		int labelStartVert = ascent + 2;
		axisName.drawRight(g, labelStartHoriz, labelStartVert);
		
		g.translate(0, axisWidth / 2);
		drawTransformedAxis(g);
		
		drawNamePower(g, labelStartHoriz, labelStartVert, LEFT_ALIGN, maxNamePowerWidth(g));
		g.translate(0, -axisWidth / 2);
		
	}
	
	private void drawTransformedAxis(Graphics g) {
		int axisStartPos = lowBorderUsed;
		int axisEndPos = axisStartPos + axisLength - 1;
		g.drawLine(axisStartPos, 0, axisEndPos, 0);
		
		TransAxisLabelEnumeration le = labelInfo.getLabelEnumeration(minPower, maxPower,
																								getPowerIndex());
		
		while (le.hasMoreLabels()) {
			AxisLabel theLabel = le.nextLabel();
			
			try {
				int horizPos = lowBorderUsed + axisPosition(theLabel.position);
				g.drawLine(horizPos, 0, horizPos, kTickLength);
				theLabel.label.drawCentred(g, horizPos, kTickLength + ascent + 1);
			} catch (AxisException ex) {
			}
		}
	}
}