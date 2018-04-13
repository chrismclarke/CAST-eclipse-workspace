package axis;

import java.awt.*;
import dataView.*;


abstract public class TimeAxis extends Axis {
	static final private int kExtraUnderName = 5;
	static final protected int kShortTickLength = 3;
	static final protected int kLongTickLength = 6;
	static final private int kBottomBorder = 4;
	
	static final protected int kLeftRightGap = 5;
	static final protected int kMinHorizAxisLength = 100;
	
	protected int noOfVals;
	
	public TimeAxis(XApplet applet, int noOfVals) {
		super(applet);
		this.noOfVals = noOfVals;
	}
	
	public void setNoOfVals(int noOfVals) {
		this.noOfVals = noOfVals;
	}
	
	public int timePosition(int index) throws AxisException {
		return axisPosition((index + 0.5) / noOfVals);
	}
	
	public int timePositionBefore(int index) throws AxisException {	//	half-way between this and previous
		return axisPosition(((double)index) / noOfVals);
	}
	
	public int positionToIndex(int pos) throws AxisException {
		int index = (int)Math.round(((double)pos) / (axisLength - 1) * noOfVals - 0.5);
		if (index < 0)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (index >= noOfVals)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else
			return index;
	}
	
	protected void drawAxis(Graphics g) {
		int axisStartPos = lowBorderUsed;
		int axisEndPos = axisStartPos + axisLength - 1;
		int axisVertPos = normalSide ? 0 : (axisWidth - 1);
		g.drawLine(axisStartPos, axisVertPos, axisEndPos, axisVertPos);
	}
	
	protected void drawShortTick(Graphics g, int index) {
		try {
			int horiz = timePosition(index) + lowBorderUsed;
			int vert1 = normalSide ? 0 : (axisWidth - 1);
			int vert2 = normalSide ? kShortTickLength : (axisWidth - kShortTickLength - 1);
			g.drawLine(horiz, vert1, horiz, vert2);
		} catch (AxisException e) {
		}
	}
	
	protected void drawLabelledTick(Graphics g, int index, Value label) {
		try {
			int horiz = timePosition(index) + lowBorderUsed;
			int vert1 = normalSide ? 0 : (axisWidth - 1);
			int vert2 = normalSide ? kLongTickLength : (axisWidth - kLongTickLength - 1);
			g.drawLine(horiz, vert1, horiz, vert2);
			
			int baseline = normalSide ? (vert2 + ascent + 1) : (vert2 - descent);
			label.drawCentred(g, horiz, baseline);
		} catch (AxisException e) {
		}
	}
	
	protected void drawName(Graphics g) {
		if (axisName != null) {
			int vert = normalSide ? (getSize().height - kBottomBorder - descent - kExtraUnderName)
											: (kBottomBorder + kExtraUnderName + ascent);
			axisName.drawLeft(g, getSize().width - highBorderUsed, vert);
		}
	}
	
	public void findAxisWidth() {					//		must be called AFTER findLengthInfo()
		axisWidth = 1 + kShortTickLength + kBottomBorder;
		if (axisName != null)
			axisWidth += (ascent + descent + kExtraUnderName);
	}
	
	public void corePaint(Graphics g) {
		drawAxis(g);
		for (int i=0 ; i<noOfVals ; i++)
			drawShortTick(g, i);
		drawName(g);
	}
}