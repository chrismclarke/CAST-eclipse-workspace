package exercisePercent;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;


public class DragCumHistoView extends DataView implements StatusInterface {
//	static public final String DRAG_CUM_HISTO = "dragCumHisto";
	
	static final private int HISTOGRAM = 0;
	static final private int DOTPLOT = 1;
	
	static final private int kCountGap = 5;
	static final private int kMidGap = 12;
	static final private int kLeftAxisWidth = 24;
	static final private int kTickLength = 4;
	static final private int kTickZeroGap = 2;
	static final private int kDotRadius = 3;
	static final private int kArrowHead = 4;
	static final private int kDotPlotHeight = 50;
	
	static final private int kHitSlop = 5;
	
	static final private double kEps = 0.00001;		//	to allow for rounding down of axis max
	static final private int kMaxCloseError = 4;
	
	static final private Color kGridColor = new Color(0xDDDDDD);
	static final private Color kHistoFillColor = new Color(0xCCCCFF);
	
	private String yKey;
	private HorizAxis theAxis;
	
	private int displayType;
	
	private double classStart[];
	private int classCount[];
	private double cumCount[];
	
	private boolean correct[] = null;
	private boolean correctStep[] = null;
	
	private int ascent;
	private boolean initialised = false;
	
	private int dragIndex = -1;
	private int hitOffset;
	
	public DragCumHistoView(DataSet theData, XApplet applet, String yKey, HorizAxis theAxis) {
		super(theData, applet, new Insets(0, kLeftAxisWidth, 0, 0));
		this.yKey = yKey;
		this.theAxis = theAxis;
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<cumCount.length ; i++)
		 s += Math.round(cumCount[i]) + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		for (int i=0 ; i<cumCount.length ; i++)
			cumCount[i] = Double.parseDouble(st.nextToken());
		repaint();
	}
	
	public void changeClasses(double class0Start, double classWidth) {
		displayType = HISTOGRAM;
		int noOfClasses = (int)Math.round(Math.floor((theAxis.maxOnAxis - class0Start) / classWidth + kEps));
		classStart = new double[noOfClasses + 1];			//	only complete classes
		for (int i=0 ; i<= noOfClasses ; i++)
			classStart[i] = Math.min(class0Start + i * classWidth, theAxis.maxOnAxis);
		
		classCount = new int[classStart.length - 1];
		NumVariable theVariable = (NumVariable)getVariable(yKey);
		NumValue sortedY[] = theVariable.getSortedData();
		int noOfVals = sortedY.length;
		int index = 0;
		int classIndex = 0;
		while (index < noOfVals && classIndex < classCount.length) {
			if (sortedY[index].toDouble() < classStart[classIndex + 1]) {
				classCount[classIndex]++;
				index++;
			}
			else
				classIndex++;
		}
		cumCount = new double[classStart.length];
		correct = null;
		correctStep = null;
	}
	
	public void changeExactCumulative() {
		displayType = DOTPLOT;
		NumVariable theVariable = (NumVariable)getVariable(yKey);
		NumValue sortedY[] = theVariable.getSortedData();
		int n = sortedY.length;
		classStart = new double[n + 1];
		classCount = new int[n];
		classStart[0] = 2 * theAxis.minOnAxis - theAxis.maxOnAxis;		//	well below axis
		for (int i=0 ; i<n ; i++) {
			classCount[i] = 1;
			classStart[i + 1] = sortedY[i].toDouble();
		}
		cumCount = new double[classStart.length];
		correct = null;
		correctStep = null;
	}
	
	public void setCorrectCumulative() {
		cumCount[0] = 0;
		for (int i=0 ; i<classCount.length ; i++)
			cumCount[i + 1] = cumCount[i] + classCount[i];
		
		correct = new boolean[cumCount.length];
		for (int i=0 ; i<cumCount.length ; i++)
			correct[i] = true;
		
		correctStep = new boolean[classStart.length];
		for (int i=0 ; i<classStart.length ; i++)
			correctStep[i] = true;
	}
	
	public boolean checkCumulative() {
		boolean allCorrect = true;
		int cumulative = 0;
		for (int i=0 ; i<cumCount.length ; i++) {
			allCorrect = allCorrect && (cumCount[i] == cumulative);
			if (i < classCount.length)
				cumulative += classCount[i];
		}
		return allCorrect;
	}
	
	public void showErrors() {
		correct = new boolean[cumCount.length];
		int cumulative = 0;
		for (int i=0 ; i<cumCount.length ; i++) {
			correct[i] = cumCount[i] == cumulative;
			if (i < classCount.length)
				cumulative += classCount[i];
		}
		correctStep = new boolean[classStart.length];
		correctStep[0] = cumCount[0] == 0;
		for (int i=1 ; i<classStart.length ; i++) {
			double attempt = (cumCount[i] - cumCount[i - 1]);
			correctStep[i] = attempt == classCount[i - 1];
		}
	}
	
	public boolean isCloseToCorrect() {
		int error = 0;
		for (int i=0 ; i<classCount.length ; i++) {
			int correct = classCount[i];
			int attempt = (int)Math.round(cumCount[i + 1] - cumCount[i]);
			error += Math.abs(attempt - correct);
		}
		return error <= kMaxCloseError;
	}

