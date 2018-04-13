package exerciseNumGraph;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;


public class DragHistoView extends DataView implements StatusInterface {
//	static public final String DRAG_HISTO = "dragHisto";
	
	static final private int kDragArrow = 5;
	static final private int kMaxHitSlop = 7;
	static final private int kCountGap = 5;
	
	static final private double kEps = 0.00001;		//	to allow for rounding down of axis max
	
	static final private Color kPaleBlue = new Color(0xCCCCFF);
	
	private String yKey;
	private HorizAxis valAxis;
	private VertAxis countAxis;
	
	private double class0Start, classWidth;
	private int baseMultiple;
	private int[] classMultiples;
	
	private double classStart[];
	private double classCount[];
	
	private boolean[] selectedBars = null;
	private boolean showCounts = true;
	
	private double startCount[] = null;
	private boolean initialised = false;
	
	private int dragIndex = -1;
	private int hitOffset;
	private boolean canDrag = true;
	
	public DragHistoView(DataSet theData, XApplet applet,
												String yKey, HorizAxis valAxis, VertAxis countAxis) {
		super(theData, applet, new Insets(2, 5, 0, 5));
		this.yKey = yKey;
		this.valAxis = valAxis;
		this.countAxis = countAxis;
	}
	
	public void setShowCounts(boolean showCounts) {
		this.showCounts = showCounts;
	}
	
	public void setCanDrag(boolean canDrag) {
		this.canDrag = canDrag;
	}

//-------------------------------------------------------------------------
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<classCount.length ; i++)
			s += new NumValue(classCount[i], 4) + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		startCount = new double[st.countTokens()];
		for (int i=0 ; i<classCount.length ; i++)
			startCount[i] = Double.parseDouble(st.nextToken());
		initialised = false;
		repaint();
	}

//-------------------------------------------------------------------------
	
	private double[] correctCounts() {
		initialise();
		double localClassCount[] = new double[classCount.length];
		
		NumVariable theVariable = (NumVariable)getVariable(yKey);
		NumValue sortedVals[] = theVariable.getSortedData();
		int noOfVals = sortedVals.length;
		int index = 0;
		int classIndex = 0;
		while (index < noOfVals && classIndex < localClassCount.length) {
			if (sortedVals[index].toDouble() < classStart[classIndex + 1]) {
				localClassCount[classIndex]++;
				index++;
			}
			else
				classIndex++;
		}
		
		if (classMultiples != null)
			for (int i=0 ; i<classCount.length ; i++)
				if (classMultiples[i] != baseMultiple)
					localClassCount[i] = localClassCount[i] * baseMultiple / classMultiples[i];
		
		return localClassCount;
	}
	
	public int maxCount() {
		initialise();
		double[] count = correctCounts();
		double maxCount = 0;
		for (int i=0 ; i<count.length ; i++)
			maxCount = Math.max(maxCount, count[i]);
		return (int)Math.round(maxCount);
	}
	
	public boolean[] wrongBars() {
		double[] correctCount = correctCounts();
		boolean isWrong[] = new boolean[classCount.length];
		for (int i=0 ; i<classCount.length ; i++)
			isWrong[i] = classCount[i] != correctCount[i];
		return isWrong;
	}
	
	public void setCorrectCounts() {
		double[] correctCount = correctCounts();
		for (int i=0 ; i<classCount.length ; i++)
			classCount[i] = correctCount[i];
	}
	
	public void setSelectedBars(boolean[] selectedBars) {
		this.selectedBars = selectedBars;
	}
	
	public void clearSelection() {
		selectedBars = null;
	}
	
	public boolean[] getClassGroups() {
		initialise();
		NumVariable theVariable = (NumVariable)getVariable(yKey);
		NumValue sortedVals[] = theVariable.getSortedData();
		int sortedIndex[] = theVariable.getSortedIndex();
		int noOfVals = sortedVals.length;
		boolean oddClass[] = new boolean[noOfVals];
		
		int classIndex = 0;
		boolean isOddClass = false;
		for (int i=0 ; i<noOfVals ; i++) {
			double y = sortedVals[i].toDouble();
			if (y >= classStart[classIndex + 1]) {
				while (y >= classStart[classIndex + 1])
					classIndex ++;
				isOddClass = !isOddClass;
			}
			oddClass[sortedIndex[i]] = isOddClass;
		}
		return oddClass;
	}
	
	public double[] getClassStarts() {
		return classStart;
	}
	
	public double[] getClassCounts() {
		return classCount;
	}

