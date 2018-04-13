package statistic2;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class BasicHistoView extends BasicDataView {
	static final private int kDragArrow = 5;
	static final private int kMaxHitSlop = 4;
	
	static final private double kTargetEps = 1e-5;
	static final private int kMaxIterations = 5;
	
	private double class0Start, classWidth;
	
	private double maxCount;
	
	public BasicHistoView(DataSet theData, XApplet applet, NumCatAxis valAxis,
							String yKey, double class0Start, double classWidth, int meanDecimals, int sdDecimals) {
		super(theData, applet, valAxis, yKey, meanDecimals, sdDecimals);
		
		changeClasses(class0Start, classWidth);
//		setAllowDrag(true);
	}

//-------------------------------------------------------------------
	
	public void changeClasses(double class0Start, double classWidth) {
		this.class0Start = class0Start;
		this.classWidth = classWidth;
						//	must call resetClasses() after
	}
	
	public void setMeanSD(double targetMean, double targetSD) {
		double mean = targetMean;
		double sd = targetSD;
		
		int iterations = 0;
		while (true) {
			double lastCumProb = 0.0;
			for (int i=0 ; i<classStart.length-1 ; i++) {
				double nextCumProb = NormalTable.cumulative((classStart[i+1] - mean) / sd);
				classCount[i] = nextCumProb - lastCumProb;
				lastCumProb = nextCumProb;
			}
			maxCount = findMaxCount();
			
			double histoMean = findMeanFromHisto();
			double histoSD = findSDFromHisto();
						
			double meanBias = targetMean - histoMean;
			double sdBias = targetSD / histoSD;
			
			mean += meanBias;
			sd *= sdBias;
			
			iterations ++;
			
			if (iterations > kMaxIterations
							|| (meanBias < (targetMean * kTargetEps) && sdBias < (targetSD * kTargetEps)))
				break;
		}
		
	}
	
	protected boolean initialise() {
		if (super.initialise()) {
			maxCount = findMaxCount();
			return true;
		}
		
		return false;
	}
	
	protected double[] defaultCounts(double localClassStart[]) {
		int noOfClasses = localClassStart.length - 1;
		double localClassCount[] = new double[noOfClasses];
		
		int midClass = noOfClasses / 2;
		int noAtSide = midClass * 2 / 3;
		for (int i=0 ; i<noAtSide ; i++)
			localClassCount[midClass - i] = localClassCount[midClass + i] = noAtSide - i;
		tooLowCount = tooHighCount = 0;
		return localClassCount;
	}
	
	protected double[] defaultClasses() {
		return initialiseClasses();
	}
	
	protected double[] initialiseClasses() {
		int noOfClasses = (int)Math.round(Math.ceil((axis.maxOnAxis - class0Start) / classWidth));
		if (class0Start > axis.minOnAxis)
			noOfClasses ++; 
		double localClassStart[] = new double[noOfClasses + 1];
		if (class0Start > axis.minOnAxis) {
			localClassStart[0] = axis.minOnAxis;
			for (int i=0 ; i< noOfClasses ; i++)
				localClassStart[i+1] = Math.min(class0Start + i * classWidth, axis.maxOnAxis);
		}
		else
			for (int i=0 ; i<= noOfClasses ; i++)
				localClassStart[i] = Math.min(class0Start + i * classWidth, axis.maxOnAxis);
		return localClassStart;
	}
	
	private double findMaxCount() {
		double maxCount = 0.0;
		for (int i=0 ; i< classCount.length ; i++) {
			double stdCount = classCount[i] * classWidth / (classStart[i+1] - classStart[i]);
			if (stdCount > maxCount)
				maxCount = stdCount;
		}
		return maxCount;
	}
	
	public void setMessages(String[] messageArray, NumValue exactAnswer) {
		messageArray[2] = "Good! The exact standard deviation of this histogram is " + exactAnswer.toString();
		messageArray[3] = "Not close enough. From the 70-95-100 rule, about 95% of the histogram area will be within 2s of the mean (a range of 4s values).";
		messageArray[4] = "The best estimate of standard deviation for this histogram is " + exactAnswer.toString();
	}

//-------------------------------------------------------------------
	
	private void drawBar(Graphics g, Point p0, Point p1, int barIndex) {
		int x = Math.min(p0.x, p1.x);
		int y = Math.min(p0.y, p1.y);
		int width = Math.max(p1.x - p0.x, p0.x - p1.x);
		int height = Math.max(p1.y - p0.y, p0.y - p1.y) + 1;
		if (height > 1) {
			g.setColor(Color.lightGray);
			g.fillRect(x, y, width, height);
			g.setColor(Color.black);
			g.drawRect(x, y, width, height);
		}
		
		if (allowDrag && (dragIndex < 0 || dragIndex == barIndex)) {
			g.setColor(Color.red);
			int midX = x + width / 2;
			g.drawLine(x, y, x + width, y);
			
			g.drawLine(midX, y - kDragArrow, midX, y + kDragArrow);
			if (dragIndex == barIndex) {
				g.drawLine(midX - 1, y - kDragArrow + 1, midX - 1, y + kDragArrow - 1);
				g.drawLine(midX + 1, y - kDragArrow + 1, midX + 1, y + kDragArrow - 1);
				for (int i=2 ; i<4 ; i++) {
					g.drawLine(midX - i, y - kDragArrow + i, midX + i, y - kDragArrow + i);
					g.drawLine(midX - i, y + kDragArrow - i, midX + i, y + kDragArrow - i);
				}
			}
			else {
				g.drawLine(midX, y - kDragArrow, midX + 3, y - kDragArrow + 3);
				g.drawLine(midX, y - kDragArrow, midX - 3, y - kDragArrow + 3);
				g.drawLine(midX, y + kDragArrow, midX + 3, y + kDragArrow - 3);
				g.drawLine(midX, y + kDragArrow, midX - 3, y + kDragArrow - 3);
			}
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		drawTopBorder(g);
		
		int maxHt = getDisplayWidth() - topBorder(g);
		
		Point p0 = null;
		Point p1 = null;
		
		int lastClassEnd = axis.numValToRawPosition(classStart[0]);
		
		for (int i=0 ; i<classCount.length ; i++) {
			double stdCount = classCount[i] * classWidth / (classStart[i+1] - classStart[i]);
			int barHt = (int)Math.round(maxHt * stdCount / maxCount);
			
			p0 = translateToScreen(lastClassEnd, barHt, p0);
			
			int thisClassEnd = axis.numValToRawPosition(classStart[i + 1]);
			p1 = translateToScreen(thisClassEnd, 0, p1);
			
			drawBar(g, p0, p1, i);
			
			lastClassEnd = thisClassEnd;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		int classEnd = axis.numValToRawPosition(classStart[0]);
		if (p.x < classEnd)
			return null;
		
		for (int i=1 ; i<classStart.length-1 ; i++) {
			classEnd = axis.numValToRawPosition(classStart[i]);
			if (p.x < classEnd) {
				int classIndex = i - 1;
				
				int maxHt = getDisplayWidth() - topBorder(getGraphics());
				double stdCount = classCount[classIndex] * classWidth
																			/ (classStart[classIndex+1] - classStart[classIndex]);
				int barHt = (int)Math.round(maxHt * stdCount / maxCount);
				
				if (Math.abs(p.y - barHt) <= kMaxHitSlop)
					return new VertDragPosInfo(y, classIndex, p.y - barHt);
				
				break;
			}
		}
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
//		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
		if (x < 0 || x >= getSize().width)
			return null;
		
		return new VertDragPosInfo(y);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		VertDragPosInfo dragPos = (VertDragPosInfo)startInfo;
		dragIndex = dragPos.index;
		hitOffset = dragPos.hitOffset;
		resetMessage();
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			Point p = translateFromScreen(0, dragPos.y, null);
			
			int maxHt = getDisplayWidth() - topBorder(getGraphics());
			int newHt = Math.max(0, Math.min(p.y - hitOffset, getDisplayWidth() - kDragArrow));
			
			double stdCount = newHt * (double)maxCount / maxHt;
			classCount[dragIndex] = Math.max(0, stdCount
											* (classStart[dragIndex + 1] - classStart[dragIndex]) / classWidth);
			
			repaint();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		maxCount = findMaxCount();
		if (maxCount == 0.0) {
			maxCount = classCount[dragIndex] = 1.0;
		}
		dragIndex = -1;
		repaint();
	}
	
}
	
