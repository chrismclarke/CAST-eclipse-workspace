package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import exercise2.*;


public class BoxplotDragView extends CoreDragView implements BoxPlotConstants {
//	static public final String BOXPLOT_DRAG = "boxplotDrag";
	
	static final private int kMaxHitSlop = 4;
	static final private int kArrowBorder = 18;
	static final private int kArrowLength = 6;
	
	static final private Color kBoxMarkerColor = new Color(0x999999);
	
	private BoxInfo boxInfo = new BoxInfo();
	
	public BoxplotDragView(DataSet theData, XApplet applet, NumCatAxis valAxis, String yKey) {
		super(theData, applet, valAxis, yKey);
		
		boxInfo.setFillColor(Color.lightGray);
		boxInfo.setBoxHeight(24);
	}

//-------------------------------------------------------------------

	public String getStatus() {
		initialise();
		String s = "";
		for (int i=0 ; i<classStart.length ; i++)
			s += classStart[i] + " ";
		return s;
	}
	
	public void setStatus(String status) {
		initialise();
		StringTokenizer st = new StringTokenizer(status);
		for (int i=0 ; i<classStart.length ; i++)
			classStart[i] = Double.parseDouble(st.nextToken());
		
		for (int i=0 ; i<5 ; i++)
			boxInfo.boxVal[i] = classStart[2 * i];
		
		repaint();
	}

//-------------------------------------------------------------------
	
	public void setMeanSD(double mean, double sd) {
		double b = sd / 1.23356;
		
		classCount[0] = classCount[7] = 0.3 * 0.25;
		classCount[1] = classCount[6] = 0.7 * 0.25;
		classCount[2] = classCount[5] = 0.47 * 0.25;
		classCount[3] = classCount[4] = 0.53 * 0.25;
		
		classStart[0] = mean - 2.5 * b;
		classStart[8] = mean + 2.5 * b;
		classStart[1] = mean - 1.75 * b;
		classStart[7] = mean + 1.75 * b;
		classStart[2] = mean - b;
		classStart[6] = mean + b;
		classStart[3] = mean - 0.5 * b;
		classStart[5] = mean + 0.5 * b;
		classStart[4] = mean;
		
		for (int i=0 ; i<5 ; i++)
			boxInfo.boxVal[i] = classStart[2 * i];
	}
	
	protected void doInitialisation() {
		super.doInitialisation();
		classCount[0] += tooLowCount;		//	in case extreme values have been rounded out of outer classes
		classCount[3] += tooHighCount;
	}
	
	protected double[] initialiseClasses() {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		NumValue sortedValues[] = yVar.getSortedData();
		boxInfo.initialiseBox(sortedValues, false, axis);
		
						//		Split each section into two, then partition the total count according to
						//		expected for normal distn
		double localClassStart[] = new double[9];
		localClassStart[0] = boxInfo.boxVal[LOW_EXT];
		localClassStart[2] = boxInfo.boxVal[LOW_QUART];
		localClassStart[1] = (localClassStart[0] + localClassStart[2]) * 0.5;
		
		localClassStart[4] = boxInfo.boxVal[MEDIAN];
		localClassStart[3] = (localClassStart[2] + localClassStart[4]) * 0.5;
		
		localClassStart[6] = boxInfo.boxVal[HIGH_QUART];
		localClassStart[5] = (localClassStart[4] + localClassStart[6]) * 0.5;
		
		localClassStart[8] = boxInfo.boxVal[HIGH_EXT];
		localClassStart[7] = (localClassStart[6] + localClassStart[8]) * 0.5;
		
		return localClassStart;
	}
	
	protected double[] defaultClasses() {
		double localClassStart[] = new double[9];
		
		double center = (axis.maxOnAxis + axis.minOnAxis) * 0.5;
		double halfDataRange = (axis.maxOnAxis - axis.minOnAxis) * 0.35;
		
		localClassStart[4] = center;
		for (int i=1 ; i<=4 ; i++) {
			localClassStart[4 - i] = center - halfDataRange * i * 0.25;
			localClassStart[4 + i] = center + halfDataRange * i * 0.25;
		}
		
		for (int i=0 ; i<5 ; i++)
			boxInfo.boxVal[i] = localClassStart[2 * i];
		
		return localClassStart;
	}
	
	protected double[] defaultCounts(double localClassStart[]) {
		double localClassCount[] = new double[8];
		
		localClassCount[0] = localClassCount[7] = 0.25 * 0.3;
		localClassCount[1] = localClassCount[6] = 0.25 * 0.7;
		localClassCount[2] = localClassCount[5] = 0.25 * 0.47;
		localClassCount[3] = localClassCount[4] = 0.25 * 0.53;
		
		tooLowCount = tooHighCount = 0;
		return localClassCount;
	}
	
	protected double[] countClasses(double localClassStart[]) {
		double localClassCount[] = super.countClasses(localClassStart);
		
		double total = localClassCount[0] + localClassCount[1];
		localClassCount[0] = total * 0.3;
		localClassCount[1] = total * 0.7;
		
		total = localClassCount[2] + localClassCount[3];
		localClassCount[2] = total * 0.53;
		localClassCount[3] = total * 0.47;
		
		total = localClassCount[4] + localClassCount[5];
		localClassCount[4] = total * 0.47;
		localClassCount[5] = total * 0.53;
		
		total = localClassCount[6] + localClassCount[7];
		localClassCount[6] = total * 0.7;
		localClassCount[7] = total * 0.3;
		
		return localClassCount;
	}
	
/*
	public void setMessages(String[] messageArray, NumValue exactAnswer) {
		messageArray[2] = "Good! That is a reasonable estimate of the standard deviation.";
		messageArray[3] = "Not close enough. From the 70-95-100 rule, you should expect most of a small or medium-sized data set be within 2s of the mean (suggesting the range is about 4s) and about 70% to be within 1s of the mean (a bit wider than the central box).";
		messageArray[4] = "A good estimate of standard deviation for this box plot is " + exactAnswer.toString();
	}
*/

//-------------------------------------------------------------------
	