//-------------------------------------------------------------------
	
	public void changeClasses(double class0Start, double classWidth) {
		changeClasses(class0Start, classWidth, 1, null);
	}
	
	public void changeClasses(double class0Start, double classWidth, int baseMultiple,
																																	int[] classMultiples) {
		this.class0Start = class0Start;
		this.classWidth = classWidth;
		this.baseMultiple = baseMultiple;
		this.classMultiples = classMultiples;
		initialised = false;
	}
	
	final public void initialise() {
		if (!initialised) {
			initialised = true;
			doInitialisation();
		}
	}
	
	protected void doInitialisation() {
		int noOfClasses;
		if (classMultiples == null) {
			noOfClasses = (int)Math.round(Math.floor((valAxis.maxOnAxis - class0Start) / classWidth + kEps));
			classStart = new double[noOfClasses + 1];			//	only complete classes
			for (int i=0 ; i<= noOfClasses ; i++)
				classStart[i] = Math.min(class0Start + i * classWidth, valAxis.maxOnAxis);
		}
		else {
			noOfClasses = classMultiples.length;
			classStart = new double[noOfClasses + 1];
			classStart[0] = class0Start;
			for (int i=0 ; i<noOfClasses ; i++)
				classStart[i + 1] = classStart[i] + classMultiples[i] * classWidth;
		}
		
		if (startCount == null) {
			classCount = new double[noOfClasses];
			if (classMultiples == null) {
				int midClass = noOfClasses / 2;
				int noAtSide = midClass * 2 / 3;
				for (int i=0 ; i<noAtSide ; i++)
					classCount[midClass - i] = classCount[midClass + i] = noAtSide - i;
			}
			else {
				double[] correctCount = correctCounts();
				for (int i=0 ; i<noOfClasses ; i++)
					classCount[i] = (classMultiples[i] == baseMultiple) ? correctCount[i] : 0;
			}
		}
		else {
			classCount = startCount;
			startCount = null;
		}
	}

