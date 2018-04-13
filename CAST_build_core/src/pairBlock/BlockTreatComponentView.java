package pairBlock;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import coreGraphics.*;

import pairBlockProg.*;


public class BlockTreatComponentView extends DotPlotView {
	
	static final public int NO_COMPONENT_DISPLAY = -1;
	static final public int TREAT_COMPONENT_DISPLAY = 0;
	static final public int BLOCK_COMPONENT_DISPLAY = 1;
	static final public int TOTAL_COMPONENT_DISPLAY = 2;
	static final public int GREY_TOTAL_COMPONENT_DISPLAY = 3;
	
	static final public int OVERALL_MEAN_ONLY = 0;
	static final public int TREAT_MEAN = 1;
	static final public int BLOCK_MEAN = 2;
	
	static final private Color kTreatMeanColor = new Color(0x99CCFF);
	static final private Color kOverallMeanColor = new Color(0xCCCCCC);
	static final private Color kPaleGreyComponentColor = new Color(0xE2E2E2);
	
	static final private int kHalfMeanGap = 5;
	
	static final private LabelValue kOverallMeanLabel = new LabelValue("Overall mean");
	static final private LabelValue kBlockMeanLabel = new LabelValue("Block mean");
	
	static final protected int kFinalFrame = 40;
	static final private int kFramesPerSec = 20;
	
	private XApplet applet;
	
	private String yKey, treatKey, blockKey;
	private HorizAxis yAxis;
	private VertAxis treatAxis;
	
	private int componentDisplay, meanDisplay;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXCat[] = null;
	private int scalePercent = 100;
	
	private boolean initialised = false;
	
	private double overallMean;
	private double[] treatMean;
	private double[] blockMean;
	
	private boolean animateToRemove;
	private boolean removeBlocksNotTreats;
	
	public BlockTreatComponentView(DataSet theData, XApplet applet, HorizAxis yAxis, VertAxis treatAxis, String yKey,
							String treatKey, String blockKey, int componentDisplay, int meanDisplay) {
		super(theData, applet, yAxis, 0.0);
		this.applet = applet;
		this.yKey = yKey;
		this.treatKey = treatKey;
		this.blockKey = blockKey;
		this.yAxis = yAxis;
		this.treatAxis = treatAxis;
		this.componentDisplay = componentDisplay;
		this.meanDisplay = meanDisplay;
		setActiveNumVariable(yKey);
	}
	
	public void changeComponentDisplay(int newComponentDisplay, int newMeanDisplay) {
		componentDisplay = newComponentDisplay;
		meanDisplay = newMeanDisplay;
		repaint();
	}
	
	private boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		ValueEnumeration be = blockVar.values();
		
