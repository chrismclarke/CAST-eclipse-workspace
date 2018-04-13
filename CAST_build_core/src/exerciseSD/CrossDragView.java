package exerciseSD;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import coreVariables.*;
import exercise2.*;


public class CrossDragView extends CoreDragView {
//	static public final String CROSS_DRAG = "crossDrag";
	
	static final private double kSDSlopFactor = 2.0;		//	allow guess to be further from exact than for histo
	
	static final private int kCrossBoxWidth = 10;
//	static final private int kCrossBoxWidth = 20;
//	static final private int kMaxHitSlop = 4;
	
//	static final private double kTargetEps = 1e-5;
//	static final private int kMaxIterations = 5;
	
	private int nValues = 0;
	private String sampleKey, scalingKey;
	
	@SuppressWarnings("unused")
	private int hitColumnIndex, hitRowIndex, xOffset, yOffset;
	private int dragX, dragY;
	private boolean doingDrag = false;
	
	public CrossDragView(DataSet theData, XApplet applet, NumCatAxis valAxis, String yKey) {
		super(theData, applet, valAxis, yKey);
	}
	
	public CrossDragView(DataSet theData, XApplet applet, NumCatAxis valAxis,
																						String sampleKey, String scalingKey) {
		this(theData, applet, valAxis, null);
		this.sampleKey = sampleKey;
		this.scalingKey = scalingKey;
	}
	
	public void setNValues(int nValues) {
		this.nValues = nValues;
	}

//-------------------------------------------------------------------

	public String getStatus() {
		initialise();
		String s = "";
		for (int i=0 ; i<classCount.length ; i++)
			s += (int)Math.round(classCount[i]) + " ";
		
		s += "*";
		int decimals = axis.getLabelDecimals() + 4;
		for (int i=0 ; i<classStart.length ; i++)
			s += new NumValue(classStart[i], decimals) + " ";
		
		return s;
	}
	
	public void setStatus(String status) {
		StringTokenizer st = new StringTokenizer(status, "*");
		
		StringTokenizer st2 = new StringTokenizer(st.nextToken());
		classCount = new double[st2.countTokens()];
		for (int i=0 ; i<classCount.length ; i++)
			classCount[i] = Double.parseDouble(st2.nextToken());
		
		st2 = new StringTokenizer(st.nextToken());
		classStart = new double[st2.countTokens()];
		for (int i=0 ; i<classStart.length ; i++)
			classStart[i] = Double.parseDouble(st2.nextToken());
		
		initialised = true;
		repaint();
	}

//-------------------------------------------------------------------
	
	public void setMeanSD(double targetMean, double targetSD) {
		NumSampleVariable zVar = (NumSampleVariable)getVariable(sampleKey);
		zVar.setSampleSize(nValues);
		zVar.generateNextSample();
		
		ValueEnumeration ze = zVar.values();
		double sz = 0.0;
		double szz = 0.0;
		int n = 0;
		while (ze.hasMoreValues()) {
			double z = ze.nextDouble();
			sz += z;
			szz += z * z;
			n ++;
		}
		double zMean = sz / n;
		double zSD = Math.sqrt((szz - sz * zMean) / n);			//	getSDFromGraph() spreads out values a little
		
		ScaledVariable yVar = (ScaledVariable)getVariable(scalingKey);
		yVar.setScale(targetMean - zMean * targetSD / zSD, targetSD / zSD, 0);
		yVar.noteVariableChange(sampleKey);
		
		yKey = scalingKey;
		classCount = countClasses(classStart);
		
		yKey = null;
	}
	
	protected double[] defaultCounts(double localClassStart[]) {
		int noOfClasses = localClassStart.length - 1;
		double localClassCount[] = new double[noOfClasses];
		
		int valsPerClass = nValues / (noOfClasses / 4);
		int midClass = noOfClasses / 2;
		
		localClassCount[midClass] = valsPerClass;
		int valuesLeft = nValues - valsPerClass;
		for (int i=1 ; i<noOfClasses / 2 ; i++) {
			if (valuesLeft < valsPerClass * 2)  {
				localClassCount[midClass - i] = valuesLeft / 2;
				localClassCount[midClass + i] = valuesLeft - valuesLeft / 2;
				break;
			}
			localClassCount[midClass - i] = localClassCount[midClass + i] = valsPerClass;
			valuesLeft -= 2 * valsPerClass;
		}
		return localClassCount;
	}
	
	protected double[] defaultClasses() {
		return initialiseClasses();
	}
	