//-------------------------------------------------------------------
	
	private void drawBar(Graphics g, Point p0, Point p1, int barIndex) {
		int x = Math.min(p0.x, p1.x);
		int y = Math.min(p0.y, p1.y);
		int width = Math.max(p1.x - p0.x, p0.x - p1.x);
		int height = Math.max(p1.y - p0.y, p0.y - p1.y) + 1;
		if (height > 1) {
			g.setColor(selectedBars != null && selectedBars[barIndex] ? Color.yellow
										: (canDrag && classMultiples != null && classMultiples[barIndex] == baseMultiple) ? kPaleBlue
										: Color.lightGray);
			g.fillRect(x, y, width, height);
			g.setColor(Color.black);
			g.drawRect(x, y, width, height);
		}
		else if (selectedBars != null && selectedBars[barIndex]) {
			g.setColor(Color.yellow);
			g.fillOval(x + width / 2 - 8, y - kDragArrow - 18, 16, 16);
		}
		
		if (canDrag && (classMultiples == null || classMultiples[barIndex] != baseMultiple)
																							&& (dragIndex < 0 || dragIndex == barIndex)) {
			g.setColor(Color.red);
			int midX = x + width / 2;
			
			g.drawLine(midX, y - kDragArrow, midX, y + kDragArrow);
			if (dragIndex == barIndex) {
				g.drawLine(0, y, x + width, y);
				g.drawLine(0, y, kDragArrow, y - kDragArrow);
				g.drawLine(0, y, kDragArrow, y + kDragArrow);
				
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
		
		Point p0 = null;
		Point p1 = null;
		
		int lastClassEnd = valAxis.numValToRawPosition(classStart[0]);
		NumValue countVal = new NumValue(0.0, 0);
		int ascent = g.getFontMetrics().getAscent();
		
		for (int i=0 ; i<classCount.length ; i++) {
			int barHt = countAxis.numValToRawPosition(classCount[i]);
			
			p0 = translateToScreen(lastClassEnd, barHt, p0);
			
			int thisClassEnd = valAxis.numValToRawPosition(classStart[i + 1]);
			p1 = translateToScreen(thisClassEnd, 0, p1);
			
			drawBar(g, p0, p1, i);
			
			if (showCounts && dragIndex < 0 && classCount[i] > 0) {
				g.setColor(Color.gray);
			
				countVal.decimals = (classMultiples == null || classMultiples[i] <= baseMultiple ? 0
																	: classMultiples[i] == baseMultiple * 2 ? 1 : 2);
				double count = classCount[i];
				if (classMultiples == null || classMultiples[i] <= baseMultiple)
					count = Math.rint(count);
				else
					count = Math.rint(count * classMultiples[i] / baseMultiple) * baseMultiple / classMultiples[i];
				countVal.setValue(count);
				int baseline = p0.y + ascent + kCountGap;
				if (baseline >= getSize().height)
					baseline = p0.y - kCountGap;
				countVal.drawCentred(g, (p0.x + p1.x) / 2, baseline);
			}
			
			lastClassEnd = thisClassEnd;
		}
		
		if (dragIndex >= 0) {
			int barHt = countAxis.numValToRawPosition(classCount[dragIndex]);
			lastClassEnd = valAxis.numValToRawPosition(classStart[dragIndex]);
			int thisClassEnd = valAxis.numValToRawPosition(classStart[dragIndex + 1]);
			p1 = translateToScreen(lastClassEnd, barHt, p1);
			p0 = translateToScreen(thisClassEnd, barHt, p0);
			g.setColor(Color.red);
			g.setFont(getApplet().getStandardBoldFont());
			
			countVal.decimals = (classMultiples == null || classMultiples[dragIndex] <= baseMultiple ? 0
																	: classMultiples[dragIndex] == baseMultiple * 2 ? 1 : 2);
			double count = classCount[dragIndex];
			if (classMultiples == null || classMultiples[dragIndex] <= baseMultiple)
				count = Math.rint(count);
			else
				count = Math.rint(count * classMultiples[dragIndex] / baseMultiple)
																								* baseMultiple / classMultiples[dragIndex];
			countVal.setValue(count);
			int baseline = p0.y - kCountGap;
			countVal.drawCentred(g, (p0.x + p1.x) / 2, baseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return canDrag;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		int classEnd = valAxis.numValToRawPosition(classStart[0]);
		if (p.x < classEnd)
			return null;
		
		for (int i=1 ; i<classStart.length ; i++) {
			classEnd = valAxis.numValToRawPosition(classStart[i]);
			if (p.x < classEnd) {
				int classIndex = i - 1;
				if (classMultiples != null && classMultiples[classIndex] == baseMultiple)
					return null;
				
				int barHt = countAxis.numValToRawPosition(classCount[i - 1]);
				
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
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			VertDragPosInfo dragPos = (VertDragPosInfo)toPos;
			Point p = translateFromScreen(0, dragPos.y, null);
			
			try {
				classCount[dragIndex] = countAxis.positionToNumVal(p.y - hitOffset);
			} catch (AxisException e) {
			}
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		double count = classCount[dragIndex];
		if (classMultiples != null && classMultiples[dragIndex] > baseMultiple)
			count = Math.rint(classCount[dragIndex] * classMultiples[dragIndex] / baseMultiple)
																												* baseMultiple / classMultiples[dragIndex];
		else
			count = Math.rint(classCount[dragIndex]);
		classCount[dragIndex] = count;
		
		dragIndex = -1;
		repaint();
	}
	
}
	
