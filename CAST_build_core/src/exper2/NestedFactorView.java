package exper2;

import java.awt.*;

import dataView.*;
import axis.*;
import models.*;


public class NestedFactorView extends DataView {
	static final private Color kFactorSeparatorColor = new Color(0xCCCCCC);
	
	static final private int kHalfBlockShade = 10;
	
	private VertAxis yAxis;
	private NestedFactorAxis xAxis;
	
	private String yKey, blockKey, factorKey;
	private String lsBlockKey, lsFactorKey;
	
	private Color[] componentColors;
	
	private boolean jitteringInitialised = false;
	private int jitter[] = null;
	private int noInXBlock[] = null;
	private int scalePercent = 100;
	
	private boolean shadeComponents = false;
	
	public NestedFactorView(DataSet theData, XApplet applet, VertAxis yAxis,
										NestedFactorAxis xAxis, String yKey, String blockKey, String factorKey,
										String lsBlockKey, String lsFactorKey, Color[] componentColors) {
		super(theData, applet, new Insets(5, 5, 5, 5));
		this.yAxis = yAxis;
		this.xAxis = xAxis;
		this.yKey = yKey;
		this.blockKey = blockKey;
		this.factorKey = factorKey;
		this.lsBlockKey = lsBlockKey;
		this.lsFactorKey = lsFactorKey;
		this.componentColors = componentColors;
	}
	
	public void setShadeComponents(boolean shadeComponents) {
		this.shadeComponents = shadeComponents;
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
	
	private void drawBackground(Graphics g, CatVariable blockVar, CatVariable factorVar) {
		Point thePoint = null;
		int nLevels = factorVar.noOfCategories();
		int nBlocks = blockVar.noOfCategories();
		int blocksPerLevel = nBlocks / nLevels;
		
		MultipleRegnModel blockModel = (MultipleRegnModel)getVariable(lsBlockKey);
		MultipleRegnModel factorModel = (MultipleRegnModel)getVariable(lsFactorKey);
		double factorMean[] = new double[nLevels];
		double total = 0.0;
		for (int i=0 ; i<nLevels ; i++) {
			factorMean[i] = factorModel.evaluateMean(factorVar.getLabel(i));
			total += factorMean[i];
		}
		double overallMean = total / nLevels;
		thePoint = getScreenPoint(overallMean, 0, thePoint);
		int meanVert = thePoint.y;
		
		int xBefore = 0;
		Color dimFactorColor = dimColor(componentColors[1], 0.9);
		Color dimBlockColor = dimColor(componentColors[2], 0.9);
		for (int i=0 ; i<nLevels ; i++) {
			int xAfter = xAxis.getPositionBefore((i + 1) * blocksPerLevel);
			thePoint = getScreenPoint(factorMean[i], xBefore, thePoint);
			int levelMeanVert = thePoint.y;
			int startPos = thePoint.x;
			int endPos = thePoint.x + (xAfter - xBefore);
			
			if (shadeComponents) {
				g.setColor(dimFactorColor);
				g.fillRect(startPos, Math.min(meanVert, levelMeanVert), endPos - startPos,
																											Math.abs(meanVert - levelMeanVert));
			}
			
			for (int block=i*blocksPerLevel ; block<(i+1)*blocksPerLevel ; block++) {
				double blockMean = blockModel.evaluateMean(blockVar.getLabel(block));
				int centerX = xAxis.catValToPosition(block);
				thePoint = getScreenPoint(blockMean, centerX, thePoint);
				int blockMeanVert = thePoint.y;
				int centerPos = thePoint.x;
				
				if (shadeComponents) {
					g.setColor(dimBlockColor);
					g.fillRect(centerPos - kHalfBlockShade, Math.min(blockMeanVert, levelMeanVert),
																			2 * kHalfBlockShade, Math.abs(blockMeanVert - levelMeanVert));
				}
				g.setColor(componentColors[2]);
				g.drawLine(centerPos - kHalfBlockShade, blockMeanVert, centerPos + kHalfBlockShade - 1, blockMeanVert);	
			}
			
			g.setColor(componentColors[1]);
			g.drawLine(startPos, levelMeanVert, endPos, levelMeanVert);
			
			if (i > 0) {
				g.setColor(kFactorSeparatorColor);
				int xPos = xAxis.getPositionBefore(i * blocksPerLevel);
				thePoint = translateToScreen(xPos, 0, thePoint);
				g.drawLine(thePoint.x, 0, thePoint.x, getSize().height);
			}
			
			xBefore = xAfter;
		}
		
		g.setColor(componentColors[0]);
		g.drawLine(0, meanVert, getSize().width, meanVert);
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (NumVariable)getVariable(yKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		CatVariable factorVar = (CatVariable)getVariable(factorKey);
		drawBackground(g, blockVar, factorVar);
		
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
	