//-------------------------------------------------------------------
	
	protected void doInitialisation(Graphics g) {
		ascent = g.getFontMetrics().getAscent();
	}
	
	
	final public void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}

//-------------------------------------------------------------------
	
	private int getBottomHeight() {
		return (displayType == HISTOGRAM) ? (getSize().height - kMidGap) * 2 / 5
																			: kDotPlotHeight;
	}
	
	private int getCumZeroPos() {
		int zeroPos = getBottomHeight() + kMidGap + ascent / 2;
		return translateToScreen(0, zeroPos, null).y;
	}
	
	private int getCumOnePos() {
		return ascent / 2;
	}
	
	private void paintBackground(Graphics g) {
		g.setColor(kGridColor);
		
		Vector labels = theAxis.getLabels();
		Enumeration le = labels.elements();
		Point p = null;
		while (le.hasMoreElements()) {
			AxisLabel label = (AxisLabel)le.nextElement();
			double x = theAxis.minOnAxis + label.position * (theAxis.maxOnAxis - theAxis.minOnAxis);
			int xPos = theAxis.numValToRawPosition(x);
			p = translateToScreen(xPos, 0, p);
			g.drawLine(p.x, 0, p.x, getSize().height);
		}
		
		g.setColor(getApplet().getBackground());
		p = translateToScreen(0, 0, p);
		g.fillRect(0, 0, kLeftAxisWidth, getSize().height);
		
		int zeroPos = getCumZeroPos();
		int histoTopPos = translateToScreen(0, getBottomHeight(), p).y;
		g.fillRect(0, zeroPos, getSize().width, histoTopPos - zeroPos);
		
		int onePos = getCumOnePos();
		g.fillRect(0, 0, getSize().width, onePos);
		
		g.setColor(getForeground());
		g.drawLine(kLeftAxisWidth - 1, onePos, kLeftAxisWidth - 1, zeroPos);
		g.drawLine(kLeftAxisWidth - 1, onePos, kLeftAxisWidth - kTickLength - 1, onePos);
		g.drawLine(kLeftAxisWidth - 1, zeroPos, kLeftAxisWidth - kTickLength - 1, zeroPos);
		
		FontMetrics fm = g.getFontMetrics();
		int zeroWidth = fm.stringWidth("0");
		int ascent = fm.getAscent();
		int zeroLeft = kLeftAxisWidth - kTickLength - kTickZeroGap - zeroWidth - 1;
		g.drawString("0", zeroLeft, zeroPos + ascent / 2);
		g.drawString("1", zeroLeft, onePos + ascent / 2);
		
		g.setColor(Color.gray);
		g.drawLine(kLeftAxisWidth, zeroPos, getSize().width, zeroPos);
		g.drawLine(kLeftAxisWidth, onePos, getSize().width, onePos);
	}
	
	private void drawBar(Graphics g, Point p0, Point p1, int barIndex) {
		int x = Math.min(p0.x, p1.x);
		int y = Math.min(p0.y, p1.y);
		int width = Math.max(p1.x - p0.x, p0.x - p1.x);
		int height = Math.max(p1.y - p0.y, p0.y - p1.y) + 1;
		if (height > 1) {
			g.setColor(correctStep == null || correctStep[barIndex + 1] ? kHistoFillColor : Color.yellow);
			g.fillRect(x, y, width, height);
			g.setColor(Color.black);
			g.drawRect(x, y, width, height);
		}
	}
	
	private void drawHistogram(Graphics g) {
		Point p0 = null;
		Point p1 = null;
		
		int lastClassEnd = theAxis.numValToRawPosition(classStart[0]);
		NumValue countVal = new NumValue(0.0, 0);
		int ascent = g.getFontMetrics().getAscent();
		
		int maxHistoHeight = getBottomHeight() * 9 / 10;
		int maxCount = 0;
		for (int i=0 ; i<classCount.length ; i++)
			maxCount = Math.max(maxCount, classCount[i]);
		
		for (int i=0 ; i<classCount.length ; i++) {
			int barHt = classCount[i] * maxHistoHeight / maxCount;
			
			p0 = translateToScreen(lastClassEnd, barHt, p0);
			
			int thisClassEnd = theAxis.numValToRawPosition(classStart[i + 1]);
			p1 = translateToScreen(thisClassEnd, 0, p1);
			
			drawBar(g, p0, p1, i);
			
			if (classCount[i] > 0) {
				g.setColor(Color.gray);
			
				countVal.setValue(classCount[i]);
				int baseline = p0.y + ascent + kCountGap;
				if (baseline >= getSize().height)
					baseline = p0.y - kCountGap;
				countVal.drawCentred(g, (p0.x + p1.x) / 2, baseline);
			}
			
			lastClassEnd = thisClassEnd;
		}
	}
	
	private void drawDotPlot(Graphics g) {
		Point p = null;
		g.setColor(getForeground());
		
		for (int i=1 ; i<classStart.length ; i++) {
			int xPos = theAxis.numValToRawPosition(classStart[i]);
			p = translateToScreen(xPos, 10, p);
			
			g.setColor(correctStep == null || correctStep[i] ? getForeground() : Color.red);
			drawCross(g, p);
		}
	}
	
	private void drawCumulative(Graphics g) {
		g.setColor(getForeground());
		int zeroPos = getCumZeroPos();
		int onePos = getCumOnePos();
		
		int xLast = kLeftAxisWidth;
		int yLast = zeroPos;
		int n = ((NumVariable)getVariable(yKey)).noOfValues();
		
		for (int i=0 ; i<cumCount.length ; i++) {
			int yNext = (int)Math.round(zeroPos + (onePos - zeroPos) * cumCount[i] / n);
			int xNext = kLeftAxisWidth + theAxis.numValToRawPosition(Math.max(theAxis.minOnAxis, classStart[i]));
			
			if (displayType == HISTOGRAM)
				g.drawLine(xLast, yLast, xNext, yNext);
			else {
				g.drawLine(xLast, yLast, xNext, yLast);
				g.drawLine(xNext, yLast, xNext, yNext);
			}
			
			xLast = xNext;
			yLast = yNext;
		}
		
		g.drawLine(xLast, yLast, getSize().width, onePos);
		
		for (int i=0 ; i<cumCount.length ; i++) {
			int yNext = (int)Math.round(zeroPos + (onePos - zeroPos) * cumCount[i] / n);
			int xNext = kLeftAxisWidth + theAxis.numValToRawPosition(classStart[i]);
			g.setColor(dragIndex == i ? Color.red
																: (correct != null) ? (correct[i] ? Color.green : Color.red)
																: Color.blue);
			g.fillOval(xNext - kDotRadius, yNext - kDotRadius, 2 * kDotRadius + 1, 2 * kDotRadius + 1);
		}
		
		if (dragIndex >= 0) {
			int yPos = (int)Math.round(zeroPos + (onePos - zeroPos) * cumCount[dragIndex] / n);
			int xPos = kLeftAxisWidth + theAxis.numValToRawPosition(classStart[dragIndex]);
			
			int count = (int)Math.round(cumCount[dragIndex]);
			g.setColor(Color.red);
			g.drawLine(kLeftAxisWidth, yPos, xPos, yPos);
			g.drawLine(kLeftAxisWidth, yPos, kLeftAxisWidth + kArrowHead, yPos + kArrowHead);
			g.drawLine(kLeftAxisWidth, yPos, kLeftAxisWidth + kArrowHead, yPos - kArrowHead);
			
			NumValue nValue = new NumValue(n, 0);
			NumValue countValue = new NumValue(count, 0);
			int fractionWidth = Math.max(nValue.stringWidth(g), countValue.stringWidth(g));
			int fractionCentre = kLeftAxisWidth + kArrowHead + 3 + fractionWidth / 2;
			int fractionBar = Math.max(yPos, ascent + 3);
			g.drawLine(fractionCentre - fractionWidth / 2, fractionBar,
																							fractionCentre + fractionWidth / 2, fractionBar);
			countValue.drawCentred(g, fractionCentre, fractionBar - 3);
			nValue.drawCentred(g, fractionCentre, fractionBar + ascent + 2);
		}
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		paintBackground(g);
		
		if (displayType == HISTOGRAM)
			drawHistogram(g);
		else
			drawDotPlot(g);
		drawCumulative(g);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		int n = ((NumVariable)getVariable(yKey)).noOfValues();
		int zeroPos = getCumZeroPos();
		int onePos = getCumOnePos();
		
		for (int i=0 ; i<classStart.length ; i++) {
			int xPos = kLeftAxisWidth + theAxis.numValToRawPosition(classStart[i]);
			int yPos = (int)Math.round(zeroPos + (onePos - zeroPos) * cumCount[i] / n);
			
			int xOffset = xPos - x;
			int yOffset = yPos - y;
			
			if (xOffset * xOffset + yOffset * yOffset < kHitSlop * kHitSlop)
				return new VertDragPosInfo(y, i, yOffset);
		}
		return null;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || x > getSize().width || y + hitOffset < 0 || y + hitOffset > getSize().height - getBottomHeight())
			return null;
		else
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
			int n = ((NumVariable)getVariable(yKey)).noOfValues();
			int zeroPos = getCumZeroPos();
			int onePos = getCumOnePos();
			
			double hitCount = n * (double)(zeroPos - dragPos.y - hitOffset) / (zeroPos - onePos);
			if (hitCount >= 0 && hitCount <= n)
				cumCount[dragIndex] = hitCount;
			
			correct = null;
			
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		cumCount[dragIndex] = Math.rint(cumCount[dragIndex]);
		dragIndex = -1;
		repaint();
	}
	
}
	
