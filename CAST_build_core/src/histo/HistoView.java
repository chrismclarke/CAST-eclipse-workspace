package histo;

import java.awt.*;

import dataView.*;
import axis.*;


public class HistoView extends MarginalDataView {
//	static public final String HISTOGRAM = "histo";
	
	static public final int NO_BARS = 0;
	static public final int VERT_BARS = 1;
	static public final int HORIZ_BARS = 2;
	static public final int BOTH_BARS = 3;
	
	static public final int kMinHistoWidth = 40;
	
	private HistoDensityInfo densityAxis;
	private double class0Start, classWidth;
	
	protected double classStart[];
	protected int classCount[];
	protected int tooLowCount;
	protected int noOfVals;
	protected boolean checkedMaxDensity = false;
	
	private int barType = NO_BARS;
	
	public HistoView(DataSet theData, XApplet applet, NumCatAxis valAxis,
													HistoDensityInfo densityAxis, double class0Start, double classWidth) {
		super(theData, applet, new Insets(0, 0, 0, 0), valAxis);
																//		no border under histo
		this.densityAxis = densityAxis;
		this.class0Start = class0Start;
		this.classWidth = classWidth;
	}

//-------------------------------------------------------------------
	
	protected void initialise() {
		if (classStart == null)
			classStart = initialiseClasses();
		if (classCount == null)
			classCount = countClasses(classStart);
		if (!checkedMaxDensity)
			checkMaxDensity();
	}
	
	protected double[] initialiseClasses() {
		int noOfClasses = (int)Math.round((axis.maxOnAxis - class0Start + classWidth / 1000.0) / classWidth - 0.5);
																		//	allow a little slop at end of axis
		double localClassStart[] = new double[noOfClasses + 1];
		localClassStart[0] = class0Start;
		for (int i=0 ; i< noOfClasses ; i++)
			localClassStart[i+1] = Math.min(localClassStart[i] + classWidth, axis.maxOnAxis);
		return localClassStart;
	}
	
	protected int[] countClasses(double localClassStart[]) {
		int noOfClasses = localClassStart.length - 1;
		int localClassCount[] = new int[noOfClasses];
		
		NumVariable theVariable = getNumVariable();
		NumValue sortedVals[] = theVariable.getSortedData();
		noOfVals = sortedVals.length;
		int index = 0;
		while (index < noOfVals && sortedVals[index].toDouble() < localClassStart[0])
			index++;
		tooLowCount = index;
		
		int classIndex = 0;
		while (index < noOfVals) {
			if (sortedVals[index].toDouble() <= localClassStart[classIndex + 1]) {
				localClassCount[classIndex]++;
				index++;
			}
			else {
				classIndex++;
				if (classIndex >= noOfClasses)
					break;
			}
		}
		return localClassCount;
	}
	
	protected void checkMaxDensity() {
		double maxDensity = 0.0;
		for (int i=0 ; i< classCount.length ; i++) {
			double density = classCount[i] / (classStart[i+1] - classStart[i]) / noOfVals;
			if (density > maxDensity)
				maxDensity = density;
		}
		if (maxDensity == 0.0)
			maxDensity = 1.0;
		if (densityAxis.changeMaximumDensity(maxDensity, getDisplayWidth()))
			repaint();
		checkedMaxDensity = true;
	}

//-------------------------------------------------------------------
	
	private int findClassStart(int classIndex) {
		try {
			return axis.numValToPosition(classStart[classIndex]);
		} catch (AxisException e) {
		}
		return 0;
	}
	
	protected BarHeight findBarHt(int classIndex, int theCount, int maxHt) {
		if (theCount == 0)
			return new BarHeight(0, false);
		try {
			return new BarHeight(densityAxis.densityToPosition(theCount
							/ (classStart[classIndex + 1] - classStart[classIndex]) / noOfVals), false);
		} catch (AxisException e) {
			return new BarHeight(maxHt, true);
		}
	}
	
	protected void fillRect(int horiz0, int horiz1, BarHeight ht0, BarHeight ht1,
																									Graphics g) {
		Point rectTopLeft = translateToScreen(horiz0, ht0.ht, null);
		Point rectBottomRight = translateToScreen(horiz1, ht1.ht, null);
		if (rectTopLeft.x > rectBottomRight.x) {
			int temp = rectTopLeft.x;
			rectTopLeft.x = rectBottomRight.x;
			rectBottomRight.x = temp;
		}
		if (rectTopLeft.y > rectBottomRight.y) {
			int temp = rectTopLeft.y;
			rectTopLeft.y = rectBottomRight.y;
			rectBottomRight.y = temp;
		}
		int startX = rectTopLeft.x;
		int startY = rectTopLeft.y;
		if (!vertNotHoriz) {
			startX++;
			startY++;
		}
		g.fillRect(startX, startY, (rectBottomRight.x - rectTopLeft.x),
																(rectBottomRight.y - rectTopLeft.y));
											//	one extra to height to avoid white line under histo
	}
	
	protected Color getHistoColor(int classIndex) {
		return Color.lightGray;
	}
	
	protected Color getHiliteColor() {
		return Color.darkGray;
	}
	