		int n = 0;
		treatMean = new double[treatVar.noOfCategories()];
		int treatN[] = new int[treatMean.length];
		blockMean = new double[blockVar.noOfCategories()];
		int blockN[] = new int[blockMean.length];
		
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			n ++;
			int treat = treatVar.labelIndex(te.nextValue());
			treatMean[treat] += y;
			treatN[treat] ++;
			int block = blockVar.labelIndex(be.nextValue());
			blockMean[block] += y;
			blockN[block] ++;
		}
		overallMean = sy / n;
		for (int i=0 ; i<treatMean.length ; i++)
			treatMean[i] /= treatN[i];
		for (int i=0 ; i<blockMean.length ; i++)
			blockMean[i] /= blockN[i];
		
		initialised = true;
		return true;
	}
	
	private void initialiseJittering(CatVariable xVar) {
		int noOfVals = xVar.noOfValues();
		if (jitter == null || jitter.length != noOfVals)
			jitter = new int[noOfVals];
		int noOfCats = xVar.noOfCategories();
		if (noInXCat == null || noInXCat.length != noOfCats)
			noInXCat = new int[noOfCats];
		else
			for (int i=0 ; i<noOfCats ; i++)
				noInXCat[i] = 0;
		
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xVar.getItemCategory(i);
			jitter[i] = noInXCat[xCat];
			noInXCat[xCat] ++;
		}
		int usedHoriz = noOfVals * 2 + noOfCats * getCrossSize() * 2;
		int availableHoriz = (getSize().width * 3) / 5;
		scalePercent = Math.min(150, (availableHoriz * 100) / usedHoriz);
		
		jitteringInitialised = true;
	}
	
	
	private void drawOverallMean(Graphics g, Color meanColor) {
		int meanPos = yAxis.numValToRawPosition(overallMean);
		int meanOnScreen = translateToScreen(meanPos, 0, null).x;
		
		g.setColor(meanColor);
		g.drawLine(meanOnScreen, 0, meanOnScreen, getSize().height - 1);
		
		g.setColor(Color.yellow);
		int stringWidth = kOverallMeanLabel.stringWidth(g);
		int baseline = g.getFontMetrics().getAscent() + 2;
		g.fillRect(meanOnScreen - stringWidth / 2 - 4, 0, stringWidth + 8, baseline + 3);
		
		g.setColor(getForeground());
		kOverallMeanLabel.drawCentred(g, meanOnScreen, baseline);
	}
	
	
	private void drawOverallMeanBackground(Graphics g) {
		int meanPos = yAxis.numValToRawPosition(overallMean);
		int meanOnScreen = translateToScreen(meanPos, 0, null).x;
		
		g.setColor(Color.yellow);
		g.drawLine(meanOnScreen - 1, 0, meanOnScreen - 1, getSize().height - 1);
		g.drawLine(meanOnScreen + 1, 0, meanOnScreen + 1, getSize().height - 1);
	}
	
	private void drawTreatMeans(Graphics g, Color treatMeanColor) {
		g.setColor(treatMeanColor);
		Point p = null;
		
		int treatSpacing = treatAxis.catValToPosition(1) - treatAxis.catValToPosition(0);
		int offset = treatSpacing / 2 - kHalfMeanGap;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		double treatShrinkPropn = 0.0;
		if (yVar instanceof RemoveBlockVariable)
			treatShrinkPropn = ((RemoveBlockVariable)yVar).getTreatEffectProportion();
		
		for (int i=0 ; i<treatMean.length ; i++) {
			int meanPos = yAxis.numValToRawPosition(treatMean[i]
																		+ treatShrinkPropn * (overallMean - treatMean[i]));
			int treatPos = treatAxis.catValToPosition(i);
			p = translateToScreen(meanPos, treatPos, p);
			
			g.drawLine(p.x, p.y - offset, p.x, p.y + offset);
		}
	}
	
	private void drawBlockMeans(Graphics g, int selectedBlock) {
		Point p = null;
		for (int i=0 ; i<blockMean.length ; i++)
			if (selectedBlock == i || selectedBlock == -1) {
				NumVariable yVar = (NumVariable)getVariable(yKey);
				double blockShrinkPropn = 0.0;
				if (yVar instanceof RemoveBlockVariable)
					blockShrinkPropn = ((RemoveBlockVariable)yVar).getBlockEffectProportion();
					
				int meanPos = yAxis.numValToRawPosition(blockMean[i]
																		+ blockShrinkPropn * (overallMean - blockMean[i]));
				p = translateToScreen(meanPos, 0, p);
				int meanOnScreen = p.x;
				
				Color blockColor = getCrossColor(i);
				g.setColor(mixColors(Color.white, blockColor, 0.7));
				g.drawLine(meanOnScreen, 0, meanOnScreen, getSize().height - 1);
				
				if (selectedBlock == i) {
					g.setColor(Color.white);
					int stringWidth = kBlockMeanLabel.stringWidth(g);
					int baseline = getSize().height - 3;
					int ascent = g.getFontMetrics().getAscent();
					g.fillRect(meanOnScreen - stringWidth / 2 - 4, baseline - ascent - 2, stringWidth + 8, ascent + 5);
					
					g.setColor(blockColor);
					kBlockMeanLabel.drawCentred(g, meanOnScreen, baseline);
				}
			}
	}
	
	private int getXPos(int treat, int index) {
		int treatPos = treatAxis.catValToPosition(treat);
		
		int offset = ((2 * jitter[index] - noInXCat[treat]) * scalePercent) / 100 + 1;
		return treatPos + offset;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int treat = treatVar.labelIndex(treatVar.valueAt(index));
		int xPos = getXPos(treat, index);
		
		int yPos = yAxis.numValToRawPosition(theVal.toDouble());
		
		return translateToScreen(yPos, xPos, thePoint);
	}
	
	private void drawComponents(Graphics g, int selectedBlock, int display) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		ValueEnumeration be = blockVar.values();
		
		int meanPos = yAxis.numValToRawPosition(overallMean);
		int meanOnScreen = translateToScreen(meanPos, 0, null).x;
		
		double blockShrinkPropn = 0.0;
		if (yVar instanceof RemoveBlockVariable)
			blockShrinkPropn = ((RemoveBlockVariable)yVar).getBlockEffectProportion();
		double treatShrinkPropn = 0.0;
		if (yVar instanceof RemoveBlockVariable)
			treatShrinkPropn = ((RemoveBlockVariable)yVar).getTreatEffectProportion();
		
		Point p = null;
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int treat = treatVar.labelIndex(te.nextValue());
			int block = blockVar.labelIndex(be.nextValue());
			
			if (selectedBlock == -1 || selectedBlock == block) {
				int xPos = getXPos(treat, index);
				
				double compEnd = (display == TREAT_COMPONENT_DISPLAY)
										? treatMean[treat] + treatShrinkPropn * (overallMean - treatMean[treat])
								: (display == BLOCK_COMPONENT_DISPLAY)
										? blockMean[block] + blockShrinkPropn * (overallMean - blockMean[block])
								: y;
				int yPos = yAxis.numValToRawPosition(compEnd);
				p  = translateToScreen(yPos, xPos, p);
				
				g.setColor(display == GREY_TOTAL_COMPONENT_DISPLAY ? kPaleGreyComponentColor
																														: getCrossColor(block));
				g.drawLine(p.x, p.y, meanOnScreen, p.y);
			}
			index ++;
		}
	}
	
	private void hiliteSelection(Graphics g, int selectedIndex, NumVariable yVar,
																															CatVariable treatVar) {
		if (selectedIndex >= 0) {
			double y = yVar.doubleValueAt(selectedIndex);
			int treat = treatVar.labelIndex(treatVar.valueAt(selectedIndex));
			
			int yPos = yAxis.numValToRawPosition(y);
			int xPos = getXPos(treat, selectedIndex);
			Point p = translateToScreen(yPos, xPos, null);
			
			if (p != null) {
				g.setColor(Color.red);
				drawCrossBackground(g, p);
				g.setColor(getForeground());
			}
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		
		if (!jitteringInitialised)
			initialiseJittering(treatVar);
		
		int selectedIndex = getSelection().findSingleSetFlag();
		int selectedBlock = -1;
		if (selectedIndex >= 0)
			selectedBlock = blockVar.labelIndex(blockVar.valueAt(selectedIndex));
		
		drawOverallMeanBackground(g);
		
		if (meanDisplay == TREAT_MEAN)
			drawTreatMeans(g, kTreatMeanColor);
		else if (meanDisplay == BLOCK_MEAN)
			drawBlockMeans(g, selectedBlock);
		
		if (componentDisplay != NO_COMPONENT_DISPLAY
																				&& componentDisplay != TOTAL_COMPONENT_DISPLAY)
			drawComponents(g, selectedBlock, GREY_TOTAL_COMPONENT_DISPLAY);
		
		if (componentDisplay != NO_COMPONENT_DISPLAY)
			drawComponents(g, selectedBlock, componentDisplay);
		
		drawOverallMean(g, kOverallMeanColor);
		
		hiliteSelection(g, selectedIndex, yVar, treatVar);
		
		g.setColor(getForeground());
		Point p = null;
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		ValueEnumeration be = blockVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int treat = treatVar.labelIndex(te.nextValue());
			int block = blockVar.labelIndex(be.nextValue());
			
			if (selectedBlock == -1 || selectedBlock == block) {
				int yPos = yAxis.numValToRawPosition(y);
				int xPos = getXPos(treat, index);
				p = translateToScreen(yPos, xPos, p);
				
				if (p != null)
					drawMark(g, p, block);
			}
			index ++;
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		jitteringInitialised = false;
		initialised = false;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		if (getCurrentFrame() == 0 || getCurrentFrame() == kFinalFrame)
						//	Don't allow during animation or animation is stopped part-way through
			super.mousePressed(e);
	}

//-----------------------------------------------------------------------------------
	
				//		Animation only works when inside a ResidComponentApplet
				//		Relies on yVariable being a RemoveBlockVariable
	
	public void animateRemoveBlock(boolean animateToRemoveBlocks) {
		animateToRemove = animateToRemoveBlocks;
		removeBlocksNotTreats = true;
		animateFrames(animateToRemove ? 0 : kFinalFrame,
													animateToRemove ? kFinalFrame : -kFinalFrame, kFramesPerSec, null);
	}
	
	public void animateRemoveTreat(boolean animateToRemoveTreats) {
		animateToRemove = animateToRemoveTreats;
		removeBlocksNotTreats = false;
		animateFrames(animateToRemove ? 0 : kFinalFrame,
													animateToRemove ? kFinalFrame : -kFinalFrame, kFramesPerSec, null);
	}
	
	protected void drawNextFrame() {
		RemoveBlockVariable yVar = (RemoveBlockVariable)getVariable(yKey);
		if (removeBlocksNotTreats)
			yVar.setBlockEffectProportion(getCurrentFrame() / (double)kFinalFrame);
		else
			yVar.setTreatEffectProportion(getCurrentFrame() / (double)kFinalFrame);
		
		super.drawNextFrame();
		
		if (animateToRemove && getCurrentFrame() == kFinalFrame
																			|| !animateToRemove && getCurrentFrame() == 0)
			((ResidComponentApplet)applet).finishedAnimation(animateToRemove, removeBlocksNotTreats);
	}
	

}