	protected int topBorder(Graphics g) {
		return super.topBorder(g) + (allowDrag ? kArrowBorder : 0);
	}
	
	public void paintView(Graphics g) {
		initialise();
		drawTopBorder(g);
		
		boxInfo.setupBoxPositions(axis);		//	needed because initialise() may have been called
																				//	before AxisLayout has given axis a length
		
		int availableHt = getDisplayWidth() - topBorder(g);
		
		if (allowDrag) {
//			int topSpace = topBorder(g);
			Point p = null;
			
			if (dragIndex >= 0) {
				g.setColor(Color.red);
				p = translateToScreen(boxInfo.boxPos[dragIndex], availableHt + kArrowBorder, p);
				g.drawLine(p.x, p.y, p.x, getSize().height);
				
//				int arrowVert = p.y + 4 + ((dragIndex+1) % 2) * 7;
				int arrowVert = getSize().height - 6 - ((dragIndex+1) % 2) * 7;
				g.drawLine(p.x - kArrowLength, arrowVert, p.x + kArrowLength, arrowVert);
				g.drawLine(p.x - kArrowLength + 1, arrowVert - 1, p.x + kArrowLength - 1, arrowVert - 1);
				g.drawLine(p.x - kArrowLength + 1, arrowVert + 1, p.x + kArrowLength - 1, arrowVert + 1);
				for (int i=2 ; i<4 ; i++) {
					g.drawLine(p.x - kArrowLength + i, arrowVert - i, p.x - kArrowLength + i, arrowVert + i);
					g.drawLine(p.x + kArrowLength - i, arrowVert - i, p.x + kArrowLength - i, arrowVert + i);
				}
			}
			else {
				for (int i=0 ; i<5 ; i++) {
					p = translateToScreen(boxInfo.boxPos[i], availableHt + kArrowBorder, p);
					g.setColor(kBoxMarkerColor);
					g.drawLine(p.x, p.y, p.x, getSize().height);
					
//					int arrowVert = p.y + 6 + ((i+1) % 2) * 6;
					int arrowVert = getSize().height - 6 - ((i+1) % 2) * 6;
					g.setColor(Color.red);
					g.drawLine(p.x - kArrowLength, arrowVert, p.x + kArrowLength, arrowVert);
					g.drawLine(p.x - kArrowLength, arrowVert, p.x - kArrowLength + 3, arrowVert - 3);
					g.drawLine(p.x - kArrowLength, arrowVert, p.x - kArrowLength + 3, arrowVert + 3);
					g.drawLine(p.x + kArrowLength, arrowVert, p.x + kArrowLength - 3, arrowVert - 3);
					g.drawLine(p.x + kArrowLength, arrowVert, p.x + kArrowLength - 3, arrowVert + 3);
				}
			}
		}
		
		boxInfo.vertMidLine = availableHt / 2;
		boxInfo.boxBottom = boxInfo.vertMidLine - boxInfo.getBoxHeight() / 2;
		
		g.setColor(getForeground());
		NumValue[] sortedData = null;
		if (yKey != null)
			sortedData = ((NumVariable)getVariable(yKey)).getSortedData();
		
		boxInfo.drawBoxPlot(g, this, sortedData, axis);
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		
		int nearestIndex = -1;
		int minDist = Integer.MAX_VALUE;
		
		for (int i=0 ; i<5 ; i++) {
			int dist = p.x - boxInfo.boxPos[i];
			if (Math.abs(dist) < Math.abs(minDist)) {
				nearestIndex = i;
				minDist = dist;
			}
		}
		
		if (Math.abs(minDist) <= kMaxHitSlop) 
			return new HorizDragPosInfo(x, nearestIndex, minDist);
		else
			return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		return new HorizDragPosInfo(x);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		HorizDragPosInfo dragPos = (HorizDragPosInfo)startInfo;
		dragIndex = dragPos.index;
		hitOffset = dragPos.hitOffset;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			HorizDragPosInfo dragPos = (HorizDragPosInfo)toPos;
			Point p = translateFromScreen(dragPos.x, 0, null);
			
			try {
				double x = axis.positionToNumVal(p.x);
				
				double lowLimit = (dragIndex == 0) ? Double.NEGATIVE_INFINITY
																													: boxInfo.boxVal[dragIndex - 1];
				double highLimit = (dragIndex == 4) ? Double.POSITIVE_INFINITY
																													: boxInfo.boxVal[dragIndex + 1];
				
				if (x > lowLimit && x < highLimit) {
					boxInfo.boxVal[dragIndex] = x;
					classStart[2 * dragIndex] = x;
					if (dragIndex > 0)
						classStart[2 * dragIndex - 1] = (classStart[2 * dragIndex]
																									+ classStart[2 * dragIndex - 2]) * 0.5;
					
					if (dragIndex < 4)
						classStart[2 * dragIndex + 1] = (classStart[2 * dragIndex]
																									+ classStart[2 * dragIndex + 2]) * 0.5;
					repaint();
				}
				
			} catch (AxisException e) {
			}
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		dragIndex = -1;
		repaint();
	}
	
	
}
	
