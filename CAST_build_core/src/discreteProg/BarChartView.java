package discreteProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class BarChartView extends HistoView {
	
	static public final int kBarIndex = 40;
	
	static final private Color kHistoFillColor = new Color(0x99CCCC);
	static final private Color kBarFillColor = new Color(0x396086);
	
	public BarChartView(DataSet theData, XApplet applet, NumCatAxis valAxis, HistoDensityInfo densityAxis,
																					double class0Start, double classWidth) {
		super(theData, applet, valAxis, densityAxis, class0Start, classWidth);
	}

//-------------------------------------------------------------------
	
	protected Color getHistoColor(int classIndex) {
		double p = getCurrentFrame() / (double)kBarIndex;
		return mixColors(kBarFillColor, kHistoFillColor, p);
	}
	
	protected void paintOneClass(Graphics g, int classIndex, BarHeight lastBarHt,
						int lastClassEnd, int previousCount,
						BarHeight thisBarHt, int thisClassEnd, int theCount,
						int maxHt, Flags selection, int[] sortedIndex, int screen0Pos) {
		int currentFrame = getCurrentFrame();
		
		if (theCount > 0) {
			int barStart = (lastClassEnd + thisClassEnd) / 2 - 1;
			int barEnd = (lastClassEnd + thisClassEnd) / 2 + 2;
			
			int classStart = lastClassEnd + ((barStart - lastClassEnd) * currentFrame) / (kBarIndex + 1);
			int classEnd = thisClassEnd - ((thisClassEnd - barEnd) * currentFrame) / (kBarIndex + 1);
			
			g.setColor(getHistoColor(classIndex));
			fillRect(classStart, classEnd, thisBarHt, findBarHt(classIndex, 0, maxHt), g);
			
			g.setColor(Color.black);
			if (!thisBarHt.tooHigh) {
				Point lineStart = translateToScreen(classStart, thisBarHt.ht, null);
				Point lineEnd = translateToScreen(classEnd, thisBarHt.ht, null);
				g.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
			}
			
			Point barTop = translateToScreen(classStart, thisBarHt.ht, null);
			g.drawLine(barTop.x, barTop.y, barTop.x, screen0Pos);
			barTop = translateToScreen(classEnd, thisBarHt.ht, barTop);
			g.drawLine(barTop.x, barTop.y, barTop.x, screen0Pos);
		}
	}
	
	protected void finishFinalBar(Graphics g, BarHeight lastBarHt, int lastClassEnd) {
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
	public void doAnimation(XSlider controller) {
		animateFrames(1, kBarIndex - 1, 4, controller);
	}
	
}
	
