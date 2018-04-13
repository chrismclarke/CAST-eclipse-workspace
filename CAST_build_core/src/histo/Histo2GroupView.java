package histo;

import java.awt.*;

import dataView.*;
import axis.*;


public class Histo2GroupView extends MarginalDataView {
//	static public final String HISTO_2_GROUP = "histo2Group";
	
	static final public int FREQ_TO_RELFREQ = 0;
	static final public int RELFREQ_TO_FREQ = 1;
	
	static private final int kMinHistoWidth = 40;
	
	static final public Color kGroup0FillColor = new Color(0x0099FF);	//		pale blue
	static final public Color kGroup1FillColor = new Color(0xFFFF33);	//		yellow
	static final private Color kMixedFillColor = new Color(0x00CC99);		//		pale turquoise
	static final private Color kBackLineColor = new Color(0x999900);
	static final private Color kBackLineShadeColor = new Color(0x009966);
	
	static private final int kMaxFrames = 30;
	
	private boolean initialised = false;
	
	private int animationType = FREQ_TO_RELFREQ;
	
	private NumCatAxis freqAxis;
	private double class0Start, classWidth;
	
	private double classStart[];
	private int classCount[][];
	
	public Histo2GroupView(DataSet theData, XApplet applet,
							NumCatAxis valAxis, NumCatAxis freqAxis, double class0Start, double classWidth) {
		super(theData, applet, new Insets(0, 0, 0, 0), valAxis);
																//		no border under histo
		this.freqAxis = freqAxis;
		this.class0Start = class0Start;
		this.classWidth = classWidth;
	}

//-------------------------------------------------------------------
	
	protected boolean initialise() {
		if (initialised)
			return false;
		if (classStart == null)
			classStart = initialiseClasses();
		if (classCount == null)
			classCount = countClasses();
		return true;
	}
	
	private double[] initialiseClasses() {
		int noOfClasses = (int)Math.round((axis.maxOnAxis - class0Start + classWidth / 1000.0) / classWidth - 0.5);
																		//	allow a little slop at end of axis
		double localClassStart[] = new double[noOfClasses + 1];
		localClassStart[0] = class0Start;
		for (int i=0 ; i< noOfClasses ; i++)
			localClassStart[i+1] = Math.min(localClassStart[i] + classWidth, axis.maxOnAxis);
		return localClassStart;
	}
	
	private int[][] countClasses() {
		int noOfClasses = classStart.length - 1;
		int localClassCount[][] = new int[2][];
		for (int i=0 ; i<2 ; i++)
			localClassCount[i] = new int[noOfClasses];
		
		NumVariable yVar = getNumVariable();
		CatVariable xVar = getCatVariable();
		
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			int x = xVar.labelIndex(xe.nextValue());
			if (y >= classStart[0])
				for (int i=0 ; i<classStart.length-1 ; i++)
					if (y <= classStart[i+1]) {
						localClassCount[x][i]++;
						break;
					}
		}
		return localClassCount;
	}

