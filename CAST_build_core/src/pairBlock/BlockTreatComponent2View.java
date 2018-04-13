package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class BlockTreatComponent2View extends DotPlotView {
	static final public int ADD_REMOVE_BLOCK = 0;
	static final public int BLOCK_TO_TREAT = 1;
	static final public int ADD_REMOVE_TREAT = 2;
	
	static final public int START = 0;
	static final public int MIDDLE = 1;
	static final public int END = 2;
	
//	static final private Color kTreatMeanColor = new Color(0x99CCFF);
	static final private Color kOverallMeanColor = new Color(0xCCCCCC);
	static final private Color kComponentColor = new Color(0x993333);
	static final private Color kOddBandBackground = new Color(0xF5F5F5);
	static final private Color kMeanLineColor = new Color(0xFF9999);
	
//	static final private int kHalfMeanGap = 5;
	
	static final public int kFinalFrame = 40;
	static final private int kFramesPerSec = 20;
	
//	private XApplet applet;
	
	private String yKey, treatKey, blockKey;
	private HorizAxis yAxis;
	private MultiVertAxis treatBlockAxis;
	
	private int transitionType = ADD_REMOVE_BLOCK;
	
	private boolean jitteringInitialised = false;
	private int blockJitter[] = null;
	private int treatJitter[] = null;
	
	private int noInBlock[], noInTreat[];
	private int blockScalePercent = 100;
	private int treatScalePercent = 100;
	
	private boolean initialised = false;
	
	private double overallMean;
	
	private boolean showResidNotExplained = true;
	
	public BlockTreatComponent2View(DataSet theData, XApplet applet, HorizAxis yAxis,
							MultiVertAxis treatBlockAxis, String yKey, String blockKey, String treatKey) {
		super(theData, applet, yAxis, 0.0);
//		this.applet = applet;
		this.yKey = yKey;
		this.treatKey = treatKey;
		this.blockKey = blockKey;
		this.yAxis = yAxis;
		this.treatBlockAxis = treatBlockAxis;
		
		setActiveNumVariable(yKey);
	}
	
	public void setShowResidNotExplained(boolean showResidNotExplained) {
		this.showResidNotExplained = showResidNotExplained;
	}
	
	public void setTransitionType(int transitionType) {
		this.transitionType = transitionType;
	}
	
	public int getTransitionType() {
		return transitionType;
	}
	
	public int getTransitionStage() {
		int currentFrame = getCurrentFrame();
		return (currentFrame == 0) ? START : (currentFrame == kFinalFrame) ? END : MIDDLE;
	}
	
	public void animateTransition(int newTransitionType) {
		int transitionStage = getTransitionStage();
		if (transitionStage == MIDDLE)
			return;
		
		int currentTransitionStart = (transitionStage == END)? (transitionType + 1) : transitionType;
		
		transitionType = newTransitionType;
		boolean add = currentTransitionStart == newTransitionType;
		
		animateFrames(add ? 1 : (kFinalFrame - 1), add ? (kFinalFrame - 1) : (1 - kFinalFrame),
																																kFramesPerSec, null);
	}
	
	private boolean initialise() {
		if (initialised)
			return false;
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		
		int n = 0;
		double sy = 0.0;
		while (ye.hasMoreValues()) {
			sy += ye.nextDouble();
			n ++;
		}
		overallMean = sy / n;
		
		initialised = true;
		return true;
	}
	
	private void setupJittering(CatVariable xVar, int[] oldJitter, int[] noInXCat) {
		int noOfVals = xVar.noOfValues();
		for (int i=0 ; i<noOfVals ; i++) {
			int xCat = xVar.getItemCategory(i);
			oldJitter[i] = noInXCat[xCat];
			noInXCat[xCat] ++;
		}
	}
	
	private void initialiseJittering(CatVariable blockVar, CatVariable treatVar) {
		int nVals = blockVar.noOfValues();
		blockJitter = new int[nVals];
		treatJitter = new int[nVals];
		
		int noOfBlocks = blockVar.noOfCategories();
		noInBlock = new int[noOfBlocks];
		int noOfTreats = treatVar.noOfCategories();
		noInTreat = new int[noOfTreats];
		
		setupJittering(blockVar, blockJitter, noInBlock);
		setupJittering(treatVar, treatJitter, noInTreat);
		
		int availableHoriz = (getSize().width * 3) / 5;
		
		int usedBlockHoriz = nVals * 2 + noOfBlocks * getCrossSize() * 2;
		blockScalePercent = Math.min(150, (availableHoriz * 100) / usedBlockHoriz);
		
		int usedTreatHoriz = nVals * 2 + noOfTreats * getCrossSize() * 2;
		treatScalePercent = Math.min(150, (availableHoriz * 100) / usedTreatHoriz);
		
		jitteringInitialised = true;
	}
	
	private int getXPos(int block, int treat, int nBlocks, int nTreats, int index) {
		int blockPos = treatBlockAxis.catValToPosition(block, nBlocks);
		int blockOffset = ((2 * blockJitter[index] - noInBlock[block]) * blockScalePercent) / 100 + 1;
		blockPos += blockOffset;
		
		int treatPos = treatBlockAxis.catValToPosition(treat, nTreats);
		int treatOffset = ((2 * treatJitter[index] - noInTreat[treat]) * treatScalePercent) / 100 + 1;
		treatPos += treatOffset;
		
		switch (transitionType) {
			case ADD_REMOVE_BLOCK:
				return blockPos;
			case ADD_REMOVE_TREAT:
				return treatPos;
			case BLOCK_TO_TREAT:
				int currentFrame = getCurrentFrame();
				return (blockPos * (kFinalFrame - currentFrame) + treatPos * currentFrame) / kFinalFrame;
		}
		return 0;
	}
	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		int treat = treatVar.labelIndex(treatVar.valueAt(index));
		int nTreats = treatVar.noOfCategories();
		
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int block = blockVar.labelIndex(blockVar.valueAt(index));
		int nBlocks = blockVar.noOfCategories();
		
		int xPos = getXPos(block, treat, nBlocks, nTreats, index);
		
		int yPos = yAxis.numValToRawPosition(theVal.toDouble());
		
		return translateToScreen(yPos, xPos, thePoint);
	}
	
	
	private void drawOverallMean(Graphics g, Color meanColor) {
		int meanPos = yAxis.numValToRawPosition(overallMean);
		int meanOnScreen = translateToScreen(meanPos, 0, null).x;
		
		g.setColor(meanColor);
		g.drawLine(meanOnScreen, 0, meanOnScreen, getSize().height - 1);
		
		g.setColor(Color.yellow);
		LabelValue kOverallMeanLabel = new LabelValue(getApplet().translate("Overall mean"));
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
	
	
	private double[] getCatMeans(CatVariable xVar) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = xVar.values();
		
		double[] xMean = new double[xVar.noOfCategories()];
		int xN[] = new int[xMean.length];
		
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int x = xVar.labelIndex(xe.nextValue());
			xMean[x] += y;
			xN[x] ++;
		}
		for (int i=0 ; i<xMean.length ; i++)
			xMean[i] /= xN[i];
			
		return xMean;
	}
	
	
	private void drawBands(Graphics g, String catKey) {
		CatVariable xVar = (CatVariable)getVariable(catKey);
		int nCats = xVar.noOfCategories();
		
		double means[] = getCatMeans(xVar);
		
		g.setColor(kOddBandBackground);
		Point lowP = null;
		Point highP = null;
		for (int i=0 ; i<nCats ; i++) {
			int pos0 = treatBlockAxis.catValToPosition(i - 1, nCats);
			int pos1 = treatBlockAxis.catValToPosition(i, nCats);
			int pos2 = treatBlockAxis.catValToPosition(i + 1, nCats);
			
			int meanPos = yAxis.numValToRawPosition(means[i]);
			
			lowP = translateToScreen(meanPos, (pos0 + pos1) / 2, lowP);
			highP = translateToScreen(meanPos, (pos2 + pos1) / 2, highP);
			
			if (i % 2 == 1) {
				g.setColor(kOddBandBackground);
				g.fillRect(0, highP.y, getSize().width, lowP.y - highP.y);
			}
			
			g.setColor(kMeanLineColor);
			g.drawLine(highP.x, highP.y, highP.x, lowP.y - 1);
		}
	}
	
	private void drawBackground(Graphics g) {
		if (transitionType == ADD_REMOVE_BLOCK
							|| transitionType == BLOCK_TO_TREAT && getTransitionStage() == START)
			drawBands(g, blockKey);
		else if (transitionType == ADD_REMOVE_TREAT
							|| transitionType == BLOCK_TO_TREAT && getTransitionStage() == END)
			drawBands(g, treatKey);
		else {
			g.setColor(dimColor(kOddBandBackground, 0.5));
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		
		drawOverallMeanBackground(g);
		drawOverallMean(g, kOverallMeanColor);
	}
	
	private void drawComponents(Graphics g, CatVariable blockVar, CatVariable treatVar,
																																				NumVariable yVar) {
		g.setColor(kComponentColor);
		int nTreats = treatVar.noOfCategories();
		int nBlocks = blockVar.noOfCategories();
		
		int meanPos = yAxis.numValToRawPosition(overallMean);
		int meanOnScreen = translateToScreen(meanPos, 0, null).x;
		
		Point p = null;
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		ValueEnumeration be = blockVar.values();
		int index = 0;
		
		if (!showResidNotExplained) {
			double treatMeans[] = getCatMeans(treatVar);
			int treatMeanPos[] = new int[treatMeans.length];
			for (int i=0 ; i<treatMeans.length ; i++)
				treatMeanPos[i] = yAxis.numValToRawPosition(treatMeans[i]);
			
			double blockMeans[] = getCatMeans(blockVar);
			int blockMeanPos[] = new int[blockMeans.length];
			for (int i=0 ; i<blockMeans.length ; i++)
				blockMeanPos[i] = yAxis.numValToRawPosition(blockMeans[i]);
			
			while (ye.hasMoreValues()) {
				@SuppressWarnings("unused")
				double y = ye.nextDouble();
				int treat = treatVar.labelIndex(te.nextValue());
				int block = blockVar.labelIndex(be.nextValue());
				
				int currentFrame = getCurrentFrame();
				int yPos = (treatMeanPos[treat] * currentFrame + blockMeanPos[block] * (kFinalFrame - currentFrame)) / kFinalFrame;
				int xPos = getXPos(block, treat, nBlocks, nTreats, index);
				p = translateToScreen(yPos, xPos, p);
				
				if (p != null)
					g.drawLine(p.x, p.y, meanOnScreen, p.y);
				
				index ++;
			}
		}
		else
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				int treat = treatVar.labelIndex(te.nextValue());
				int block = blockVar.labelIndex(be.nextValue());
				
				int yPos = yAxis.numValToRawPosition(y);
				int xPos = getXPos(block, treat, nBlocks, nTreats, index);
				p = translateToScreen(yPos, xPos, p);
				
				if (p != null)
					g.drawLine(p.x, p.y, meanOnScreen, p.y);
				
				index ++;
			}
	}
	
	private void drawData(Graphics g, CatVariable blockVar, CatVariable treatVar,
																																			NumVariable yVar) {
		drawComponents(g, blockVar, treatVar, yVar);
		
		int nTreats = treatVar.noOfCategories();
		int nBlocks = blockVar.noOfCategories();
		
		Point p = null;
		ValueEnumeration ye = yVar.values();
		ValueEnumeration te = treatVar.values();
		ValueEnumeration be = blockVar.values();
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int treat = treatVar.labelIndex(te.nextValue());
			int block = blockVar.labelIndex(be.nextValue());
			
			int yPos = yAxis.numValToRawPosition(y);
			int xPos = getXPos(block, treat, nBlocks, nTreats, index);
			p = translateToScreen(yPos, xPos, p);
			
			if (p != null) {
				g.setColor(CatPieChartView.catColor[treat]);
				drawCross(g, p);
			}
			
			index ++;
		}
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		
		if (!jitteringInitialised)
			initialiseJittering(blockVar, treatVar);
		
		drawBackground(g);
		
		drawData(g, blockVar, treatVar, yVar);
	}

//-----------------------------------------------------------------------------------
	
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}

//-----------------------------------------------------------------------------------
	
				//		Animation relies on yVariable being a RemoveBlockVariable
	
	protected void drawNextFrame() {
		if (transitionType == ADD_REMOVE_BLOCK || transitionType == ADD_REMOVE_TREAT) {
			double transitionPropn = getCurrentFrame() / (double)kFinalFrame;
			RemoveBlockVariable yVar = (RemoveBlockVariable)getVariable(yKey);
			
			if (transitionType == ADD_REMOVE_BLOCK)
				yVar.setBlockEffectProportion(transitionPropn);
			else if (transitionType == ADD_REMOVE_TREAT)
				yVar.setTreatEffectProportion(transitionPropn);
		}
		
		super.drawNextFrame();
	}
	

}