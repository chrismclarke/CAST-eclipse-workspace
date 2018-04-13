package dotPlot;

import java.awt.*;

import dataView.*;
import axis.*;


public class DotPlotXView extends StackingDotPlotView {
//	static public final String DOTPLOT_X = "dotPlotX";
	static public final int BASIC = 0;
	static public final int JITTERED = 1;
	static public final int STACKED = 2;
	
	static private final int kNoOfTransitions = 30;
	
	private int startState = BASIC;
	private int endState = JITTERED;
	
	public DotPlotXView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		super(theData, applet, theAxis, 1.0);
	}
	
	private Point corePoint(int state, int index) {
		switch (state) {
			case BASIC:
				return new Point(initAxisPos[index], 0);
			case JITTERED:
				int vertJitterPos = (currentJitter * jittering[index]) >> 14;
				return new Point(initAxisPos[index], vertJitterPos);
			case STACKED:
				return new Point(initAxisPos[index], groupVert[index]);
//				return new Point(groupHoriz[index], groupVert[index]);
			default:
				return null;
		}
	}
	
	protected Point getScreenPoint(int index, int currentFrame, Point thePoint) {
		if (initAxisPos[index] == kOffAxisPos)
			return null;
		
		Point startPoint = corePoint(startState, index);
		Point endPoint = corePoint(endState, index);
		
		int horizPos = ((kNoOfTransitions - currentFrame) * startPoint.x + currentFrame * endPoint.x)
																									/ kNoOfTransitions;
		int vertPos = ((kNoOfTransitions - currentFrame) * startPoint.y + currentFrame * endPoint.y)
																									/ kNoOfTransitions;
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected void paintGrid(Graphics g, int currentFrame) {
	}
	
	public void animateTo(int endState) {
		if (axisPosInitialised()) {			//	Don't try animation until painted once
			if (getCurrentFrame() == kNoOfTransitions)
				startState = this.endState;
			this.endState = endState;
			if (startState != endState)
				animateFrames(0, kNoOfTransitions, 15, null);
		}
	}
}