	protected double[] initialiseClasses() {
		int noOfClasses = (axis.axisLength - 1) / kCrossBoxWidth + 1;			//	round up
		
		double localClassStart[] = new double[noOfClasses + 1];
		for (int i=0 ; i<=noOfClasses ; i++)
			try {
				localClassStart[i] = axis.positionToNumVal(i * kCrossBoxWidth);
			} catch (AxisException e) {
				localClassStart[i] = axis.maxOnAxis;	//		must be AxisException.TOO_HIGH_ERROR
			}
		return localClassStart;
	}
	
//-------------------------------------------------------------------
	
	public double getExtremeSD(boolean maxNotMin) {
		double approxSD = super.getExtremeSD(maxNotMin);
		double exactSD = getSDFromGraph();
		
		return exactSD + (approxSD - exactSD) * kSDSlopFactor;
	}
	
//-------------------------------------------------------------------
	
	public void paintView(Graphics g) {
		initialise();
		
		drawTopBorder(g);
		
		Point p = null;
//		int boxLeft = 0;
		g.setColor(getForeground());
		
		int lowDragColumn = -1;
		int highDragColumn = -1;
		int lowSpacePix = 0;
		int highSpacePix = 0;
		if (doingDrag) {
			lowDragColumn = dragX / kCrossBoxWidth;
			highDragColumn = lowDragColumn + 1;
			highSpacePix = dragX - lowDragColumn * kCrossBoxWidth;
			lowSpacePix = kCrossBoxWidth - highSpacePix;
		}
		
		int dragRow = doingDrag ? (dragY + kCrossBoxWidth / 2) / kCrossBoxWidth : -1;
		
		for (int i=0 ; i<classCount.length ; i++) {
			int count = (int)Math.round(classCount[i]);
			int boxLeft = axis.numValToRawPosition(classStart[i]);
			
			if (doingDrag && i == hitColumnIndex)
				count --;
			int boxBottom = 0;
			for (int j=0 ; j<count ; j++) {
				if (doingDrag && i == lowDragColumn && j == dragRow)
					boxBottom += lowSpacePix;
				else if (doingDrag && i == highDragColumn && j == dragRow)
					boxBottom += highSpacePix;
					
				p = translateToScreen(boxLeft, boxBottom, p);
				drawBox(g, p.x, p.y);
				
				boxBottom += kCrossBoxWidth;
			}
//			
//			boxLeft += kCrossBoxWidth;
		}
		
		if (doingDrag) {
			p = translateToScreen(dragX, dragY, p);
			g.setColor(Color.red);
			drawBox(g, p.x, p.y);
		}
	}
	
	private void drawBox(Graphics g, int left, int bottom) {
		g.drawLine(left + 1, bottom - 1, left + kCrossBoxWidth - 1, bottom - kCrossBoxWidth + 1);
		g.drawLine(left + 1, bottom - kCrossBoxWidth + 1, left + kCrossBoxWidth - 1, bottom - 1);
	}

//-----------------------------------------------------------------------------------
	
	protected PositionInfo getInitialPosition(int x, int y) {
		initialise();
		
		Point p = translateFromScreen(x, y, null);
		int hitColumnIndex = p.x / kCrossBoxWidth;
		int hitRowIndex = p.y / kCrossBoxWidth;
		
		if (hitRowIndex >= classCount[hitColumnIndex])
			return null;
		
		int xOffset = p.x - hitColumnIndex * kCrossBoxWidth;		//	offset from bottom left
		int yOffset = p.y - hitRowIndex * kCrossBoxWidth;
		
		return new CrossHitPosInfo(p.x, p.y, hitColumnIndex, hitRowIndex, xOffset, yOffset);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		Point p = translateFromScreen(x, y, null);
		
		if (p.x < 0 || x >= axis.axisLength)
			return null;
		
		return new CrossHitPosInfo(p.x, p.y);		//	in local coordinates
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		CrossHitPosInfo hitPos = (CrossHitPosInfo)startInfo;
		hitColumnIndex = hitPos.columnIndex;
		hitRowIndex = hitPos.rowIndex;
		xOffset = hitPos.xOffset;
		yOffset = hitPos.yOffset;
		dragX = hitPos.x - xOffset;
		dragY = hitPos.y - yOffset;
		doingDrag = true;
		
		repaint();
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null) {
			CrossHitPosInfo dragPos = (CrossHitPosInfo)toPos;
			dragX = dragPos.x - xOffset;
			dragY = dragPos.y - yOffset;
			repaint();
			
			((ExerciseApplet)getApplet()).noteChangedWorking();
		}
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (endPos != null) {
			CrossHitPosInfo endDragPos = (CrossHitPosInfo)endPos;
			int endColumnIndex = (endDragPos.x - xOffset + kCrossBoxWidth / 2) / kCrossBoxWidth;
			
			classCount[hitColumnIndex] -= 1.0;
			classCount[endColumnIndex] += 1.0;
		}
		doingDrag = false;
		repaint();
	}
	
}
	
