package time;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import random.RandomBits;


public class TimeJitterView extends TimeView {
//	static public final String TIME_JITTER_PLOT = "timeJitterPlot";
	
	static public final int kEndFrame = 50;
	static private final int kHorizJitter = 15;
	static private final int kJitterOffset = 20;		//		since points should not be hard against vert axis
	
	private boolean showLines = false;
	protected int jittering[] = null;
	
	public TimeJitterView(DataSet theData, XApplet applet, TimeAxis timeAxis, VertAxis numAxis) {
		super(theData, applet, timeAxis, numAxis);
	}
	
	protected Point getScreenPoint(int index, double theVal, Point thePoint) {
		Point result = super.getScreenPoint(index, theVal, thePoint);
		if (result != null) {
			int jitteredHoriz = ((kHorizJitter * jittering[index]) >> 14) + kJitterOffset;
			result.x = (jitteredHoriz * (kEndFrame - getCurrentFrame()) + result.x * getCurrentFrame()) / kEndFrame;
		}
		return result;
	}
	
	protected void drawDotPlot(Graphics g, NumVariable variable) {
		g.setColor(Color.black);
		checkJittering();
		
		ValueEnumeration e = variable.values();
		Point thePoint = null;
		int index = 0;
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null)
				drawCross(g, thePoint);
			index++;
		}
	}
	
	public void setShowLines(boolean showLines) {
		if (showLines != this.showLines) {
			this.showLines = showLines;
			repaint();
		}
	}
	
	public boolean getShowLines() {
		return showLines;
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		
		if (showLines) {			//		will only join data points
			setSmoothedVariable(getData().getKey(variable));
			drawSmoothed(g, 0);
		}
		drawDotPlot(g, variable);
		
		getTimeAxis().show(getCurrentFrame() == kEndFrame);
	}

//-----------------------------------------------------------------------------------
	
	protected void checkJittering() {
		int dataLength = getNumVariable().noOfValues();
		if (jittering == null || jittering.length != dataLength) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public void doOrderingAnimation(XSlider controller) {
		animateFrames(1, kEndFrame - 1, 10, controller);
	}
}
	
