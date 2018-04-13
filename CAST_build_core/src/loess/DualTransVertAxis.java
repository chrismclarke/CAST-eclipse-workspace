package loess;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;


public class DualTransVertAxis extends VertAxis implements TransAxisInterface {
	static final private int kNameGap = 3;
	static final private int kAxisGap = 5;
	static final private int kTopBorder = 3;
	
	private int distanceBetweenAxes;
	private int maxTransAxisName;
	
	private TransAxisLabelInfo labelInfo;
	private NumValue maxHighVal, maxLowVal;
	
	public DualTransVertAxis(XApplet applet, String transformStepString) {
		super(applet);
		
		labelInfo = new TransAxisLabelInfo(transformStepString);
	}
	
	public void setMaxTransformed(String maxTransformedString) {
		StringTokenizer st = new StringTokenizer(maxTransformedString);
		maxLowVal = new NumValue(st.nextToken());
		maxHighVal = new NumValue(st.nextToken());
	}
	
	protected int getTopExtraHeight() {
		return ascent + descent + kNameGap + kTopBorder;
	}
	
	public void findAxisWidth() {
		super.findAxisWidth();
		
		Graphics g = getGraphics();
		int nameWidth = axisName.stringWidth(g);
		axisWidth = Math.max(axisWidth, nameWidth) + kAxisGap;
		distanceBetweenAxes = axisWidth;
		
		maxTransAxisName = maxNamePowerWidth(g);
		
		int maxLabelWidth = Math.max(maxLowVal.stringWidth(g), maxHighVal.stringWidth(g));
		axisWidth += Math.max(kTickLength + 1 + maxLabelWidth, maxTransAxisName);
	}
	
	public void corePaint(Graphics g) {
		if (canDrag()) {
			int transPos = getSize().height - 1 - (lowBorderUsed + findDragPos());
			if (selectedVal) {
				g.setColor(Color.yellow);
				g.fillRect(0, transPos - 2, getSize().width, 5);
			}
			g.setColor(Color.red);
			g.drawLine(0, transPos, getSize().width - 1, transPos);
			
			g.setColor(getForeground());
		}
		
		super.corePaint(g);
		
		int labelStartVert = ascent + kTopBorder;
		axisName.drawLeft(g, axisWidth, labelStartVert);
		
		drawTransformedAxis(g);
		
		drawNamePower(g, getSize().width - distanceBetweenAxes - maxTransAxisName,
															labelStartVert, RIGHT_ALIGN, maxTransAxisName);
	}
	
	private void drawTransformedAxis(Graphics g) {
		int axisStartPos = highBorderUsed;
		int axisEndPos = axisStartPos + axisLength - 1;
		int axisHorizPos = axisWidth - distanceBetweenAxes - 1;
		g.drawLine(axisHorizPos, axisStartPos, axisHorizPos, axisEndPos);
		
		int textBaselineOffset = (ascent - descent) / 2;
		
		int tickEndHorizPos = axisHorizPos - kTickLength;
		int labelEndHorizPos = tickEndHorizPos - kTextTickGap;
		
		TransAxisLabelEnumeration le = labelInfo.getLabelEnumeration(minPower, maxPower,
																								getPowerIndex());
		while (le.hasMoreLabels()) {
			AxisLabel theLabel = le.nextLabel();
			try {
				int axisPos = axisPosition(theLabel.position);
				int vertPos = axisEndPos - axisPos;
				g.drawLine(axisHorizPos, vertPos, tickEndHorizPos, vertPos);
				theLabel.label.drawLeft(g, labelEndHorizPos, vertPos + textBaselineOffset);
			} catch (AxisException ex) {
			}
		}
	}
}