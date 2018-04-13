package axis;

import java.awt.*;

import dataView.*;


abstract public class Axis extends BufferedCanvas {
	protected int fontHeight, ascent, descent;
	protected boolean normalSide;
	protected boolean showUnlabelledAxis = true;
	
	public int lowBorder, highBorder;
								//	numbers of pixels to edge of panel at each end of axis
	public int lowBorderUsed, highBorderUsed;
								//	numbers of pixels actually used for labels at each end of axis
	public int axisLength = 0;
								//	the axis will be created with size lowBorderUsed + highBorderUsed + axisLength
	public int axisWidth;
	
	protected LabelValue axisName = null;
	
	public Axis(XApplet applet) {
		super(applet);
		normalSide = true;
		repaint();
	}
	
	abstract public void findLengthInfo(int availableLength, int minLowBorder, int minHighBorder);
	abstract public void findAxisWidth();		//	for horiz axis, must be called AFTER findLengthInfo()
	
	
	public void setSide(boolean isNormalSide) {
		normalSide = isNormalSide;
	}
	
	public int getAxisLength() {
		return axisLength;
	}
	
	public int getAxisWidth() {
		return axisWidth;
	}
	
	public void setAxisName(String name) {
		axisName = new LabelValue(name);
	}
	
	public void setShowUnlabelledAxis(boolean showUnlabelledAxis) {
		this.showUnlabelledAxis = showUnlabelledAxis;
	}
	
	protected void setFontInfo(Graphics g) {
		fontHeight = g.getFontMetrics().getHeight();
		ascent = g.getFontMetrics().getAscent();
		descent = g.getFontMetrics().getDescent();
	}
	
	public int axisPosition(double fraction) throws AxisException {
		int thePosition = (int)Math.round((axisLength - 1) * fraction);
		if (thePosition < 0)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (thePosition > axisLength)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else
			return thePosition;
	}
	
	public void setBordersAndLength(int lowBorder, int highBorder, int axisLength) {
													//	this is only available to allow scatterplot matrix to force
													//	axis position after graphs have been positioned
		this.lowBorder = lowBorderUsed = lowBorder;
		this.highBorder = highBorderUsed = highBorder;
		this.axisLength = axisLength;
	}
	
//---------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
	}
}