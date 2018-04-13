package coreGraphics;

import java.awt.*;

import dataView.*;
import axis.*;
import random.RandomBits;


public class DotPlotView extends MarginalDataView {
	private static final int kMaxJitter = 50;
	private static final int kMinDotPlotWidth = 30;
	
	static final private int kMaxScreenDist = 9999;
	
//	static public final String DOTPLOT = "dotPlot";
	
	private boolean canDragCrosses = true;
	
	protected int currentJitter = 0;
	protected int jittering[] = null;
	private double initialJittering = 0.0;
	private boolean jitteringInitialised = false;
	
	private int minWidth = kMinDotPlotWidth;
	
	public DotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis, double initialJittering) {
		super(theData, applet, new Insets(5, 5, 5, 5), theAxis);
																//		5 pixels round for crosses to overlap into
		this.initialJittering = initialJittering;
	}
	
	public DotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		this(theData, applet, theAxis, 0.0);
	}
	
	public void setCanDragCrosses(boolean canDragCrosses) {
		this.canDragCrosses = canDragCrosses;
	}
	
	public int minDisplayWidth() {
		return minWidth;
	}
	
	public void setMinDisplayWidth(int minWidth) {
		this.minWidth = minWidth;
	}
	
	protected boolean isBadValue(NumValue theVal) {
		return (theVal == null || Double.isNaN(theVal.toDouble()) || Double.isInfinite(theVal.toDouble()));
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		if (isBadValue(theVal))
			return null;
		try {
			int horizPos = axis.numValToPosition(theVal.toDouble());
			int vertPos = (currentJitter > 0 && jittering != null && index < jittering.length) ? ((currentJitter * jittering[index]) >> 14) : 0;
			return translateToScreen(horizPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected int groupIndex(int itemIndex) {
		return 0;
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		if (thePoint != null)
			drawCrossBackground(g, thePoint);
	}
	
	protected void fiddleColor(Graphics g, int index) {
	}
	
	public void paintView(Graphics g) {
		NumVariable variable = getNumVariable();
		Point thePoint = null;
		
		checkJittering();
		
		g.setColor(Color.red);
		ValueEnumeration e = variable.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		int index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			boolean nextSel = fe.nextFlag();
			if (nextSel) {
				thePoint = getScreenPoint(index, nextVal, thePoint);
				doHilite(g, index, thePoint);
			}
			index++;
		}
		g.setColor(getForeground());
		e = variable.values();
		index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			thePoint = getScreenPoint(index, nextVal, thePoint);
			if (thePoint != null) {
				fiddleColor(g, index);
				drawMark(g, thePoint, groupIndex(index));
			}
			index++;
		}
	}
	
	protected void initialiseJittering() {
		if (!jitteringInitialised) {
			setJitter(initialJittering);
			jitteringInitialised = true;
		}
	}
	
	protected void checkJittering() {
		initialiseJittering();
		int dataLength = getNumVariable().noOfValues();
		if (currentJitter > 0 && (jittering == null || jittering.length != dataLength)) {
			RandomBits generator = new RandomBits(14, dataLength);			//	between 0 and 2^14 = 16384
			jittering = generator.generate();
		}
	}
	
	public void newRandomJittering() {
		jittering = null;
		crossPos = null;
		repaint();
	}
	
	protected int getMaxJitter() {
		return Math.min(kMaxJitter, getDisplayWidth() - getDisplayBorderNearAxis() - getDisplayBorderAwayAxis());
	}
	
	public void setJitter(double fraction) {
		initialJittering = fraction;		//	in case it has not already been initialised
		int maxJitter = getMaxJitter();
		currentJitter = (int)(fraction * maxJitter);
		crossPos = null;
		repaint();
	}

//-----------------------------------------------------------------------------------

	protected void doAddValues(Graphics g, int noOfValues) {
		if (jittering != null) {
			int oldSize = jittering.length;
			int oldJitter[] = jittering;
			jittering = new int[noOfValues];
			int noOfCopyVals = Math.min(noOfValues, oldSize);
			System.arraycopy(oldJitter, 0, jittering, 0, noOfCopyVals);
			
			if (oldSize < noOfValues) {
				RandomBits generator = new RandomBits(14, noOfValues - oldSize);			//	between 0 and 2^14 = 16384
				int newJitters[] = generator.generate();
				for (int i=0 ; i<newJitters.length ; i++)
					jittering[oldSize + i] = newJitters[i];
			}
		}
		
		repaint();
	}

//-----------------------------------------------------------------------------------
	
	private Point crossPos[];
	private static final int kMinHitDist = 9;
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return canDragCrosses;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (crossPos == null) {
			NumVariable variable = getNumVariable();
			int noOfVals = variable.noOfValues();
			crossPos = new Point[noOfVals];
			for (int i=0 ; i<noOfVals ; i++)
				crossPos[i] = getScreenPoint(i, (NumValue)(variable.valueAt(i)), null);
		}
		
		int minIndex = -1;
		int minDist = 0;
		boolean gotPoint = false;
		for (int i=0 ; i<crossPos.length ; i++)
			if (crossPos[i] != null) {
				int xDist = crossPos[i].x - x;
				int yDist = crossPos[i].y - y;
				if (xDist < -kMaxScreenDist || xDist > kMaxScreenDist
								|| yDist < -kMaxScreenDist || yDist > kMaxScreenDist)
					continue;		//		It is possible for crossPos to be so far off screen that
										//		dist can overflow and turn negative
					
				int dist = xDist*xDist + yDist*yDist;
				if (!gotPoint) {
					gotPoint = true;
					minIndex = i;
					minDist = dist;
				}
				else if (dist < minDist) {
					minIndex = i;
					minDist = dist;
				}
			}
		if (gotPoint && minDist < kMinHitDist) {
//			System.out.println("hit index = " + minIndex);
			return new IndexPosInfo(minIndex);
		}
		else
			return null;
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		super.endDrag(startPos, endPos);
		crossPos = null;
	}
	
}
	