//-------------------------------------------------------------------
	
	private int findClassStart(int classIndex) {
		try {
			return axis.numValToPosition(classStart[classIndex]);
		} catch (AxisException e) {
		}
		return 0;
	}
	
	private int countToVertPos(double count) {
		try {
			return freqAxis.numValToPosition(count);
		} catch (AxisException e) {
		}
		return 0;
	}
	
	private void fillOneClass(Graphics g, int classIndex, double count0, double count1,
																					Point p0, Point p1) {
		int lowHoriz = findClassStart(classIndex);
		int highHoriz = findClassStart(classIndex + 1);
		double minCount, maxCount;
		Color maxColor = null;
		if (count0 < count1) {
			minCount = count0;
			maxCount = count1;
			maxColor = kGroup1FillColor;
		}
		else {
			minCount = count1;
			maxCount = count0;
			maxColor = kGroup0FillColor;
		}
		
		if (minCount > 0) {
			int lowVert = countToVertPos(0);
			int highVert = countToVertPos(minCount);
			p0 = translateToScreen(lowHoriz, highVert, p0);
			p1 = translateToScreen(highHoriz, lowVert, p1);
			g.setColor(kMixedFillColor);
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y + 1));
		}
		
		if (maxColor != null) {
			int lowVert = countToVertPos(minCount);
			int highVert = countToVertPos(maxCount);
			p0 = translateToScreen(lowHoriz, highVert, p0);
			p1 = translateToScreen(highHoriz, lowVert, p1);
			g.setColor(maxColor);
			g.fillRect(p0.x, p0.y, (p1.x - p0.x), (p1.y - p0.y + 1));
		}
		g.setColor(getForeground());
	}
	
	private void fillClasses(Graphics g, double frontFactor) {
		Point p0 = new Point(0,0);
		Point p1 = new Point(0,0);
		for (int i=0 ; i<classCount[0].length ; i++)
			fillOneClass(g, i, classCount[1][i] * frontFactor, classCount[0][i], p0, p1);
	}
	
	private void drawVertLine(Graphics g, int horiz, int vert0, int vert1, Point p0, Point p1) {
		p0 = translateToScreen(horiz, vert0, p0);
		p1 = translateToScreen(horiz, vert1, p1);
		g.drawLine(p0.x, p0.y, p1.x, p1.y);
	}
	
	private void drawBackOutline(Graphics g, int[] backClassCount, int[] frontClassCount,
																								double frontFactor) {
		int lastVert = countToVertPos(0);
		int lastFrontVert = lastVert;
		int lastHoriz = findClassStart(0);
		Point pLast = translateToScreen(lastHoriz, lastVert, null);
		Point pNext = new Point(0,0); 
		for (int i=0 ; i<classStart.length-1 ; i++) {
			int nextHoriz = findClassStart(i+1);
			int nextVert = countToVertPos(backClassCount[i]);
			int nextFrontVert = countToVertPos(frontClassCount[i] * frontFactor);
			
			int lowBack = Math.min(lastVert, nextVert);
			int highBack = Math.max(lastVert, nextVert);
			int lowFront = Math.min(lastFrontVert, nextFrontVert);
			int highFront = Math.max(lastFrontVert, nextFrontVert);
			
			int maxLow = Math.min(highBack, lowFront);
			if (lowBack < maxLow) {
				g.setColor(kBackLineShadeColor);
				drawVertLine(g, lastHoriz, lowBack, maxLow, pLast, pNext);
			}
			
			int minHigh = Math.max(lowBack, highFront);
			if (highBack > minHigh) {
				g.setColor(kBackLineColor);
				drawVertLine(g, lastHoriz, minHigh, highBack, pLast, pNext);
			}
			
			pNext = translateToScreen(nextHoriz, nextVert, pNext);
			if (backClassCount[i] > 0) {
				if (backClassCount[i] > frontClassCount[i] * frontFactor)
					g.setColor(kBackLineColor);
				else
					g.setColor(kBackLineShadeColor);
				g.drawLine(pLast.x, pNext.y, pNext.x, pNext.y);
			}
			Point temp = pNext;
			pNext = pLast;
			pLast = temp;
			lastHoriz = nextHoriz;
			lastVert = nextVert;
			lastFrontVert = nextFrontVert;
		}
		int finalVert = countToVertPos(0);
		pNext = translateToScreen(0, finalVert, pNext);
		g.drawLine(pLast.x, pLast.y, pLast.x, pNext.y);
	}
	
	private void drawFrontOutline(Graphics g, int[] classCount, double frontFactor) {
		g.setColor(getForeground());
		int lastVert = countToVertPos(0);
		int lastHoriz = findClassStart(0);
		Point pLast = translateToScreen(lastHoriz, lastVert, null);
		Point pNext = null; 
		for (int i=0 ; i<classStart.length-1 ; i++) {
			int nextVert = countToVertPos(classCount[i] * frontFactor);
			int nextHoriz = findClassStart(i+1);
			pNext = translateToScreen(nextHoriz, nextVert, pNext);
			g.drawLine(pLast.x, pLast.y, pLast.x, pNext.y);
			if (classCount[i] > 0)
				g.drawLine(pLast.x, pNext.y, pNext.x, pNext.y);
			Point temp = pNext;
			pNext = pLast;
			pLast = temp;
		}
		int finalVert = countToVertPos(0);
		pNext = translateToScreen(0, finalVert, pNext);
		g.drawLine(pLast.x, pLast.y, pLast.x, pNext.y);
	}
	
	private double getFrontFactor() {
		CatVariable xVar = getCatVariable();
		int[] count = xVar.getCounts();
		
		double frame = (animationType == FREQ_TO_RELFREQ) ? getCurrentFrame()
																		: kMaxFrames - getCurrentFrame();
		double frontFactor = ((double)count[0]) / count[1];
		return Math.pow(frontFactor, frame / kMaxFrames);
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		double frontFactor = getFrontFactor();
		
		fillClasses(g, frontFactor);
		drawBackOutline(g, classCount[0], classCount[1], frontFactor);
		drawFrontOutline(g, classCount[1], frontFactor);
	}

//-----------------------------------------------------------------------------------

	public void doAnimation(int animationType) {
		this.animationType = animationType;
		animateFrames(1, kMaxFrames, 10, null);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------

	public int minDisplayWidth() {
		return kMinHistoWidth;
	}
}
	
