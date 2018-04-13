package dotPlot;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;


public class StackingDotPlotView extends DotPlotView {
	static private final int kFramesPerSec = 10;
	static private final int kShadeFrames = 10;
	static private final int kGroupingFrames = 30;
	static private final int kStackingFrames = 24;
	static public final int kStackedIndex = 2 * kShadeFrames + kGroupingFrames + kStackingFrames;
	
	
	static protected final int kOffAxisPos = -100;		//	a value small enough to be off the axes after grouping
	
	static final private int kRed = 0x99;
	static final private int kGreen = 0xCC;
	static final private int kBlue = 0xFF;		//	pale blue
	
	private Color gridColor[] = new Color[kShadeFrames];
	
	protected int initAxisPos[] = null;
	protected int groupVert[] = null;
	protected int groupHoriz[] = null;
	
	public StackingDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
									double initialJittering) {
		super(theData, applet, theAxis, initialJittering);
		for (int i=0 ; i<kShadeFrames ; i++)
			gridColor[i] = new Color(0xFF - (i+1) * (0xFF - kRed) / kShadeFrames,
												0xFF - (i+1) * (0xFF - kGreen) / kShadeFrames,
												0xFF - (i+1) * (0xFF - kBlue) / kShadeFrames);
	}
	
	public StackingDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis) {
		this(theData, applet, theAxis, 1.0);
	}
	
	protected boolean axisPosInitialised() {
		return initAxisPos != null;
	}
	
	public void reinitialiseAfterTransform() {
		if (initAxisPos == null)
			findInitPositions();
		else {
			NumVariable variable = getNumVariable();
			int noOfValues = variable.noOfValues();
			
			for (int i=0 ; i<noOfValues ; i++)
				try {
					initAxisPos[i] = axis.numValToPosition(variable.doubleValueAt(i));
				} catch (AxisException ex) {
					initAxisPos[i] = kOffAxisPos;
				}
			
			int groupSize = getCrossPix() * 2 + 3;
			int noOfGroups = (axis.getAxisLength() - 1) / groupSize + 1;
			setStackingInfo(noOfValues, groupSize, noOfGroups);
		}
	}
	
	protected void findInitPositions() {
		NumVariable variable = getNumVariable();
		int noOfValues = variable.noOfValues();
		
		if (initAxisPos == null) {
			initAxisPos = new int[noOfValues];
			
			for (int i=0 ; i<noOfValues ; i++)
				try {
					initAxisPos[i] = axis.numValToPosition(variable.doubleValueAt(i));
				} catch (AxisException ex) {
					initAxisPos[i] = kOffAxisPos;
				}
		}
		
		int groupSize = getCrossPix() * 2 + 3;
		int noOfGroups = (axis.getAxisLength() - 1) / groupSize + 1;
		
		if (groupHoriz == null)
			groupHoriz = new int[noOfValues];
		if (groupVert == null)
			groupVert = new int[noOfValues];
		
		setStackingInfo(noOfValues, groupSize, noOfGroups);
	}
	
	private void setStackingInfo(int noOfValues, int groupSize, int noOfGroups) {
		for (int i=0 ; i<noOfValues ; i++)
			groupHoriz[i] = initAxisPos[i] / groupSize;
		
		for (int i=0 ; i<noOfValues ; i++)
			groupVert[i] = -1;
		
		int currentGroupVert = getCrossPix() + 1;
		boolean gotVal = true;
		int minPos[] = new int[noOfGroups];
		
		while (gotVal) {
			for (int i=0 ; i<noOfGroups ; i++)
				minPos[i] = -1;
			for (int i=0 ; i<noOfValues ; i++)
				if (groupVert[i] == -1) {
					int j0 = (jittering == null) ? 0 : jittering[i];
					int j1 = (minPos[groupHoriz[i]] == -1) ? (j0 + 1)
										: (jittering == null) ? 0 : jittering[minPos[groupHoriz[i]]];
					if (groupHoriz[i] >= 0 && groupHoriz[i] < noOfGroups && j0 < j1)
						minPos[groupHoriz[i]] = i;
				}
			gotVal = false;
			for (int i=0 ; i<noOfGroups ; i++)
				if (minPos[i] != -1) {
					groupVert[minPos[i]] = currentGroupVert;
					gotVal = true;
				}
			currentGroupVert += (groupSize - 1);
		}
		
		for (int i=0 ; i<noOfValues ; i++)
			groupHoriz[i] = groupHoriz[i] * groupSize + getCrossPix() + 1;
	}
	
	protected Point getScreenPoint(int index, int currentFrame, Point thePoint) {
		if (initAxisPos[index] == kOffAxisPos)
			return null;
		int horizPos, vertPos;
		if (currentFrame <= kShadeFrames)
			horizPos = initAxisPos[index];
		else {
			if (currentFrame >= kGroupingFrames + kShadeFrames)
				horizPos = groupHoriz[index];
			else
				horizPos = (initAxisPos[index] * (kGroupingFrames + kShadeFrames - currentFrame)
										+ groupHoriz[index] * (currentFrame - kShadeFrames)) / kGroupingFrames;
		}
		
		int vertJitterPos = (currentJitter > 0 && jittering != null) ? (currentJitter * jittering[index]) >> 14 : 0;
		if (currentFrame <= kGroupingFrames + kShadeFrames)
			vertPos = vertJitterPos;
		else if (currentFrame >= kStackedIndex - kShadeFrames)
			vertPos = groupVert[index];
		else
			vertPos = (vertJitterPos * (kStackedIndex - kShadeFrames - currentFrame) + groupVert[index]
							* (currentFrame - kGroupingFrames - kShadeFrames)) / kStackingFrames;
		
		return translateToScreen(horizPos, vertPos, thePoint);
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		return getScreenPoint(index, getCurrentFrame(), thePoint);
	}
	
	protected void paintGrid(Graphics g, int currentFrame) {
		if (currentFrame > 0 && currentFrame < kStackedIndex) {
			int colourIndex = Math.min(kShadeFrames, Math.min(currentFrame,
																			kStackedIndex - currentFrame)) - 1;
			
			g.setColor(gridColor[colourIndex]);
			int maxHorizPos = getSize().width - getViewBorder().right - 1;
			int horizPos = getViewBorder().left;
			while (true) {
				if (horizPos > maxHorizPos)
					break;
				g.drawLine(horizPos, 0, horizPos, getSize().height - 1);
				horizPos += getCrossPix() * 2 + 2;
				if (horizPos > maxHorizPos)
					break;
				g.drawLine(horizPos, 0, horizPos, getSize().height - 1);
				horizPos ++;
			}
		}
	}
	
	public void paintView(Graphics g) {
		checkJittering();
		
		if (!axisPosInitialised())
			findInitPositions();
		
		int currentFrame = getCurrentFrame();		//	it may change during update, so use init value
		
		paintGrid(g, currentFrame);
		
		g.setColor(getForeground());
		Point crossPoint = null;
		FlagEnumeration fe = getSelection().getEnumeration();
		for (int i=0 ; i<initAxisPos.length ; i++) { 
			crossPoint = getScreenPoint(i, currentFrame, crossPoint);
			boolean nextSel = fe.nextFlag();
			if (crossPoint != null) {
				if (nextSel) {
					g.setColor(Color.red);
					drawCrossBackground(g, crossPoint);
					g.setColor(getForeground());
				}
				drawMark(g, crossPoint, groupIndex(i));
			}
		}
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	public void setFinalFrame() {
		setFrame(kStackedIndex);
	}
	
	public void initialiseToFinalFrame() {
		setInitialFrame(kStackedIndex);
	}
	
	public void setCrossSize(int crossSize, XSlider controller) {
		synchronized(getData()) {
			setFrame(0, controller);
			super.setCrossSize(crossSize);
			repaint();
		}
		if (axisPosInitialised())
			findInitPositions();				//	Don't try to find axis positions until painted once
	}
	
	public void doStackingAnimation(XSlider controller) {
		if (axisPosInitialised())			//	Don't try animation until painted once
			animateFrames(1, kStackedIndex - 1, kFramesPerSec, controller);
	}
}