package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class SplitPlotView extends DataView {
	static final private Color kOverallMeanColor = new Color(0xCCCCCC);
	static final private Color kBlockMeanColor = new Color(0xFF6666);
	static final private Color kOddBackgroundColor = new Color(0xEEEEFF);
	
	private VertAxis yAxis;
	private HorizAxis xAxis;
	
	private String yKey, blockKey;
	private String lsBlockKey;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXBlock[] = null;
	private int scalePercent = 100;
	
	public SplitPlotView(DataSet theData, XApplet applet, VertAxis yAxis, HorizAxis xAxis,
																	String yKey, String blockKey, String lsBlockKey) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.yAxis = yAxis;
		this.xAxis = xAxis;
		this.yKey = yKey;
		this.blockKey = blockKey;
		this.lsBlockKey = lsBlockKey;
	}
	
	protected Point getScreenPoint(double y, int xPos, Point thePoint) {
		if (Double.isNaN(y))
			return null;
		try {
			int vertPos = yAxis.numValToPosition(y);
			return translateToScreen(xPos, vertPos, thePoint);
		} catch (AxisException ex) {
			return null;
		}
	}
	
	protected int getXPos(int block, CatVariable blockVar, int index) {
		if (!jitteringInitialised)
			initialiseJittering(blockVar);
		int offset = ((2 * jitter[index] - noInXBlock[block]) * scalePercent) / 100 + 1;
		int xCatPos = xAxis.catValToPosition(block);
		return xCatPos + offset;
	}
	
	private void initialiseJittering(CatVariable blockVar) {
		int noOfVals = blockVar.noOfValues();
		if (jitter == null || jitter.length != noOfVals)
			jitter = new int[noOfVals];
		int noOfBlocks = blockVar.noOfCategories();
		if (noInXBlock == null || noInXBlock.length != noOfBlocks)
			noInXBlock = new int[noOfBlocks];
		else
			for (int i=0 ; i<noOfBlocks ; i++)
				noInXBlock[i] = 0;
		
		for (int i=0 ; i<noOfVals ; i++) {
			int block = blockVar.getItemCategory(i);
			jitter[i] = noInXBlock[block];
			noInXBlock[block] ++;
		}
		int usedHoriz = noOfVals * 2 + noOfBlocks * getCrossSize() * 2;
		int availableHoriz = (getSize().width * 3) / 5;
		scalePercent = Math.min(100, (availableHoriz * 100) / usedHoriz);
		
		jitteringInitialised = true;
	}
	
	private void drawBackground(Graphics g, CatVariable blockVar) {
		Point thePoint = null;
		int nBlocks = blockVar.noOfCategories();
		
		MultipleRegnModel blockModel = (MultipleRegnModel)getVariable(lsBlockKey);
		double blockMean[] = new double[nBlocks];
		double total = 0.0;
		for (int i=0 ; i<nBlocks ; i++) {
			blockMean[i] = blockModel.evaluateMean(blockVar.getLabel(i));
			total += blockMean[i];
		}
		double overallMean = total / nBlocks;
		thePoint = getScreenPoint(overallMean, 0, thePoint);
		int meanVert = thePoint.y;
		
		int xBefore = 0;
		for (int i=0 ; i<nBlocks ; i++) {
			int xAfter = (xAxis.catValToPosition(i) + xAxis.catValToPosition(i + 1)) / 2;
			thePoint = getScreenPoint(blockMean[i], xBefore, thePoint);
			int levelMeanVert = thePoint.y;
			int startPos = thePoint.x;
			int endPos = thePoint.x + (xAfter - xBefore);
			
			if (i % 2 == 1) {
				g.setColor(kOddBackgroundColor);
				g.fillRect(startPos, 0, endPos - startPos, getSize().height);
			}
			
			g.setColor(kBlockMeanColor);
			g.drawLine(startPos, levelMeanVert, endPos - 1, levelMeanVert);
			
			xBefore = xAfter;
		}
		
		g.setColor(kOverallMeanColor);
		g.drawLine(0, meanVert, getSize().width, meanVert);
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		drawBackground(g, blockVar);
		
		Point thePoint = null;
		g.setColor(getForeground());
		ValueEnumeration ye = yVar.values();
		ValueEnumeration xe = blockVar.values();
		int index = 0;
		while (ye.hasMoreValues() && xe.hasMoreValues()) {
			double y = ye.nextDouble();
			int block = blockVar.labelIndex(xe.nextValue());
			int xPos = getXPos(block, blockVar, index);
			thePoint = getScreenPoint(y, xPos, thePoint);
			
			if (thePoint != null)
				drawCross(g, thePoint);
			index ++;
		}
	}
	
//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