	protected void paintOneClass(Graphics g, int classIndex, BarHeight lastBarHt,
						int lastClassEnd, int previousCount, BarHeight thisBarHt, int thisClassEnd,
						int theCount, int maxHt, Flags selection, int[] sortedIndex, int screen0Pos) {
		if (theCount > 0) {
			g.setColor(getHistoColor(classIndex));
			fillRect(lastClassEnd, thisClassEnd, thisBarHt, findBarHt(classIndex, 0, maxHt), g);
			
			g.setColor(getHiliteColor());
			for (int i=0 ; i<theCount ; i++)
				if (selection.valueAt(sortedIndex[previousCount + i])) {
					BarHeight bottomHt = findBarHt(classIndex, i, maxHt);
					BarHeight topHt = findBarHt(classIndex, i+1, maxHt);
					fillRect(lastClassEnd, thisClassEnd, topHt, bottomHt, g);
				}
			
			g.setColor(Color.black);
			if ((barType & HORIZ_BARS) != 0 && theCount > 1)
				for (int i=1 ; i<theCount ; i++) {
					BarHeight topHt = findBarHt(classIndex, i, maxHt);
					Point lineStart = translateToScreen(lastClassEnd, topHt.ht, null);
					Point lineEnd = translateToScreen(thisClassEnd, topHt.ht, null);
					g.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
				}
			
			if (!thisBarHt.tooHigh) {
				Point lineStart = translateToScreen(lastClassEnd, thisBarHt.ht, null);
				Point lineEnd = translateToScreen(thisClassEnd, thisBarHt.ht, null);
				g.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y);
			}
		}
		
		if ((barType & VERT_BARS) != 0) {
			int maxBarHt = Math.max(thisBarHt.ht, lastBarHt.ht);
			if (maxBarHt != 0) {
				Point barBottom = translateToScreen(lastClassEnd, 0, null);
				Point barTop = translateToScreen(lastClassEnd, maxBarHt, null);
				g.drawLine(barTop.x, barTop.y, barBottom.x, barBottom.y);
			}
		}
		else if (lastBarHt.ht != thisBarHt.ht && (!thisBarHt.tooHigh || !lastBarHt.tooHigh)) {
			Point start = translateToScreen(lastClassEnd, lastBarHt.ht, null);
			Point end = translateToScreen(lastClassEnd, thisBarHt.ht, null);
			g.drawLine(start.x, start.y, end.x, end.y);
		}
	}
	
	protected void finishFinalBar(Graphics g, BarHeight lastBarHt, int lastClassEnd) {
		if (lastBarHt.ht > 0) {
			Point lastBarEnd = translateToScreen(lastClassEnd, 0, null);
			Point lastBarStart = translateToScreen(lastClassEnd, lastBarHt.ht, null);
			g.drawLine(lastBarStart.x, lastBarStart.y, lastBarEnd.x, lastBarEnd.y);
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		NumVariable theVariable = getNumVariable();
		int sortedIndex[] = theVariable.getSortedIndex();
		Flags selection = getSelection();
		
		int maxHt = getDisplayWidth();
		
		int previousCount = tooLowCount;
		BarHeight lastBarHt = new BarHeight(0, false);
		int lastClassEnd = findClassStart(0);
		
		int thisClassEnd = 0;
		BarHeight thisBarHt;
		int screen0Pos = translateToScreen(0, 0, null).y;
		
		for (int classIndex = 0 ; classIndex < classCount.length ; classIndex++) {
			int theCount = classCount[classIndex];
			thisClassEnd = findClassStart(classIndex + 1);
			
			thisBarHt = findBarHt(classIndex, theCount, maxHt);
			
			paintOneClass(g, classIndex, lastBarHt, lastClassEnd, previousCount,
					thisBarHt, thisClassEnd, theCount, maxHt, selection, sortedIndex, screen0Pos);
			
			lastBarHt = thisBarHt;
			lastClassEnd = thisClassEnd;
			previousCount += theCount;
		}
		finishFinalBar(g, lastBarHt, lastClassEnd);
	}
	
	public void setBarType(int barType) {
		this.barType = barType;
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	protected void doTransformView(Graphics g, NumCatAxis theAxis) {
		if (axis == theAxis) {
			reinitialiseAfterTransform();
			repaint();
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal = 0.0;
		try {
			hitVal = axis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			return null;
		}
		
		if (hitVal <= classStart[0])
			return null;
		int countSoFar = tooLowCount;
		for (int i=0 ; i<classCount.length ; i++) {
			if (hitVal <= classStart[i+1]) {
				int indexInColumn = 0;
				try {
					indexInColumn = (int)(densityAxis.positionToDensity(hitPos.y)
													* (classStart[i+1] - classStart[i]) * noOfVals);
				} catch (AxisException e) {
					break;
				}
				if (indexInColumn < 0 || indexInColumn >= classCount[i])
					break;
				NumVariable theVariable = getNumVariable();
				int index = theVariable.rankToIndex(countSoFar + indexInColumn);
				return new IndexPosInfo(index);
			}
			countSoFar += classCount[i];
		}
		return null;
	}

//-----------------------------------------------------------------------------------

	public int minDisplayWidth() {
		return kMinHistoWidth;
	}
	
	protected void doChangeVariable(Graphics g, String key) {
		classStart = null;
		classCount = null;
		checkedMaxDensity = false;
		repaint();
	}
	
}
	
