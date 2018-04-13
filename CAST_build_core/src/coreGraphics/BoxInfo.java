package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;

public class BoxInfo implements BoxPlotConstants {
	
	static public int kBoxHeight = 14;				//		default
	
	public double boxVal[] = new double[5];
	public int boxPos[] = new int[5];
	int lowExtremes, highExtremes;
	
	private int boxHeight = kBoxHeight;
	public int boxBottom;
	public int vertMidLine;
	
	private Color fillColor = null;
	
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	
	public void setBoxHeight(int newBoxHeight) {
		boxHeight = newBoxHeight;
		vertMidLine = boxBottom + boxHeight / 2;
	}
	
	public int getBoxHeight() {
		return boxHeight;
	}
	
	private int getAxisPos(double x, NumCatAxis axis) {
		int horizPos = 0;
		try {
			horizPos = axis.numValToPosition(x);
		} catch (AxisException ex) {
			if (ex.axisProblem == AxisException.TOO_HIGH_ERROR)
				horizPos = axis.getAxisLength();
		}
		return horizPos;
	}
	
	public void initialiseBox(NumValue sortedVal[], boolean showOutliers, NumCatAxis axis) {
		int noOfVals = sortedVal.length;
		if ((noOfVals & 0x1) == 1)		//	odd
			boxVal[MEDIAN] = sortedVal[noOfVals / 2].toDouble();
		else
			boxVal[MEDIAN] = (sortedVal[noOfVals / 2 - 1].toDouble() + sortedVal[noOfVals / 2].toDouble()) * 0.5;
		
		int halfCount = noOfVals / 2;
		if ((halfCount & 0x1) == 1) {		//	odd
			boxVal[LOW_QUART] = sortedVal[halfCount / 2].toDouble();
			boxVal[HIGH_QUART] = sortedVal[noOfVals - 1 - halfCount / 2].toDouble();
		}
		else {
			boxVal[LOW_QUART] = (sortedVal[halfCount / 2 - 1].toDouble() + sortedVal[halfCount / 2].toDouble()) * 0.5;
			boxVal[HIGH_QUART] = (sortedVal[noOfVals - 1 - halfCount / 2].toDouble() + sortedVal[noOfVals - halfCount / 2].toDouble()) * 0.5;
		}
		
		lowExtremes = 0;
		double minWhisker = Double.NEGATIVE_INFINITY;
		if (showOutliers)
			minWhisker = 2.5 * boxVal[LOW_QUART] - 1.5 * boxVal[HIGH_QUART];
		for (int i=0 ; i<noOfVals ; i++)
			if (sortedVal[i].toDouble() >= minWhisker) {
				boxVal[LOW_EXT] = sortedVal[i].toDouble();
				break;
			}
			else
				lowExtremes++;
		
		highExtremes = 0;
		double maxWhisker = Double.POSITIVE_INFINITY;
		if (showOutliers)
			maxWhisker = 2.5 * boxVal[HIGH_QUART]
														- 1.5 * boxVal[LOW_QUART];
		for (int i=noOfVals-1 ; i>=0 ; i--)
			if (sortedVal[i].toDouble() <= maxWhisker) {
				boxVal[HIGH_EXT] = sortedVal[i].toDouble();
				break;
			}
			else
				highExtremes++;
		
		if (axis != null)
			setupBoxPositions(axis);
	}
	
	public void countOutliers(NumValue sortedVal[], NumCatAxis axis) {
										//		used by exerciseNumGraph.DragBoxPlotView when extremes are set by dragging
										//		only used when showExtremes is set
		int noOfVals = sortedVal.length;
	
		lowExtremes = noOfVals;
		highExtremes = noOfVals;
		
		int minWhiskerPos = boxPos[LOW_EXT];
		for (int i=0 ; i<noOfVals ; i++) {
			int yPos = getAxisPos(sortedVal[i].toDouble(), axis);
			if (yPos >= minWhiskerPos) {
				lowExtremes = i;
				break;
			}
		}
		
		int maxWhiskerPos = boxPos[HIGH_EXT];
		for (int i=noOfVals-1 ; i>=0 ; i--) {
			int yPos = getAxisPos(sortedVal[i].toDouble(), axis);
			if (yPos <= maxWhiskerPos) {
				highExtremes = noOfVals - i - 1;
				break;
			}
		}	
	}
	
	public void clearOutliers() {
		lowExtremes = highExtremes = 0;
	}
	
	public void setupBoxPositions(NumCatAxis axis) {
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i++)
			boxPos[i] = getAxisPos(boxVal[i], axis);
	}
	
	public void drawBoxPlot(Graphics g, DataView view, NumValue sortedVal[], NumCatAxis axis) {
		Point p1 = null;
		Point p2 = null;
		
		if (fillColor != null) {
			Color oldColor = g.getColor();
			g.setColor(fillColor);
			
			p1 = view.translateToScreen(boxPos[HIGH_QUART], boxBottom, p1);
			p2 = view.translateToScreen(boxPos[LOW_QUART], boxBottom + boxHeight, p2);
			if (p2.y > p1.y)
				g.fillRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
			else
				g.fillRect(p2.x, p2.y, (p1.x - p2.x), (p1.y - p2.y));
			
			g.setColor(oldColor);
		}
		
		for (int i=LOW_EXT ; i<=HIGH_EXT ; i+=2) {		//	low & high extremes & median
			p1 = view.translateToScreen(boxPos[i], boxBottom, p1);
			p2 = view.translateToScreen(boxPos[i], boxBottom + boxHeight, p2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		
		p1 = view.translateToScreen(boxPos[LOW_EXT], vertMidLine, p1);
		p2 = view.translateToScreen(boxPos[LOW_QUART], vertMidLine, p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p1 = view.translateToScreen(boxPos[HIGH_EXT], vertMidLine, p1);
		p2 = view.translateToScreen(boxPos[HIGH_QUART], vertMidLine, p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		
		p1 = view.translateToScreen(boxPos[LOW_QUART], boxBottom + boxHeight, p1);
		p2 = view.translateToScreen(boxPos[HIGH_QUART], boxBottom, p2);
		g.drawLine(p1.x, p1.y, p1.x, p2.y);
		g.drawLine(p1.x, p2.y, p2.x, p2.y);
		g.drawLine(p2.x, p2.y, p2.x, p1.y);
		g.drawLine(p2.x, p1.y, p1.x, p1.y);
		
		if (lowExtremes > 0 || highExtremes > 0) {
			int noOfVals = sortedVal.length;
			Point p = null;
			for (int i=0 ; i<lowExtremes ; i++)
				try {
					int horizPos = axis.numValToPosition(sortedVal[i].toDouble());
					p = view.translateToScreen(horizPos, vertMidLine, p);
					view.drawMark(g, p, 0);
				} catch (AxisException ex) {
				}
			for (int i=noOfVals-1 ; i>=noOfVals-highExtremes ; i--)
				try {
					int horizPos = axis.numValToPosition(sortedVal[i].toDouble());
					p = view.translateToScreen(horizPos, vertMidLine, p);
					view.drawMark(g, p, 0);
				} catch (AxisException ex) {
				}
		}
	}
}
